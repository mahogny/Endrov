package util2.paperCeExpression.old;

import java.util.HashMap;
import java.util.concurrent.Semaphore;


public class SemaphoreSet<E>
	{
	private HashMap<E,Semaphore> locks=new HashMap<E, Semaphore>();   
	private int startPermits=1;
	
	public void acquire(E e) throws InterruptedException
		{
		synchronized (locks)
			{
			if(locks.get(e)==null)
				locks.put(e, new Semaphore(startPermits));
			}
		locks.get(e).acquire();
		}

	public void release(E e) throws InterruptedException
		{
		synchronized (locks)
			{
			if(locks.get(e)==null)
				locks.put(e, new Semaphore(startPermits));
			}
		locks.get(e).release();
		}
	
	}
