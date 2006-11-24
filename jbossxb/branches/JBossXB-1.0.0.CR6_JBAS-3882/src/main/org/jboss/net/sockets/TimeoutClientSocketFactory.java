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
package org.jboss.net.sockets;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.server.RMIClientSocketFactory;
import java.net.Socket;
import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;

/**
 * A RMIClientSocketFactory that installs a InterruptableInputStream to be
 * responsive to thead interruption events.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class TimeoutClientSocketFactory
   implements RMIClientSocketFactory, Serializable
{
   private static final long serialVersionUID = -920483051658660269L;

   public TimeoutClientSocketFactory()
   {
   }

   /**
    * Create a server socket on the specified port (port 0 indicates
    * an anonymous port).
    * @param  port the port number
    * @return the server socket on the specified port
    * @exception java.io.IOException if an I/O error occurs during server socket
    * creation
    * @since 1.2
    */
   public Socket createSocket(String host, int port) throws IOException
   {
      Socket s = new Socket(host, port);
      s.setSoTimeout(1000);
      TimeoutSocket ts = new TimeoutSocket(s);
      return ts;
   }
   
   public boolean equals(Object obj)
   {
      return obj instanceof TimeoutClientSocketFactory;
   }
   public int hashCode()
   {
      return getClass().getName().hashCode();
   }
   
}
