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

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public interface Constants
{
   String NS_XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
   String NS_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
   String NS_XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
   String NS_XML_XMLNS = "http://www.w3.org/2000/xmlns/";
   String NS_XML_MIME = "http://www.w3.org/2005/05/xmlmime";
   String NS_XOP_INCLUDE = "http://www.w3.org/2004/08/xop/include";
   
   String NS_JAXB = "http://java.sun.com/xml/ns/jaxb";
   String NS_JBXB = "http://www.jboss.org/xml/ns/jbxb";

   //
   // XML schema type names
   //

   QName QNAME_ANYSIMPLETYPE = new QName(NS_XML_SCHEMA, "anySimpleType");
   QName QNAME_ANYTYPE = new QName(NS_XML_SCHEMA, "anyType");

   // primitive datatypes
   QName QNAME_STRING = new QName(NS_XML_SCHEMA, "string");
   QName QNAME_BOOLEAN = new QName(NS_XML_SCHEMA, "boolean");
   QName QNAME_DECIMAL = new QName(NS_XML_SCHEMA, "decimal");
   QName QNAME_FLOAT = new QName(NS_XML_SCHEMA, "float");
   QName QNAME_DOUBLE = new QName(NS_XML_SCHEMA, "double");
   QName QNAME_DURATION = new QName(NS_XML_SCHEMA, "duration");
   QName QNAME_DATETIME = new QName(NS_XML_SCHEMA, "dateTime");
   QName QNAME_TIME = new QName(NS_XML_SCHEMA, "time");
   QName QNAME_DATE = new QName(NS_XML_SCHEMA, "date");
   QName QNAME_GYEARMONTH = new QName(NS_XML_SCHEMA, "gYearMonth");
   QName QNAME_GYEAR = new QName(NS_XML_SCHEMA, "gYear");
   QName QNAME_GMONTHDAY = new QName(NS_XML_SCHEMA, "gMonthDay");
   QName QNAME_GDAY = new QName(NS_XML_SCHEMA, "gDay");
   QName QNAME_GMONTH = new QName(NS_XML_SCHEMA, "gMonth");
   QName QNAME_HEXBINARY = new QName(NS_XML_SCHEMA, "hexBinary");
   QName QNAME_BASE64BINARY = new QName(NS_XML_SCHEMA, "base64Binary");
   QName QNAME_ANYURI = new QName(NS_XML_SCHEMA, "anyURI");
   QName QNAME_QNAME = new QName(NS_XML_SCHEMA, "QName");
   QName QNAME_NOTATION = new QName(NS_XML_SCHEMA, "NOTATION");

   // derived datatypes
   QName QNAME_NORMALIZEDSTRING = new QName(NS_XML_SCHEMA, "normalizedString");
   QName QNAME_TOKEN = new QName(NS_XML_SCHEMA, "token");
   QName QNAME_LANGUAGE = new QName(NS_XML_SCHEMA, "language");
   QName QNAME_NMTOKEN = new QName(NS_XML_SCHEMA, "NMTOKEN");
   QName QNAME_NMTOKENS = new QName(NS_XML_SCHEMA, "NMTOKENS");
   QName QNAME_NAME = new QName(NS_XML_SCHEMA, "Name");
   QName QNAME_NCNAME = new QName(NS_XML_SCHEMA, "NCName");
   QName QNAME_ID = new QName(NS_XML_SCHEMA, "ID");
   QName QNAME_IDREF = new QName(NS_XML_SCHEMA, "IDREF");
   QName QNAME_IDREFS = new QName(NS_XML_SCHEMA, "IDREFS");
   QName QNAME_ENTITY = new QName(NS_XML_SCHEMA, "ENTITY");
   QName QNAME_ENTITIES = new QName(NS_XML_SCHEMA, "ENTITIES");
   QName QNAME_INTEGER = new QName(NS_XML_SCHEMA, "integer");
   QName QNAME_NONPOSITIVEINTEGER = new QName(NS_XML_SCHEMA, "nonPositiveInteger");
   QName QNAME_NEGATIVEINTEGER = new QName(NS_XML_SCHEMA, "negativeInteger");
   QName QNAME_LONG = new QName(NS_XML_SCHEMA, "long");
   QName QNAME_INT = new QName(NS_XML_SCHEMA, "int");
   QName QNAME_SHORT = new QName(NS_XML_SCHEMA, "short");
   QName QNAME_BYTE = new QName(NS_XML_SCHEMA, "byte");
   QName QNAME_NONNEGATIVEINTEGER = new QName(NS_XML_SCHEMA, "nonNegativeInteger");
   QName QNAME_UNSIGNEDLONG = new QName(NS_XML_SCHEMA, "unsignedLong");
   QName QNAME_UNSIGNEDINT = new QName(NS_XML_SCHEMA, "unsignedInt");
   QName QNAME_UNSIGNEDSHORT = new QName(NS_XML_SCHEMA, "unsignedShort");
   QName QNAME_UNSIGNEDBYTE = new QName(NS_XML_SCHEMA, "unsignedByte");
   QName QNAME_POSITIVEINTEGER = new QName(NS_XML_SCHEMA, "positiveInteger");

   QName QNAME_XMIME_BASE64BINARY = new QName(NS_XML_MIME, "base64Binary");
   QName QNAME_XMIME_CONTENTTYPE = new QName(NS_XML_MIME, "contentType");
   QName QNAME_XOP_INCLUDE = new QName(Constants.NS_XOP_INCLUDE, "Include");
}
