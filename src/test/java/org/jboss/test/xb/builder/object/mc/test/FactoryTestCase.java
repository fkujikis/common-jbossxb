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

import junit.framework.Test;

import org.jboss.test.xb.builder.object.mc.support.model.AbstractBeanMetaData;
import org.jboss.test.xb.builder.object.mc.support.model.AbstractDependencyValueMetaData;
import org.jboss.test.xb.builder.object.mc.support.model.AbstractValueMetaData;
import org.jboss.test.xb.builder.object.mc.support.model.ConstructorMetaData;
import org.jboss.test.xb.builder.object.mc.support.model.ControllerState;
import org.jboss.test.xb.builder.object.mc.support.model.ValueMetaData;

/**
 * FactoryTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 40798 $
 */
public class FactoryTestCase extends AbstractMCTest
{
   protected AbstractValueMetaData getFactory() throws Exception
   {
      AbstractBeanMetaData bean = unmarshalBean();
      ConstructorMetaData constructor = bean.getConstructor();
      assertNotNull(constructor);
      ValueMetaData factory = constructor.getFactory();
      assertNotNull(factory);
      assertTrue(factory instanceof AbstractValueMetaData);
      return (AbstractValueMetaData) factory;
   }

   protected AbstractDependencyValueMetaData getFactoryDependency() throws Exception
   {
      return (AbstractDependencyValueMetaData) getFactory();
   }

   public void testFactoryWithBean() throws Exception
   {
      AbstractDependencyValueMetaData dependency = getFactoryDependency();
      assertEquals("Bean1", dependency.getValue());
      assertNull(dependency.getProperty());
      assertEquals(ControllerState.INSTALLED, dependency.getDependentState());
   }

   public void testFactoryWithProperty() throws Exception
   {
      AbstractDependencyValueMetaData dependency = getFactoryDependency();
      assertEquals("Dummy", dependency.getValue());
      assertEquals("Property1", dependency.getProperty());
      assertEquals(ControllerState.INSTALLED, dependency.getDependentState());
   }

   public void testFactoryWithState() throws Exception
   {
      AbstractDependencyValueMetaData dependency = getFactoryDependency();
      assertEquals("Dummy", dependency.getValue());
      assertNull(dependency.getProperty());
      assertEquals(ControllerState.CONFIGURED, dependency.getDependentState());
   }

   public void testFactoryWithWildcard() throws Exception
   {
      assertWildcard(getFactory());
   }

   /* TODO
   public void testFactoryBadNoBeanOrWildcard() throws Exception
   {
      try
      {
         unmarshalBean("FactoryBadNoBeanOrWildcard.xml");
         fail("Should not be here");
      }
      catch (Exception expected)
      {
         checkJBossXBException(IllegalArgumentException.class, expected);
      }
   }
   */

   public static Test suite()
   {
      return suite(FactoryTestCase.class);
   }

   public FactoryTestCase(String name)
   {
      super(name);
   }
}
