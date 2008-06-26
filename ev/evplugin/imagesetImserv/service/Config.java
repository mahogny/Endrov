package evplugin.imagesetImserv.service;

import java.io.File;

import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;

import evplugin.ev.EvXMLutils;

/**
 * Handle config file for ImServ
 * @author Johan Henriksson
 */
public class Config
	{
	static File configfile=new File("imserv.conf");
	
	/**
	 * Read config from file
	 */
	public static void readConfig(Daemon daemon)
		{
		try
			{
			Document doc=EvXMLutils.readXML(configfile);
			Element root=doc.getRootElement();
			
			for(Object o:root.getChildren())
				{
				Element e=(Element)o;
				
				if(e.getName().equals("rep"))
					{
					String filename=e.getAttributeValue("dir");
					daemon.addRepository(new File(filename));
					}
				else if(e.getName().equals("auth"))
					{
					Class<?> autho=Class.forName(e.getAttributeValue("class"));
					daemon.auth=(Auth)autho.newInstance();
					daemon.auth.readConfig(e);
					}
				}
			}
		catch (Exception e)
			{
			JOptionPane.showMessageDialog(null, "Failed to read config file. Check syntax.\n"+e.getMessage());
			e.printStackTrace();
			System.exit(1);
			}
		if(daemon.auth==null)
			{
			JOptionPane.showMessageDialog(null, "No authorization section was found in the config file!");
			System.exit(1);
			}
		}
	

	/**
	 * Save config to file
	 */
	public static void saveConfig(Daemon daemon)
		{
		try
			{
			Element root=new Element("imserv");
			Document doc=new Document(root);
			
			for(Daemon.RepositoryDir rep:daemon.reps)
				{
				Element e=new Element("rep");
				e.setAttribute("dir", rep.dir.toString());
				root.addContent(e);
				}

			Element authe=new Element("auth");
			authe.setAttribute("class", daemon.auth.getClass().getCanonicalName());
			daemon.auth.writeConfig(authe);
			root.addContent(authe);
			
			EvXMLutils.writeXmlData(doc, configfile);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	
	}
