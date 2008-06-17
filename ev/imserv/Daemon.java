package imserv;


import java.io.File;
import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;


/**
 * ImServ daemon
 * 
 * @author Johan Henriksson
 */
public class Daemon extends Thread
	{
	private Vector<DaemonListener> listeners=new Vector<DaemonListener>();
	
	
	public static final int PORT = 2020;

	
	Vector<RepositoryDir> reps=new Vector<RepositoryDir>();
	
	WeakHashMap<ClientSessionIF, Object> sessions=new WeakHashMap<ClientSessionIF, Object>();
	
	public static class RepositoryDir
		{
		File dir;
		Map<String, DataIF> data=Collections.synchronizedMap(new HashMap<String, DataIF>());
		
		//handle synch manually at higher level
		}
	
	public Map<String,DataIF> getAllDataMap()
		{
		Map<String,DataIF> map=new HashMap<String, DataIF>();
		for(RepositoryDir rep:reps)
			map.putAll(rep.data);
		return map;
		}
	
	
	
	public void run()
		{
		//Create and install a security manager
		if (System.getSecurityManager() == null) 
			System.setSecurityManager(new RMISecurityManager());
	
		//Set up RMI service
		try 
			{
			Registry registry = LocateRegistry.createRegistry(PORT, new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
			registry.bind("imserv", new ImservImpl(this));
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
			
			//conc mod exception?
			
			
			for(RepositoryDir rep:reps)
				{
				//Check what to remove and what to keep
				HashSet<String> incoming=new HashSet<String>();
				File[] children=rep.dir.listFiles();
				for(File file:children)
					if(!file.getName().startsWith("."))
						incoming.add(file.getName());
				
				HashSet<String> toAdd=new HashSet<String>();
				toAdd.addAll(incoming);
				toAdd.removeAll(rep.data.keySet());
				
				HashSet<String> toRemove=new HashSet<String>();
				toRemove.addAll(rep.data.keySet());
				toRemove.removeAll(incoming);
				
				//TODO
				
//				for(DataIF data:rep.data)
				
				}
			
			//Sleep for a while
			try{Thread.sleep(1000);}
			catch (InterruptedException e){}
			}
		
		
		
		}
	
	
	
	public void addListener(DaemonListener listener)
		{
		listeners.add(listener);
		}
	
	
	}
