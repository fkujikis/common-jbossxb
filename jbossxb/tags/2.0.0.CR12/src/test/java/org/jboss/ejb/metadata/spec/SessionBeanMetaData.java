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
package org.jboss.ejb.metadata.spec;

import org.jboss.javaee.metadata.spec.LifecycleCallbacksMetaData;
import org.jboss.javaee.metadata.spec.SecurityRoleRefsMetaData;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SessionBeanMetaData.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
@XmlType(name="session-beanType")
public class SessionBeanMetaData extends EnterpriseBeanMetaData
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 2093953120735574157L;
   
   /** The home interface */
   private String home;

   /** The remote interface */
   private String remote;

   /** The local home */
   private String localHome;

   /** The local */
   private String local;
   
   /** The business locals */
   private BusinessLocalsMetaData businessLocals;
   
   /** The business remotes */
   private BusinessRemotesMetaData businessRemotes;
   
   /** The service endpoint */
   private String serviceEndpoint;

   /** The sesion type */
   private SessionType sessionType;
   
   /** The timeout method */
   private NamedMethodMetaData timeoutMethod;
   
   /** The init methods */
   private InitMethodsMetaData initMethods;
   
   /** The remove methods */
   private RemoveMethodsMetaData removeMethods;
   
   /** The transaction type */
   private TransactionType transactionType;
   
   /** The around invoke */
   private AroundInvokesMetaData aroundInvokes;

   /** The post activates */
   private LifecycleCallbacksMetaData postActivates;

   /** The pre passivates */
   private LifecycleCallbacksMetaData prePassivates;

   /** The security role ref */
   private SecurityRoleRefsMetaData securityRoleRefs;
   
   /**
    * Create a new SessionBeanMetaData.
    */
   public SessionBeanMetaData()
   {
      // For serialization
   }

   @Override
   public boolean isSession()
   {
      return true;
   }

   /**
    * Get the home.
    * 
    * @return the home.
    */
   public String getHome()
   {
      return home;
   }

   /**
    * Set the home.
    * 
    * @param home the home.
    * @throws IllegalArgumentException for a null home
    */
   public void setHome(String home)
   {
      if (home == null)
         throw new IllegalArgumentException("Null home");
      this.home = home;
   }

   /**
    * Get the remote.
    * 
    * @return the remote.
    */
   public String getRemote()
   {
      return remote;
   }

   /**
    * Set the remote.
    * 
    * @param remote the remote.
    * @throws IllegalArgumentException for a null remote
    */
   public void setRemote(String remote)
   {
      if (remote == null)
         throw new IllegalArgumentException("Null remote");
      this.remote = remote;
   }

   /**
    * Get the localHome.
    * 
    * @return the localHome.
    */
   public String getLocalHome()
   {
      return localHome;
   }

   /**
    * Set the localHome.
    * 
    * @param localHome the localHome.
    * @throws IllegalArgumentException for a null localHome
    */
   public void setLocalHome(String localHome)
   {
      if (localHome == null)
         throw new IllegalArgumentException("Null localHome");
      this.localHome = localHome;
   }

   /**
    * Get the local.
    * 
    * @return the local.
    */
   public String getLocal()
   {
      return local;
   }

   /**
    * Set the local.
    * 
    * @param local the local.
    * @throws IllegalArgumentException for a null local
    */
   public void setLocal(String local)
   {
      if (local == null)
         throw new IllegalArgumentException("Null local");
      this.local = local;
   }

   /**
    * Get the businessLocals.
    * 
    * @return the businessLocals.
    */
   public BusinessLocalsMetaData getBusinessLocals()
   {
      return businessLocals;
   }

   /**
    * Set the businessLocals.
    * 
    * @param businessLocals the businessLocals.
    * @throws IllegalArgumentException for a null businessLocasl
    */
   @XmlElement(name="business-local", required=false)
   public void setBusinessLocals(BusinessLocalsMetaData businessLocals)
   {
      if (businessLocals == null)
         throw new IllegalArgumentException("Null businessLocals");
      this.businessLocals = businessLocals;
   }

   /**
    * Get the businessRemotes.
    * 
    * @return the businessRemotes.
    */
   public BusinessRemotesMetaData getBusinessRemotes()
   {
      return businessRemotes;
   }

   /**
    * Set the businessRemotes.
    * 
    * @param businessRemotes the businessRemotes.
    * @throws IllegalArgumentException for a null businessRemotes
    */
   @XmlElement(name="business-remote", required=false)
   public void setBusinessRemotes(BusinessRemotesMetaData businessRemotes)
   {
      if (businessRemotes == null)
         throw new IllegalArgumentException("Null businessRemotes");
      this.businessRemotes = businessRemotes;
   }

   /**
    * Get the serviceEndpoint.
    * 
    * @return the serviceEndpoint.
    */
   public String getServiceEndpoint()
   {
      return serviceEndpoint;
   }

   /**
    * Set the serviceEndpoint.
    * 
    * @param serviceEndpoint the serviceEndpoint.
    * @throws IllegalArgumentException for a null serviceEndpoint
    */
   public void setServiceEndpoint(String serviceEndpoint)
   {
      if (serviceEndpoint == null)
         throw new IllegalArgumentException("Null serviceEndpoint");
      this.serviceEndpoint = serviceEndpoint;
   }

   /**
    * Get the sessionType.
    * 
    * @return the sessionType.
    */
   public SessionType getSessionType()
   {
      return sessionType;
   }

   /**
    * Set the sessionType.
    * 
    * @param sessionType the sessionType.
    * @throws IllegalArgumentException for a null sessionType
    */
   public void setSessionType(SessionType sessionType)
   {
      if (sessionType == null)
         throw new IllegalArgumentException("Null sessionType");
      this.sessionType = sessionType;
   }

   /**
    * Is this stateful
    * 
    * @return true for stateful
    */
   public boolean isStateful()
   {
      if (sessionType == null)
         return false;
      return sessionType == SessionType.Stateful;
   }

   /**
    * Is this stateless
    * 
    * @return true for stateless
    */
   public boolean isStateless()
   {
      return isStateful() == false;
   }
   
   /**
    * Get the timeoutMethod.
    * 
    * @return the timeoutMethod.
    */
   public NamedMethodMetaData getTimeoutMethod()
   {
      return timeoutMethod;
   }

   /**
    * Set the timeoutMethod.
    * 
    * @param timeoutMethod the timeoutMethod.
    * @throws IllegalArgumentException for a null timeoutMethod
    */
   @XmlElement(required=false)
   public void setTimeoutMethod(NamedMethodMetaData timeoutMethod)
   {
      if (timeoutMethod == null)
         throw new IllegalArgumentException("Null timeoutMethod");
      this.timeoutMethod = timeoutMethod;
   }

   /**
    * Get the initMethods.
    * 
    * @return the initMethods.
    */
   public InitMethodsMetaData getInitMethods()
   {
      return initMethods;
   }

   /**
    * Set the initMethods.
    * 
    * @param initMethods the initMethods.
    * @throws IllegalArgumentException for a null initMethods
    */
   @XmlElement(name="init-method", required=false)
   public void setInitMethods(InitMethodsMetaData initMethods)
   {
      if (initMethods == null)
         throw new IllegalArgumentException("Null initMethods");
      this.initMethods = initMethods;
   }

   /**
    * Get the removeMethods.
    * 
    * @return the removeMethods.
    */
   public RemoveMethodsMetaData getRemoveMethods()
   {
      return removeMethods;
   }

   /**
    * Set the removeMethods.
    * 
    * @param removeMethods the removeMethods.
    * @throws IllegalArgumentException for a null removeMethods
    */
   @XmlElement(name="remove-method", required=false)
   public void setRemoveMethods(RemoveMethodsMetaData removeMethods)
   {
      if (removeMethods == null)
         throw new IllegalArgumentException("Null removeMethods");
      this.removeMethods = removeMethods;
   }

   @Override
   public TransactionType getTransactionType()
   {
      return transactionType;
   }

   /**
    * Set the transactionType.
    * 
    * @param transactionType the transactionType.
    * @throws IllegalArgumentException for a null transactionType
    */
   public void setTransactionType(TransactionType transactionType)
   {
      if (transactionType == null)
         throw new IllegalArgumentException("Null transactionType");
      this.transactionType = transactionType;
   }

   /**
    * Get the aroundInvokes.
    * 
    * @return the aroundInvokes.
    */
   public AroundInvokesMetaData getAroundInvokes()
   {
      return aroundInvokes;
   }

   /**
    * Set the aroundInvokes.
    * 
    * @param aroundInvokes the aroundInvokes.
    * @throws IllegalArgumentException for a null aroundInvokes
    */
   @XmlElement(name="around-invoke", required=false)
   public void setAroundInvokes(AroundInvokesMetaData aroundInvokes)
   {
      if (aroundInvokes == null)
         throw new IllegalArgumentException("Null aroundInvokes");
      this.aroundInvokes = aroundInvokes;
   }

   /**
    * Get the postActivates.
    * 
    * @return the postActivates.
    */
   public LifecycleCallbacksMetaData getPostActivates()
   {
      return postActivates;
   }

   /**
    * Set the postActivates.
    * 
    * @param postActivates the postActivates.
    * @throws IllegalArgumentException for a null postActivates
    */
   @XmlElement(name="post-activate", required=false)
   public void setPostActivates(LifecycleCallbacksMetaData postActivates)
   {
      if (postActivates == null)
         throw new IllegalArgumentException("Null postActivates");
      this.postActivates = postActivates;
   }

   /**
    * Get the prePassivates.
    * 
    * @return the prePassivates.
    */
   public LifecycleCallbacksMetaData getPrePassivates()
   {
      return prePassivates;
   }

   /**
    * Set the prePassivates.
    * 
    * @param prePassivates the prePassivates.
    * @throws IllegalArgumentException for a null prePassivates
    */
   @XmlElement(name="pre-passivate", required=false)
   public void setPrePassivates(LifecycleCallbacksMetaData prePassivates)
   {
      if (prePassivates == null)
         throw new IllegalArgumentException("Null prePassivates");
      this.prePassivates = prePassivates;
   }

   /**
    * Get the securityRoleRefs.
    * 
    * @return the securityRoleRefs.
    */
   public SecurityRoleRefsMetaData getSecurityRoleRefs()
   {
      return securityRoleRefs;
   }

   /**
    * Set the securityRoleRefs.
    * 
    * @param securityRoleRefs the securityRoleRefs.
    * @throws IllegalArgumentException for a null securityRoleRefs
    */
   @XmlElement(name="security-role-ref")
   public void setSecurityRoleRefs(SecurityRoleRefsMetaData securityRoleRefs)
   {
      if (securityRoleRefs == null)
         throw new IllegalArgumentException("Null securityRoleRefs");
      this.securityRoleRefs = securityRoleRefs;
   }
}
