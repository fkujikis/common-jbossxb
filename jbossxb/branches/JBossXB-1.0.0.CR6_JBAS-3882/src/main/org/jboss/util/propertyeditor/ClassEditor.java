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

import org.jboss.util.NestedRuntimeException;

/**
 * A property editor for {@link java.lang.Class}.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ClassEditor extends TextPropertyEditorSupport
{
   /**
    * Returns a Class for the input object converted to a string.
    *
    * @return a Class object
    *
    * @throws NestedRuntimeException   Failed to create Class instance.
    */
   public Object getValue()
   {
      try
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         String classname = getAsText();
         Class type = loader.loadClass(classname);

         return type;
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }
}
