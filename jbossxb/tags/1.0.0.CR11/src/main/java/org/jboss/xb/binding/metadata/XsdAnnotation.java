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
package org.jboss.xb.binding.metadata;

import java.io.StringReader;
import javax.xml.namespace.QName;

import org.jboss.xb.binding.Constants;
import org.jboss.xb.binding.GenericObjectModelFactory;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.jboss.logging.Logger;
import org.xml.sax.Attributes;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class XsdAnnotation
   extends XsdElement
{
   private static final Logger log = Logger.getLogger(XsdAnnotation.class);

   public XsdAnnotation(QName qName)
   {
      super(qName);
   }

   public static final XsdAnnotation unmarshal(String annotation)
   {
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      unmarshaller.mapFactoryToNamespace(JaxbObjectModelFactory.INSTANCE, Constants.NS_JAXB);
      unmarshaller.mapFactoryToNamespace(JbxbObjectModelFactory.INSTANCE, Constants.NS_JBXB);

      try
      {
         return (XsdAnnotation)unmarshaller.unmarshal(new StringReader(annotation),
            XsdObjectModelFactory.INSTANCE,
            (Object)null
         );
      }
      catch(JBossXBException e)
      {
         throw new JBossXBRuntimeException("Failed to parse annotation string: " + annotation + ": " + e.getMessage(),
            e
         );
      }
   }

   public XsdAppInfo getAppInfo()
   {
      return (XsdAppInfo)getChild(XsdAppInfo.QNAME);
   }

   // Inner

   private static abstract class AbstractGOMF
      implements GenericObjectModelFactory
   {
      public Object newChild(Object parent,
                             UnmarshallingContext ctx,
                             String namespaceURI,
                             String localName,
                             Attributes attrs)
      {
         return null;
      }

      public void addChild(Object parent,
                           Object child,
                           UnmarshallingContext ctx,
                           String namespaceURI,
                           String localName)
      {
         XsdElement p = (XsdElement)parent;
         XsdElement c = (XsdElement)child;
         p.addChild(c);
      }

      public void setValue(Object o, UnmarshallingContext ctx, String namespaceURI, String localName, String value)
      {
         XsdElement e = (XsdElement)o;
         e.setData(value);
      }

      public Object completeRoot(Object root, UnmarshallingContext ctx, String namespaceURI, String localName)
      {
         return root;
      }

      }

   private static final class XsdObjectModelFactory
      extends AbstractGOMF
   {
      public static final GenericObjectModelFactory INSTANCE = new XsdObjectModelFactory();

      public Object newChild(Object parent,
                             UnmarshallingContext ctx,
                             String namespaceURI,
                             String localName,
                             Attributes attrs)
      {
         XsdElement element = null;
         if("appinfo".equals(localName))
         {
            element = new XsdAppInfo();
            for(int i = 0; i < attrs.getLength(); ++i)
            {
               element.addAttribute(new QName(attrs.getURI(i), attrs.getLocalName(i)), attrs.getValue(i));
            }
         }
         return element;
      }

      public void addChild(Object parent,
                           Object child,
                           UnmarshallingContext ctx,
                           String namespaceURI,
                           String localName)
      {
         if(parent instanceof XsdAppInfo)
         {
            XsdAppInfo appInfo = (XsdAppInfo)parent;
            if(child instanceof ClassMetaData)
            {
               appInfo.setClassMetaData((ClassMetaData)child);
            }
            else if(child instanceof PropertyMetaData)
            {
               appInfo.setPropertyMetaData((PropertyMetaData)child);
            }
            else if(child instanceof SchemaMetaData)
            {
               appInfo.setSchemaMetaData((SchemaMetaData)child);
            }
            else if(child instanceof ValueMetaData)
            {
               appInfo.setValueMetaData((ValueMetaData)child);
            }
            else if(child instanceof CharactersMetaData)
            {
               appInfo.setCharactersMetaData((CharactersMetaData)child);
            }
         }
         else
         {
            super.addChild(parent, child, ctx, namespaceURI, localName);
         }
      }

      public Object newRoot(Object root,
                            UnmarshallingContext ctx,
                            String namespaceURI,
                            String localName,
                            Attributes attrs)
      {
         return new XsdAnnotation(new QName(namespaceURI, localName));
      }
   }

   private static final class JaxbObjectModelFactory
      implements GenericObjectModelFactory
   {
      public static final GenericObjectModelFactory INSTANCE = new JaxbObjectModelFactory();

      public Object newChild(Object parent,
                             UnmarshallingContext ctx,
                             String namespaceURI,
                             String localName,
                             Attributes attrs)
      {
         Object element = null;
         if("package".equals(localName))
         {
            element = new PackageMetaData();
            setAttributes(element, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("name".equals(localName))
                  {
                     ((PackageMetaData)o).setName(value);
                  }
               }
            }
            );
         }
         else if("javaType".equals(localName))
         {
            ValueMetaData valueMetaData = new ValueMetaData();
            setAttributes(valueMetaData, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("parseMethod".equals(localName))
                  {
                     ((ValueMetaData)o).setUnmarshalMethod(value);
                  }
                  else if("printMethod".equals(localName))
                  {
                     ((ValueMetaData)o).setMarshalMethod(value);
                  }
               }
            }
            );

            // todo review this...
            XsdAppInfo appInfo = (XsdAppInfo)parent;
            appInfo.setValueMetaData(valueMetaData);
         }

         return element;
      }

      public void addChild(Object parent,
                           Object child,
                           UnmarshallingContext ctx,
                           String namespaceURI,
                           String localName)
      {
         if(parent instanceof SchemaMetaData)
         {
            SchemaMetaData schemaMetaData = (SchemaMetaData)parent;
            if(child instanceof PackageMetaData)
            {
               schemaMetaData.setPackage((PackageMetaData)child);
            }
            else
            {
               schemaMetaData.addValue((ValueMetaData)child);
            }
         }
      }

      public void setValue(Object o, UnmarshallingContext ctx, String namespaceURI, String localName, String value)
      {
      }

      public Object newRoot(Object root,
                            UnmarshallingContext ctx,
                            String namespaceURI,
                            String localName,
                            Attributes attrs)
      {
         Object element = null;
         if("schemaBindings".equals(localName))
         {
            element = new SchemaMetaData();
         }
         else if("property".equals(localName))
         {
            PropertyMetaData property = new PropertyMetaData();
            setAttributes(property, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("name".equals(localName))
                  {
                     ((PropertyMetaData)o).setName(value);
                  }
                  else if("collectionType".equals(localName))
                  {
                     ((PropertyMetaData)o).setCollectionType(value);
                  }
               }
            }
            );
            //element = property;
            XsdAppInfo appInfo = (XsdAppInfo)root;
            appInfo.setPropertyMetaData(property);
            // return null;
         }
         else if("class".equals(localName))
         {
            element = new ClassMetaData();
            setAttributes(element, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("implClass".equals(localName))
                  {
                     ((ClassMetaData)o).setImpl(value);
                  }
               }
            }
            );
         }
         else if("javaType".equals(localName))
         {
            element = new ValueMetaData();
            setAttributes(element, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("printMethod".equals(localName))
                  {
                     ((ValueMetaData)o).setMarshalMethod(value);
                  }
                  else if("parseMethod".equals(localName))
                  {
                     ((ValueMetaData)o).setUnmarshalMethod(value);
                  }
               }
            }
            );
         }

         return element;
      }

      public Object completeRoot(Object root, UnmarshallingContext ctx, String namespaceURI, String localName)
      {
         return root;
      }

      private void setAttributes(Object o, Attributes attrs, AttributeSetter attrSetter)
      {
         for(int i = 0; i < attrs.getLength(); ++i)
         {
            attrSetter.setAttribute(o, attrs.getURI(i), attrs.getLocalName(i), attrs.getValue(i));
         }
      }
   }

   private static final class JbxbObjectModelFactory
      implements GenericObjectModelFactory
   {
      public static final JbxbObjectModelFactory INSTANCE = new JbxbObjectModelFactory();

      public Object newChild(Object parent,
                             UnmarshallingContext ctx,
                             String namespaceURI,
                             String localName,
                             Attributes attrs)
      {
         Object child = null;
         // schemaBindings/package
         if("package".equals(localName))
         {
            child = new PackageMetaData();
            setAttributes(child, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("name".equals(localName))
                  {
                     ((PackageMetaData)o).setName(value);
                  }
               }
            }
            );
         }
         else if("value".equals(localName))
         {
            child = new ValueMetaData();
            setAttributes(child, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("marshalMethod".equals(localName))
                  {
                     ((ValueMetaData)o).setMarshalMethod(value);
                  }
                  else if("unmarshalMethod".equals(localName))
                  {
                     ((ValueMetaData)o).setUnmarshalMethod(value);
                  }
               }
            }
            );
         }
         else if("property".equals(localName))
         {
            PropertyMetaData property = new PropertyMetaData();
            setAttributes(property, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("name".equals(localName))
                  {
                     ((PropertyMetaData)o).setName(value);
                  }
                  else if("collectionType".equals(localName))
                  {
                     ((PropertyMetaData)o).setCollectionType(value);
                  }
               }
            }
            );

            if(parent instanceof XsdAppInfo)
            {
               ((XsdAppInfo)parent).setPropertyMetaData(property);
            }
            else
            {
               ((CharactersMetaData)parent).setProperty(property);
            }
         }
         else if("mapEntryKey".equals(localName))
         {
            if(parent instanceof XsdAppInfo)
            {
               ((XsdAppInfo)parent).setMapEntryKey(true);
            }
            else
            {
               ((CharactersMetaData)parent).setMapEntryKey(true);
            }
         }
         else if("mapEntryValue".equals(localName))
         {
            if(parent instanceof XsdAppInfo)
            {
               ((XsdAppInfo)parent).setMapEntryValue(true);
            }
            else
            {
               ((CharactersMetaData)parent).setMapEntryValue(true);
            }
         }
         else if("skip".equals(localName))
         {
            XsdAppInfo appInfo = (XsdAppInfo)parent;
            appInfo.setSkip(true);
         }
         else
         {
            // Log a warning for any unexpected elements
            if( "ignoreUnresolvedFieldOrClass".equals(localName) == false
               && "replacePropertyRefs".equals(localName) == false )
            {
               log.warn("newChild: " + localName);
            }
         }
         return child;
      }

      public void addChild(Object parent,
                           Object child,
                           UnmarshallingContext ctx,
                           String namespaceURI,
                           String localName)
      {
         if(child instanceof PackageMetaData)
         {
            SchemaMetaData schema = (SchemaMetaData)parent;
            schema.setPackage((PackageMetaData)child);
         }
         else if(child instanceof ValueMetaData)
         {
            ValueMetaData valueMetaData = (ValueMetaData)child;
            if(parent instanceof XsdAppInfo)
            {
               ((XsdAppInfo)parent).setValueMetaData(valueMetaData);
            }
            else
            {
               ((CharactersMetaData)parent).setValue(valueMetaData);
            }
         }
         else if(child instanceof CharactersMetaData)
         {
            CharactersMetaData charMD = (CharactersMetaData)child;
            ((XsdAppInfo)parent).setCharactersMetaData(charMD);
         }
         else
         {
            log.warn("addChild: " + localName + "=" + child);
         }
      }

      public void setValue(Object o, UnmarshallingContext ctx, String namespaceURI, String localName, String value)
      {
         // schemaBindings/ignoreUnresolvedFieldOrClass
         if( "ignoreUnresolvedFieldOrClass".equals(localName) )
         {
            SchemaMetaData schema = (SchemaMetaData) o;
            Boolean flag = Boolean.valueOf(value);
            schema.setIgnoreUnresolvedFieldOrClass(flag.booleanValue());
         }
         // schemaBindings/replacePropertyRefs
         else if( "replacePropertyRefs".equals(localName) )
         {
            SchemaMetaData schema = (SchemaMetaData) o;
            Boolean flag = Boolean.valueOf(value);
            schema.setReplacePropertyRefs(flag.booleanValue());
         }
         else
         {
            log.warn("setValue: " + localName + "=" + value);
         }
      }

      public Object newRoot(Object root,
                            UnmarshallingContext ctx,
                            String namespaceURI,
                            String localName,
                            Attributes attrs)
      {
         Object element = null;
         if("schemaBindings".equals(localName))
         {
            element = new SchemaMetaData();
         }
         // Legacy schema element name change to schemaBindings for jaxb consistency
         else if("schema".equals(localName))
         {
            element = new SchemaMetaData();
         }
         else if("value".equals(localName))
         {
            element = new ValueMetaData();
            setAttributes(element, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("marshalMethod".equals(localName))
                  {
                     ((ValueMetaData)o).setMarshalMethod(value);
                  }
                  else if("unmarshalMethod".equals(localName))
                  {
                     ((ValueMetaData)o).setUnmarshalMethod(value);
                  }
               }
            }
            );
         }
         else if("class".equals(localName))
         {
            element = new ClassMetaData();
            setAttributes(element, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("impl".equals(localName))
                  {
                     ((ClassMetaData)o).setImpl(value);
                  }
               }
            }
            );
         }
         else if("property".equals(localName))
         {
            PropertyMetaData property = new PropertyMetaData();
            setAttributes(property, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("name".equals(localName))
                  {
                     ((PropertyMetaData)o).setName(value);
                  }
                  else if("collectionType".equals(localName))
                  {
                     ((PropertyMetaData)o).setCollectionType(value);
                  }
               }
            }
            );
            XsdAppInfo appInfo = (XsdAppInfo)root;
            appInfo.setPropertyMetaData(property);
         }
         else if("putMethod".equals(localName))
         {
            PutMethodMetaData putMethod = new PutMethodMetaData();
            setAttributes(putMethod, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("name".equals(localName))
                  {
                     ((PutMethodMetaData)o).setName(value);
                  }
                  else if("keyType".equals(localName))
                  {
                     ((PutMethodMetaData)o).setKeyType(value);
                  }
                  else if("valueType".equals(localName))
                  {
                     ((PutMethodMetaData)o).setValueType(value);
                  }
               }
            }
            );
            XsdAppInfo appInfo = (XsdAppInfo)root;
            appInfo.setPutMethodMetaData(putMethod);
         }
         else if("addMethod".equals(localName))
         {
            AddMethodMetaData addMethod = new AddMethodMetaData();
            setAttributes(addMethod, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("name".equals(localName))
                  {
                     ((AddMethodMetaData)o).setMethodName(value);
                  }
                  else if("valueType".equals(localName))
                  {
                     if("child".equals(value))
                     {
                        ((AddMethodMetaData)o).setChildType(true);
                     }
                     else
                     {
                        ((AddMethodMetaData)o).setValueType(value);
                     }
                  }
               }
            }
            );
            XsdAppInfo appInfo = (XsdAppInfo)root;
            appInfo.setAddMethodMetaData(addMethod);
         }
         else if("mapEntry".equals(localName))
         {
            MapEntryMetaData mapEntry = new MapEntryMetaData();
            setAttributes(mapEntry, attrs, new AttributeSetter()
            {
               public void setAttribute(Object o, String nsUri, String localName, String value)
               {
                  if("impl".equals(localName))
                  {
                     ((MapEntryMetaData)o).setImpl(value);
                  }
                  else if("getKeyMethod".equals(localName))
                  {
                     ((MapEntryMetaData)o).setGetKeyMethod(value);
                  }
                  else if("setKeyMethod".equals(localName))
                  {
                     ((MapEntryMetaData)o).setSetKeyMethod(value);
                  }
                  else if("getValueMethod".equals(localName))
                  {
                     ((MapEntryMetaData)o).setGetValueMethod(value);
                  }
                  else if("setValueMethod".equals(localName))
                  {
                     ((MapEntryMetaData)o).setSetValueMethod(value);
                  }
                  else if("valueType".equals(localName))
                  {
                     ((MapEntryMetaData)o).setValueType(value);
                  }
                  else if("nonNullValue".equals(localName))
                  {
                     boolean b = Boolean.valueOf(value).booleanValue();
                     ((MapEntryMetaData)o).setNonNullValue(b);
                  }
               }
            }
            );
            XsdAppInfo appInfo = (XsdAppInfo)root;
            appInfo.setMapEntryMetaData(mapEntry);
         }
         else if("mapEntryKey".equals(localName))
         {
            XsdAppInfo appInfo = (XsdAppInfo)root;
            appInfo.setMapEntryKey(true);
         }
         else if("mapEntryValue".equals(localName))
         {
            XsdAppInfo appInfo = (XsdAppInfo)root;
            appInfo.setMapEntryValue(true);
         }
         else if("characters".equals(localName))
         {
            element = new CharactersMetaData();
         }
         else if("skip".equals(localName))
         {
            XsdAppInfo appInfo = (XsdAppInfo)root;
            appInfo.setSkip(true);
         }
         else
         {
            log.warn("Unexpected jbxb annotation: ns=" + namespaceURI + ", localName=" + localName);
         }
         return element;
      }

      public Object completeRoot(Object root, UnmarshallingContext ctx, String namespaceURI, String localName)
      {
         return root;
      }

      // Private

      private void setAttributes(Object o, Attributes attrs, AttributeSetter attrSetter)
      {
         for(int i = 0; i < attrs.getLength(); ++i)
         {
            attrSetter.setAttribute(o, attrs.getURI(i), attrs.getLocalName(i), attrs.getValue(i));
         }
      }
   }

   interface AttributeSetter
   {
      void setAttribute(Object o, String nsUri, String localName, String value);
   }
}
