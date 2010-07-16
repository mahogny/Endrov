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
	public static <A,B> List<B> map(int numThread, List<A> in, final FuncAB<A,B> func)
		{
		final LinkedList<B> out=new LinkedList<B>();
		try
			{
//			System.out.println("#CPU "+numThread);
			final Semaphore putsem=new Semaphore(numThread);
			for(final A e:in)
				{
				putsem.acquire();
				new Thread(new Runnable(){
				public void run()
					{
					B b=func.func(e);
					synchronized (out){out.add(b);}
					putsem.release();
					}
				}).start();
				}
			//System.out.println("----------- waiting for all threads to finish "+putsem.availablePermits());
			putsem.acquire(numThread);
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		return out;
		}

	/**
	 * Map_ :: [A] -> (A->_) -> []
	 */
	public static <A> void map_(Collection<A> in, final FuncAB<A,Object> func)
		{
		map_(numThread, in, func);
		}
	
	/**
	 * Map_ :: [A] -> (A->_) -> []
	 */
	public static <A> void map_(int numThread, Collection<A> in, final FuncAB<A,Object> func)
		{
		try
			{
//			System.out.println("#CPU "+numThread);
			final Semaphore putsem=new Semaphore(numThread);
			for(final A e:in)
				{
				putsem.acquire();
				new Thread(new Runnable(){
				public void run()
					{
					func.func(e);
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
