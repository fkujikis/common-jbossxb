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
package org.jboss.test.xb.builder.object.javabean;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jboss.test.xb.builder.object.javabean.test.ParameterInstantiateUnitTestCase;
import org.jboss.test.xb.builder.object.javabean.test.PropertyUnitTestCase;
import org.jboss.test.xb.builder.object.javabean.test.PropertyWildcardUnitTestCase;
import org.jboss.test.xb.builder.object.javabean.test.SimpleInstantiateUnitTestCase;
import org.jboss.test.xb.builder.object.javabean.test.StaticClassMethodInstantiateUnitTestCase;
import org.jboss.test.xb.builder.object.javabean.test.StaticMethodInstantiateUnitTestCase;

/**
 * ObjectJavaBeanTestSuite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ObjectJavaBeanTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Object JavaBean Tests");

      suite.addTest(SimpleInstantiateUnitTestCase.suite());
      suite.addTest(StaticMethodInstantiateUnitTestCase.suite());
      suite.addTest(StaticClassMethodInstantiateUnitTestCase.suite());
      suite.addTest(ParameterInstantiateUnitTestCase.suite());
      suite.addTest(PropertyUnitTestCase.suite());
      suite.addTest(PropertyWildcardUnitTestCase.suite());
      
      return suite;
   }
}
