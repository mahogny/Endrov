package evplugin.ev;

import java.util.*;

public abstract class BatchThread extends Thread
	{
	private Vector<BatchListener> listeners=new Vector<BatchListener>();
	public boolean die=false;
	
	public void addBatchListener(BatchListener b)
		{
		listeners.add(b);
		}

	public abstract String getBatchName();

	
	public void batchLog(String s)
		{
		for(BatchListener b:listeners)
			b.batchLog(s);
		}

	public void batchError(String s)
		{
		for(BatchListener b:listeners)
			b.batchError(s);
		}

	public void batchDone()
		{
		for(BatchListener b:listeners)
			b.batchDone();
		}
	}
