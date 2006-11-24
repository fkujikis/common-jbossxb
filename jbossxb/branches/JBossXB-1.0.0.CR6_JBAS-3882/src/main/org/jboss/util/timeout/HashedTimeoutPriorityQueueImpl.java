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
package org.jboss.util.timeout;

import org.jboss.util.JBossStringBuilder;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * HashedTimeoutPriorityQueueImpl.
 *
 *  This is a balanced binary tree. If nonempty, the root is at index 1,
 *  and all nodes are at indices 1..size. Nodes with index greater than
 *  size are null. Index 0 is never used.
 *  Children of the node at index <code>j</code> are at <code>j*2</code>
 *  and <code>j*2+1</code>. The children of a node always fire the timeout
 *  no earlier than the node.
 *
 *
 *  Or, more formally:
 *
 *  Only indices <code>1</code>..<code>size</code> of this array are used.
 *  All other indices contain the null reference.
 *  This array represent a balanced binary tree.
 *
 *  If <code>size</code> is <code>0</code> the tree is empty, otherwise
 *  the root of the tree is at index <code>1</code>.
 *
 *  Given an arbitrary node at index <code>n</code> that is not the root
 *  node, the parent node of <code>n</code> is at index <code>n/2</code>.
 *
 *  Given an arbitrary node at index <code>n</code>; if
 *  <code>2*n <= size</code> the node at <code>n</code> has its left child
 *  at index <code>2*n</code>, otherwise the node at <code>n</code> has
 *  no left child.
 *
 *  Given an arbitrary node at index <code>n</code>; if
 *  <code>2*n+1 <= size</code> the node at <code>n</code> has its right child
 *  at index <code>2*n+1</code>, otherwise the node at <code>n</code> has
 *  no right child.
 *
 *  The priority function is called T. Given a node <code>n</code>,
 *  <code>T(n)</code> denotes the absolute time (in milliseconds since
 *  the epoch) that the timeout for node <code>n</code> should happen.
 *  Smaller values of <code>T</code> means higher priority.
 *
 *  The tree satisfies the following invariant:
 *  <i>
 *  For any node <code>n</code> in the tree:
 *  If node <code>n</code> has a left child <code>l</code>,
 *  <code>T(n) <= T(l)</code>.
 *  If node <code>n</code> has a right child <code>r</code>,
 *  <code>T(n) <= T(r)</code>.
 *  </i>
 *
 *
 *  The invariant may be temporarily broken while executing synchronized
 *  on <code>this</code> instance, but is always reestablished before
 *  leaving the synchronized code.
 *
 *  The node at index <code>1</code> is always the first node to timeout,
 *  as can be deduced from the invariant.
 *
 *  For the following algorithm pseudocode, the operation
 *  <code>swap(n,m)</code> denotes the exchange of the nodes at indices
 *  <code>n</code> and <code>m</code> in the tree.
 *
 *  Insertion of a new node happend as follows:
 *  <pre>
 *    IF size = q.length THEN
 *      "expand q array to be larger";
 *    ENDIF
 *    size <- size + 1;
 *    q[size] <- "new node";
 *    n <- size;
 *    WHILE n > 1 AND T(n/2) > T(n) DO
 *      swap(n/2, n);
 *      n <- n/2;
 *    ENDWHILE
 *  </pre>
 *  Proof that this insertion algorithm respects the invariant is left to
 *  the interested reader.
 *
 *  The removal algorithm is a bit more complicated. To remove the node
 *  at index <code>n</code>:
 *  <pre>
 *    swap(n, size);
 *    size <- size - 1;
 *    IF n > 1 AND T(n/2) > T(n) THEN
 *      WHILE n > 1 AND T(n/2) > T(n) DO
 *        swap(n/2, n);
 *        n <- n/2;
 *      ENDWHILE
 *    ELSE
 *      WHILE 2*n <= size DO
 *        IF 2*n+1 <= size THEN
 *          // Both children present
 *          IF T(2*n) <= T(2*n+1) THEN
 *            IF T(n) <= T(2*n) THEN
 *              EXIT;
 *            ENDIF
 *            swap(n, 2*n);
 *            n <- 2*n;
 *          ELSE
 *            IF T(n) <= T(2*n+1) THEN
 *              EXIT;
 *            ENDIF
 *            swap(n, 2*n+1);
 *            n <- 2*n+1;
 *          ENDIF
 *        ELSE
 *          // Only left child, right child not present.
 *          IF T(n) <= T(2*n) THEN
 *            EXIT;
 *          ENDIF
 *          swap(n, 2*n);
 *          n <- 2*n;
 *        ENDIF
 *      ENDWHILE
 *    ENDIF
 *  </pre>
 *  Proof that this removal algorithm respects the invariant is left to
 *  the interested reader. Really, I am not going to prove it here.
 *
 *  If you are interested, you can find this data structure and its
 *  associated operations in most textbooks on algorithmics.
 *
 *  @see checkTree
 * 
 * @author <a href="osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="genman@maison-otaku.net">Elias Ross</a>  
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision$
 */
public class HashedTimeoutPriorityQueueImpl implements TimeoutPriorityQueue
{
   //  Code commented out with the mark "INV:" are runtime checks
   //  of invariants that are not needed for a production system.
   //  For problem solving, you can remove these comments.
   //  Multithreading notes:
   //
   //  While a TimeoutImpl is enqueued, its index field contains the index
   //  of the instance in the queue; that is, for 1 <= n <= size,
   //  q[n].index = n.
   //  Modifications of an enqueued TimeoutImpl instance may only happen
   //  in code synchronized on the TimeoutFactory instance that has it
   //  enqueued.
   //  Modifications on the priority queue may only happen while running in
   //  code synchronized on the TimeoutFactory instance that holds the queue.
   //  When a TimeoutImpl instance is no longer enqueued, its index field
   //  changes to one of the negative constants declared in the TimeoutImpl
   //  class.
   //
   //  Cancellation may race with the timeout.
   //  To avoid problems with this, the TimeoutImpl index field is set to
   //  TimeoutImpl.TIMEOUT when the TimeoutImpl is taken out of the queue.
   //  Finally the index field is set to TimeoutImpl.DONE, and
   //  the TimeoutImpl instance is discarded.

   /** The lock object */
   private Object topLock = new Object();

   /** The top element */
   private TimeoutExtImpl top;
   
   /** The hashed queues */
   private InternalPriorityQueue[] queues;

   private SynchronizedBoolean cancelled = new SynchronizedBoolean(false);
   
   /**
    * Create a new TimeoutPriorityQueueImpl.
    */
   public HashedTimeoutPriorityQueueImpl()
   {
      queues = new InternalPriorityQueue[40];
      for (int i = 0; i < queues.length; ++ i)
         queues[i] = new InternalPriorityQueue();
   }

   public TimeoutExt offer(long time, TimeoutTarget target)
   {
      if (cancelled.get())
         throw new IllegalStateException("TimeoutPriorityQueue has been cancelled");
      if (time < 0)
         throw new IllegalArgumentException("Negative time");
      if (target == null)
         throw new IllegalArgumentException("Null timeout target");

      TimeoutExtImpl timeout = new TimeoutExtImpl();
      timeout.time = time;
      timeout.target = target;
      int index = timeout.hashCode() % queues.length;
      return queues[index].offer(timeout);
   }

   public TimeoutExt take()
   {
      return poll(-1);
   }

   public TimeoutExt poll()
   {
      return poll(1);
   }
   
   public TimeoutExt poll(long wait)
   {
      long endWait = -1;
      if (wait > 0)
         endWait = System.currentTimeMillis() + wait;
      // Look for work
      synchronized (topLock)
      {
         while (cancelled.get() == false && (wait >= 0 || endWait == -1))
         {
            if (top == null)
            {
               try
               {
                  if (endWait == -1)
                     topLock.wait();
                  else
                     topLock.wait(wait);
               }
               catch (InterruptedException ex)
               {
               }
            }
            else
            {
               long now = System.currentTimeMillis();
               if (top.time > now)
               {
                  long waitForFirst = top.time - now;
                  if (endWait != -1 && waitForFirst > wait)
                     waitForFirst = wait;
                  try
                  {
                     topLock.wait(waitForFirst);
                  }
                  catch (InterruptedException ex)
                  {
                  }
               }
               if (cancelled.get() == false && top != null && top.time <= System.currentTimeMillis())
               {
                  TimeoutExtImpl result = top;
                  result.queue = null;
                  result.index = TimeoutExtImpl.TIMEOUT;
                  top = null;
                  recalculateTop(false);
                  return result;
               }
            }
            if (endWait != -1)
               wait = endWait - System.currentTimeMillis();
         }
      }
      return null;
   }
   
   public TimeoutExt peek()
   {
      synchronized (topLock)
      {
         return top;
      }
   }

   public boolean remove(TimeoutExt timeout)
   {
      TimeoutExtImpl timeoutImpl = (TimeoutExtImpl) timeout;
      
      // Fast way of doing it
      InternalPriorityQueue queue = timeoutImpl.queue;
      if (queue != null && queue.remove(timeoutImpl))
         return true;

      synchronized (topLock)
      {
         // Check the top element
         if (top == timeout)
         {
            top.done();
            top = null;
            recalculateTop(true);
            return true;
         }
         
         // Double check, it might have been the top then
         // just got moved back into the queue
         queue = timeoutImpl.queue;
         if (queue != null)
            return queue.remove(timeoutImpl);
      }
      return false;
   }

   public void clear()
   {
      synchronized (topLock)
      {
         if (cancelled.get())
            return;
         
         // cleanup queues
         for (int i = 1; i < queues.length; ++i)
            queues[i].clear();

         // cleanup the top
         top = cleanupTimeoutExtImpl(top);
      }
   }

   public void cancel()
   {
      synchronized (topLock)
      {
         if (cancelled.get())
            return;

         clear();
         topLock.notifyAll();
      }
   }

   public int size()
   {
      int size = 0; 
      if (top != null)
         size =1;
      for (int i = 0; i < queues.length; ++i)
         size += queues[i].size();
      return size;
   }
   
   /**
    * Whether the queue is cancelled
    * 
    * @return true when cancelled
    */
   public boolean isCancelled()
   {
      return cancelled.get();
   }

   private void recalculateTop(boolean notify)
   {
      for (int i = 0; i < queues.length; ++i)
         queues[i].compareAndSwapWithTop(notify);
   }

   /**
    * Recursive cleanup of a TimeoutImpl
    * 
    * @return null
    */
   private TimeoutExtImpl cleanupTimeoutExtImpl(TimeoutExtImpl timeout)
   {
      if (timeout != null)
         timeout.target = null;
      return null;
   }

   /**
    * Debugging helper.
    */
   private void assertExpr(boolean expr)
   {
      if (!expr)
         throw new IllegalStateException("***** assert failed *****");
   }

   /**
    * Internal priority queue
    */
   private class InternalPriorityQueue
   {
      /** The lock object */
      private Object lock = new Object();

      /** The size of the timeout queue. */
      private int size;

      /** The timeouts */
      private TimeoutExtImpl[] queue;

      /**
       * Create a new InternalPriorityQueue.
       */
      InternalPriorityQueue()
      {
         queue = new TimeoutExtImpl[16];
         size = 0;
      }

      TimeoutExt offer(TimeoutExtImpl timeout)
      {
         boolean checkTop = false;
         synchronized (lock)
         {
            // INV: checkTree();
            // INV: assertExpr(size < queue.length);
            if (++size == queue.length)
            {
               TimeoutExtImpl[] newQ = new TimeoutExtImpl[2 * queue.length];
               System.arraycopy(queue, 0, newQ, 0, queue.length);
               queue = newQ;
            }
            // INV: assertExpr(size < queue.length);
            // INV: assertExpr(queue[size] == null);
            queue[size] = timeout;
            timeout.queue = this;
            timeout.index = size;
            normalizeUp(size);
            if (timeout.index == 1)
               checkTop = true;
            // INV: checkTree();
         }
         if (checkTop)
         {
            synchronized (topLock)
            {
               compareAndSwapWithTop(true);
            }
         }
         return timeout;
      }

      boolean compareAndSwapWithTop(boolean notify)
      {
         synchronized (lock)
         {
            if (size == 0)
               return false;

            if (top == null)
            {
               top = removeNode(1);
               top.queue = null;
               top.index = TimeoutExtImpl.TOP;
               if (notify)
                  topLock.notify();
               return top != null;
            }
            
            if (top.time > queue[1].time)
            {
               TimeoutExtImpl temp = top;
               top = queue[1];
               top.queue = null;
               top.index = TimeoutExtImpl.TOP;
               queue[1] = temp;
               temp.queue = this;
               temp.index = 1;
               if (size > 1)
                  normalizeDown(1);
               if (notify)
                  topLock.notify();
            }
         }
         return false;
      }
      
      boolean remove(TimeoutExt timeout)
      {
         synchronized (lock)
         {
            TimeoutExtImpl timeoutImpl = (TimeoutExtImpl) timeout;
            if (timeoutImpl.queue == this && timeoutImpl.index > 0)
            {
               // Active timeout, remove it.
               // INV: assertExpr(queue[timeoutImpl.index] == timeout);
               // INV: checkTree();
               removeNode(timeoutImpl.index);
               // INV: checkTree();
               timeoutImpl.queue = null;
               timeoutImpl.index = TimeoutExtImpl.DONE;

               // execution cancelled
               return true;
            }
            else
            {
               // has already been executed (DONE) or
               // is currently executing (TIMEOUT)
               return false;
            }
         }
      }

      public void clear()
      {
         synchronized (lock)
         {
            if (cancelled.get())
               return;

            // cleanup queue
            for (int i = 1; i <= size; ++i)
               queue[i] = cleanupTimeoutExtImpl(queue[i]);
         }
      }

      public void cancel()
      {
         synchronized (lock)
         {
            if (cancelled.get())
               return;
            clear();
         }
      }

      public int size()
      {
         return size;
      }

      /**
       *  A new node has been added at index <code>index</code>.
       *  Normalize the tree by moving the new node up the tree.
       *
       *  @return true if the tree was modified.
       */
      private boolean normalizeUp(int index)
      {
         // INV: assertExpr(index > 0);
         // INV: assertExpr(index <= size);
         // INV: assertExpr(queue[index] != null);
         if (index == 1)
            return false; // at root
         boolean ret = false;
         long t = queue[index].time;
         int p = index >> 1;
         while (queue[p].time > t)
         {
            // INV: assertExpr(queue[index].time == t);
            swap(p, index);
            ret = true;
            if (p == 1)
               break; // at root
            index = p;
            p >>= 1;
         }
         return ret;
      }
      
      void normalizeDown(int index)
      {
         long t = queue[index].time;
         int c = index << 1;
         while (c <= size)
         {
            // INV: assertExpr(q[index].time == t);
            TimeoutExtImpl l = queue[c];
            // INV: assertExpr(l != null);
            // INV: assertExpr(l.index == c);
            if (c + 1 <= size)
            {
               // two children, swap with smallest
               TimeoutExtImpl r = queue[c + 1];
               // INV: assertExpr(r != null);
               // INV: assertExpr(r.index == c+1);
               if (l.time <= r.time)
               {
                  if (t <= l.time)
                     break; // done
                  swap(index, c);
                  index = c;
               }
               else
               {
                  if (t <= r.time)
                     break; // done
                  swap(index, c + 1);
                  index = c + 1;
               }
            }
            else
            { // one child
               if (t <= l.time)
                  break; // done
               swap(index, c);
               index = c;
            }
            c = index << 1;
         }
      }

      /**
       * Swap two nodes in the tree.
       * 
       * @param a the first index
       * @param b the second index
       */
      private void swap(int a, int b)
      {
         // INV: assertExpr(a > 0);
         // INV: assertExpr(a <= size);
         // INV: assertExpr(b > 0);
         // INV: assertExpr(b <= size);
         // INV: assertExpr(queue[a] != null);
         // INV: assertExpr(queue[b] != null);
         // INV: assertExpr(queue[a].index == a);
         // INV: assertExpr(queue[b].index == b);
         TimeoutExtImpl temp = queue[a];
         queue[a] = queue[b];
         queue[a].index = a;
         queue[b] = temp;
         queue[b].index = b;
      }

      /**
       * Remove a node from the tree and normalize.
       *
       * @param index the index in the queue
       * @return the removed node.
       */
      private TimeoutExtImpl removeNode(int index)
      {
         // INV: assertExpr(index > 0);
         // INV: assertExpr(index <= size);
         TimeoutExtImpl res = queue[index];
         // INV: assertExpr(res != null);
         // INV: assertExpr(res.index == index);
         if (index == size)
         {
            --size;
            queue[index] = null;
            return res;
         }
         swap(index, size); // Exchange removed node with last leaf node
         --size;
         // INV: assertExpr(res.index == size + 1);
         queue[res.index] = null;
         if (normalizeUp(index))
            return res; // Node moved up, so it shouldn't move down
         normalizeDown(index);
         return res;
      }

      /**
       * Check invariants of the queue.
       */
      void checkTree()
      {
         assertExpr(size >= 0);
         assertExpr(size < queue.length);
         assertExpr(queue[0] == null);
         if (size > 0)
         {
            assertExpr(queue[1] != null);
            assertExpr(queue[1].index == 1);
            assertExpr(queue[1].queue == this);
            for (int i = 2; i <= size; ++i)
            {
               assertExpr(queue[i] != null);
               assertExpr(queue[i].index == i);
               assertExpr(queue[i].queue == this);
               assertExpr(queue[i >> 1].time <= queue[i].time); // parent fires first
            }
            for (int i = size + 1; i < queue.length; ++i)
               assertExpr(queue[i] == null);
         }
      }
      
   }
   
   /**
    *  Our private Timeout implementation.
    */
   private class TimeoutExtImpl implements TimeoutExt
   {
      /** Top */
      static final int TOP = 0;

      /** Done */
      static final int DONE = -1;

      /** In timeout */
      static final int TIMEOUT = -2;

      /** The internal priority queue */
      InternalPriorityQueue queue;
      
      /** Index in the queue */
      int index;

      /** Time of the timeout */
      long time;

      /** The timeout target */
      TimeoutTarget target;

      public long getTime()
      {
         return time;
      }

      public TimeoutTarget getTimeoutTarget()
      {
         return target;
      }

      public void done()
      {
         queue = null;
         index = DONE;
      }
      
      public boolean cancel()
      {
         return remove(this);
      }
   }
   
   public String dump()
   {
      JBossStringBuilder buffer = new JBossStringBuilder();
      buffer.append("TOP=");
      if (top == null)
         buffer.append("null");
      else
         buffer.append(top.time);
      buffer.append(" size=").append(size()).append('\n');
      for (int i = 0; i < queues.length; ++i)
      {
         buffer.append(i).append("=");
         for (int j = 1; j <= queues[i].size; ++j)
            buffer.append(queues[i].queue[j].time).append(',');
         buffer.append('\n');
      }
      return buffer.toString();
   }
}
