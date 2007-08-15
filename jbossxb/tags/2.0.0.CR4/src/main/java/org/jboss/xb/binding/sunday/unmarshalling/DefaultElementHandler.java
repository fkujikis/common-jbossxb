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
package org.jboss.xb.binding.sunday.unmarshalling;

import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;

import org.jboss.logging.Logger;
import org.xml.sax.Attributes;

/**
 * This handler can only be used if model group binding is not used.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision$</tt>
 */
public class DefaultElementHandler
   implements ElementHandler, ParticleHandler
{
   /** The log */
   private static final Logger log = Logger.getLogger(DefaultElementHandler.class);
   
   public static final DefaultElementHandler INSTANCE = new DefaultElementHandler();

   private AttributesHandler attrsHandler;

   public DefaultElementHandler()
   {
      this(AttributesHandler.INSTANCE);
   }

   public DefaultElementHandler(AttributesHandler attrsHandler)
   {
      this.attrsHandler = attrsHandler;
   }

   public Object startElement(Object parent, QName qName, ElementBinding element)
   {
      return parent;
   }

   public void attributes(Object o, QName elementName, ElementBinding element, Attributes attrs, NamespaceContext nsCtx)
   {
      if(attrsHandler != null)
      {
         attrsHandler.attributes(o, elementName, element.getType(), attrs, nsCtx);
      }
   }

   public Object endElement(Object o, QName qName, ElementBinding element)
   {
      return o;
   }

   public void setParent(Object parent, Object o, QName qName, ElementBinding element, ElementBinding parentElement)
   {
      if (log.isTraceEnabled())
         log.trace("Not setting " + o + " on " + parent + " for " + qName);
   }

   // ParticleHandler impl

   public Object startParticle(Object parent,
                               QName elementName,
                               ParticleBinding particle,
                               Attributes attrs,
                               NamespaceContext nsCtx)
   {
      ElementBinding element = (ElementBinding)particle.getTerm();
      Object o = startElement(parent, elementName, element);
      if(o != null)
      {
         attrs = element.getType().expandWithDefaultAttributes(attrs);
         attributes(o, elementName, element, attrs, nsCtx);
      }
      return o;
   }

   public Object endParticle(Object o, QName elementName, ParticleBinding particle)
   {
      return endElement(o, elementName, (ElementBinding)particle.getTerm());
   }

   public void setParent(Object parent,
                         Object o,
                         QName elementName,
                         ParticleBinding particle,
                         ParticleBinding parentParticle)
   {
      ElementBinding element = (ElementBinding)particle.getTerm();
      ElementBinding parentElement = (ElementBinding)parentParticle.getTerm();
      setParent(parent, o, elementName, element, parentElement);
   }
}
