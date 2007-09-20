/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.javaee.metadata.support;

import javax.xml.bind.annotation.XmlAttribute;

import org.jboss.util.UnreachableStatementException;

/**
 * IdMetaDataImpl.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class IdMetaDataImpl implements IdMetaData, Cloneable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2952233733011178332L;
   
   /** The id */
   String id;

   /**
    * Create a new IdMetaDataImpl.
    */
   public IdMetaDataImpl()
   {
      // For serialization
   }
   
   public String getId()
   {
      return id;
   }

   @XmlAttribute
   public void setId(String id)
   {
      if (id == null)
         throw new IllegalArgumentException("Null id");
      this.id = id;
   }

   @Override
   public IdMetaDataImpl clone()
   {
      try
      {
         return (IdMetaDataImpl) super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         throw new UnreachableStatementException("clone");
      }
   }

   /**
    * Merge
    * 
    * @param merged the data to merge into
    * @param original the original data
    * @throws IllegalArgumentException for a null merged or original
    */
   public void merge(IdMetaDataImpl merged, IdMetaDataImpl original)
   {
      if (merged == null)
         throw new IllegalArgumentException("Null merged");
      if (original == null)
         throw new IllegalArgumentException("Null original");
      if (id != null)
         merged.setId(id);
      else if (original.id != null)
         merged.setId(original.id);
   }

   @Override
   public int hashCode()
   {
      final int PRIME = 31;
      int result = 1;
      result = PRIME * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final IdMetaDataImpl other = (IdMetaDataImpl) obj;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      return true;
   }
}
