/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.core;

//can be useful to improve performance, not used right now: -Dsun.java2d.opengl=true


import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.prefs.*;
import java.io.*;


import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import endrov.core.log.EvLog;
import endrov.starter.EvSystemUtil;
import endrov.util.io.EvXmlUtil;
import endrov.util.mutable.Mutable;

/**
 * Support functions for the EV framework
 * 
 * @author Johan Henriksson
 */
public class EndrovCore
	{
	public static final String programName="Endrov";
	public static final String website="http://www.endrov.net";
	public static final String websiteWikiPrefix="http://www.endrov.net/wiki/index.php?title=";
	
	public static final boolean debugMode=false;
	public static boolean userShouldConfirmQuit=true;

	private static Map<String,PersonalConfig> personalConfigLoaders=Collections.synchronizedMap(new HashMap<String,PersonalConfig>());

	private static boolean useHomedirConfig=false;
	private static Mutable<File> swapDirectory=new Mutable<File>(null);
	private static Semaphore hasStartedUp=new Semaphore(0);

	
	public static void addPersonalConfigLoader(String s,PersonalConfig pc)
		{
		personalConfigLoaders.put(s, pc);
		}
	
	/** 
	 * Check if the application has started up. It has when all plugins have been loaded 
	 */
	public static void waitUntilStartedUp()
		{
		try
			{
			hasStartedUp.acquire();
			hasStartedUp.release();
			}
		catch (InterruptedException e){}
		}
	
	/** 
	 * Set that the application has started up 
	 */
	public static void setHasStartedUp()
		{
		hasStartedUp.release();
		}

	/**
	 * Load personal config file or load defaults if it does not exist
	 */
	public static void loadPersonalConfig()
		{
		//TODO check if this is allowed e.g. applet
		useHomedirConfig=true;
		
		
		//TODO XDG can have several config directories
		Preferences prefs = Preferences.userNodeForPackage (EndrovCore.class);
		String s=null;
		if(!useHomedirConfig)
			prefs.get("evdata", null);
		if(s==null)
			{
			File configFile=EvSystemUtil.getPersonalConfigFileName();
			if(configFile.exists())
				{
				s="";
		    try
					{
					DataInputStream in = new DataInputStream(new FileInputStream(configFile));
					    BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String strLine;
					while ((strLine = br.readLine()) != null)   
					  s+=strLine;
					in.close();
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				
				
				System.out.println("Loading config file from home directory");
				useHomedirConfig=true;
				}
			}
		
		
		if(s!=null)
			{
	    try 
	    	{
	  		SAXBuilder saxBuilder = new SAXBuilder();
	  		Reader reader=new StringReader(s);
	  		Document document = saxBuilder.build(reader);
	  		Element element = document.getRootElement();

	  		//Extract objects
	  		List<?> children=element.getChildren(); 
	  		for(Object ochild:children)
	  			{
	  			Element child=(Element)ochild;
	  			String id=child.getName();
	  			PersonalConfig p=personalConfigLoaders.get(id);
	  			if(p!=null)
	  				p.loadPersonalConfig(child);
	  			else
	  				EvLog.printError("Could not find loader for id "+id,null);
	  			}
	    	} 
	    catch (Exception e) 
	    	{
	    	e.printStackTrace();
	    	} 
			}
		else
			EvLog.printLog("No personal config file");
		}
	
	/**
	 * Wipe out record in the java registry
	 */
	public static void resetPersonalConfig()
		{
		Preferences prefs = Preferences.userNodeForPackage(EndrovCore.class);
		prefs.remove("evdata");
		if(useHomedirConfig)
			EvSystemUtil.getPersonalConfigFileName().delete();
		}
	
	/**
	 * Save personal config file
	 */
	public static void savePersonalConfig()
		{
		writeSystemConfig();
		
		Element root=new Element("ev");
		Document document=new Document(root);
		for(PersonalConfig pc:personalConfigLoaders.values())
			pc.savePersonalConfig(root);

		String s="";
		try 
			{
			Format format=Format.getPrettyFormat();
			XMLOutputter outputter = new XMLOutputter(format);
			StringWriter writer = new StringWriter();
			outputter.output(document, writer);
			writer.flush();
			s=writer.toString();
			} 
		catch (java.io.IOException e) 
			{
			e.printStackTrace();
			}
		
		if(useHomedirConfig)
			{
			try
				{
				EvSystemUtil.getPersonalConfigFileName().getParentFile().mkdirs();
				BufferedWriter bufferedwriter = new BufferedWriter(new FileWriter(EvSystemUtil.getPersonalConfigFileName()));
				bufferedwriter.write(s);
				bufferedwriter.close();
				}
			catch (IOException e)
				{
				e.printStackTrace();
				}
			}
		else
			{		
			Preferences prefs = Preferences.userNodeForPackage(EndrovCore.class);
			prefs.put("evdata", s);
			//What if this fails?
			}
		
		if(EndrovCore.debugMode)
			EvLog.printLog(s);
		}

	
	
	/**
	 * Save plugin list to file
	 */
	public static void savePluginList()
		{
		if(!EvPluginManager.readFromList)
	    try
	    	{
	      FileOutputStream out = new FileOutputStream(new File(new File("endrov","ev"),"pluginlist.txt"));
	      PrintStream p = new PrintStream( out );
	    	for(EvPluginManager pi:EvPluginManager.getPluginList())
	    		p.println(pi.classPath);
	    	p.close();
	    	out.close();
	    	}
	    catch (Exception e)
	    	{
	    	System.err.println ("Error writing to file");
	    	}
		}
	
	/**
	 * Load all plugins from evplugin/
	 */
	public static void loadPlugins()
		{
		for(EvPluginManager pi:EvPluginManager.getPluginList())
			pi.load();
		
		//Read system config file
		loadSystemConfig();
		}
	
	/**
	 * Read the system configuration: non-GUI related settings
	 */
	private static void loadSystemConfig()
		{
		File sysconfigFile=EvSystemUtil.getSystemConfigFileName();
		
		try
			{
			Document doc=EvXmlUtil.readXML(sysconfigFile);
			
			Element root=doc.getRootElement();
			
			Element eSwap=root.getChild("swapdirectory");
			if(eSwap!=null)
				setSwapDirectory(new File(eSwap.getText()));
			}
		catch (Exception e)
			{
			EvLog.printError("Could not read system config", null);
			}
		}
	
	/**
	 * Write the system configuration to disk: non-GUI related settings
	 */
	private static void writeSystemConfig()
		{
		try
			{
			File sysconfigFile=EvSystemUtil.getSystemConfigFileName();
			Element root=new Element("sysconfig");
			
			File swapDirectory=getSwapDirectory();
			if(swapDirectory!=null)
				{
				Element eSwap=new Element("swapdirectory");
				root.addContent(eSwap);
				eSwap.setText(swapDirectory.toString());
				}
			
			EvXmlUtil.writeXmlData(new Document(root), sysconfigFile);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	/**
	 * Quit. Saves personal config.
	 */
	public static void quit()
		{
  	EndrovCore.savePersonalConfig();
  	System.exit(0);
		}
	
	

	
	
	/**
	 * Create temporary files. The folder can be selected by the user  
	 */
	public static File createTempFile(String prefix, String suffix) throws IOException
		{
		synchronized (swapDirectory)
			{
			return File.createTempFile(prefix, suffix, swapDirectory.get());
			}
		}
	
	public static void setSwapDirectory(File d)
		{
		synchronized (swapDirectory)
			{
			swapDirectory.setValue(d);
			}
		}

	public static File getSwapDirectory()
		{
		synchronized (swapDirectory)
			{
			return swapDirectory.get();
			}
		}

	}
