/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.collection;


import java.util.*;
import java.util.concurrent.Semaphore;

import endrov.util.FuncAB;



/**
 * Functions for simple parallel processing
 * @author Johan Henriksson
 */
public class EvParallel
	{
	/** Number of threads to use */
	public static int numThread=Runtime.getRuntime().availableProcessors();
	
	public static <A,B,C,D> Map<C,D> map(Map<A,B> in, final FuncAB<Tuple<A,B>,Tuple<C, D>> func)
		{
		return EvListUtil.tuples2map(map(numThread,EvListUtil.map2tuples(in), func));
		}

	public static <A,B,C,D> Map<C,D> map(int numThread, Map<A,B> in, final FuncAB<Tuple<A,B>,Tuple<C, D>> func)
		{
		return EvListUtil.tuples2map(map(numThread,EvListUtil.map2tuples(in), func));
		}

	/**
	 * Map :: [A] -> (A->B) -> [B]
	 */
	public static <A,B> List<B> map(List<A> in, final FuncAB<A,B> func)
		{
		return map(numThread, in,func);
		}
	
	
	/**
	 * Map :: [A] -> (A->B) -> [B]
	 */
	/*
	public static <A,B> List<B> map(final int numThread, Collection<A> in, final FuncAB<A,B> func)
		{
		final Semaphore putsem=new Semaphore(numThread);
		final LinkedList<RuntimeException> ex=new LinkedList<RuntimeException>();
		final LinkedList<B> out=new LinkedList<B>();
		try
			{
//			System.out.println("#CPU "+numThread);
			for(final A e:in)
				{
				synchronized (ex)
					{
					if(!ex.isEmpty())
						break;
					}
				putsem.acquire();
				new Thread(new Runnable(){
					public void run()
						{
						B b;
						try
							{
							b = func.func(e);
							synchronized (out){out.add(b);}
							}
						catch (RuntimeException e)
							{
							synchronized (ex)
								{
								ex.add(e);
								}
							}
						putsem.release();
						}
					}).start();
				}
			putsem.acquire(numThread);
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		synchronized (ex)
			{
			//All threads have stopped executing, well-defined state. Did any of them fail?
			if(!ex.isEmpty())
				throw ex.getFirst();
			}
		return out;
		}*/

	//Version that spawns fewer threads
	
	/**
	 * Map :: [A] -> (A->B) -> [B]
	 */
	public static <A,B> List<B> map(int numThread, Collection<A> in, final FuncAB<A,B> func)
		{
		//No need to start more threads than work items
		if(numThread>in.size())
			numThread=in.size();
		
		final ArrayList<B> out = new ArrayList<B>(in.size());
		for(int i=0;i<in.size();i++)
			out.add(null);
		final LinkedList<RuntimeException> ex = new LinkedList<RuntimeException>();
		final StrongReference<Integer> jobcounter = new StrongReference<Integer>(0);
		try
			{
			final Iterator<A> inIterator=in.iterator();
			final Semaphore putsem=new Semaphore(0);
			for(int curThread=0;curThread<numThread;curThread++)
				{
				final int fcurThread=curThread;
				new Thread()
					{
					public void run()
						{
						A a;
						for(;;)
							{
							long startTime=System.currentTimeMillis();
							System.out.println("-------- Starting job in thread #"+fcurThread);
							int jobnum;
							//Get a job
							synchronized (inIterator)
								{
								if(inIterator.hasNext())
									{
									a=inIterator.next();
									jobnum=jobcounter.get();
									jobcounter.set(jobnum+1);
									}
								else
									break;
								}
							//Execute function, handle potential error
							try
								{
								B b=func.func(a);
								synchronized (out)
									{
									out.set(jobnum,b);
									}
								}
							catch (RuntimeException e)
								{
								//Finish off all elements
								synchronized (inIterator)
									{
									while(inIterator.hasNext())
										inIterator.next();
									}
								//Store the error
								synchronized (ex)
									{
									ex.add(e);
									break;
									}
								}
							long endTime=System.currentTimeMillis();
							System.out.println("-------- Finished job in thread #"+fcurThread+"  time "+(endTime-startTime));
							}
						putsem.release();
						}
					}.start();
				}
				
			putsem.acquire(numThread);
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		
		//All threads have stopped executing, well-defined state. Did any of them fail?
		if(!ex.isEmpty())
			throw ex.getFirst();
		return out;
		}

	
	/**
	 * Map_ :: [A] -> (A->_) -> []
	 */
	public static <A> void map_(Collection<A> in, final FuncAB<A,Object> func)
		{
		map(numThread, in, func);
		}
	
	/**
	 * Map_ :: [A] -> (A->_) -> []
	 */
	public static <A> void map_(int numThread, Collection<A> in, final FuncAB<A,Object> func)
		{
		map(numThread, in, func);
		}
	
		
	/**
	 * Map :: SortedMap A,B -> (B->C) -> SortedMap A,C
	 */
	public static <A,B,C> SortedMap<A, C> mapValues(SortedMap<A, B> map, final FuncAB<B,C> func)
		{
		return EvListUtil.tuples2map(EvParallel.map(EvListUtil.map2tuples(map), new FuncAB<Tuple<A,B>,Tuple<A,C>>(){
			public Tuple<A,C> func(Tuple<A,B> in)	{return new Tuple<A, C>(in.fst(),func.func(in.snd()));}
		}));
		}
	
	}
