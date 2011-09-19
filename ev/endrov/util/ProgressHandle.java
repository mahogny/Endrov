package endrov.util;

import java.util.LinkedList;
import java.util.List;


/**
 * A handle to pass into evaluator, controlling progress
 *
 */
public class ProgressHandle
	{
	private boolean cancel=false;
	
	private int currentProc;
	private int totalProc;
	
	private List<ProgressListener> progressListeners=new LinkedList<ProgressListener>(); 

	private List<Stoppable> cancelListeners=new LinkedList<Stoppable>();

	public static interface Stoppable
		{
		public void signalStop();
		}

	/**
	 * Progress changed, from 0-1
	 */
	public static interface ProgressListener
		{
		public void eventProgress(ProgressHandle h, double d);
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
			super("Evaluation cancelled");
			}
		}

	/**
	 * Specify which process in the queue is running
	 */
	public void setProcNum(int current, int total)
		{
		currentProc=current;
		totalProc=total;
		setFraction(0);
		
		System.out.println("---------- calculation "+current+"/"+total);
		}
	
	/**
	 * Set progress for this process
	 */
	public void setFraction(double d)
		{
		double p=(currentProc+d)/totalProc;
		for(ProgressListener l:progressListeners)
			l.eventProgress(this, p);
		}
	
	public void addListener(ProgressListener l)
		{
		progressListeners.add(l);
		}
	
	public void removeListener(ProgressListener l)
		{
		progressListeners.remove(l);
		}
	
	/**
	 * Cancel evaluation
	 */
	public void cancel()
		{
		synchronized (cancelListeners)
			{
			if(!cancel)
				{
				cancel=true;
				for(Stoppable s:cancelListeners)
					s.signalStop();
				}
			}
		}

	public static void checkStop(ProgressHandle progh)
		{
		if(progh.cancel)
			throw new CancelException();
		}
	
	public static boolean shouldStop(ProgressHandle progh)
		{
		if(progh!=null)
			return progh.cancel;
		else
			return false;
		}



	
	
	public void addStoppable(Stoppable s)
		{
		synchronized (cancelListeners)
			{
			cancelListeners.add(s);
			}
		}
	
	public void removeStoppable(Stoppable s)
		{
		synchronized (cancelListeners)
			{
			cancelListeners.remove(s);
			}
		}
	
	
	
	
	}