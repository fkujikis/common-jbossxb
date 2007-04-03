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
package org.jboss.xb.binding.sunday.xop;

import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
import org.jboss.xb.binding.sunday.unmarshalling.ParticleHandler;
import org.jboss.xb.binding.sunday.unmarshalling.ParticleBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultHandlers;
import org.xml.sax.Attributes;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class XOPElementHandler
   implements ParticleHandler
{
   public Object startParticle(Object parent,
                               QName elementName,
                               ParticleBinding particle,
                               Attributes attrs,
                               NamespaceContext nsCtx)
   {
      ElementBinding element = (ElementBinding)particle.getTerm();
      XOPUnmarshaller xopUnmarshaller = element.getXopUnmarshaller();
      if(xopUnmarshaller == null || !xopUnmarshaller.isXOPPackage())
      {
         return null;
      }
      else
      {
         return new XOPElement();
      }
   }

   public Object endParticle(Object o, QName elementName, ParticleBinding particle)
   {
      return o instanceof XOPElement ? ((XOPElement)o).value : o;
   }

   public void setParent(Object parent,
                         Object o,
                         QName elementName,
                         ParticleBinding particle,
                         ParticleBinding parentParticle)
   {
      if(parent == o)
      {
         return;
      }

      // should actually use the handler that would normally be used by the SundayContentHandler
      DefaultHandlers.ELEMENT_HANDLER.setParent(parent, o, elementName, particle, parentParticle);
   }

   public static class XOPElement
   {
      public Object value;
   }
}
