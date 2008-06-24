package evplugin.imagesetImserv.service;


import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;


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
	public Vector<RepositoryDir> reps=new Vector<RepositoryDir>();
	public WeakHashMap<ClientSessionIF, Object> sessions=new WeakHashMap<ClientSessionIF, Object>();
	
	public Date lastUpdate=new Date();
	
	public Map<String,Set<DataIF>> channels=new TreeMap<String,Set<DataIF>>();
	public Map<String,Set<DataIF>> tags=new TreeMap<String,Set<DataIF>>();
	public Map<String,Set<DataIF>> objs=new TreeMap<String,Set<DataIF>>();
	
	public Map<String,User> users=new TreeMap<String,User>();
	
	public static class User
		{
		/** Encrypted password */
		public String passwd;
		
		//permission here
	
		}


	/**
	 * One repository location
	 */
	public static class RepositoryDir
		{
		public File dir;
		public Map<String, DataImpl> data=Collections.synchronizedMap(new HashMap<String, DataImpl>());
		
		//handle synch manually at higher level
		
		
		
		}

	

	
	public Map<String,DataIF> getAllDataMap()
		{
		Map<String,DataIF> map=new HashMap<String, DataIF>();
		for(RepositoryDir rep:reps)
			map.putAll(rep.data);
		return map;
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
							{
							if(file.isDirectory() || file.getName().endsWith(".ostxml"))
								incoming.add(file.getName());
							}
					
					HashSet<String> toAdd=new HashSet<String>(incoming);
					toAdd.removeAll(rep.data.keySet());
					
					HashSet<String> toRemove=new HashSet<String>(rep.data.keySet());
					toRemove.removeAll(incoming);
					
					//Add newfound entries
					for(String name:toAdd)
						{
						try
							{
							DataImpl data=new DataImpl(this,name,new File(rep.dir,name));
							addData(rep,data);
							for(DaemonListener list:listeners)
								list.log("Found new dataset: "+data.getName());
							}
						catch (Exception e)
							{
							System.out.println("For file: "+new File(rep.dir,name));
							e.printStackTrace();
							}
						}
					
					//Remove lost entries
					for(String name:toRemove)
						{
						DataImpl data=rep.data.get(name);
						removeData(rep, data);
						for(DaemonListener list:listeners)
							list.log("Removed dataset: "+data.getName());
						}
					
					if(!toAdd.isEmpty() || !toRemove.isEmpty())
						{
						setLastUpdate();
						for(DaemonListener list:listeners)
							list.repListUpdated();
						}
					}
			
			//Sleep for a while
			try{Thread.sleep(1000);}
			catch (InterruptedException e){}
			}
		
		
		
		}
	
	/**
	 * Set last updated time to now
	 */
	public void setLastUpdate()
		{
		lastUpdate=new Date();
		}

	/**
	 * Add data to one repository
	 */
	private synchronized void addData(RepositoryDir rep, DataImpl data)
		{
		rep.data.put(data.getName(), data);
		for(String s:data.channels)
			getMapCreate(s, channels).add(data);
		for(String s:data.tags)
			getMapCreate(s, tags).add(data);
		for(String s:data.objs)
			getMapCreate(s, objs).add(data);
		}
	
	private synchronized void removeData(RepositoryDir rep, DataImpl data)
		{
		rep.data.remove(data.getName());
		for(String s:data.channels)
			getMapCreate(s, channels).remove(data);
		for(String s:data.tags)
			getMapCreate(s, tags).remove(data);
		for(String s:data.objs)
			getMapCreate(s, objs).remove(data);
		}
	
	
	public Set<DataIF> getMapCreate(String key, Map<String,Set<DataIF>> map)
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

	/**
	 * Add a repository
	 */
	public synchronized void addRepository(File dir)
		{
		RepositoryDir repdir=new RepositoryDir();
		repdir.dir=dir;
		reps.add(repdir);
		notify();
		for(DaemonListener list:listeners)
			list.repListUpdated();
		}
	
	/**
	 * Remove a repository
	 */
	public synchronized void removeRepository(File dir)
		{
		for(int i=0;i<reps.size();i++)
			if(reps.get(i).dir.equals(dir))
				{
				RepositoryDir rep=reps.get(i);
				for(DataImpl data:new HashSet<DataImpl>(rep.data.values()))
					removeData(rep, data);
				reps.remove(i);
				break;
				}
		notify();
		setLastUpdate();
		for(DaemonListener list:listeners)
			list.repListUpdated();
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
	
	/**
	 * Add user
	 */
	public synchronized User addUser(String user)
		{
		User u=new User();
		users.put(user, u);
		return u;
		}

	
	}
