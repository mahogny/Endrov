package imserv;


import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;


//when refactoring, probably move most of this to imserv

/**
 * ImServ daemon
 * 
 * @author Johan Henriksson
 */
public class Daemon extends Thread
	{
	public static final int PORT = 2020;

	private Vector<DaemonListener> listeners=new Vector<DaemonListener>();
	private Vector<RepositoryDir> reps=new Vector<RepositoryDir>();
	public WeakHashMap<ClientSessionIF, Object> sessions=new WeakHashMap<ClientSessionIF, Object>();

	
	public Map<String,Set<DataIF>> channels=new HashMap<String,Set<DataIF>>();
	public Map<String,Set<DataIF>> tags=new HashMap<String,Set<DataIF>>();
	public Map<String,Set<DataIF>> objs=new HashMap<String,Set<DataIF>>();
	
	
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
//		if (System.getSecurityManager() == null) 
//			System.setSecurityManager(new RMISecurityManager());
	
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
				if(rep.dir.exists())
					{
					//Check what to remove and what to keep
					HashSet<String> incoming=new HashSet<String>();
					for(File file:rep.dir.listFiles())
						if(!file.getName().startsWith("."))
							incoming.add(file.getName());
					
					
					HashSet<String> toAdd=new HashSet<String>();
					toAdd.addAll(incoming);
					toAdd.removeAll(rep.data.keySet());
					
					HashSet<String> toRemove=new HashSet<String>();
					toRemove.addAll(rep.data.keySet());
					toRemove.removeAll(incoming);
					
					for(String name:toAdd)
						{
						try
							{
							DataImpl data=new DataImpl(name,new File(rep.dir,name));
							addData(rep,data);
//							rep.data.put(name, data);
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}
						}
					
					for(String name:toRemove)
						{
						rep.data.remove(name);
						}
					
					if(!toAdd.isEmpty() || !toRemove.isEmpty())
						for(DaemonListener list:listeners)
							list.repListUpdated();
					}
			
			//Sleep for a while
			try{Thread.sleep(1000);}
			catch (InterruptedException e){}
			}
		
		
		
		}
	
	
	private synchronized void addData(RepositoryDir rep, DataImpl data)
		{
		rep.data.put(data.getName(), data);
		for(String s:data.channels)
			getMapCreate(s, channels).add(data);
		for(String s:data.tags)
			getMapCreate(s, tags).add(data);
		for(String s:data.objs)
			getMapCreate(s, objs).add(data);
		
		for(DaemonListener list:listeners)
			list.log("Found new object: "+data.getName());
		}
	
	private Set<DataIF> getMapCreate(String key, Map<String,Set<DataIF>> map)
		{
		Set<DataIF> set=map.get(key);
		if(set==null)
			{
			set=new HashSet<DataIF>();
			map.put(key,set);
			}
		return set;
		}
	
	
	
	public synchronized DataIF getData(String name)
		{
		for(RepositoryDir rep:reps)
			{
			DataIF data=rep.data.get(name);
			if(data!=null)
				return data;
			}
		return null;
		}
	
	public synchronized void addRepository(File dir)
		{
		RepositoryDir repdir=new RepositoryDir();
		repdir.dir=dir;
		reps.add(repdir);
		System.out.println("add");
		notify();
		}
	
	
	public synchronized void addListener(DaemonListener listener)
		{
		listeners.add(listener);
		}
	
	
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
