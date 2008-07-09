package bioserv.imserv;

//http://java.sun.com/j2se/1.5.0/docs/api/javax/rmi/ssl/package-summary.html
//http://java.sun.com/j2se/1.5.0/docs/guide/rmi/socketfactory/SSLInfo.html
//http://java.sun.com/j2se/1.5.0/docs/guide/security/jsse/samples/index.html

import java.io.File;
import java.util.*;
import javax.swing.JComponent;

import org.jdom.Element;

import bioserv.*;


/**
 * Server "imserv" object: implementation
 * 
 * @author Johan Henriksson
 */
public class ImservImpl extends BioservModule implements ImservIF
	{
	public static final long serialVersionUID=0;
	
	private BioservDaemon daemon;
	
	private Vector<ImservListener> listeners=new Vector<ImservListener>();
	
	public Vector<RepositoryDir> reps=new Vector<RepositoryDir>();
	public Map<String,Set<DataIF>> tags=new TreeMap<String,Set<DataIF>>();
	
	public Date lastUpdate=new Date();

	private ImservImpl selfref=this;
	private ImservThread thread=new ImservThread();
	
	
	
	
	public ImservImpl() throws Exception 
		{
		super();
		}
	
	public void start(BioservDaemon daemon)
		{
		this.daemon=daemon;
		thread.start();
		}
	
	public ClientSessionIF auth(String user, String pass) throws Exception
		{
		if(daemon.auth.canLogin(user, pass))
			{
			ClientSessionImpl sess=new ClientSessionImpl(user);
			daemon.addSession(sess);
			return sess;
			}
		else
			{
			daemon.log("Failed to log in: "+user);
			return null;
			}
		}
	
	
	
	public String[] getDataKeys(String filter) throws Exception
		{
		TagExpr filter2=TagExpr.parse(filter);
		if(filter2!=null)
			{
			Map<String,DataIF> map=getAllDataMap();
			filter2.filter(this, map);
			return map.keySet().toArray(new String[]{});
			}
		return null;
		}
		
	
	public String[] getDataKeys() throws Exception
		{
		return getAllDataMap().keySet().toArray(new String[]{});
		}
	
	public Map<String,DataIF> getDataMap()
		{
		return getAllDataMap();
		}
	
	public String[] getTags() throws Exception
		{
		return tags.keySet().toArray(new String[]{});
		}
	
	
	public Date getLastUpdate() throws Exception
		{
		return lastUpdate;
		}
	
		
	public String getBioservModuleName()
		{
		return "imserv";
		}
	
	
	
	public JComponent getBioservModuleSwingComponent(BioservGUI gui)
		{
		ImservModulePanel p=new ImservModulePanel(gui,this);
		listeners.add(p);
		return p;
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
		
	
	public static interface ImservListener
		{
		public void repListUpdated();
		}
	

	public static String nicifyName(String name)
		{
		
		if(name.endsWith(".ost"))
			return name.substring(0,name.length()-".ost".length());
		else if(name.endsWith(".ostxml"))
			return name.substring(0,name.length()-".ostxml".length());
		else
			return name;
		}
	
	public class ImservThread extends Thread
		{
		/**
		 * Thread: run continuously
		 */
		public void run()
			{
			//Create and install a security manager
		//	if (System.getSecurityManager() == null) 
		//	System.setSecurityManager(new RMISecurityManager());
		
		
			//Poll for updates
			for(;;)
				{
//				System.out.println("imthread");
		
				//conc mod exception?
		
		
		
				for(RepositoryDir rep:reps)
					if(rep.dir.exists())
						{
						//Check what to remove and what to keep
						HashMap<String,File> incoming=new HashMap<String,File>();
						for(File file:rep.dir.listFiles())
							if(!file.getName().startsWith("."))
								{
								if(file.isDirectory() || file.getName().endsWith(".ostxml"))
									incoming.put(nicifyName(file.getName()),file);
								}
		
						HashSet<String> toAdd=new HashSet<String>(incoming.keySet());
						toAdd.removeAll(rep.data.keySet());
		
						HashSet<String> toRemove=new HashSet<String>(rep.data.keySet());
						toRemove.removeAll(incoming.keySet());
		
						//Add newfound entries
						for(String name:toAdd)
							{
							try
								{
								DataImpl data=new DataImpl(selfref,name,incoming.get(name)/*new File(rep.dir,name)*/);
								addData(rep,data);
								daemon.log("Found new dataset: "+data.getName());
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
							daemon.log("Removed dataset: "+data.getName());
							}
		
						if(!toAdd.isEmpty() || !toRemove.isEmpty())
							{
							setLastUpdate();
							for(ImservListener list:listeners)
								list.repListUpdated();
							}
						}
		
				
				//Sleep for a while
				try{Thread.sleep(1000);}
				catch (InterruptedException e){}
				}
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
		System.out.println(data.getName());
		rep.data.put(data.getName(), data);
		for(String s:data.tags.keySet())
			getMapCreate(s, tags).add(data);
		}
	
	private synchronized void removeData(RepositoryDir rep, DataImpl data)
		{
		rep.data.remove(data.getName());
		for(String s:data.tags.keySet())
			internalRemoveTag(s, data);
		}
	
	
	public synchronized void internalAddTag(String s, DataIF data)
		{
		getMapCreate(s, tags).add(data);
		}
	public synchronized void internalRemoveTag(String s, DataIF data)
		{
		Set<DataIF> set=getMapCreate(s, tags);
		set.remove(data);
		if(set.isEmpty())
			tags.remove(s);
		}
	
	private static synchronized Set<DataIF> getMapCreate(String key, Map<String,Set<DataIF>> map)
		{
		Set<DataIF> set=map.get(key);
		if(set==null)
			{
			set=new HashSet<DataIF>();
			map.put(key,set);
			}
		return set;
		}
	
	
	
	
	
	public synchronized DataIF getData(String name) throws Exception
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
		for(ImservListener list:listeners)
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
		for(ImservListener list:listeners)
			list.repListUpdated();
		}


	public synchronized Map<String,DataIF> getAllDataMap()
		{
		Map<String,DataIF> map=new HashMap<String, DataIF>();
		for(RepositoryDir rep:reps)
			map.putAll(rep.data);
		return map;
		}


	/**
	 * Load from config file
	 */
	public void loadConfig(Element root)
		{
		for(Object o:root.getChildren())
			{
			Element e=(Element)o;

			if(e.getName().equals("rep"))
				{
				String filename=e.getAttributeValue("dir");
				addRepository(new File(filename));
				}
			}
		}

	/**
	 * Save to config file
	 */
	public void saveConfig(Element root)
		{
		for(RepositoryDir rep:reps)
			{
			Element e=new Element("rep");
			e.setAttribute("dir", rep.dir.toString());
			root.addContent(e);
			}
		}






	}

