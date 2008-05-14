package util2.makeStdWormDist4;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.Semaphore;

import evplugin.ev.Tuple;

public class Parallel
	{

	public static int numCpu=Runtime.getRuntime().availableProcessors();
	
	/**
	 * Function A -> B
	 */
	public static interface FuncAB<A,B>
		{
		public B func(A in);
		}
	
	
	/**
	 * Map :: [A] -> (A->B) -> [B]
	 */
	public static <A,B> List<B> map(List<A> in, final FuncAB<A,B> func)
		{
		final LinkedList<B> out=new LinkedList<B>();
		try
			{
			System.out.println("#CPU "+numCpu);
			final Semaphore putsem=new Semaphore(numCpu);
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
			putsem.acquire(numCpu);
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		return out;
		}
	
	
	/**
	 * Map over values in sorted maps
	 */
	public static <A,B> SortedMap<A, B> mapTreeValues(SortedMap<A, B> map, final Parallel.FuncAB<B,B> func)
		{
		return EvListUtil.tuples2map(Parallel.map(EvListUtil.map2tuples(map), new Parallel.FuncAB<Tuple<A,B>,Tuple<A,B>>(){
			public Tuple<A,B> func(Tuple<A,B> in)	{return new Tuple<A, B>(in.fst(),func.func(in.snd()));}
		}));
		}
	
	/*
	public static interface Func
		{
		public <A,B> Tuple<A,B> fmap(A a, B b);
		}
	

	
	
	
	
	public TreeMap<A,B> fmap(TreeMap<A,B> in, final Func func)
		{
		final TreeMap<A, B> out=new TreeMap<A, B>();
		
		int numCpu=Runtime.getRuntime().availableProcessors();
		System.out.println("#CPU "+numCpu);
		
		final Semaphore putsem=new Semaphore(numCpu);

		for(final Map.Entry<A, B> e:in.entrySet())
			{
			boolean got=false;
			while(!got)
				try
					{
					putsem.acquire();
					got=true;
					}
				catch (InterruptedException e1)
					{
					e1.printStackTrace();
					}
			
				
				new Thread(new Runnable(){
					public void run()
						{
						Tuple<A,B> p=func.fmap(e.getKey(), e.getValue());
						synchronized (out)
							{
							out.put(p.fst(), p.snd());
							}
						putsem.release();
						}
				}).start();
			}
		return out;
		}
	*/
	}
