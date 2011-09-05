/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * Lazy evaluation and memoization. Haskell semantics for java.
 * 
 * My special brew with many additions
 * 
 * @author Johan Henriksson
 */
public abstract class MemoizeX<E>
	{
	private boolean evaluated=false;
	private E value;
	
	private WeakHashMap<MemoizeX<?>, Object> hasToRunFirst=new WeakHashMap<MemoizeX<?>, Object>();
	private WeakHashMap<MemoizeX<?>, Object> canRunNext=new WeakHashMap<MemoizeX<?>, Object>();
	
	private boolean keepValue=false;
	
	private Long lastEvaluationTime=null;
	
	public Long getLastEvalTime()
		{
		return lastEvaluationTime;
		}
	
	
	public static interface ProgressListener
		{
		/**
		 * Progress changed, from 0-1
		 */
		public void eventProgress(ProgressHandle h, double d);
		}
	
	/**
	 * A handle to pass into evaluator, controlling progress
	 *
	 */
	public static class ProgressHandle
		{
		private boolean cancel=false;
		
		private int currentProc;
		private int totalProc;
		
		private List<ProgressListener> listeners=new LinkedList<ProgressListener>(); 
		

		/**
		 * Specify which process in the queue is running
		 */
		public void setProcNum(int current, int total)
			{
			currentProc=current;
			totalProc=total;
			setFraction(0);
			}
		
		/**
		 * Set progress for this process
		 */
		public void setFraction(double d)
			{
			double p=(currentProc+d)/totalProc;
			for(ProgressListener l:listeners)
				l.eventProgress(this, p);
			}
		
		public void addListener(ProgressListener l)
			{
			listeners.add(l);
			}
		
		public void removeListener(ProgressListener l)
			{
			listeners.remove(l);
			}
		
		/**
		 * Cancel evaluation
		 */
		public void cancel()
			{
			cancel=true;
			}
		}
	
	/**
	 * Exception to be raised in evaluator if user cancels
	 *
	 */
	public static class CancelException extends RuntimeException
		{
		private static final long serialVersionUID = 1L;
		public CancelException()
			{
			super("Evalutation cancelled");
			}
		}
	
	public synchronized void evaluatePermanently(ProgressHandle c)
		{
		get(c);
		keepValue=true;
		//TODO
		
		
		hasToRunFirst.clear();
		}
	
	public synchronized void forget()
		{
		if(!keepValue)
			{
			value=null;
			evaluated=false;
			}
		}
	
	/**
	 * Get value, evaluate if required. Evaluation occurs at most once
	 */
	private synchronized E get(ProgressHandle c) //Can make a cheaper lock
		{
		if(!evaluated)
			{
			long startTime=System.currentTimeMillis();
			value=eval(c);
			lastEvaluationTime=System.currentTimeMillis()-startTime;
			evaluated=true;
			}
		return value;
		}

	/**
	 * Evaluate value
	 */
	protected abstract E eval(ProgressHandle c);
	
	public boolean isEvaluated()
		{
		return evaluated;
		}
	
	/**
	 * 
	 */
	public synchronized void superGet(ProgressHandle c)
		{
		//Important: during this evaluation 
		
		//Figure out the dependency graph
		//Best evaluation order keeps as little in memory as possible
		
//		Map<MemoizeX<?>, Integer> lockCount=null;
	
		//First find the starting point of the evaluation
		Set<MemoizeX<?>> roots=new HashSet<MemoizeX<?>>();
		findRoots(roots);
		
		//TODO circular dependencies. this code is not very thread-safe
		
		//Find the best evaluation order by following a working set
		List<MemoizeX<?>> evalOrder=new ArrayList<MemoizeX<?>>();
		//TODO figure out if unit can forget
		Set<MemoizeX<?>> front=new HashSet<MemoizeX<?>>(roots);
		Set<MemoizeX<?>> virtualEvaluated=new HashSet<MemoizeX<?>>();
		
		System.out.println("roots "+roots);
		
		while(!front.isEmpty())
			{
			//For each unit, see which one is best to evaluate
			Integer bestCountRelease=null;
			MemoizeX<?> bestRelease=null;
			
			//Check which unit to evaluate releases
			for(MemoizeX<?> m:front)
				{
				if(!virtualEvaluated.containsAll(m.hasToRunFirst.keySet()))
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
				front.addAll(bestRelease.canRunNext.keySet());
			}

		System.out.println(evalOrder);
	
		//Evaluate
		Set<MemoizeX<?>> evaluated=new HashSet<MemoizeX<?>>();
		for(MemoizeX<?> m:evalOrder)
			{
			
			m.get(c); //TODO
			
			evaluated.add(m);

			for(MemoizeX<?> before:m.hasToRunFirst.keySet())
				if(evaluated.containsAll(before.canRunNext.keySet()))
					{
					System.out.println("Deleting "+before);
					before.forget();
					
					
					
					}
			
			}
		//TODO Forgetting should be more like unlocking the images from memory
		
		
		}
	
	
	public void dependsOn(MemoizeX<?> m)
		{
		m.canRunNext.put(this,null);
		hasToRunFirst.put(m,null);
		}
	
	private static class MemoizeXtest extends MemoizeX<String>
		{
		private String s;
		
		public MemoizeXtest(String s)
			{
			this.s=s;
			}
		
		protected String eval(endrov.util.MemoizeX.ProgressHandle c)
			{
			System.out.println(s);
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
		MemoizeXtest e=new MemoizeXtest("e");
		MemoizeXtest f=new MemoizeXtest("f");
			
		b.dependsOn(a);
		b.dependsOn(f);
		c.dependsOn(b);
		d.dependsOn(a);
		e.dependsOn(c);
		e.dependsOn(d);
		
		e.superGet(null);
		}
	
	private void findRoots(Set<MemoizeX<?>> roots)
		{
		if(isEvaluated() || hasToRunFirst.isEmpty())
			roots.add(this);
		else
			for(MemoizeX<?> m:hasToRunFirst.keySet())
				m.findRoots(roots);
		}
	
	
	}
