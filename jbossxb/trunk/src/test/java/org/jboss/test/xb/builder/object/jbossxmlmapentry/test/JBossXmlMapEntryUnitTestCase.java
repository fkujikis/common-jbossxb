/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.xb.builder.object.jbossxmlmapentry.test;

import java.util.Map;

import org.jboss.test.xb.builder.AbstractBuilderTest;
import org.jboss.test.xb.builder.object.jbossxmlmapentry.support.Root;


/**
 * A JBossXmlMapEntryUnitTestCase.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class JBossXmlMapEntryUnitTestCase extends AbstractBuilderTest
{

   public JBossXmlMapEntryUnitTestCase(String name)
   {
      super(name);
   }

   public void testKeyValueSequence() throws Exception
   {
      Root root = unmarshalObject(Root.class);
      assertMap(root.getKeyValueSequence());
   }

   public void testWrappedKeyValueSequence() throws Exception
   {
      Root root = unmarshalObject(Root.class);
      assertMap(root.getWrappedKeyValueSequence());
   }

   public void testKeyValueAttributes() throws Exception
   {
      Root root = unmarshalObject(Root.class);
      assertMap(root.getKeyValueAttributes());
   }

   public void testKeyAttributeValueEntryContent() throws Exception
   {
      Root root = unmarshalObject(Root.class);
      assertMap(root.getKeyAttributeValueEntryContent());
   }

   private void assertMap(Map<String, Integer> map)
   {
      assertNotNull(map);
      assertEquals(3, map.size());
      assertEquals(new Integer(1), map.get("key1"));
      assertEquals(new Integer(22), map.get("key2"));
      assertEquals(new Integer(333), map.get("key3"));
   }
}
