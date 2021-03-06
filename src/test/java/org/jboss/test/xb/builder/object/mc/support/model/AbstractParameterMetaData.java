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
package org.jboss.test.xb.builder.object.mc.support.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.jboss.util.JBossStringBuilder;

/**
 * Metadata for a parameter.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 60019 $
 */
@XmlType(propOrder={"annotations", "value"})
public class AbstractParameterMetaData extends AbstractFeatureMetaData
   implements ParameterMetaData, ValueMetaDataAware, Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * The parameter type
    */
   protected String type;

   /**
    * The parameter value
    */
   protected ValueMetaData value;

   /**
    * The index in parameter list
    */
   protected int index;

   /**
    * Create a new parameter meta data
    */
   public AbstractParameterMetaData()
   {
   }

   /**
    * Create a new parameter meta data
    *
    * @param value the value
    */
   public AbstractParameterMetaData(Object value)
   {
      this.type = value.getClass().getName();
      this.value = new AbstractValueMetaData(value);
   }

   /**
    * Create a new parameter meta data
    *
    * @param value the value metadata
    */
   public AbstractParameterMetaData(ValueMetaData value)
   {
      this.value = value;
   }

   /**
    * Create a new parameter meta data
    *
    * @param type  the type
    * @param value the value
    */
   public AbstractParameterMetaData(String type, Object value)
   {
      this.type = type;
      this.value = new AbstractValueMetaData(value);
   }

   /**
    * Create a new parameter meta data
    *
    * @param type  the type
    * @param value the string value
    */
   public AbstractParameterMetaData(String type, String value)
   {
      this.type = type;
      this.value = new StringValueMetaData(value);
   }

   /**
    * Create a new parameter meta data
    *
    * @param type  the type
    * @param value the value meta data
    */
   public AbstractParameterMetaData(String type, ValueMetaData value)
   {
      this.type = type;
      this.value = value;
   }

   public String getType()
   {
      return type;
   }

   @XmlAttribute(name="class")
   public void setType(String type)
   {
      this.type = type;
      flushJBossObjectCache();
   }

   public ValueMetaData getValue()
   {
      return value;
   }

   public int getIndex()
   {
      return index;
   }

   public void setIndex(int index)
   {
      this.index = index;
   }

   @XmlElements
   ({
      @XmlElement(name="array", type=AbstractArrayMetaData.class),
      @XmlElement(name="collection", type=AbstractCollectionMetaData.class),
      @XmlElement(name="inject", type=AbstractDependencyValueMetaData.class),
      @XmlElement(name="list", type=AbstractListMetaData.class),
      @XmlElement(name="map", type=AbstractMapMetaData.class),
      @XmlElement(name="set", type=AbstractSetMetaData.class),
      @XmlElement(name="this", type=ThisValueMetaData.class),
      @XmlElement(name="value", type=StringValueMetaData.class)
   })
   public void setValue(ValueMetaData value)
   {
      this.value = value;
      flushJBossObjectCache();
   }

   @XmlAnyElement
   public void setValueObject(Object value)
   {
      if (value == null)
         setValue(null);
      else if (value instanceof ValueMetaData)
         setValue((ValueMetaData) value);
      else
         setValue(new AbstractValueMetaData(value));
   }

   @XmlValue
   public void setValueString(String value)
   {
      if (value == null)
         setValue(null);
      else
      {
         StringValueMetaData stringValue = new StringValueMetaData(value);
         stringValue.setType(getType());
         setValue(stringValue);
      }
   }
   
   public void toString(JBossStringBuilder buffer)
   {
      buffer.append("type=").append(type);
      buffer.append(" value=").append(value);
      super.toString(buffer);
   }

   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(type);
   }
}
