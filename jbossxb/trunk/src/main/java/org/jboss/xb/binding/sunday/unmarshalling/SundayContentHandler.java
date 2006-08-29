/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package org.jboss.xb.binding.sunday.unmarshalling;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import org.apache.xerces.xs.XSTypeDefinition;
import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.xb.binding.NamespaceRegistry;
import org.jboss.xb.binding.Util;
import org.jboss.xb.binding.group.ValueList;
import org.jboss.xb.binding.metadata.CharactersMetaData;
import org.jboss.xb.binding.metadata.ValueMetaData;
import org.jboss.xb.binding.parser.JBossXBParser;
import org.xml.sax.Attributes;

/**
 * todo: to improve performance, consider gathering all the necessary binding metadata in startElement and
 * pushing it instead of ElementBinding into elementStack to free the endElement implementation from
 * re-gathering this same metadata again.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class SundayContentHandler
   implements JBossXBParser.ContentHandler
{
   private final static Logger log = Logger.getLogger(SundayContentHandler.class);

   private final static Object NIL = new Object();

   private final SchemaBinding schema;
   private final SchemaBindingResolver schemaResolver;

   private final StackImpl stack = new StackImpl();

   private Object root;
   private NamespaceRegistry nsRegistry = new NamespaceRegistry();

   private ParticleHandler defParticleHandler = DefaultHandlers.ELEMENT_HANDLER;

   private final boolean trace = log.isTraceEnabled();

   public SundayContentHandler(SchemaBinding schema)
   {
      this.schema = schema;
      this.schemaResolver = null;
   }

   public SundayContentHandler(SchemaBindingResolver schemaResolver)
   {
      this.schemaResolver = schemaResolver;
      this.schema = null;
   }

   public void characters(char[] ch, int start, int length)
   {
      StackItem stackItem = (StackItem)stack.peek();
      if(stackItem.particle != null)
      {
         if(stackItem.textContent == null)
         {
            stackItem.textContent = new StringBuffer(length);
         }
         stackItem.textContent.append(ch, start, length);
      }
   }

   public void endElement(String namespaceURI, String localName, String qName)
   {
      ElementBinding elementBinding = null;
      QName endName = null;
      StackItem item = pop();
      while(true)
      {
         if(item.particle != null)
         {
            elementBinding = (ElementBinding)item.particle.getTerm();
            break;
         }

         if(stack.isEmpty())
         {
            break;
         }

         if(endName == null)
         {
            endName = localName.length() == 0 ? new QName(qName) : new QName(namespaceURI, localName);
         }

         item = endParticle(item, endName);
         pop();
      }

      if(elementBinding == null)
      {
         throw new JBossXBRuntimeException("Failed to endElement " + qName + ": binding not found");
      }

      endName = elementBinding.getQName();
      if(!endName.getLocalPart().equals(localName) || !endName.getNamespaceURI().equals(namespaceURI))
      {
         throw new JBossXBRuntimeException("Failed to end element " +
            new QName(namespaceURI, localName) +
            ": element on the stack is " + endName
         );
      }

      endElement(item.o, item.particle, item.textContent == null ? "" : item.textContent.toString());

      // if parent group is choice, it should also be finished
      if(!stack.isEmpty() && ((StackItem)stack.peek()).cursor.getParticle().getTerm() instanceof ChoiceBinding)
      {
         endParticle(pop(), endName);
      }
   }

   public void startElement(String namespaceURI,
                            String localName,
                            String qName,
                            Attributes atts,
                            XSTypeDefinition xercesType)
   {
      QName startName = localName.length() == 0 ? new QName(qName) : new QName(namespaceURI, localName);
      ParticleBinding particle = null;
      ModelGroupBinding.Cursor cursor = null; // used only when particle is a wildcard
      SchemaBinding schemaBinding = schema;

      if(stack.isEmpty())
      {
         if(schemaBinding != null)
         {
            particle = schemaBinding.getElementParticle(startName);
         }
         else if(schemaResolver != null)
         {
            String schemaLocation = atts == null ? null : Util.getSchemaLocation(atts, namespaceURI);
            schemaBinding = schemaResolver.resolve(namespaceURI, null, schemaLocation);
            if(schemaBinding != null)
            {
               particle = schemaBinding.getElementParticle(startName);
            }
         }
         else
         {
            throw new JBossXBRuntimeException("Neither schema binding nor schema binding resolver is available!");
         }
      }
      else
      {
         StackItem item = (StackItem)stack.peek();
         if(item.particle != null)
         {
            TermBinding term = item.particle.getTerm();
            ElementBinding element = (ElementBinding)term;
            ParticleBinding typeParticle = element.getType().getParticle();
            ModelGroupBinding modelGroup = typeParticle == null ?
               null :
               (ModelGroupBinding)typeParticle.getTerm();
            if(modelGroup == null)
            {
               QName typeName = element.getType().getQName();
               throw new JBossXBRuntimeException(
                  (typeName == null ? "Anonymous" : typeName.toString()) +
                  " type of element " + element.getQName() +
                  " should be complex and contain " + startName + " as a child element."
               );
            }

            cursor = modelGroup.newCursor(typeParticle);
            List newCursors = cursor.startElement(startName, atts);
            if(newCursors.isEmpty())
            {
               throw new JBossXBRuntimeException(
                  startName + " not found as a child of " + ((ElementBinding)term).getQName()
               );
            }
            else
            {
               // push all except the last one
               for(int i = newCursors.size() - 1; i >= 0; --i)
               {
                  cursor = (ModelGroupBinding.Cursor)newCursors.get(i);

                  ParticleBinding modelGroupParticle = cursor.getParticle();
                  ParticleHandler handler = ((ModelGroupBinding)modelGroupParticle.getTerm()).getHandler();
                  if(handler == null)
                  {
                     handler = defParticleHandler;
                  }
                  Object o = handler.startParticle(item.o, startName, modelGroupParticle, atts, nsRegistry);

                  push(cursor, o);
               }
               particle = cursor.getCurrentParticle();
            }
         }
         else
         {
            while(!stack.isEmpty())
            {
               if(item.particle != null)
               {
                  TermBinding term = item.particle.getTerm();
                  ElementBinding element = (ElementBinding)term;
                  ParticleBinding typeParticle = element.getType().getParticle();
                  ModelGroupBinding modelGroup = typeParticle == null ?
                     null :
                     (ModelGroupBinding)typeParticle.getTerm();
                  if(modelGroup == null)
                  {
                     throw new JBossXBRuntimeException(
                        "Element " + element.getQName() + " should be of a complex type!"
                     );
                  }

                  /*
                  if(!typeParticle.isRepeatable())
                  {
                     throw new JBossXBRuntimeException("Particle is not repeatable for " + startName);
                  } */

                  cursor = modelGroup.newCursor(typeParticle);
                  List newCursors = cursor.startElement(startName, atts);
                  if(newCursors.isEmpty())
                  {
                     throw new JBossXBRuntimeException(
                        startName + " not found as a child of " + ((ElementBinding)term).getQName()
                     );
                  }
                  else
                  {
                     // push all except the last one
                     for(int i = newCursors.size() - 1; i >= 0; --i)
                     {
                        cursor = (ModelGroupBinding.Cursor)newCursors.get(i);

                        ParticleBinding modelGroupParticle = cursor.getParticle();
                        ParticleHandler handler = ((ModelGroupBinding)modelGroupParticle.getTerm()).getHandler();
                        if(handler == null)
                        {
                           handler = defParticleHandler;
                        }
                        Object o = handler.startParticle(item.o, startName, modelGroupParticle, atts, nsRegistry);

                        push(cursor, o);
                     }
                     particle = cursor.getCurrentParticle();
                  }
                  break;
               }
               else
               {
                  cursor = item.cursor;
                  if (cursor == null)
                     throw new JBossXBRuntimeException("No cursor for " + startName);
                  int currentOccurence = cursor.getOccurence();
                  List newCursors = cursor.startElement(startName, atts);
                  if(newCursors.isEmpty())
                  {
                     pop();
                     item = endParticle(item, startName);
                  }
                  else
                  {
                     if(cursor.getOccurence() - currentOccurence > 0)
                     {
                        item = endParticle(item, startName);

                        ParticleBinding modelGroupParticle = cursor.getParticle();
                        ParticleHandler handler = ((ModelGroupBinding)modelGroupParticle.getTerm()).getHandler();
                        if(handler == null)
                        {
                           handler = defParticleHandler;
                        }
                        Object o = handler.startParticle(item.o, startName, modelGroupParticle, atts, nsRegistry);

                        item = push(cursor, o);
                     }

                     // push all except the last one
                     for(int i = newCursors.size() - 2; i >= 0; --i)
                     {
                        cursor = (ModelGroupBinding.Cursor)newCursors.get(i);

                        ParticleBinding modelGroupParticle = cursor.getParticle();
                        ParticleHandler handler = ((ModelGroupBinding)modelGroupParticle.getTerm()).getHandler();
                        if(handler == null)
                        {
                           handler = defParticleHandler;
                        }
                        Object o = handler.startParticle(item.o, startName, modelGroupParticle, atts, nsRegistry);

                        push(cursor, o);
                     }
                     cursor = (ModelGroupBinding.Cursor)newCursors.get(0);
                     particle = cursor.getCurrentParticle();
                     break;
                  }
               }
            }
         }
      }

      Object o = null;
      if(particle != null)
      {
         Object parent = stack.isEmpty() ? null : ((StackItem)stack.peek()).o;
         if(particle.getTerm() instanceof WildcardBinding)
         {
            /*
            WildcardBinding wildcard = (WildcardBinding)particle.getTerm();
            ElementBinding element = wildcard.getElement(startName, atts);
            */
            ElementBinding element = cursor.getElement();
            if(element == null)
            {
               throw new JBossXBRuntimeException("Failed to resolve element " +
                  startName + " for wildcard."
               );
            }

            particle = new ParticleBinding(element, particle.getMinOccurs(), particle.getMaxOccurs(), particle.getMaxOccursUnbounded());
         }

         ElementBinding element = (ElementBinding)particle.getTerm();

         // todo xsi:type support should be implemented in a better way
         String xsiType = atts.getValue("xsi:type");
         if(xsiType != null)
         {
            if(trace)
            {
               log.trace(element.getQName() + " uses xsi:type " + xsiType);
            }

            String xsiTypePrefix;
            String xsiTypeLocal;
            int colon = xsiType.indexOf(':');
            if(colon == -1)
            {
               xsiTypePrefix = "";
               xsiTypeLocal = xsiType;
            }
            else
            {
               xsiTypePrefix = xsiType.substring(0, colon);
               xsiTypeLocal = xsiType.substring(colon + 1);
            }

            String xsiTypeNs = nsRegistry.getNamespaceURI(xsiTypePrefix);
            QName xsiTypeQName = new QName(xsiTypeNs, xsiTypeLocal);

            TypeBinding xsiTypeBinding = schemaBinding.getType(xsiTypeQName);
            if(xsiTypeBinding == null)
            {
               throw new JBossXBRuntimeException("Type binding not found for type " +
                  xsiTypeQName +
                  " specified with xsi:type for element " + startName
               );
            }

            element = new ElementBinding(schemaBinding, startName, xsiTypeBinding);
            particle = new ParticleBinding(element, particle.getMinOccurs(), particle.getMaxOccurs(), particle.getMaxOccursUnbounded());
         }

         TypeBinding type = element.getType();
         if(type == null)
         {
            throw new JBossXBRuntimeException("No type for element " + element);
         }

         List interceptors = element.getInterceptors();
         for(int i = 0; i < interceptors.size(); ++i)
         {
            ElementInterceptor interceptor = (ElementInterceptor)interceptors.get(i);
            parent = interceptor.startElement(parent, startName, type);
            push(startName, particle, parent);
            interceptor.attributes(parent, startName, type, atts, nsRegistry);
         }

         ParticleHandler handler = type.getHandler();
         if(handler == null)
         {
            handler = defParticleHandler;
         }

         String nil = atts.getValue("xsi:nil");
         if(nil == null || !("1".equals(nil) || "true".equals(nil)))
         {
            o = handler.startParticle(parent, startName, particle, atts, nsRegistry);
         }
         else
         {
            o = NIL;
         }
      }
      else
      {
         ElementBinding parentBinding = null;
         if(!stack.isEmpty())
         {
            ParticleBinding stackParticle = ((StackItem)stack.peek()).particle;
            if(stackParticle != null)
            {
               parentBinding = (ElementBinding)stackParticle.getTerm();
            }
         }

         if(parentBinding != null && parentBinding.getSchema() != null)
         {
            schemaBinding = parentBinding.getSchema();
         }

         String msg = "Element " +
            startName +
            " is not bound " +
            (parentBinding == null ? "as a global element." : "in type " + parentBinding.getType().getQName());
         if(schemaBinding != null && schemaBinding.isStrictSchema())
         {
            throw new JBossXBRuntimeException(msg);
         }
         else if(trace)
         {
            log.trace(msg);
         }
      }

      push(startName, particle, o);
   }

   private StackItem endParticle(StackItem item, QName qName)
   {
      ParticleBinding modelGroupParticle = item.cursor.getParticle();
      ParticleHandler handler = ((ModelGroupBinding)modelGroupParticle.getTerm()).getHandler();
      if(handler == null)
      {
         handler = defParticleHandler;
      }

      Object o;
      if(item.o instanceof ValueList && !modelGroupParticle.getTerm().isSkip())
      {
         if(trace)
         {
            log.trace("endParticle " + qName + " valueList");
         }
         ValueList valueList = (ValueList)item.o;
         o = valueList.getHandler().newInstance(modelGroupParticle, valueList);
      }
      else
      {
         o = handler.endParticle(item.o, qName, modelGroupParticle);
      }

      // model group should always have parent particle
      item = (StackItem)stack.peek();
      if(item.o != null)
      {
         ParticleBinding parentParticle = item.particle;
         if(parentParticle == null)
         {
            parentParticle = item.cursor.getParticle();
         }
         setParent(handler, item.o, o, qName, modelGroupParticle, parentParticle);
      }

      if(item.particle == null && item.cursor.getParticle().getTerm() instanceof ChoiceBinding)
      {
         item = endParticle(pop(), qName);
      }

      return item;
   }

   public void startPrefixMapping(String prefix, String uri)
   {
      nsRegistry.addPrefixMapping(prefix, uri);
   }

   public void endPrefixMapping(String prefix)
   {
      nsRegistry.removePrefixMapping(prefix);
   }

   public void processingInstruction(String target, String data)
   {
   }

   public Object getRoot()
   {
      return root;
   }

   // Private

   private void endElement(Object o, ParticleBinding particle, String textContent)
   {
      ElementBinding element = (ElementBinding)particle.getTerm();
      QName endName = element.getQName();
      TypeBinding type = element.getType();
      List interceptors = element.getInterceptors();

      if(o != NIL)
      {
         //
         // characters
         //

         TypeBinding charType = type.getSimpleType();
         if(charType == null)
         {
            charType = type;
         }

         CharactersHandler charHandler = charType.getCharactersHandler();

         /**
          * If there is text content then unmarshal it and set.
          * If there is no text content and the type is simple and
          * its characters handler is not null then unmarshal and set.
          * If the type is complex and there is no text data then the unmarshalled value
          * of the empty text content is assumed to be null
          * (in case of simple types that's not always true and depends on nillable attribute).
          */
         if(textContent.length() > 0 || charHandler != null && type.isSimple())
         {
            String dataContent;
            SchemaBinding schema = element.getSchema();
            if(textContent.length() == 0)
            {
               dataContent = null;
            }
            else
            {
               dataContent = textContent.toString();
               if(schema != null && schema.isReplacePropertyRefs())
               {
                  dataContent = StringPropertyReplacer.replaceProperties(dataContent);
               }

               //textContent.delete(0, textContent.length());
            }

            Object unmarshalled;

            if(charHandler == null)
            {
               if(!type.isSimple() && schema != null && schema.isStrictSchema()
                  // todo this isSkip() doesn't look nice here
                  && !element.isSkip())
               {
                  throw new JBossXBRuntimeException("Element " +
                     endName +
                     " with type binding " +
                     type.getQName() +
                     " does not include text content binding: " + dataContent
                  );
               }
               unmarshalled = dataContent;
            }
            else
            {
               ValueMetaData valueMetaData = element.getValueMetaData();
               if(valueMetaData == null)
               {
                  CharactersMetaData charactersMetaData = type.getCharactersMetaData();
                  if(charactersMetaData != null)
                  {
                     valueMetaData = charactersMetaData.getValue();
                  }
               }

               // todo valueMetaData is available from type
               unmarshalled = dataContent == null ?
                  charHandler.unmarshalEmpty(endName, charType, nsRegistry, valueMetaData) :
                  charHandler.unmarshal(endName, charType, nsRegistry, valueMetaData, dataContent);
            }

            if(unmarshalled != null)
            {
               // if startElement returned null, we use characters as the object for this element
               if(o == null)
               {
                  o = unmarshalled;
               }
               else if(charHandler != null)
               {
                  if(o instanceof ValueList)
                  {
                     ValueList valueList = (ValueList)o;
                     if(type.isSimple())
                     {
                        valueList.getInitializer().addTermValue(endName, particle, charHandler, valueList, unmarshalled);
                     }
                     else
                     {
                        valueList.getInitializer().addTextValue(endName, particle, charHandler, valueList, unmarshalled);
                     }
                  }
                  else
                  {
                     charHandler.setValue(endName, element, o, unmarshalled);
                  }
               }
            }

            // todo interceptors get dataContent?
            int i = interceptors.size();
            while(i-- > 0)
            {
               ElementInterceptor interceptor = (ElementInterceptor)interceptors.get(i);
               interceptor.characters(((StackItem)stack.peek(interceptors.size() - 1 - i)).o,
                  endName, type, nsRegistry, dataContent
               );
            }
         }
      }
      else
      {
         o = null;
      }

      //
      // endElement
      //

      Object parent = stack.isEmpty() ? null : ((StackItem)stack.peek()).o;
      ParticleHandler handler = type.getHandler();
      if(handler == null)
      {
         handler = defParticleHandler;
      }

      if(o instanceof ValueList && !particle.getTerm().isSkip())
      {
         if(trace)
         {
            log.trace("endParticle " + endName + " valueList");
         }
         ValueList valueList = (ValueList)o;
         o = valueList.getHandler().newInstance(particle, valueList);
      }
      else
      {
         o = handler.endParticle(o, endName, particle);
      }

      int i = interceptors.size();
      while(i-- > 0)
      {
         ElementInterceptor interceptor = (ElementInterceptor)interceptors.get(i);
         interceptor.endElement(((StackItem)stack.peek(interceptors.size() - 1 - i)).o, endName, type);
      }

      //
      // setParent
      //

      i = interceptors.size();
      // todo yack...
      if(i == 0)
      {
         ParticleHandler wildcardHandler = null;

         ParticleBinding parentParticle = null;
         for(ListIterator iter = stack.prevIterator(); iter.hasPrevious();)
         {
            StackItem item = (StackItem)iter.previous();
            ParticleBinding peeked = item.particle;
            if(peeked != null && peeked.getTerm() instanceof ElementBinding)
            {
               parentParticle = peeked;
               WildcardBinding wildcard = ((ElementBinding)parentParticle.getTerm()).getType().getWildcard();
               if(wildcard != null)
               {
                  wildcardHandler = wildcard.getWildcardHandler();
               }
               break;
            }
         }

         if(parent != null)
         {
            /*if(o == null)
            {
               throw new JBossXBRuntimeException(endName + " is null!");
            } */
            if(wildcardHandler != null)
            {
               setParent(wildcardHandler, parent, o, endName, particle, parentParticle);
            }
            else
            {
               setParent(handler, parent, o, endName, particle, parentParticle);
            }
         }
         else if(parentParticle != null &&
            ((ElementBinding)parentParticle.getTerm()).getType().hasWildcard() &&
            !stack.isEmpty())
         {
            // todo: review this> the parent has anyType, so it gets the value of its child
            for(ListIterator iter = stack.prevIterator(); iter.hasPrevious();)
            {
               StackItem peeked = (StackItem)iter.previous();
               peeked.o = o;
               if(peeked.particle != null)
               {
                  break;
               }
            }

            if(trace)
            {
               log.trace("Value of " + endName + " " + o + " is promoted as the value of its parent element.");
            }
         }
      }
      else
      {
         while(i-- > 0)
         {
            ElementInterceptor interceptor = (ElementInterceptor)interceptors.get(i);
            parent = ((StackItem)stack.pop()).o;
            interceptor.add(parent, o, endName);
            o = parent;
         }
      }

      if(stack.isEmpty())
      {
         root = o;
      }
   }

   private void setParent(ParticleHandler handler,
                          Object parent,
                          Object o,
                          QName endName,
                          ParticleBinding particle,
                          ParticleBinding parentParticle)
   {
      if(parent instanceof ValueList && !particle.getTerm().isSkip())
      {
         ValueList valueList = (ValueList)parent;
         valueList.getInitializer().addTermValue(endName, particle, handler, valueList, o);
      }
      else
      {
         handler.setParent(parent, o, endName, particle, parentParticle);
      }
   }

   private void push(QName qName, ParticleBinding particle, Object o)
   {
      StackItem item = new StackItem(particle, o);
      stack.push(item);
      if(trace)
      {
         Object binding = null;
         if(particle != null)
         {
            binding = particle.getTerm();
         }
         log.trace("pushed " + qName + "=" + o + ", binding=" + binding);
      }
   }

   private StackItem push(ModelGroupBinding.Cursor cursor, Object o)
   {
      StackItem item = new StackItem(cursor, o);
      stack.push(item);
      if(trace)
      {
         log.trace("pushed cursor " + cursor);
      }
      return item;
   }

   private StackItem pop()
   {
      StackItem item = (StackItem)stack.pop();
      if(trace)
      {
         if(item.particle != null)
         {
            log.trace("poped " + ((ElementBinding)item.particle.getTerm()).getQName() + "=" + item.particle);
         }
         else if(item.cursor != null)
         {
            log.trace("poped " + item.cursor.getCurrentParticle().getTerm());
         }
         else
         {
            log.trace("poped null");
         }
      }
      return item;
   }

   // Inner

   private static class StackItem
   {
      final ModelGroupBinding.Cursor cursor;
      final ParticleBinding particle;
      Object o;
      StringBuffer textContent;

      public StackItem(ModelGroupBinding.Cursor cursor, Object o)
      {
         this.cursor = cursor;
         this.particle = null;
         this.o = o;
      }

      public StackItem(ParticleBinding particle, Object o)
      {
         this.cursor = null;
         this.particle = particle;
         this.o = o;
      }
   }

   static class StackImpl
   {
      private List list = new ArrayList();

      public void clear()
      {
         list.clear();
      }

      public void push(Object o)
      {
         list.add(o);
      }

      public Object pop()
      {
         return list.remove(list.size() - 1);
      }

      public Object peek()
      {
         return list.get(list.size() - 1);
      }

      public ListIterator prevIterator()
      {
         return list.listIterator(list.size());
      }

      public Object peek(int i)
      {
         return list.get(list.size() - 1 - i);
      }

      public boolean isEmpty()
      {
         return list.isEmpty();
      }

      public int size()
      {
         return list.size();
      }
   }
}
