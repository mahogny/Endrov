package evplugin.imagesetImserv.service;

import java.io.File;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import evplugin.ev.EvXMLutils;

/**
 * Handle config file for ImServ
 * @author Johan Henriksson
 *
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
				else if(e.getName().equals("login"))
					{
					Daemon.User u=daemon.addUser(e.getAttributeValue("user"));
					
					String passwd=e.getAttributeValue("pass");
					if(passwd==null)
						passwd=e.getAttributeValue("epass");
					else
						passwd=HashPassword.SHA1(passwd);
					if(passwd==null)
						u.passwd="";
					else
						u.passwd=passwd;
					}
				}
			
			
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
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
			
			for(Map.Entry<String, Daemon.User> u:daemon.users.entrySet())
				{
				Element e=new Element("login");
				e.setAttribute("user", u.getKey());
				e.setAttribute("epass", u.getValue().passwd);
				root.addContent(e);
				}
			
			
			EvXMLutils.writeXmlData(doc, configfile);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	
	}
