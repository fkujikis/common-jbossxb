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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import org.apache.xerces.xs.XSTypeDefinition;
import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.xb.binding.AttributesImpl;
import org.jboss.xb.binding.Constants;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.xb.binding.NamespaceRegistry;
import org.jboss.xb.binding.Util;
import org.jboss.xb.binding.introspection.FieldInfo;
import org.jboss.xb.binding.metadata.CharactersMetaData;
import org.jboss.xb.binding.metadata.PropertyMetaData;
import org.jboss.xb.binding.metadata.ValueMetaData;
import org.jboss.xb.binding.parser.JBossXBParser;
import org.jboss.xb.binding.resolver.MutableSchemaResolver;
import org.jboss.xb.binding.sunday.xop.XOPIncludeHandler;
import org.xml.sax.Attributes;

/**
 * Default ContentHandler
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class SundayContentHandler
   implements JBossXBParser.DtdAwareContentHandler
{
   private final static Logger log = Logger.getLogger(SundayContentHandler.class);

   private final static Object NIL = new Object();

   private final SchemaBinding schema;
   private final SchemaBindingResolver schemaResolver;

   private final StackImpl stack = new StackImpl();

   private Object root;
   private NamespaceRegistry nsRegistry = new NamespaceRegistry();

   private ParticleHandler defParticleHandler = DefaultHandlers.ELEMENT_HANDLER;

   private UnmarshallingContextImpl ctx = new UnmarshallingContextImpl();
   // DTD information frm startDTD
   private String dtdRootName;
   private String dtdPublicId;
   private String dtdSystemId;
   private boolean sawDTD;

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

   
   public void startDTD(String dtdRootName, String dtdPublicId, String dtdSystemId)
   {
      this.dtdRootName = dtdRootName;
      this.dtdPublicId = dtdPublicId;
      this.dtdSystemId = dtdSystemId;
   }
   public void endDTD()
   {
      this.sawDTD = true;
   }

   public void characters(char[] ch, int start, int length)
   {
      Position position = stack.peek();
      if(position.isModelGroup())
         return;
      
      ElementBinding e = (ElementBinding) position.particle.getTerm();
      // if current is ended the characters belong to its parent
      if(position.ended)
      {
         position = stack.peek1();
         if(position.isModelGroup())
         {
            for(int i = stack.size() - 3; i >= 0; --i)
            {
               position = stack.peek(i);
               if(position.isElement())
                  break;
            }
         }
         e = (ElementBinding) position.particle.getTerm();
      }

      // collect characters only if they are allowed content
      if(e.getType().isTextContentAllowed())
      {
         if(position.indentation != Boolean.FALSE)
         {
            if(e.getType().isSimple())
            {
               // simple content is not analyzed
               position.indentation = Boolean.FALSE;
               position.ignorableCharacters = false;
            }
            else if(e.getSchema() != null && !e.getSchema().isIgnoreWhitespacesInMixedContent())
            {
               position.indentation = Boolean.FALSE;
               position.ignorableCharacters = false;
            }
            else
            {
               // the indentation is currently defined as whitespaces with next line characters
               // this should probably be externalized in the form of a filter or something
               for (int i = start; i < start + length; ++i)
               {
                  if(ch[i] == 0x0a)
                  {
                     position.indentation = Boolean.TRUE;
                  }
                  else if (!Character.isWhitespace(ch[i]))
                  {
                     position.indentation = Boolean.FALSE;
                     position.ignorableCharacters = false;
                     break;
                  }
               }
            }
         }
         
         if (position.textContent == null)
         {
            position.textContent = new StringBuffer();
         }
         position.textContent.append(ch, start, length);
      }
   }

   public void endElement(String namespaceURI, String localName, String qName)
   {
      ElementBinding elementBinding = null;
      QName endName = localName.length() == 0 ? new QName(qName) : new QName(namespaceURI, localName);
      Position position;
      while(true)
      {
         position = stack.peek();
         if(position.isElement())
         {
            if(position.ended)
            {
               if(position.particle.isRepeatable())
               {
                  Position parentPosition = stack.peek1();
                  if(parentPosition.repeatableParticleValue != null)
                     endRepeatableParticle(parentPosition, position.qName, position.particle, parentPosition.particle);
               }
               pop();
            }
            else
            {
               elementBinding = (ElementBinding)position.particle.getTerm();
               position.ended = true;
               break;
            }
         }
         else
         {
            if(!position.ended) // could be ended if it's a choice
               endParticle(position);

            if(position.particle.isRepeatable())
            {
               Position parentPosition = stack.peek1();
               if(parentPosition.repeatableParticleValue != null)
                  endRepeatableParticle(parentPosition, position.qName, position.particle, parentPosition.particle);
            }
            pop();
         }
      }

      if(elementBinding == null)
         throw new JBossXBRuntimeException("Failed to endElement " + qName + ": binding not found");

      if(!elementBinding.getQName().equals(endName))
      {
         throw new JBossXBRuntimeException("Failed to end element " +
            new QName(namespaceURI, localName) +
            ": element on the stack is " + elementBinding.getQName()
         );
      }

      endElement();
   }

   public void startElement(String namespaceURI,
                            String localName,
                            String qName,
                            Attributes atts,
                            XSTypeDefinition xercesType)
   {
      QName startName = localName.length() == 0 ? new QName(qName) : new QName(namespaceURI, localName);
      ParticleBinding particle = null;
      ParticleHandler handler = null;
      TypeBinding parentType = null;
      boolean repeated = false;
      Position position = null;
      SchemaBinding schemaBinding = schema;

      atts = preprocessAttributes(atts);
      
      if(stack.isEmpty())
      {
         if(schemaBinding != null)
         {
            particle = schemaBinding.getElementParticle(startName);
         }
         else if(schemaResolver != null)
         {
            String schemaLocation = atts == null ? null : Util.getSchemaLocation(atts, namespaceURI);
            // Use the dtd info if it exists and there is no schemaLocation
            if(schemaLocation == null || schemaLocation.length() == 0)
            {
               if(sawDTD)
                  schemaLocation = dtdSystemId;
               // If there is still no schemaLocation and no namespaceURI, pass in the root local name
               // if the namespace is not null then schemaLocation should be left null and resolved by EntityResolver
               if(schemaLocation == null && (namespaceURI == null || namespaceURI.length() == 0))
                  schemaLocation = localName;
            }
            
            schemaBinding = schemaResolver.resolve(namespaceURI, null, schemaLocation);
            if(schemaBinding != null)
               particle = schemaBinding.getElementParticle(startName);
            else
               throw new JBossXBRuntimeException("Failed to resolve schema nsURI=" + namespaceURI + " location=" + schemaLocation);
         }
         else
         {
            throw new JBossXBRuntimeException("Neither schema binding nor schema binding resolver is available!");
         }
      }
      else
      {
         while(!stack.isEmpty())
         {
            position = stack.peek();
            if(position.isElement())
            {
               TermBinding term = position.particle.getTerm();
               ElementBinding element = (ElementBinding)term;
               if(position.ended)
               {
                  if(element.getQName().equals(startName))
                  {
                     if(position.particle.isRepeatable())
                     {
                        NonElementPosition parentPosition = (NonElementPosition) stack.peek1();
                        if(parentPosition.repeatTerm(startName, atts))
                        {
                           position.reset();
                           particle = position.particle;
                           parentType = position.parentType;
                           repeated = true;
                        }
                        else
                        {
                           pop();
                           if(parentPosition.repeatableParticleValue != null)
                              endRepeatableParticle(parentPosition, position.qName, position.particle, parentPosition.particle);
                           continue;
                        }
                     }
                     else
                     {
                        position.reset();
                        particle = position.particle;
                        parentType = position.parentType;
                        repeated = true;

                        endRepeatableParent(startName);
                     }
                  }
                  else
                  {
                     if(position.particle.isRepeatable())
                     {
                        Position parentPosition = stack.peek1();
                        if(parentPosition.repeatableParticleValue != null)
                        {
                           endRepeatableParticle(parentPosition, position.qName, position.particle, parentPosition.particle);
                        }
                     }
                     pop();
                     continue;
                  }
               }
               else
               {
                  parentType = element.getType();
                  ParticleBinding typeParticle = parentType.getParticle();
                  ModelGroupBinding modelGroup = typeParticle == null ? null : (ModelGroupBinding)typeParticle.getTerm();
                  if(modelGroup == null)
                  {
                     if(startName.equals(Constants.QNAME_XOP_INCLUDE))
                     {
                        TypeBinding anyUriType = schema.getType(Constants.QNAME_ANYURI);
                        if(anyUriType == null)
                        {
                           log.warn("Type " + Constants.QNAME_ANYURI + " not bound.");
                        }

                        TypeBinding xopIncludeType = new TypeBinding(new QName(Constants.NS_XOP_INCLUDE, "Include"));
                        xopIncludeType.setSchemaBinding(schema);
                        xopIncludeType.addAttribute(new QName("href"), anyUriType, DefaultHandlers.ATTRIBUTE_HANDLER);
                        xopIncludeType.setHandler(new XOPIncludeHandler(parentType, schema.getXopUnmarshaller()));

                        ElementBinding xopInclude = new ElementBinding(schema, Constants.QNAME_XOP_INCLUDE, xopIncludeType);

                        particle = new ParticleBinding(xopInclude);
                        
                        ElementBinding parentElement = (ElementBinding) position.particle.getTerm();
                        parentElement.setXopUnmarshaller(schema.getXopUnmarshaller());

                        flushIgnorableCharacters();
                        position.handler = DefaultHandlers.XOP_HANDLER;
                        position.ignoreCharacters = true;
                        position.o = position.handler.startParticle(stack.peek().o, startName, stack.peek().particle, null, nsRegistry);
                        break;
                     }

                     QName typeName = parentType.getQName();
                     throw new JBossXBRuntimeException((typeName == null ? "Anonymous" : typeName.toString()) +
                        " type of element " +
                        element.getQName() +
                        " should be complex and contain " + startName + " as a child element."
                     );
                  }

                  NonElementPosition newPosition = modelGroup.newPosition(startName, atts, typeParticle);
                  if(newPosition == null)
                  {
                     throw new JBossXBRuntimeException(startName +
                        " not found as a child of " +
                        ((ElementBinding)term).getQName() + " in " + modelGroup
                     );
                  }
                  else
                  {
                     flushIgnorableCharacters();

                     NonElementPosition groupPosition = null;
                     Object o = position.o;
                     while(newPosition != null)
                     {
                        groupPosition = newPosition;
                        ParticleBinding groupParticle = newPosition.getParticle();
                        if(groupParticle.isRepeatable())
                           startRepeatableParticle(stack.peek(), o, startName, groupParticle);

                        handler = getHandler(groupParticle.getTerm());
                        o = handler.startParticle(o, startName, groupParticle, atts, nsRegistry);
                        push(newPosition, o, handler, parentType);
                        
                        newPosition = newPosition.getNext();
                     }
                     particle = groupPosition.getCurrentParticle();
                  }                  
               }
               break;
            }
            else
            {
               NonElementPosition groupPosition = (NonElementPosition) position;

               ParticleBinding prevParticle = groupPosition.getCurrentParticle();
               NonElementPosition newPosition = groupPosition.startElement(startName, atts);               
               if(newPosition == null)
               {
                  if(!position.ended)
                     endParticle(position);
                                    
                  pop();
                  if(!position.particle.isRepeatable() && stack.peek().isElement())
                  {
                     TermBinding t = groupPosition.getParticle().getTerm();
                     StringBuffer sb = new StringBuffer(250);
                     sb.append(startName).append(" cannot appear in this position. Expected content of ")
                     .append(((ElementBinding)stack.peek().particle.getTerm()).getQName())
                     .append(" is ").append(t);
                     throw new JBossXBRuntimeException(sb.toString());
                  }
               }
               else
               {
                  if(position.ended) // for repeatable choices
                  {
                     if(!position.particle.isRepeatable())
                        throw new JBossXBRuntimeException("The particle expected to be repeatable but it's not: " + position.particle.getTerm());
                     
                     position.reset();                     
                     handler = getHandler(position.particle.getTerm());
                     position.o = handler.startParticle(stack.peek1().o, startName, position.particle, atts, nsRegistry);
                  }
                  
                  ParticleBinding curParticle = groupPosition.getCurrentParticle();
                  if(curParticle != prevParticle)
                  {
                     if(position.repeatableParticleValue != null &&
                           prevParticle != null && prevParticle.isRepeatable() && prevParticle.getTerm().isModelGroup())
                     {
                        endRepeatableParticle(position, position.qName, prevParticle, position.particle);
                     }

                     if(newPosition.getNext() != null && curParticle.isRepeatable())
                     {
                        startRepeatableParticle(position, position.o, startName, curParticle);
                     }
                  }

                  // push all except the last one
                  parentType = position.parentType;
                  Object o = position.o;
                  groupPosition = newPosition;
                  newPosition = newPosition.getNext();
                  while(newPosition != null)
                  {
                     groupPosition = newPosition;
                     ParticleBinding modelGroupParticle = groupPosition.getParticle();
                     handler = getHandler(modelGroupParticle.getTerm());
                     o = handler.startParticle(o, startName, modelGroupParticle, atts, nsRegistry);
                     push(groupPosition, o, handler, parentType);
                     
                     newPosition = newPosition.getNext();
                  }
                  particle = groupPosition.getCurrentParticle();
                  break;
               }
            }
         }
      }

      Object o = null;
      if(particle != null)
      {
         Object parent = stack.isEmpty() ? null :
            (repeated ? stack.peek1().o : stack.peek().o);

         ElementBinding element = (ElementBinding)particle.getTerm();

         // TODO xsi:type support should be implemented in a better way
         String xsiType = atts.getValue(Constants.NS_XML_SCHEMA_INSTANCE, "type");
         if(xsiType != null)
         {
            if(trace)
               log.trace(element.getQName() + " uses xsi:type " + xsiType);

            if(position != null && position.nonXsiParticle == null)
               position.nonXsiParticle = particle;
            
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

            ElementBinding xsiElement = new ElementBinding(schemaBinding, startName, xsiTypeBinding);
            xsiElement.setRepeatableHandler(element.getRepeatableHandler());
            particle =
               new ParticleBinding(xsiElement,
                  particle.getMinOccurs(),
                  particle.getMaxOccurs(),
                  particle.getMaxOccursUnbounded()
               );
         }

         if(!repeated && particle.isRepeatable())
         {
            startRepeatableParticle(stack.peek(), parent, startName, particle);
         }

         TypeBinding type = element.getType();
         if(type == null)
         {
            throw new JBossXBRuntimeException("No type for element " + element);
         }

         handler = type.getHandler();         
         if(handler == null)
         {
            handler = defParticleHandler;
         }

         List<ElementInterceptor> localInterceptors = parentType == null ? Collections.EMPTY_LIST : parentType.getInterceptors(startName);         
         List<ElementInterceptor> interceptors = element.getInterceptors();
         if(interceptors.size() + localInterceptors.size() > 0)
         {
            if (repeated)
               pop();

            for (int i = 0; i < localInterceptors.size(); ++i)
            {
               ElementInterceptor interceptor = localInterceptors.get(i);
               parent = interceptor.startElement(parent, startName, type);
               push(startName, particle, parent, handler, parentType);
               interceptor.attributes(parent, startName, type, atts, nsRegistry);
            }

            for (int i = 0; i < interceptors.size(); ++i)
            {
               ElementInterceptor interceptor = interceptors.get(i);
               parent = interceptor.startElement(parent, startName, type);
               push(startName, particle, parent, handler, parentType);
               interceptor.attributes(parent, startName, type, atts, nsRegistry);
            }

            if (repeated)
            {
               // to have correct endRepeatableParticle calls
               stack.push(position);
            }
         }

         String nil = atts.getValue(Constants.NS_XML_SCHEMA_INSTANCE, "nil");
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
            ParticleBinding stackParticle = repeated ? stack.peek1().particle : stack.peek().particle;
            if(stackParticle != null)
               parentBinding = (ElementBinding)stackParticle.getTerm();
         }

         if(parentBinding != null && parentBinding.getSchema() != null)
            schemaBinding = parentBinding.getSchema();

         String msg = "Element " +
            startName +
            " is not bound " +
            (parentBinding == null ? "as a global element." : "in type " + parentBinding.getType().getQName());
         if(schemaBinding != null && schemaBinding.isStrictSchema())
            throw new JBossXBRuntimeException(msg);
         else if(trace)
            log.trace(msg);
      }

      if(repeated)
      {
         position.o = o;
         // in case of collection of abstract types
         position.particle = particle;
      }
      else
      {
         push(startName, particle, o, handler, parentType);
      }
   }

   private ParticleHandler getHandler(TermBinding term)
   {
      ParticleHandler handler = null;
      if(term.isModelGroup())
         handler = ((ModelGroupBinding)term).getHandler();
      else if(term.isWildcard())
         //handler = ((WildcardBinding)term).getWildcardHandler();
         handler = NoopParticleHandler.INSTANCE;
      else
         throw new IllegalArgumentException("Unexpected term " + term);
      return handler == null ? defParticleHandler : handler;
   }

   private void endRepeatableParent(QName startName)
   {
      int stackIndex = stack.size() - 2;
      Position position;
      Position parentPosition = stack.peek1();
      while(true)
      {
         if(parentPosition.isElement())
         {
            throw new JBossXBRuntimeException(
               "Failed to start " + startName +
               ": the element is not repeatable, repeatable parent expected to be a model group but got element " +
               ((ElementBinding)parentPosition.particle.getTerm()).getQName()
            );
         }

         position = parentPosition;
         if(position.particle.isRepeatable())
         {
            endParticle(position, stackIndex - 1);

            ParticleHandler handler = getHandler(position.particle.getTerm());
            position.reset();
            parentPosition = stack.peek(stackIndex - 1);
            position.o = handler.startParticle(parentPosition.o, position.qName, position.particle, null, nsRegistry);

            break;
         }

         parentPosition = stack.peek(--stackIndex);
         endParticle(position, stackIndex);
      }

/*      if(!parentParticle.isRepeatable())
      {
         StringBuffer msg = new StringBuffer();

         item = stack.peek();
         ParticleBinding currentParticle = item.particle;
         msg.append("Failed to start ").append(startName).append(": ")
            .append(currentParticle.getTerm())
            .append(" is not repeatable.")
            .append(" Its parent ")
            .append(parentParticle.getTerm())
            .append(" expected to be repeatable!")
            .append("\ncurrent stack: ");

         for(int i = 0; i < stack.size() - 1; ++i)
         {
            item = stack.peek(i);
            ParticleBinding particle = item.particle;
            TermBinding term = particle.getTerm();
            if(term.isModelGroup())
            {
               if(term instanceof SequenceBinding)
               {
                  msg.append("sequence");
               }
               else if(term instanceof ChoiceBinding)
               {
                  msg.append("choice");
               }
               else
               {
                  msg.append("all");
               }
            }
            else if(term.isWildcard())
            {
               msg.append("wildcard");
            }
            else
            {
               msg.append(((ElementBinding)term).getQName());
            }
            msg.append("\\");
         }

         throw new JBossXBRuntimeException(msg.toString());
      }
*/
      while(++stackIndex < stack.size() - 1)
      {
         parentPosition = position;
         position = stack.peek(stackIndex);
         ParticleHandler handler = getHandler(position.particle.getTerm());
         position.reset();
         position.o = handler.startParticle(parentPosition.o, position.qName, position.particle, null, nsRegistry);
      }
   }

   private void startRepeatableParticle(Position parentPosition, Object parent, QName startName, ParticleBinding particle)
   {
      if(trace)
         log.trace(" start repeatable (" + stack.size() + "): " + particle.getTerm());

      RepeatableParticleHandler repeatableHandler = particle.getTerm().getRepeatableHandler();
      // the way it is now it's never null
      Object repeatableContainer = repeatableHandler.startRepeatableParticle(parent, startName, particle);
      if(repeatableContainer != null)
      {
         if(parentPosition.repeatableParticleValue != null)
            throw new IllegalStateException();
         parentPosition.repeatableParticleValue = repeatableContainer;
         parentPosition.repeatableHandler = repeatableHandler;
      }
   }

   private void endRepeatableParticle(Position parentPosition, QName elementName, ParticleBinding particle, ParticleBinding parentParticle)
   {
      if (trace)
         log.trace(" end repeatable (" + stack.size() + "): " + particle.getTerm());
      RepeatableParticleHandler repeatableHandler = parentPosition.repeatableHandler;
      // the way it is now it's never null
      repeatableHandler.endRepeatableParticle(parentPosition.o, parentPosition.repeatableParticleValue, elementName, particle, parentParticle);
      parentPosition.repeatableParticleValue = null;
      parentPosition.repeatableHandler = null;
   }

   private void endParticle(Position position)
   {
      if(position.ended)
         throw new JBossXBRuntimeException(position.particle.getTerm() + " has already been ended.");

      ParticleHandler handler = position.handler;
      Object o = handler.endParticle(position.o, position.qName, position.particle);

      position.ended = true;
      // model group should always have parent particle
      //Position parentPosition = getNotSkippedParent();
      Position parentPosition = stack.peek1();
      if(parentPosition.o != null)
      {
         if(parentPosition.repeatableParticleValue == null)
            setParent(handler, parentPosition.o, o, position.qName, position.particle, parentPosition.particle);
         else
            parentPosition.repeatableHandler.addTermValue(parentPosition.repeatableParticleValue, o, position.qName, position.particle, parentPosition.particle, handler);
      }
   }

   private void endParticle(Position position, int parentIdex)
   {
      if(position.ended)
         throw new JBossXBRuntimeException(position.particle.getTerm() + " has already been ended.");

      ParticleHandler handler = position.handler;
      Object o = handler.endParticle(position.o, position.qName, position.particle);

      position.ended = true;
      // model group should always have parent particle
      Position parentPosition = getNotSkippedParent(parentIdex);
      if(parentPosition.o != null)
      {
         if(parentPosition.repeatableParticleValue == null)
            setParent(handler, parentPosition.o, o, position.qName, position.particle, parentPosition.particle);
         else
            parentPosition.repeatableHandler.addTermValue(parentPosition.repeatableParticleValue, o, position.qName, position.particle, parentPosition.particle, handler);
      }
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

   private Attributes preprocessAttributes(Attributes attrs)
   {
      SchemaBindingResolver resolver = schemaResolver == null ? schema.getSchemaResolver() : schemaResolver;
      if(resolver == null || !(resolver instanceof MutableSchemaResolver))
         return attrs;
      
      int ind = attrs.getIndex(Constants.NS_JBXB, "schemabinding");
      if (ind != -1)
      {
         MutableSchemaResolver defaultResolver = (MutableSchemaResolver)resolver;
         String value = attrs.getValue(ind);
         java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(value);
         while(tokenizer.hasMoreTokens())
         {
            String uri = tokenizer.nextToken();
            if(!tokenizer.hasMoreTokens())
               throw new JBossXBRuntimeException("jbxb:schemabinding attribute value is invalid: ns uri '" + uri + "' is missing value in '" + value + "'");
            String cls = tokenizer.nextToken();
            try
            {
               defaultResolver.mapURIToClass(uri, cls);
            }
            catch (Exception e)
            {
               throw new JBossXBRuntimeException("Failed to addClassBinding: uri='" + uri + "', class='" + cls + "'", e);
            }
         }
         
         AttributesImpl attrsImpl = new AttributesImpl(attrs.getLength() - 1);
         for(int i = 0; i < attrs.getLength(); ++i)
         {
            if(i != ind)
               attrsImpl.add(attrs.getURI(i), attrs.getLocalName(i), attrs.getQName(i), attrs.getType(i), attrs.getValue(i));
         }
         attrs = attrsImpl;
      }
      return attrs;
   }
   
   private void flushIgnorableCharacters()
   {
      Position position = stack.peek();
      if(position.isModelGroup() || position.textContent == null)
         return;

      if(position.indentation == Boolean.TRUE || position.ignorableCharacters)
      {
         if(trace)
         {
            log.trace("ignored characters: " + ((ElementBinding) position.particle.getTerm()).getQName() + " '"
               + position.textContent + "'");
         }
         position.textContent = null;
         position.indentation = null;
      }
   }
   
   private Position getNotSkippedParent()
   {
      Position position = stack.peek1();
      if(position == null)
         return null;
      
      ParticleBinding particle = position.particle;
      if(!particle.getTerm().isSkip() || position.repeatableParticleValue != null)
         return position;
      
      Position wildcardPosition = null;
      if(particle.getTerm().isWildcard())
         wildcardPosition = position;

      for(int i = stack.size() - 3; i >= 0; --i)
      {
         position = stack.peek(i);
         particle = position.particle;
         if(!particle.getTerm().isSkip() || position.repeatableParticleValue != null)
            return position;
         else if(wildcardPosition != null)
            return wildcardPosition;

         if(particle.getTerm().isWildcard())
            wildcardPosition = position;
      }
      return wildcardPosition;
   }

   private Position getNotSkippedParent(int i)
   {
      Position position = null;
      while(i >= 0)
      {
         position = stack.peek(i--);
         ParticleBinding particle = position.particle;
         if(!particle.getTerm().isSkip() || position.repeatableParticleValue != null)
            return position;
      }
      return null;
   }

   private void endElement()
   {
      Position position = stack.peek();
      Object o = position.o;
      ParticleBinding particle = position.particle;
      
      ElementBinding element = (ElementBinding)particle.getTerm();
      QName endName = element.getQName();
      TypeBinding type = element.getType();
      List<ElementInterceptor> interceptors = element.getInterceptors();
      List<ElementInterceptor> localInterceptors = position.parentType == null ? Collections.EMPTY_LIST : position.parentType.getInterceptors(endName);
      int allInterceptors = interceptors.size() + localInterceptors.size();

      if(o != NIL)
      {
         //
         // characters
         //

         flushIgnorableCharacters();

         TypeBinding charType = type.getSimpleType();
         if(charType == null)
         {
            charType = type;
         }

         CharactersHandler charHandler = position.ignoreCharacters ? null : charType.getCharactersHandler();

         /**
          * If there is text content then unmarshal it and set.
          * If there is no text content and the type is simple and
          * its characters handler is not null then unmarshal and set.
          * If the type is complex and there is no text data then the unmarshalled value
          * of the empty text content is assumed to be null
          * (in case of simple types that's not always true and depends on nillable attribute).
          */
         String textContent = position.textContent == null ? "" : position.textContent.toString();
         if(textContent.length() > 0 || charHandler != null && !type.isIgnoreEmptyString())
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
               
               if(element.isNormalizeSpace())
                  dataContent = dataContent.trim();
            }

            Object unmarshalled;

            if(charHandler == null)
            {
               if(!type.isSimple() &&
                  schema != null &&
                  schema.isStrictSchema()
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
                  TermBeforeSetParentCallback beforeSetParent = charType.getBeforeSetParentCallback();
                  if(beforeSetParent != null)
                  {
                     ctx.parent = o;
                     ctx.particle = particle;
                     ctx.parentParticle = getNotSkippedParent().particle;
                     unmarshalled = beforeSetParent.beforeSetParent(unmarshalled, ctx);
                     ctx.clear();
                  }
                  
                  charHandler.setValue(endName, element, o, unmarshalled);
               }
            }

            if(allInterceptors > 0)
            {
               int interceptorIndex = stack.size() - 1 - allInterceptors;
               for (int i = interceptors.size() - 1; i >= 0; --i)
               {
                  ElementInterceptor interceptor = interceptors.get(i);
                  interceptor.characters(stack.peek(interceptorIndex++).o, endName, type, nsRegistry, dataContent);
               }

               for (int i = localInterceptors.size() - 1; i >= 0; --i)
               {
                  ElementInterceptor interceptor = localInterceptors.get(i);
                  interceptor.characters(stack.peek(interceptorIndex++).o, endName, type, nsRegistry, dataContent);
               }
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

      Position parentPosition = stack.size() == 1 ? null : stack.peek1();
      Object parent = parentPosition == null ? null : parentPosition.o;
      ParticleHandler handler = position.handler;
      
      o = handler.endParticle(o, endName, particle);

      if(!interceptors.isEmpty())
      {
         int interceptorIndex = stack.size() - 1 - interceptors.size();
         for (int i = interceptors.size() - 1; i >= 0; --i)
         {
            ElementInterceptor interceptor = interceptors.get(i);
            interceptor.endElement(stack.peek(interceptorIndex++).o, endName, type);
         }
      }
      
      //
      // setParent
      //

      if(allInterceptors == 0)
      {
         Position notSkippedParent = getNotSkippedParent();
         if (notSkippedParent != null)
         {
            ParticleBinding parentParticle = notSkippedParent.particle;
            TermBinding parentTerm = parentParticle.getTerm();
            if (parentTerm.isWildcard())
            {
               ParticleHandler wh = ((WildcardBinding) parentTerm).getWildcardHandler();
               if (wh != null)
                  handler = wh;
            }

            if (parent != null)
            {
               if (notSkippedParent.repeatableParticleValue == null)
                  setParent(handler, parent, o, endName, particle, parentParticle);
               else
                  notSkippedParent.repeatableHandler.addTermValue(notSkippedParent.repeatableParticleValue, o, endName,
                        particle, parentParticle, handler);
            }
            else if (parentTerm.isWildcard() && stack.size() > 1)
            {
               // the parent has anyType, so it gets the value of its child
               for (int i = stack.size() - 2; i >= 0; --i)
               {
                  Position peeked = stack.peek(i);
                  peeked.o = o;
                  if (peeked.isElement())
                     break;
               }

               if (trace)
                  log.trace("Value of " + endName + " " + o + " is promoted as the value of its parent element.");
            }
         }
      }
      else
      {
         Position popped = pop();

         for(int i = interceptors.size() - 1; i >= 0; --i)
         {
            ElementInterceptor interceptor = interceptors.get(i);
            parent = pop().o;
            interceptor.add(parent, o, endName);
            o = parent;
         }

         for(int i = localInterceptors.size() - 1; i >= 0; --i)
         {
            ElementInterceptor interceptor = localInterceptors.get(i);
            parent = pop().o;
            interceptor.add(parent, o, endName);
            o = parent;
         }

         // need to have correst endRepeatableParticle events
         stack.push(popped);
      }

      if(stack.size() == 1)
      {
         o = type.getValueAdapter().cast(o, Object.class);
         root = o;
         stack.clear();
         
         if(sawDTD)
         {
            // Probably should be integrated into schema binding?
            try
            {
               // setDTD(String root, String publicId, String systemId)
               Class[] sig = {String.class, String.class, String.class};
               Method setDTD = o.getClass().getMethod("setDTD", sig);
               Object[] args = {dtdRootName, dtdPublicId, dtdSystemId};
               setDTD.invoke(o, args);
            }
            catch(Exception e)
            {
               log.debug("No setDTD found on root: " + o);
            }
         }
      }
   }

   private void setParent(ParticleHandler handler,
                          Object parent,
                          Object o,
                          QName endName,
                          ParticleBinding particle,
                          ParticleBinding parentParticle)
   {
      TermBeforeSetParentCallback beforeSetParent = particle.getTerm().getBeforeSetParentCallback();
      if(beforeSetParent != null)
      {
         ctx.parent = parent;
         ctx.particle = particle;
         ctx.parentParticle = getNotSkippedParent().particle;
         o = beforeSetParent.beforeSetParent(o, ctx);
         ctx.clear();
      }
      
      handler.setParent(parent, o, endName, particle, parentParticle);
   }

   private void push(QName qName, ParticleBinding particle, Object o, ParticleHandler handler, TypeBinding parentType)
   {
      Position position = new Position(qName, particle);
      position.o = o;
      position.handler = handler;
      position.parentType = parentType;      
      stack.push(position);
      if(trace)
         log.trace("pushed[" + (stack.size() - 1) + "] " + particle.getTerm().getQName() + "=" + o);
   }

   private void push(NonElementPosition position, Object o, ParticleHandler handler, TypeBinding parentType)
   {
      position.o = o;
      position.handler = handler;
      position.parentType = parentType;
      stack.push(position);
      if(trace)
         log.trace("pushed[" + (stack.size() - 1) + "] " + position + ", o=" + o);
   }

   private Position pop()
   {
      Position position = stack.pop();
      if(trace)
         log.trace("poped[" + stack.size() + "] " + position.particle.getTerm());
      return position;
   }

   // Inner

   public static class Position
   {
      protected boolean trace;
      final QName qName;
      ParticleBinding particle;
      ParticleBinding nonXsiParticle;
      ParticleHandler handler;
      TypeBinding parentType;
      boolean ignoreCharacters;
      Object o;
      Object repeatableParticleValue;
      RepeatableParticleHandler repeatableHandler;
      StringBuffer textContent;
      Boolean indentation;
      boolean ignorableCharacters = true;
      boolean ended;

      public Position(QName qName, ParticleBinding particle)
      {
         if (particle == null)
            throw new IllegalArgumentException("Null particle");
         
         if(qName == null)
            throw new IllegalArgumentException("Null qName");
         this.qName = qName;

         this.particle = particle;
      }

      protected boolean isElement()
      {
         return true;
      }

      protected boolean isModelGroup()
      {
         return false;
      }
      
      void reset()
      {
         if(!ended)
         {
            throw new JBossXBRuntimeException(
               "Attempt to reset a particle that has already been reset: " + particle.getTerm()
            );
         }

         ended = false;
         o = null;
         if(textContent != null)
            textContent.setLength(0);
         
         indentation = null;
         ignorableCharacters = true;
         
         if(nonXsiParticle != null)
            particle = nonXsiParticle;
      }
   }

   static class StackImpl
   {
      private List<Position> list = new ArrayList<Position>();
      private Position head;
      private Position peek1;

      public void clear()
      {
         list.clear();
         head = null;
         peek1 = null;
      }

      public void push(Position o)
      {
         list.add(o);
         peek1 = head;
         head = o;
      }

      public Position pop()
      {
         head = peek1;
         int index = list.size() - 1;
         peek1 = index > 1 ? list.get(index - 2) : null;
         return list.remove(index);
      }

      public Position peek()
      {
         return head;
      }

      public Position peek1()
      {
         return peek1;
      }

      public Position peek(int i)
      {
         return list.get(i);
      }

      public boolean isEmpty()
      {
         return head == null;//list.isEmpty();
      }

      public int size()
      {
         return list.size();
      }
   }
   
   private class UnmarshallingContextImpl implements UnmarshallingContext
   {
      Object parent;
      ParticleBinding particle;
      ParticleBinding parentParticle;
      
      public Object getParentValue()
      {
         return parent;
      }
      
      public ParticleBinding getParticle()
      {
         return particle;
      }
      
      public ParticleBinding getParentParticle()
      {
         return parentParticle;
      }
      
      public String resolvePropertyName()
      {
         TermBinding term = particle.getTerm();
         PropertyMetaData propertyMetaData = term.getPropertyMetaData();
         String prop = propertyMetaData == null ? null : propertyMetaData.getName();
         
         if(prop != null)
         {
            return prop;
         }
         
         if(term.isElement())
         {
            QName name = ((ElementBinding)term).getQName();
            prop = Util.xmlNameToFieldName(name.getLocalPart(), term.getSchema().isIgnoreLowLine());
         }
         
         return prop;
      }

      public Class<?> resolvePropertyType()
      {
         if(parent == null)
         {
            return null;
         }
         
         String prop = resolvePropertyName();
         if(prop != null)
         {      
            FieldInfo fieldInfo = FieldInfo.getFieldInfo(parent.getClass(), prop, false);
            if (fieldInfo != null)
            {
               return fieldInfo.getType();
            }
         }
         return null;
      }
      
      // private
      
      void clear()
      {
         ctx.parent = null;
         ctx.particle = null;
         ctx.parentParticle = null;
      }
   }
}
