/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import IceInternal.Timer;

import endrov.util.EvParallel.FuncAB;
import endrov.util.ProgressHandle.Stoppable;


/**
 * Lazy evaluation and memoization. Haskell semantics for java.
 * 
 * My special brew with many additions
 * 
 * @author Johan Henriksson
 */
public abstract class MemoizeX<E> implements Stoppable
	{
	private boolean evaluated=false;
	private E value;
	
	private WeakHashMap<MemoizeX<?>, Object> hasToRunFirst=new WeakHashMap<MemoizeX<?>, Object>();
	private WeakHashMap<MemoizeX<?>, Object> canRunNext=new WeakHashMap<MemoizeX<?>, Object>();

	protected Set<Object> locksValue=new HashSet<Object>();

	private CalcThread calcThread=null;
	private final Set<Object> locksWantResult=new HashSet<Object>();

	
	private Long lastEvaluationTime=null;
	public Long getLastEvalTime()
		{
		return lastEvaluationTime;
		}
	
	
	
	/**
	 * Evaluate value permanently. After this the value cannot be forgotten
	 */
	public synchronized void evaluatePermanently(ProgressHandle c)
		{
		get(c);
		locksValue.add(new Object()); //This is a permanent lock as it is never removed
		hasToRunFirst.clear();
		}
	
	/**
	 * Try and remove the calculated value from memory
	 * 
	 * @return If the value could be forgotten
	 */
	public synchronized boolean forget()
		{
		if(locksValue.isEmpty() && locksWantResult.isEmpty())
			{
			if(evaluated)
				System.out.println("Forgetting "+this);
			
			value=null;
			evaluated=false;
				
			return true;
			}
		else
			return false;
		}

	/**
	 * Evaluate value
	 */
	protected abstract E eval(ProgressHandle c);
	
	public boolean isEvaluated()
		{
		return evaluated;
		}
	
	private void checkCircDep()
		{
		checkCircDep(new HashSet<MemoizeX<?>>());
		}
	
	private synchronized void checkCircDep(Set<MemoizeX<?>> beenTo)
		{
		if(beenTo.contains(this))
			throw new RuntimeException("Circular dependency detected");
		beenTo.add(this);
		for(MemoizeX<?> m:hasToRunFirst.keySet())
			m.checkCircDep(beenTo);
		}
	
	
	/**
	 * Get the value of this object. Calculates the optimal way of executing previous objects and executes
	 */
	public E get(ProgressHandle c)
		{
		Object thisLock=new Object();
		
		System.out.println("getFinal");
		
		//Important: during this evaluation 
		
		//Figure out the dependency graph
		//Best evaluation order keeps as little in memory as possible
		//TODO permanent evaluations can screw up here. more synch!

		//First find the starting points of the evaluation
		Set<MemoizeX<?>> roots=new HashSet<MemoizeX<?>>();
		Set<MemoizeX<?>> needEval=new HashSet<MemoizeX<?>>();
		findRoots(roots, needEval, thisLock);
		//System.out.println("roots "+roots);
		//System.out.println("need eval "+needEval);
		
		//Find the best evaluation order by following a working set
		ArrayList<MemoizeX<?>> evalOrder=new ArrayList<MemoizeX<?>>();
		Set<MemoizeX<?>> front=new HashSet<MemoizeX<?>>(roots);
		Set<MemoizeX<?>> virtualEvaluated=new HashSet<MemoizeX<?>>();
		Set<MemoizeX<?>> needNotEval=new HashSet<MemoizeX<?>>();
		
		synchronized(this)
			{
			while(!front.isEmpty())
				{
				//For each unit, see which one is best to evaluate
				Integer bestCountRelease=null;
				MemoizeX<?> bestRelease=null;
				
				//Check which unit to evaluate releases
				for(MemoizeX<?> m:front)
					{					
					//If this unit cannot be evaluated then ignore it for now
					//TODO would be better to keep a separate list for non-runnable units. would scale much better! 
					if(!virtualEvaluated.containsAll(m.hasToRunFirst.keySet()) && !m.isEvaluated())
						continue;
					
					//Count how many units can be released
					int countRelease=0;
					for(MemoizeX<?> before:m.hasToRunFirst.keySet())
						{
						//Can any units be released after evaluation?
						//They can if all units depending on them will be done
						boolean canRelease=true;
						for(MemoizeX<?> afterBefore:before.canRunNext.keySet())
							if(afterBefore!=m && !virtualEvaluated.contains(afterBefore))
								canRelease=false;
						if(canRelease)
							countRelease++;
						}
					
					//Check if this is the best one to release so far
					if(bestCountRelease==null || countRelease>bestCountRelease)
						{
						bestRelease=m;
						bestCountRelease=countRelease;
						}
					
					}
				
				
				//Simulate evaluating this unit
				virtualEvaluated.add(bestRelease);
				evalOrder.add(bestRelease);
				front.remove(bestRelease);
				if(front!=this)
					{
					for(MemoizeX<?> m:bestRelease.canRunNext.keySet())
						{
						if(needEval.contains(m))
							front.add(m);
						else
							{
							//If this unit does not need evaluation at all then consider it done. Add it to virtual done set so the dependency is unlocked
							virtualEvaluated.add(m);
							needNotEval.add(m);
							}
						}
						
					//front.addAll(bestRelease.canRunNext.keySet());
					
					}
				}
			}

		/*
		System.out.println("need not eval: "+needNotEval);
		System.out.println("eval order: "+evalOrder);
		for(MemoizeX<?> m:evalOrder)
			System.out.println(""+m+" depends on "+m.hasToRunFirst.keySet());
		System.out.println("-------");
		*/
		
		//Evaluate
		Set<MemoizeX<?>> evaluated=new HashSet<MemoizeX<?>>(needNotEval);
		
		for(int curEval=0;curEval<evalOrder.size();curEval++)
			{
			if(c!=null)
				{
				c.setProcNum(curEval, evalOrder.size());
				c.setFraction(0);
				}
			
			//Evaluate this object. Because it is locked there is no need to worry about it loosing the value
			MemoizeX<?> m=evalOrder.get(curEval);
			m.getSingle(c);
			evaluated.add(m);
			
			//Check if execution should be canceled
			if(ProgressHandle.shouldStop(c))
				{
				//If so, unlock all nodes
				for(MemoizeX<?> unlockM:evalOrder)
					{
					unlockM.unlock(thisLock);
					unlockM.forget();
					}
				throw new ProgressHandle.CancelException();
				}
			
			//See if any previous node can be unloaded
			for(MemoizeX<?> before:m.hasToRunFirst.keySet())
				if(evaluated.containsAll(before.canRunNext.keySet()))
					{
					//Unlock the value of the previous node and try to delete the value
					before.unlock(thisLock);
					
					//System.out.println("Deleting "+before);
					before.forget();
					}
			
			}

		//Get this value, unlock the node, and return it
		E thisVal=getSingle(c);
		unlock(thisLock);
		forget();
		
		//System.out.println("----- returning from eval "+thisVal);
		
		return thisVal;
		}
	
	
	public void dependsOn(MemoizeX<?> m)
		{
		m.canRunNext.put(this,null);
		hasToRunFirst.put(m,null);
		checkCircDep();
		}
	

	
	/**
	 * For calculating the value, find the first nodes that must be evaluated. Lock the values of these nodes with given lock.
	 * Also check which nodes need evaluation
	 */
	private synchronized void findRoots(Set<MemoizeX<?>> roots, Set<MemoizeX<?>> needEval, Object lock)
		{
		needEval.add(this);
		if(isEvaluated() || hasToRunFirst.isEmpty())
			{
			roots.add(this);
			locksValue.add(lock);
			}
		else
			for(MemoizeX<?> m:hasToRunFirst.keySet())
				m.findRoots(roots, needEval, lock);
		}
	

	private void unlock(Object lock)
		{
		locksValue.remove(lock);
		}
		
	
	
	
	private class CalcThread extends Thread
		{
		private ProgressHandle pm=new ProgressHandle();
		//private boolean running=false;
		
		@Override
		public void run()
			{
		//	running=true;
			E result=null;
			
			try
				{
				long startTime=System.currentTimeMillis();
				result=eval(pm);
				lastEvaluationTime=System.currentTimeMillis()-startTime;
				
				
				//System.out.println("-----result "+result);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			
			synchronized (MemoizeX.this)
				{
				value=result;
				evaluated=true;
				calcThread=null;
				MemoizeX.this.notifyAll();
				}
			
			//running=false;
			}
		
		}

	
	/**
	 * Notify that execution *might* be canceled
	 */
	public void signalStop()
		{
		synchronized (this)
			{
			this.notifyAll();
			}
		}
	
	
	/**
	 * Evaluate value and return it. Ensures that it is only evaluated once, even if multiple threads call it at the same time
	 */
	private synchronized E getSingle(ProgressHandle progh)
		{
		//System.out.println("calcth "+calcThread);
		
		//Add a lock telling that the result is wanted
		Object wantValueLock=new Object();
		locksWantResult.add(wantValueLock);
	
		//Until a value is obtained or execution is aborted
		for(;;)
			{
			//Check if execution should stop
			if(ProgressHandle.shouldStop(progh))
				{
				locksWantResult.remove(wantValueLock);
				if(locksWantResult.isEmpty())
					{
					if(calcThread!=null)
						calcThread.pm.cancel();
					}
				}
			
			if(evaluated)
				{
				//If there is a value then return it. Be careful that the value does not disappear
				E localValue=value;
				locksWantResult.remove(wantValueLock);
				return localValue;
				}
			else
				{
				//If there is no value then start calculating it (or just wait if calculation is pending)
				//if(!calcThread.isAlive())
				//if(!calcThread.running)
				if(calcThread==null)
					{
					calcThread=new CalcThread();
					//calcThread.pm=new ProgressHandle(); //Make sure it is not canceled right away
					//calcThread.running=true;
					calcThread.start();
					}
				}

			//Wait until something happens. Either calculation finishes or execution is aborted
			try
				{
				this.wait();
				}
			catch (InterruptedException e)
				{
				e.printStackTrace();
				}
			}
		
		}
	
	
	

	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	private static class MemoizeXtest extends MemoizeX<String>
		{
		private String s;
		
		public MemoizeXtest(String s)
			{
			this.s=s;
			}
		
		protected String eval(endrov.util.ProgressHandle c)
			{
			System.out.println(s);
			try
				{
				Timer.sleep(1000);
				}
			catch (InterruptedException e)
				{
				e.printStackTrace();
				}
			
			return s;
			}
		@Override
		public String toString()
			{
			return s;
			}
		}	
	

	public static void main(String[] args)
		{
		MemoizeXtest a=new MemoizeXtest("a");
		MemoizeXtest b=new MemoizeXtest("b");
		MemoizeXtest c=new MemoizeXtest("c");
		MemoizeXtest d=new MemoizeXtest("d");
		final MemoizeXtest e=new MemoizeXtest("e");
		MemoizeXtest f=new MemoizeXtest("f");
			
		b.dependsOn(a);
		b.dependsOn(f);
		c.dependsOn(b);
		d.dependsOn(a);
		e.dependsOn(c);
		e.dependsOn(d);   //TODO how to do this dependency?????????
		
		
		
		
		
		EvParallel.map(50,Arrays.asList(1,2,3,4,324,243,213,436,658,976,856,65,5,214,243,634,7,96,987,75,64,325,234,4213,421), new FuncAB<Integer,Object>()
					{
					public Object func(Integer in)
						{
						e.get(null);
						return null;
						}
					
					});
		
		/*
		final ProgressHandle pm=new ProgressHandle();
		new Thread(){@Override
		public void run()
			{
			e.getFinal(pm);
			}
		}.start();
		try
			{
			Timer.sleep(5000);
			}
		catch (InterruptedException e1)
			{
			}
		
		pm.cancel();
		*/
		}
	
	
	
	
	}
