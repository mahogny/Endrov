/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ev;

import java.util.concurrent.Semaphore;

/**
 * Run a batch and don't return until the job is done. Simplifies using EV as a library.
 * @author Johan Henriksson
 */
public class CompleteBatch implements BatchListener
	{
//	private boolean done=false;
	Semaphore sem=new Semaphore(0);
	
	public CompleteBatch(BatchThread c)
		{
		c.addBatchListener(this);
		c.start();
	
		/*
		while(!done)
			{
			try {Thread.sleep(1000);}
			catch (InterruptedException e) {}
			}
			*/
		try
			{
			sem.acquire();
			}
		catch (InterruptedException e)
			{
			}
		}
	
	
	public void batchLog(String s)
		{
		EvLog.printLog(s);
		}
	public void batchError(String s)
		{
		EvLog.printError(s,null);
		}
	public void batchDone()
		{
		sem.release();
//		done=true;
		}
	}
