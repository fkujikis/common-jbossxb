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
package org.jboss.util.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.util.NestedRuntimeException;
import org.jboss.util.xml.DOMWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A property editor for {@link org.w3c.dom.Document}.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:eross@noderunner.net">Elias Ross</a>
 */
public class DocumentEditor extends PropertyEditorSupport
{
   /**
    * Returns the property as a String.
    *
    * @throws NestedRuntimeException  conversion exception occured
    */
   public String getAsText()
   {
      return DOMWriter.printNode((Node)getValue(), false);
   }

   /**
    * Sets as an Document created by a String.
    *
    * @throws NestedRuntimeException  A parse exception occured
    */
   public void setAsText(String text)
   {
      setValue(getAsDocument(text));
   }

   protected Document getAsDocument(String text)
   {
      try
      {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         StringReader sr = new StringReader(text);
         InputSource is = new InputSource(sr);
         Document d = db.parse(is);
         return d;
      }
      catch (ParserConfigurationException e)
      {
         throw new NestedRuntimeException(e);
      }
      catch (SAXException e)
      {
         throw new NestedRuntimeException(e);
      }
      catch (IOException e)
      {
         throw new NestedRuntimeException(e);
      }
   }
}
