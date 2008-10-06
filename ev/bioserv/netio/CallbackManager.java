package bioserv.netio;

import java.util.HashMap;

/**
 * 
 * @author mahogny
 *
 */
public class CallbackManager
	{
	
	private HashMap<Integer, Runnable> cb=new HashMap<Integer, Runnable>();
	private int nextCB=0;
	
	public synchronized int register(Runnable r)
		{
		synchronized (cb)
			{
			//Can in theory fill up entire queue
			while(cb.containsKey(nextCB))
				nextCB++;
			cb.put(nextCB, r);
			return nextCB;
			}
		}
	
	public void invoke(int id)
		{
		Runnable r;
		synchronized (cb)
			{
			r=cb.remove(id);
			}
		if(r!=null)
			new Thread(r).run();
		//r.run();
		//which semantic is wanted?
		}
	
	
	}
