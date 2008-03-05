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
package org.jboss.xb.binding.sunday.marshalling;

import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import org.jboss.xb.binding.Constants;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.xb.binding.SimpleTypeBindings;
import org.jboss.xb.binding.Util;
import org.jboss.xb.binding.introspection.FieldInfo;
import org.jboss.xb.binding.metadata.PropertyMetaData;
import org.jboss.xb.binding.sunday.unmarshalling.AttributeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.TermBinding;
import org.jboss.xb.binding.sunday.unmarshalling.TypeBinding;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class DefaultAttributeMarshaller
   extends AbstractAttributeMarshaller
{
   public static final DefaultAttributeMarshaller INSTANCE = new DefaultAttributeMarshaller();
   
   public Object getValue(MarshallingContext ctx)
   {
      Object owner = ctx.peek();
      SchemaBinding schema = ctx.getSchemaBinding();
      AttributeBinding binding = ctx.getAttributeBinding();
      QName qName = binding.getQName();

      String fieldName = null;
      PropertyMetaData propertyMetaData = binding.getPropertyMetaData();
      if(propertyMetaData != null)
      {
         fieldName = propertyMetaData.getName();
      }

      if(fieldName == null)
      {
         fieldName =
            Util.xmlNameToFieldName(qName.getLocalPart(), schema.isIgnoreLowLine());
      }

      FieldInfo fieldInfo = FieldInfo.getFieldInfo(
         owner.getClass(), fieldName, binding.getRequired() && !schema.isIgnoreUnresolvedFieldOrClass()
      );
      Object value = null;
      if(fieldInfo != null)
      {
         value = fieldInfo.getValue(owner);
      }

      return value;
   }

   public String marshalValue(MarshallingContext ctx, Object value)
   {
      AttributeBinding binding = ctx.getAttributeBinding();

      if(value == null)
      {
         if(binding.getRequired())
         {
            ElementBinding element = (ElementBinding) ctx.getParticleBinding().getTerm();
            throw new JBossXBRuntimeException("Missing value for the required attribute " + binding.getQName() + " of element " + element.getQName());
         }   
         return null;
      }

      String marshalled;

      TypeBinding attrType = binding.getType();

      if(attrType.getItemType() != null)
      {
         TypeBinding itemType = attrType.getItemType();
         if(Constants.NS_XML_SCHEMA.equals(itemType.getQName().getNamespaceURI()))
         {
            List list;
            if(value instanceof List)
            {
               list = (List)value;
            }
            else if(value.getClass().isArray())
            {
               list = Arrays.asList((Object[])value);
            }
            else
            {
               throw new JBossXBRuntimeException("Expected value for list type is an array or " +
                  List.class.getName() +
                  " but got: " +
                  value
               );
            }

            if(Constants.QNAME_QNAME.getLocalPart().equals(itemType.getQName().getLocalPart()))
            {
               String attrLocal = binding.getQName().getLocalPart();
               for(int listInd = 0; listInd < list.size(); ++listInd)
               {
                  QName item = (QName)list.get(listInd);
                  String itemNs = item.getNamespaceURI();
                  if(itemNs != null && itemNs.length() > 0)
                  {
                     String itemPrefix = ctx.getPrefix(itemNs);
                     if(itemPrefix == null)
                     {
                        itemPrefix = item.getPrefix();
                        if(itemPrefix == null || itemPrefix.length() == 0)
                        {
                           itemPrefix = attrLocal + listInd;
                        }
                        ctx.declareNamespace(itemPrefix, itemNs);
                     }

                     if(!itemPrefix.equals(item.getPrefix()))
                     {
                        item = new QName(item.getNamespaceURI(), item.getLocalPart(), itemPrefix);
                        list.set(listInd, item);
                     }
                  }
               }
            }

            marshalled = SimpleTypeBindings.marshalList(itemType.getQName().getLocalPart(), list, null);
         }
         else
         {
            throw new JBossXBRuntimeException("Marshalling of list types with item types not from " +
               Constants.NS_XML_SCHEMA + " is not supported."
            );
         }
      }
      else if(attrType.getLexicalPattern() != null &&
         attrType.getBaseType() != null &&
         Constants.QNAME_BOOLEAN.equals(attrType.getBaseType().getQName()))
      {
         String item = (String)attrType.getLexicalPattern().get(0);
         if(item.indexOf('0') != -1 && item.indexOf('1') != -1)
         {
            marshalled = ((Boolean)value).booleanValue() ? "1" : "0";
         }
         else
         {
            marshalled = ((Boolean)value).booleanValue() ? "true" : "false";
         }
      }
      else if(Constants.QNAME_QNAME.equals(attrType.getQName()))
      {
         boolean removePrefix = false;
         String prefix = null;
         String ns = ((QName)value).getNamespaceURI();
         if(ns != null && ns.length() > 0)
         {
            prefix = ctx.getPrefix(ns);
            if(prefix == null)
            {
               prefix = ((QName)value).getPrefix();
               if(prefix == null || prefix.length() == 0)
               {
                  prefix = "ns_" + ((QName)value).getLocalPart();
               }
               ctx.declareNamespace(prefix, ns);
            }
            ctx.getNamespaceContext().addPrefixMapping(prefix, ns);
            removePrefix = true;
         }

         marshalled = SimpleTypeBindings.marshalQName((QName)value, ctx.getNamespaceContext());

         if(removePrefix)
         {
            ctx.getNamespaceContext().removePrefixMapping(prefix);
         }
      }
      else
      {
         marshalled = value.toString();
      }

      return marshalled;
   }

}
