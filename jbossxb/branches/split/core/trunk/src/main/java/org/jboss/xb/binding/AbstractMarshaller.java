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
package org.jboss.xb.binding;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.jboss.logging.Logger;
import org.jboss.xb.binding.introspection.FieldInfo;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public abstract class AbstractMarshaller
   implements Marshaller
{
   protected final Logger log = Logger.getLogger(getClass());

   protected String version = VERSION;
   protected String encoding = ENCODING;
   protected List<QName> rootQNames = new ArrayList<QName>();

   protected NamespaceRegistry nsRegistry = new NamespaceRegistry();

   private Map<Class<?>, ClassMapping> classMappings = Collections.emptyMap();
   protected Map<Class<?>, FieldToWildcardMapping> field2WildcardMap = Collections.emptyMap();
   protected Map<Class<?>, QName> cls2TypeMap = Collections.emptyMap();

   /**
    * Content the result is written to
    */
   protected Content content = new Content();

   private Properties props;

   // Marshaller implementation

   public void mapClassToGlobalElement(Class<?> cls,
                                       String localName,
                                       String namespaceUri,
                                       String schemaUrl,
                                       ObjectModelProvider provider)
   {
      GenericObjectModelProvider delProv = null;
      if(provider != null)
      {
         delProv = provider instanceof GenericObjectModelProvider ?
         (GenericObjectModelProvider)provider :
         new DelegatingObjectModelProvider(provider);
      }

      ClassMapping mapping = new ClassMapping(cls,
         localName,
         null,
         namespaceUri,
         schemaUrl,
         delProv
      );

      addClassMapping(mapping);
   }

   public void mapClassToGlobalType(Class<?> cls,
                                    String localName,
                                    String nsUri,
                                    String schemaUrl,
                                    ObjectModelProvider provider)
   {
      ClassMapping mapping = new ClassMapping(cls,
         null,
         localName,
         nsUri,
         schemaUrl,
         provider instanceof GenericObjectModelProvider ?
         (GenericObjectModelProvider)provider :
         new DelegatingObjectModelProvider(provider)
      );

      addClassMapping(mapping);
   }

   public void mapFieldToWildcard(Class<?> cls, String field, ObjectLocalMarshaller marshaller)
   {
      FieldToWildcardMapping mapping = new FieldToWildcardMapping(cls, field, marshaller);
      if(field2WildcardMap.isEmpty())
         field2WildcardMap = new HashMap<Class<?>, FieldToWildcardMapping>(field2WildcardMap);
      field2WildcardMap.put(cls, mapping);
   }

   public void mapClassToXsiType(Class<?> cls, String typeNs, String typeLocalPart)
   {
      QName typeQName = new QName(typeNs, typeLocalPart);
      if(cls2TypeMap.isEmpty())
         cls2TypeMap = new HashMap<Class<?>, QName>(cls2TypeMap);
      cls2TypeMap.put(cls, typeQName);

      if(log.isTraceEnabled())
         log.trace("mapped " + cls + " to xsi:type " + typeQName);
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }

   public void mapPublicIdToSystemId(String publicId, String systemId)
   {
      throw new UnsupportedOperationException();
   }

   public void addRootElement(String namespaceUri, String prefix, String name)
   {
      addRootElement(new QName(namespaceUri, name, prefix));
   }

   public void addRootElement(QName qName)
   {
      rootQNames.add(qName);
   }

   public void setProperty(String name, String value)
   {
      if(props == null)
      {
         props = new Properties();
      }
      props.setProperty(name, value);
   }

   public String getProperty(String name)
   {
      return props == null ? null : props.getProperty(name);
   }

   /**
    * Defines a namespace. The namespace declaration will appear in the root element.
    * <p>If <code>prefix</code> argument is <code>null</code> or is an empty string then
    * the passed in URI will be used for the default namespace, i.e. <code>xmlns</code>.
    * Otherwise, the declaration will follow the format <code>xmlns:prefix=uri</code>.
    * <p>If the namespace with the given prefix was already declared, its value is overwritten.
    *
    * @param prefix the prefix for the namespace to declare (can be null or empty string)
    * @param uri    the URI of the namespace.
    */
   public void declareNamespace(String prefix, String uri)
   {
      if(prefix == null)
      {
         return;
      }

      nsRegistry.addPrefixMapping(prefix, uri);
   }

   public void removePrefixMapping(String prefix)
   {
      nsRegistry.removePrefixMapping(prefix);
   }

   public String getPrefix(String ns)
   {
      String prefix = nsRegistry.getPrefix(ns);
      if(prefix == null && Constants.NS_XML_NAMESPACE.equals(ns))
      {
         prefix = "xml";
      }
      return prefix;
   }

   protected void declareNs(AttributesImpl attrs)
   {
      for(Iterator<String> i = nsRegistry.getRegisteredURIs(); i.hasNext();)
      {
         String uri = i.next();
         String prefix = nsRegistry.getPrefix(uri);
         declareNs(attrs, prefix, uri);
      }
   }

   protected static void declareNs(AttributesImpl attrs, String prefix, String ns)
   {
      attrs.add(null, prefix, prefix.length() == 0 ? "xmlns" : "xmlns:" + prefix, "CDATA", ns);
   }

   /**
    * Adds xsi:type attribute and optionally declares namespaces for xsi and type's namespace.
    *
    * @param typeQName the type to declare xsi:type attribute for
    * @param attrs     the attributes to add xsi:type attribute to
    * @return prefix for the type's ns if it was generated
    */
   protected String declareXsiType(QName typeQName, AttributesImpl attrs)
   {
      String result = null;
      String xsiPrefix = nsRegistry.getPrefix(Constants.NS_XML_SCHEMA_INSTANCE);
      if(xsiPrefix == null)
      {
         declareNs(attrs, "xsi", Constants.NS_XML_SCHEMA_INSTANCE);
         xsiPrefix = "xsi";
      }

      String pref = getPrefix(typeQName.getNamespaceURI());
      if(pref == null)
      {
         // the ns is not declared
         result = pref = typeQName.getLocalPart() + "_ns";
      }

      String qName = prefixLocalName(pref, typeQName.getLocalPart());
      attrs.add(Constants.NS_XML_SCHEMA_INSTANCE, "type", xsiPrefix + ":type", "CDATA", qName);
      return result;
   }

   protected static String prefixLocalName(String prefix, String local)
   {
      return prefix == null || prefix.length() == 0 ? local : prefix + ':' + local;
   }

   public abstract void addAttribute(String prefix, String localName, String type, String value);

   // Protected

   protected boolean propertyIsTrueOrNotSet(String name)
   {
      String value = getProperty(name);
      return value == null || "true".equalsIgnoreCase(value);
   }

   protected void writeXmlVersion(Writer writer) throws IOException
   {
      String xmlVersion = getProperty(Marshaller.PROP_OUTPUT_XML_VERSION);
      if(xmlVersion == null || "true".equalsIgnoreCase(xmlVersion))
      {
         writer.write("<?xml version=\"");
         writer.write(version);
         writer.write("\" encoding=\"");
         writer.write(encoding);
         writer.write("\"?>\n");
      }
   }

   protected ClassMapping getClassMapping(Class<?> cls)
   {
      return (ClassMapping)classMappings.get(cls);
   }

   private void addClassMapping(ClassMapping mapping)
   {
      if(classMappings.isEmpty())
      {
         classMappings = new HashMap<Class<?>, ClassMapping>();
      }
      classMappings.put(mapping.cls, mapping);
   }

   // Inner

   protected class ClassMapping
   {
      public final Class<?> cls;
      public final QName elementName;
      public final QName typeName;
      public final String schemaUrl;
      public final GenericObjectModelProvider provider;

      public ClassMapping(Class<?> cls,
                          String elementName,
                          String typeName,
                          String nsUri,
                          String schemaUrl,
                          GenericObjectModelProvider provider)
      {
         this.cls = cls;
         if(elementName != null)
         {
            this.elementName = nsUri == null ? new QName(elementName) : new QName(nsUri, elementName);
            this.typeName = null;
         }
         else if(typeName != null)
         {
            this.elementName = null;
            this.typeName = nsUri == null ? new QName(typeName) : new QName(nsUri, typeName);
         }
         else
         {
            throw new JBossXBRuntimeException("Element or type name must not null for " + cls);
         }

         this.schemaUrl = schemaUrl;
         this.provider = provider;
      }

      public boolean equals(Object o)
      {
         if(this == o)
         {
            return true;
         }
         if(o == null || getClass() != o.getClass())
         {
            return false;
         }

         final ClassMapping that = (ClassMapping)o;

         if(cls != null ? !cls.equals(that.cls) : that.cls != null)
         {
            return false;
         }
         if(elementName != null ? !elementName.equals(that.elementName) : that.elementName != null)
         {
            return false;
         }
         if(provider != null ? !provider.equals(that.provider) : that.provider != null)
         {
            return false;
         }
         if(schemaUrl != null ? !schemaUrl.equals(that.schemaUrl) : that.schemaUrl != null)
         {
            return false;
         }
         if(typeName != null ? !typeName.equals(that.typeName) : that.typeName != null)
         {
            return false;
         }

         return true;
      }

      public int hashCode()
      {
         int result;
         result = (cls != null ? cls.hashCode() : 0);
         result = 29 * result + (elementName != null ? elementName.hashCode() : 0);
         result = 29 * result + (typeName != null ? typeName.hashCode() : 0);
         result = 29 * result + (schemaUrl != null ? schemaUrl.hashCode() : 0);
         result = 29 * result + (provider != null ? provider.hashCode() : 0);
         return result;
      }
   }

   protected class FieldToWildcardMapping
   {
      public final Class<?> cls;
      public final ObjectLocalMarshaller marshaller;
      public final FieldInfo fieldInfo;

      public FieldToWildcardMapping(Class<?> cls, String field, ObjectLocalMarshaller marshaller)
      {
         if(log.isTraceEnabled())
         {
            log.trace("new FieldToWildcardMapping: [cls=" +
               cls.getName() +
               ",field=" +
               field +
               "]"
            );
         }

         this.cls = cls;
         this.marshaller = marshaller;
         this.fieldInfo = FieldInfo.getFieldInfo(cls, field, true);
      }

      public boolean equals(Object o)
      {
         if(this == o)
         {
            return true;
         }
         if(!(o instanceof FieldToWildcardMapping))
         {
            return false;
         }

         final FieldToWildcardMapping fieldToWildcardMapping = (FieldToWildcardMapping)o;

         if(!cls.equals(fieldToWildcardMapping.cls))
         {
            return false;
         }
         if(!fieldInfo.getName().equals(fieldToWildcardMapping.fieldInfo.getName()))
         {
            return false;
         }

         return true;
      }

      public int hashCode()
      {
         int result;
         result = cls.hashCode();
         result = 29 * result + fieldInfo.getName().hashCode();
         return result;
      }
   }

   protected static interface Stack
   {
      void clear();

      void push(Object o);

      Object pop();

      Object peek();

      boolean isEmpty();
   }

   public static class StackImpl
      implements Stack
   {
      private List<Object> list = new ArrayList<Object>();

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

      public boolean isEmpty()
      {
         return list.isEmpty();
      }
   }

   public class ContentHandlerAdaptor
      implements ContentHandler
   {
      public void setDocumentLocator(Locator locator)
      {
      }

      public void startDocument() throws SAXException
      {
         // this is used to marshal a fragment of a document so we don't delegate startDocument
      }

      public void endDocument() throws SAXException
      {
         // this is used to marshal a fragment of a document so we don't delegate endDocument
      }

      public void startPrefixMapping(String prefix, String uri) throws SAXException
      {
         content.startPrefixMapping(prefix, uri);
      }

      public void endPrefixMapping(String prefix) throws SAXException
      {
         content.endPrefixMapping(prefix);
      }

      public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
      {
         content.startElement(uri, localName, qName, atts);
      }

      public void endElement(String uri, String localName, String qName) throws SAXException
      {
         content.endElement(uri, localName, qName);
      }

      public void characters(char ch[], int start, int length) throws SAXException
      {
         content.characters(ch, start, length);
      }

      public void ignorableWhitespace(char ch[], int start, int length) throws SAXException
      {
      }

      public void processingInstruction(String target, String data) throws SAXException
      {
      }

      public void skippedEntity(String name) throws SAXException
      {
      }
   }
}
