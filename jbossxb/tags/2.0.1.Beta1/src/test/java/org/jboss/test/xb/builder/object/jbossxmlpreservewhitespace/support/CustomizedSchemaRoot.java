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
package org.jboss.test.xb.builder.object.jbossxmlpreservewhitespace.support;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.xb.annotations.JBossXmlPreserveWhitespace;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * A CustomizedSchemaRoot.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
@XmlRootElement(name="root")
@JBossXmlSchema(normalizeSpace=true)
public class CustomizedSchemaRoot
{
   private String schemaDefaults;
   private String preserveTrue;
   private String preserveFalse;
   
   private String schemaDefaultsAttribute;
   private String preserveTrueAttribute;
   private String preserveFalseAttribute;

   public String getSchemaDefaults()
   {
      return schemaDefaults;
   }
   
   public void setSchemaDefaults(String str)
   {
      this.schemaDefaults = str;
   }
   
   @JBossXmlPreserveWhitespace
   public String getPreserveTrue()
   {
      return preserveTrue;
   }
   
   public void setPreserveTrue(String str)
   {
      this.preserveTrue = str;
   }

   @JBossXmlPreserveWhitespace(preserve=false)
   public String getPreserveFalse()
   {
      return preserveFalse;
   }
   
   public void setPreserveFalse(String str)
   {
      this.preserveFalse = str;
   }

   @XmlAttribute()
   public String getSchemaDefaultsAttribute()
   {
      return schemaDefaultsAttribute;
   }
   
   public void setSchemaDefaultsAttribute(String str)
   {
      this.schemaDefaultsAttribute = str;
   }
   
   @XmlAttribute()
   @JBossXmlPreserveWhitespace
   public String getPreserveTrueAttribute()
   {
      return preserveTrueAttribute;
   }
   
   public void setPreserveTrueAttribute(String str)
   {
      this.preserveTrueAttribute = str;
   }

   @XmlAttribute()
   @JBossXmlPreserveWhitespace(preserve=false)
   public String getPreserveFalseAttribute()
   {
      return preserveFalseAttribute;
   }
   
   public void setPreserveFalseAttribute(String str)
   {
      this.preserveFalseAttribute = str;
   }
}
