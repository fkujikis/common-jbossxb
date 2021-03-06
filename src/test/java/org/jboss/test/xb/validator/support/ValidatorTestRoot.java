/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.xb.validator.support;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * A AbstractValidatorTestRoot.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
@JBossXmlSchema(namespace="urn:jboss:xb:test", elementFormDefault=XmlNsForm.QUALIFIED)
@XmlRootElement(name="root", namespace="urn:jboss:xb:test")
@XmlType(propOrder={"e1", "e2"}, name="")
public class ValidatorTestRoot
{
   private String e1;
   private String e2;

   public ValidatorTestRoot()
   {
      super();
   }

   public String getE1()
   {
      return e1;
   }

   public void setE1(String e1)
   {
      this.e1 = e1;
   }

   public String getE2()
   {
      return e2;
   }

   public void setE2(String e2)
   {
      this.e2 = e2;
   }

}