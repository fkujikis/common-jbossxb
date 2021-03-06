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
package org.jboss.test.xb.builder.object.mc.test;

import java.util.Set;

import junit.framework.Test;

import org.jboss.test.xb.builder.object.mc.support.model.AbstractBeanMetaData;
import org.jboss.test.xb.builder.object.mc.support.model.AbstractDependencyValueMetaData;
import org.jboss.test.xb.builder.object.mc.support.model.ControllerState;
import org.jboss.test.xb.builder.object.mc.support.model.PropertyMetaData;
import org.jboss.test.xb.builder.object.mc.support.model.ValueMetaData;

/**
 * InjectionTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57133 $
 */
public class InjectionTestCase extends AbstractMCTest
{
   protected AbstractDependencyValueMetaData getInjection() throws Exception
   {
      AbstractBeanMetaData bean = unmarshalBean();
      Set<?> properties = bean.getProperties();
      assertNotNull(properties);
      assertEquals(1, properties.size());
      PropertyMetaData property = (PropertyMetaData) properties.iterator().next();
      assertNotNull(property);
      ValueMetaData value = property.getValue();
      assertNotNull(property);
      assertTrue(value instanceof AbstractDependencyValueMetaData);
      return (AbstractDependencyValueMetaData) value;
   }

   public void testInjectionWithBean() throws Exception
   {
      AbstractDependencyValueMetaData dependency = getInjection();
      assertEquals("Bean1", dependency.getValue());
      assertNull(dependency.getProperty());
      assertEquals(ControllerState.INSTALLED, dependency.getDependentState());
   }

   public void testInjectionWithProperty() throws Exception
   {
      AbstractDependencyValueMetaData dependency = getInjection();
      assertEquals("Dummy", dependency.getValue());
      assertEquals("Property1", dependency.getProperty());
      assertEquals(ControllerState.INSTALLED, dependency.getDependentState());
   }

   public void testInjectionWithState() throws Exception
   {
      AbstractDependencyValueMetaData dependency = getInjection();
      assertEquals("Dummy", dependency.getValue());
      assertNull(dependency.getProperty());
      assertEquals(ControllerState.CONFIGURED, dependency.getDependentState());
   }

   /* TODO
   public void testInjectionBadNoBean() throws Exception
   {
      try
      {
         AbstractDependencyValueMetaData dependency = getInjection("InjectionBadNoBean.xml");
         assertNull(dependency.getValue());                  
      }
      catch (Exception expected)
      {
         checkJBossXBException(IllegalArgumentException.class, expected);
      }
   }
   */

   public static Test suite()
   {
      return suite(InjectionTestCase.class);
   }

   public InjectionTestCase(String name)
   {
      super(name);
   }
}
