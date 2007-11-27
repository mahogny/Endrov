package evplugin.ev;

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
		Log.printLog(s);
		}
	public void batchError(String s)
		{
		Log.printError(s,null);
		}
	public void batchDone()
		{
		sem.release();
//		done=true;
		}
	}
