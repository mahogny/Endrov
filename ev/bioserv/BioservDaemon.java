package bioserv;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import bioserv.auth.Auth;




//when refactoring, probably move most of this to imserv

//conc mod: need a strategy, common synch object?

/**
 * ImServ daemon
 * 
 * @author Johan Henriksson
 */
public class BioservDaemon extends Thread
	{
	public static final int PORT = 2020;

	private Vector<DaemonListener> listeners=new Vector<DaemonListener>();
	public WeakHashMap<ClientSessionIF, Object> sessions=new WeakHashMap<ClientSessionIF, Object>();
	public Auth auth=null;
	public Vector<BioservModule> modules=new Vector<BioservModule>();
	
	
	
	public static interface DaemonListener
		{
		public void log(String s);
		public void sessionListUpdated();
		}

	public void log(String s)
		{
//		System.out.println("log "+s);
		for(DaemonListener d:listeners)
			d.log(s);
		}
	
	
	
	
	/**
	 * Thread: run continuously
	 */
	public void run()
		{
		//Create and install a security manager
//		if (System.getSecurityManager() == null) 
//			System.setSecurityManager(new RMISecurityManager());
	
		//Set up RMI service
		try 
			{
			Registry registry = LocateRegistry.createRegistry(PORT, new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
			
			
			for(BioservModule module:modules)
				{
				registry.bind(module.getBioservModuleName(), module);
				module.start(this);
//				System.out.println("one module");
				//				registry.bind("imserv", new ImservImpl(this));
				}
			
			System.out.println("Bound in registry");
	
			//needed at all?
//			Log.listeners.add(new evplugin.ev.StdoutLog());
//			EV.loadPlugins();

			} 
		catch (Exception e) 
			{
			e.printStackTrace();
			return;
			}

		//Poll for updates
		for(;;)
			{
			
			
			//Sleep for a while
			try{Thread.sleep(1000);}
			catch (InterruptedException e){}
			}
		
		
		
		}
	
	
	
	
	
	public synchronized void addListener(DaemonListener listener)
		{
		listeners.add(listener);
		}
	
	/**
	 * Add a session
	 */
	public synchronized void addSession(ClientSessionIF sess)
		{
		sessions.put(sess, null);
		for(DaemonListener list:listeners)
			{
			list.sessionListUpdated();
			list.log("Incoming connection");
			}
		}
	
	
	
	}
