/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ev;

//can be useful to improve performance, not used right now: -Dsun.java2d.opengl=true

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.prefs.*;
//import java.awt.Desktop;
import java.io.*;
import javax.swing.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import endrov.starter.EvSystemUtil;
import endrov.util.EvDecimal;

/**
 * Support functions for the EV framework
 * 
 * @author Johan Henriksson
 */
public class EV
	{
	public static final String website="http://www.endrov.net/index.php?title=";
	public static final boolean debugMode=false;
	public static final String programName="EV";
	public static boolean confirmQuit=true;

	public static Map<String,PersonalConfig> personalConfigLoaders=Collections.synchronizedMap(new HashMap<String,PersonalConfig>());

	private static boolean useHomedirConfig=false;
	
	static
		{
		//This option is not needed on mac but anyway.
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		}
	
	
	private static Semaphore hasStartedUp=new Semaphore(0);
	
	/** Check if the application has started up. It has when all plugins have been loaded */
	public static void waitUntilStartedUp()
		{
		try
			{
			hasStartedUp.acquire();
			hasStartedUp.release();
			}
		catch (InterruptedException e){}
		}
	
	/** Set that the application has started up */
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
		Preferences prefs = Preferences.userNodeForPackage (EV.class);
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
		Preferences prefs = Preferences.userNodeForPackage(EV.class);
		prefs.remove("evdata");
		if(useHomedirConfig)
			EvSystemUtil.getPersonalConfigFileName().delete();
		}
	
	/**
	 * Save personal config file
	 */
	public static void savePersonalConfig()
		{
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
			Preferences prefs = Preferences.userNodeForPackage(EV.class);
			prefs.put("evdata", s);
			//What if this fails?
			}
		
		if(EV.debugMode)
			EvLog.printLog(s);
		}

	
	
	/**
	 * Save plugin list to file
	 */
	public static void savePluginList()
		{
		if(!PluginInfo.readFromList)
	    try
	    	{
	      FileOutputStream out = new FileOutputStream(new File(new File("endrov","ev"),"pluginlist.txt"));
	      PrintStream p = new PrintStream( out );
	    	for(PluginInfo pi:PluginInfo.getPluginList())
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
		for(PluginInfo pi:PluginInfo.getPluginList())
			pi.load();
		}
	
	public static String pad(EvDecimal d, int len)
		{
		StringBuffer sb=new StringBuffer();
		pad(d,len,sb);
		return sb.toString();
		}
	public static void pad(EvDecimal d, int len, StringBuffer sb)
		{
		String s=d.toString();
		String topad="0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		int slen=s.indexOf(".");
		if(slen==-1)
			slen=s.length();
		len-=slen;
		if(slen<=0)
			sb.append(s);
//			return s;
		else if(len<100)
			sb.append(topad.substring(0,len)+s);
//			return topad.substring(0,len)+s;
		else
			{
//			StringBuffer sb=new StringBuffer(slen+len+10);
			while(len>0)
				{
				sb.append('0');
				len--;
				}
			sb.append(s);
//			return sb.toString();
			}
		}
	
	/**
	 * Format a number to a certain number of digits.
	 * @param n The number
	 * @param len The length of the final string
	 */
	public static String pad(int n, int len)
		{
		String s=Integer.toString(n);
		String topad="0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		int slen=s.length();
		len-=slen;
		if(slen<=0)
			return s;
		else if(len<100)
			return topad.substring(0,len)+s;
		else
			{
			StringBuffer sb=new StringBuffer(slen+len+10);
			while(len>0)
				{
				sb.append('0');
				len--;
				}
			sb.append(s);
			return sb.toString();
			}
		}
	
	/**
	 * Format a number to a certain number of digits. Append to existing buffer.
	 * @param n The number
	 * @param len The length of the final string
	 */
	public static void pad(int n, int len, StringBuffer sb)
		{
		String s=Integer.toString(n);
		String topad="0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		int slen=s.length();
		len-=slen;
		if(slen<=0)
			sb.append(s);
		else if(len<100)
			{
			sb.append(topad.substring(0,len));
			sb.append(s);
			}
		else
			{
			while(len>0)
				{
				sb.append('0');
				len--;
				}
			sb.append(s);
			}
		}
	
	

	
	public static void openExternal(File f)
		{
		try
			{
			if(EvSystemUtil.isMac())
				Runtime.getRuntime().exec(new String[]{"/usr/bin/open",f.getAbsolutePath()});
			else if(EvSystemUtil.isLinux())
				{
				Runtime.getRuntime().exec(new String[]{"/usr/bin/xdg-open",f.getAbsolutePath()});
				}
			else
				{
				//TODO JAVA6
				/*
				if(Desktop.isDesktopSupported())
					Desktop.getDesktop().open(f);
				else*/
					JOptionPane.showMessageDialog(null, "Feature not supported on this platform");
				}
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	/**
	 * Quit EV. Saves personal config.
	 */
	public static void quit()
		{
  	EV.savePersonalConfig();
  	System.exit(0);
		}
	
	

	/**
	 * Cast iterable to whatever type
	 */
	public static<E> Iterable<E> castIterable(Class<E> cl, final Iterable<?> o)
		{
		return new Iterable<E>(){
			public Iterator<E> iterator(){
				return new Iterator<E>(){
					Iterator<?> it=o.iterator();
					public boolean hasNext(){return it.hasNext();}
					@SuppressWarnings("all") public E next(){return (E)it.next();}
					public void remove(){it.remove();}
				};
			}
		};
		}
	
	/**
	 * Cast iterable to whatever type
	 */
	public static Iterable<Element> castIterableElement(final Iterable<?> o)
		{
		return new Iterable<Element>(){
			public Iterator<Element> iterator(){
				return new Iterator<Element>(){
					Iterator<?> it=o.iterator();
					public boolean hasNext(){return it.hasNext();}
					@SuppressWarnings("all") public Element next(){return (Element)it.next();}
					public void remove(){it.remove();}
				};
			}
		};
		}

	/**
	 * For debugging
	 */
	public static void printStackTrace(String msg)
		{
		try
			{
			throw new Exception("tracing: "+msg);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	
	}
