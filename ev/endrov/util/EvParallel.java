/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

import java.util.*;
import java.util.concurrent.Semaphore;



/**
 * Functions for simple parallel processing
 * @author Johan Henriksson
 */
public class EvParallel
	{
	/** Number of threads to use */
	public static int numThread=Runtime.getRuntime().availableProcessors();
	
	/**
	 * Function A -> B
	 */
	public static interface FuncAB<A,B>
		{
		public B func(A in);
		}
	

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
		}

	
	/**
	 * Map_ :: [A] -> (A->_) -> []
	 */
	public static <A> void map_(Collection<A> in, final FuncAB<A,Object> func)
		{
		map(numThread, in, func);
		}
	
		
	/**
	 * Map :: SortedMap A,B -> (B->C) -> SortedMap A,C
	 */
	public static <A,B,C> SortedMap<A, C> mapValues(SortedMap<A, B> map, final EvParallel.FuncAB<B,C> func)
		{
		return EvListUtil.tuples2map(EvParallel.map(EvListUtil.map2tuples(map), new EvParallel.FuncAB<Tuple<A,B>,Tuple<A,C>>(){
			public Tuple<A,C> func(Tuple<A,B> in)	{return new Tuple<A, C>(in.fst(),func.func(in.snd()));}
		}));
		}
	
	}
