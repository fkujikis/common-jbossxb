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

import org.jboss.util.JBossStringBuilder;
import org.jboss.util.Strings;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class PackageMetaData
{
   private String name;

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }
      if(!(o instanceof PackageMetaData))
      {
         return false;
      }

      final PackageMetaData packageMetaData = (PackageMetaData)o;

      if(name != null ? !name.equals(packageMetaData.name) : packageMetaData.name != null)
      {
         return false;
      }

      return true;
   }

   public int hashCode()
   {
      return (name != null ? name.hashCode() : 0);
   }
   
   public String toString()
   {
      JBossStringBuilder buffer = new JBossStringBuilder();
      Strings.defaultToString(buffer, this);
      buffer.append('[');
      buffer.append("name=").append(name);
      buffer.append(']');
      return buffer.toString();
   }
}
