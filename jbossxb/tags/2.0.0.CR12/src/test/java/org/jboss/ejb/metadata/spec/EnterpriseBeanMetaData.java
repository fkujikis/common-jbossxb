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

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.javaee.metadata.spec.EJBLocalReferenceMetaData;
import org.jboss.javaee.metadata.spec.EJBLocalReferencesMetaData;
import org.jboss.javaee.metadata.spec.EJBReferenceMetaData;
import org.jboss.javaee.metadata.spec.EJBReferencesMetaData;
import org.jboss.javaee.metadata.spec.Environment;
import org.jboss.javaee.metadata.spec.EnvironmentEntriesMetaData;
import org.jboss.javaee.metadata.spec.EnvironmentEntryMetaData;
import org.jboss.javaee.metadata.spec.EnvironmentRefsGroupMetaData;
import org.jboss.javaee.metadata.spec.LifecycleCallbacksMetaData;
import org.jboss.javaee.metadata.spec.MessageDestinationReferenceMetaData;
import org.jboss.javaee.metadata.spec.MessageDestinationReferencesMetaData;
import org.jboss.javaee.metadata.spec.PersistenceContextReferenceMetaData;
import org.jboss.javaee.metadata.spec.PersistenceContextReferencesMetaData;
import org.jboss.javaee.metadata.spec.PersistenceUnitReferenceMetaData;
import org.jboss.javaee.metadata.spec.PersistenceUnitReferencesMetaData;
import org.jboss.javaee.metadata.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.javaee.metadata.spec.ResourceEnvironmentReferencesMetaData;
import org.jboss.javaee.metadata.spec.ResourceReferenceMetaData;
import org.jboss.javaee.metadata.spec.ResourceReferencesMetaData;
import org.jboss.javaee.metadata.spec.ServiceReferenceMetaData;
import org.jboss.javaee.metadata.spec.ServiceReferencesMetaData;
import org.jboss.javaee.metadata.support.AbstractMappedMetaData;
import org.jboss.javaee.metadata.support.NamedMetaDataWithDescriptionGroup;
import org.jboss.xb.annotations.JBossXmlConstants;
import org.jboss.xb.annotations.JBossXmlModelGroup;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * EnterpriseBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
@JBossXmlModelGroup(
      kind=JBossXmlConstants.MODEL_GROUP_CHOICE,
      particles={
            @JBossXmlModelGroup.Particle(element=@XmlElement(name="session"), type=SessionBeanMetaData.class),
            @JBossXmlModelGroup.Particle(element=@XmlElement(name="entity"), type=EntityBeanMetaData.class),
            @JBossXmlModelGroup.Particle(element=@XmlElement(name="message-driven"), type=MessageDrivenBeanMetaData.class)})
public abstract class EnterpriseBeanMetaData extends NamedMetaDataWithDescriptionGroup implements Environment
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -10005320902508914L;

   /** The enterprise bean container */
   private EnterpriseBeansMetaData enterpriseBeansMetaData;

   /** The mapped name */
   private String mappedName;

   /** The ejb class */
   private String ejbClass;
   
   /** The environment */
   private EnvironmentRefsGroupMetaData jndiEnvironmentRefsGroup;
   
   /** The security identity */
   private SecurityIdentityMetaData securityIdentity;

   /** The cached container transactions */
   private transient ContainerTransactionsMetaData cachedContainerTransactions;
   
   /** The transaction type cache */
   private transient ConcurrentHashMap<Method, TransAttributeType> methodTx; 
   
   /**
    * Create a new EnterpriseBeanMetaData.
    */
   public EnterpriseBeanMetaData()
   {
      // For serialization
   }

   /**
    * Set the enterpriseBeansMetaData.
    * 
    * @param enterpriseBeansMetaData the enterpriseBeansMetaData.
    */
   @XmlTransient
   void setEnterpriseBeansMetaData(EnterpriseBeansMetaData enterpriseBeansMetaData)
   {
      this.enterpriseBeansMetaData = enterpriseBeansMetaData;
   }

   /**
    * Get the ejbJarMetaData.
    * 
    * @return the ejbJarMetaData.
    */
   @XmlTransient
   public EjbJarMetaData getEjbJarMetaData()
   {
      if (enterpriseBeansMetaData == null)
         return null;
      return enterpriseBeansMetaData.getEjbJarMetaData();
   }

   /**
    * Get the assembly descriptor
    * 
    * @return the ejbJarMetaData.
    */
   AssemblyDescriptorMetaData getAssemblyDescriptor()
   {
      EjbJarMetaData ejbJar = getEjbJarMetaData();
      if (ejbJar == null)
         return null;
      return ejbJar.getAssemblyDescriptor();
   }
   
   /**
    * Get the ejbName.
    * 
    * @return the ejbName.
    */
   public String getEjbName()
   {
      return getName();
   }

   /**
    * Set the ejbName.
    * 
    * @param ejbName the ejbName.
    * @throws IllegalArgumentException for a null ejbName
    */
   public void setEjbName(String ejbName)
   {
      setName(ejbName);
   }

   /**
    * Whether this is a session bean
    * 
    * @return true when a session bean
    */
   public boolean isSession()
   {
      return false;
   }

   /**
    * Whether this is a message driven bean
    * 
    * @return true when a message driven bean
    */
   public boolean isMessageDriven()
   {
      return false;
   }

   /**
    * Whether this is an entity bean
    * 
    * @return true when an entity bean
    */
   public boolean isEntity()
   {
      return false;
   }

   /**
    * Get the transactionType.
    * 
    * @return the transactionType.
    */
   public TransactionType getTransactionType()
   {
      return TransactionType.Container;
   }

   /**
    * Is this container managed transactions
    * 
    * @return true when CMT
    */
   public boolean isCMT()
   {
      TransactionType type = getTransactionType();
      if (type == null)
         return true;
      else
         return type == TransactionType.Container;
   }

   /**
    * Is this bean managed transactions
    * 
    * @return true when BMT
    */
   public boolean isBMT()
   {
      return isCMT() == false;
   }
   
   /**
    * Get the mappedName.
    * 
    * @return the mappedName.
    */
   public String getMappedName()
   {
      return mappedName;
   }

   /**
    * Set the mappedName.
    * 
    * @param mappedName the mappedName.
    * @throws IllegalArgumentException for a null mappedName
    */
   @XmlElement(required=false)
   public void setMappedName(String mappedName)
   {
      if (mappedName == null)
         throw new IllegalArgumentException("Null mappedName");
      this.mappedName = mappedName;
   }

   /**
    * Get the ejbClass.
    * 
    * @return the ejbClass.
    */
   public String getEjbClass()
   {
      return ejbClass;
   }

   /**
    * Set the ejbClass.
    * 
    * @param ejbClass the ejbClass.
    * @throws IllegalArgumentException for a null ejbClass
    */
   public void setEjbClass(String ejbClass)
   {
      if (ejbClass == null)
         throw new IllegalArgumentException("Null ejbClass");
      this.ejbClass = ejbClass;
   }

   /**
    * Get the jndiEnvironmentRefsGroup.
    * 
    * @return the jndiEnvironmentRefsGroup.
    */
   public EnvironmentRefsGroupMetaData getJndiEnvironmentRefsGroup()
   {
      return jndiEnvironmentRefsGroup;
   }

   /**
    * Set the jndiEnvironmentRefsGroup.
    * 
    * @param jndiEnvironmentRefsGroup the jndiEnvironmentRefsGroup.
    * @throws IllegalArgumentException for a null jndiEnvironmentRefsGroup
    */
   public void setJndiEnvironmentRefsGroup(EnvironmentRefsGroupMetaData jndiEnvironmentRefsGroup)
   {
      if (jndiEnvironmentRefsGroup == null)
         throw new IllegalArgumentException("Null jndiEnvironmentRefsGroup");
      this.jndiEnvironmentRefsGroup = jndiEnvironmentRefsGroup;
   }

   /**
    * Get the securityIdentity.
    * 
    * @return the securityIdentity.
    */
   public SecurityIdentityMetaData getSecurityIdentity()
   {
      return securityIdentity;
   }

   /**
    * Set the securityIdentity.
    * 
    * @param securityIdentity the securityIdentity.
    * @throws IllegalArgumentException for a null securityIdentity
    */
   public void setSecurityIdentity(SecurityIdentityMetaData securityIdentity)
   {
      if (securityIdentity == null)
         throw new IllegalArgumentException("Null securityIdentity");
      this.securityIdentity = securityIdentity;
   }

   public EJBLocalReferenceMetaData getEjbLocalReferenceByName(String name)
   {
      return AbstractMappedMetaData.getByName(name, getEjbLocalReferences());
   }

   public EJBLocalReferencesMetaData getEjbLocalReferences()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getEjbLocalReferences();
      return null;
   }

   public EJBReferenceMetaData getEjbReferenceByName(String name)
   {
      return AbstractMappedMetaData.getByName(name, getEjbReferences());
   }

   public EJBReferencesMetaData getEjbReferences()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getEjbReferences();
      return null;
   }

   public EnvironmentEntriesMetaData getEnvironmentEntries()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getEnvironmentEntries();
      return null;
   }

   public EnvironmentEntryMetaData getEnvironmentEntryByName(String name)
   {
      return AbstractMappedMetaData.getByName(name, getEnvironmentEntries());
   }

   public MessageDestinationReferenceMetaData getMessageDestinationReferenceByName(String name)
   {
      return AbstractMappedMetaData.getByName(name, getMessageDestinationReferences());
   }

   public MessageDestinationReferencesMetaData getMessageDestinationReferences()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getMessageDestinationReferences();
      return null;
   }

   public PersistenceContextReferenceMetaData getPersistenceContextReferenceByName(String name)
   {
      return AbstractMappedMetaData.getByName(name, getPersistenceContextRefs());
   }

   public PersistenceContextReferencesMetaData getPersistenceContextRefs()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getPersistenceContextRefs();
      return null;
   }

   public PersistenceUnitReferenceMetaData getPersistenceUnitReferenceByName(String name)
   {
      return AbstractMappedMetaData.getByName(name, getPersistenceUnitRefs());
   }

   public PersistenceUnitReferencesMetaData getPersistenceUnitRefs()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getPersistenceUnitRefs();
      return null;
   }

   public LifecycleCallbacksMetaData getPostConstructs()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getPostConstructs();
      return null;
   }

   public LifecycleCallbacksMetaData getPreDestroys()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getPreDestroys();
      return null;
   }

   public ResourceEnvironmentReferenceMetaData getResourceEnvironmentReferenceByName(String name)
   {
      return AbstractMappedMetaData.getByName(name, getResourceEnvironmentReferences());
   }

   public ResourceEnvironmentReferencesMetaData getResourceEnvironmentReferences()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getResourceEnvironmentReferences();
      return null;
   }

   public ResourceReferenceMetaData getResourceReferenceByName(String name)
   {
      return AbstractMappedMetaData.getByName(name, getResourceReferences());
   }

   public ResourceReferencesMetaData getResourceReferences()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getResourceReferences();
      return null;
   }

   public ServiceReferenceMetaData getServiceReferenceByName(String name)
   {
      return AbstractMappedMetaData.getByName(name, getServiceReferences());
   }

   public ServiceReferencesMetaData getServiceReferences()
   {
      if (jndiEnvironmentRefsGroup != null)
         return jndiEnvironmentRefsGroup.getServiceReferences();
      return null;
   }

   /**
    * Get the methods permissions
    * 
    * @return the method permissions or null for no result
    */
   public MethodPermissionsMetaData getMethodPermissions()
   {
      AssemblyDescriptorMetaData assemblyDescriptor = getAssemblyDescriptor();
      if (assemblyDescriptor == null)
         return null;
      return assemblyDescriptor.getMethodPermissionsByEjbName(getEjbName()); 
   }

   /**
    * Get the container transactions
    * 
    * @return the container transactions or null for no result
    */
   public ContainerTransactionsMetaData getContainerTransactions()
   {
      if (cachedContainerTransactions != null)
         return cachedContainerTransactions;
      AssemblyDescriptorMetaData assemblyDescriptor = getAssemblyDescriptor();
      if (assemblyDescriptor == null)
         return null;
      return assemblyDescriptor.getContainerTransactionsByEjbName(getEjbName()); 
   }

   /**
    * Get the method transaction type
    * 
    * @param methodName the method name
    * @param params the parameters
    * @param iface the interface type
    * @return the method transaction type
    */
   public TransAttributeType getMethodTransactionType(String methodName, Class[] params, MethodInterfaceType iface)
   {
      // default value
      TransAttributeType result = null;

      ContainerTransactionsMetaData containerTransactions = getContainerTransactions();
      if (containerTransactions == null || containerTransactions.isEmpty())
         return result;

      ContainerTransactionMetaData bestMatchTransaction = null;
      MethodMetaData bestMatch = null;
      for (ContainerTransactionMetaData transaction : containerTransactions)
      {
         MethodMetaData match = transaction.bestMatch(methodName, params, iface, bestMatch);
         if (match != bestMatch)
         {
            bestMatchTransaction = transaction;
            bestMatch = match;
         }
      }

      if (bestMatchTransaction != null)
         result = bestMatchTransaction.getTransAttribute();

      return result;
   }

   /**
    * Get the transaction type
    * 
    * @param m the method
    * @param iface the interface type
    * @return the transaction type
    */
   public TransAttributeType getMethodTransactionType(Method m, MethodInterfaceType iface)
   {
      if (m == null)
         return TransAttributeType.Supports;

      TransAttributeType result = null;
      if (methodTx != null)
      {
         result = methodTx.get(m);
         if (result != null) 
            return result;
      }

      result = getMethodTransactionType(m.getName(), m.getParameterTypes(), iface);

      // provide default if method is not found in descriptor
      if (result == null)
         result = TransAttributeType.Required;

      if (methodTx == null)
         methodTx = new ConcurrentHashMap<Method, TransAttributeType>();
      methodTx.put(m, result);
      return result;
   }

   /**
    * Get the interceptor binding
    * 
    * @return the interceptor binding or null for no result
    */
   public InterceptorBindingMetaData getInterceptorBinding()
   {
      AssemblyDescriptorMetaData assemblyDescriptor = getAssemblyDescriptor();
      if (assemblyDescriptor == null)
         return null;
      return assemblyDescriptor.getInterceptorBindingByEjbName(getEjbName()); 
   }

   /**
    * Get the exclude list
    * 
    * @return the exclude list or null for no result
    */
   public ExcludeListMetaData getExcludeList()
   {
      AssemblyDescriptorMetaData assemblyDescriptor = getAssemblyDescriptor();
      if (assemblyDescriptor == null)
         return null;
      return assemblyDescriptor.getExcludeListByEjbName(getEjbName()); 
   }
}
