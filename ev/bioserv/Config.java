package bioserv;

import java.io.File;

import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;

import bioserv.auth.Auth;


import endrov.ev.EvXMLutils;

/**
 * Handle config file for ImServ
 * @author Johan Henriksson
 */
public class Config
	{
	static File configfile=new File("bioserv.conf");
	
	/**
	 * Read config from file
	 */
	public static void readConfig(BioservDaemon daemon)
		{
		try
			{
			Document doc=EvXMLutils.readXML(configfile);
			Element root=doc.getRootElement();
			
			for(Object o:root.getChildren())
				{
				Element e=(Element)o;
				
				if(e.getName().equals("module"))
					{
					Class<?> modulo=Class.forName(e.getAttributeValue("class"));
					//Constructor<?> constr=modulo.getConstructor(new Class[]{BioservDaemon.class});
					BioservModule module=(BioservModule)modulo.newInstance();
					//BioservModule module=(BioservModule)constr.newInstance(new Object[]{daemon});
					
					module.loadConfig(e);
					daemon.modules.add(module);
					}
				else if(e.getName().equals("auth"))
					{
					Class<?> autho=Class.forName(e.getAttributeValue("class"));
					daemon.auth=(Auth)autho.newInstance();
					daemon.auth.readConfig(e);
					}
				}
			}
		catch(ClassNotFoundException e)
			{
			JOptionPane.showMessageDialog(null, "Could not find class "+e.getMessage());
			e.printStackTrace();
			System.exit(1);
			}
		catch(Exception e)
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
	public static void saveConfig(BioservDaemon daemon)
		{
		try
			{
			Element root=new Element("conf");
			Document doc=new Document(root);
			
			//All modules
			for(BioservModule module:daemon.modules)
				{
				Element e=new Element("module");
				module.saveConfig(e);
				e.setAttribute("class",module.getClass().getCanonicalName());
				root.addContent(e);
				}
			
			//Auth system
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
