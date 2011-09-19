/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ev;

import java.util.*;

import endrov.util.ProgressHandle;

public abstract class BatchThread extends Thread
	{
	private Vector<BatchListener> listeners=new Vector<BatchListener>();
	public boolean die=false;
	public ProgressHandle progh=new ProgressHandle(); //TODO  also get rid of die - not needed
	
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
