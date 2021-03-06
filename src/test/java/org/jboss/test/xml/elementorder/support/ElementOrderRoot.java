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
package org.jboss.test.xml.elementorder.support;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A ElementOrderRoot.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
@XmlRootElement(name="root")
@XmlType(propOrder={"first", "second", "ABSequence", "repeatedCD"})
public class ElementOrderRoot
{
   private String first;
   private String second;
   private ABSequence abSequence;
   private List<CDSequence> repeatedCD;
   
   public String getFrist()
   {
      return first;
   }

   public void setFirst(String first)
   {
      this.first = first;
   }
   
   public String getSecond()
   {
      return second;
   }
   
   public void setSecond(String second)
   {
      this.second = second;
   }
   
   public ABSequence getABSequence()
   {
      return abSequence;
   }
   
   public void setABSequence(ABSequence abSequence)
   {
      this.abSequence = abSequence;
   }
   
   public List<CDSequence> getRepeatedCD()
   {
      return repeatedCD;
   }
   
   public void setRepeatedCD(List<CDSequence> repeatedCD)
   {
      this.repeatedCD = repeatedCD;
   }
}
