/**
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.auth;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import org.jdom.Element;

import bioserv.BioservDaemon;
import bioserv.BioservModule;
import bioserv.HashPassword;
import bioserv.imserv.DataIF;
import bioserv.imserv.ImservImpl;
import bioserv.imserv.TagExpr;


/**
 * Permissions based on user/pass tags in XML
 * @author Johan Henriksson
 */
public class AuthNormal implements Auth
	{
	public Map<String,User> users=new TreeMap<String,User>();
	
	public static class User
		{
		/** Encrypted password */
		public String passwd;
		/** Read permission */
		public TagExpr read=TagExpr.makeMatchNone();
		/** Write permission */
		public TagExpr write=TagExpr.makeMatchNone();
		}


	
	/**
	 * Read configuration block
	 */
	public void readConfig(Element ee)
		{
		for(Object o:ee.getChildren())
			{
			Element e=(Element)o;
			if(e.getName().equals("login"))
				{
				try
					{
					User u=addUser(e.getAttributeValue("user"));
					String passwd=e.getAttributeValue("pass");
					if(passwd==null)
						passwd=e.getAttributeValue("epass");
					else
						passwd=HashPassword.SHA1(passwd);
					if(passwd==null)
						u.passwd="";
					else
						u.passwd=passwd;
					String readperm=e.getAttributeValue("read");
					if(readperm!=null) u.read=TagExpr.parse(readperm);
					if(u.read==null) u.read=TagExpr.makeMatchNone();
					
					String writeperm=e.getAttributeValue("write");
					if(writeperm!=null) u.write=TagExpr.parse(writeperm);
					if(u.write==null) u.write=TagExpr.makeMatchNone();
					}
				catch (NoSuchAlgorithmException e1)
					{
					e1.printStackTrace();
					}
				catch (UnsupportedEncodingException e1)
					{
					e1.printStackTrace();
					}
				}
			}
		}
	
	
	
	/**
	 * Write configuration block
	 */
	public void writeConfig(Element root)
		{
		for(Map.Entry<String, User> u:users.entrySet())
			{
			Element e=new Element("login");
			e.setAttribute("user", u.getKey());
			e.setAttribute("epass", u.getValue().passwd);
			e.setAttribute("read", u.getValue().read.toString());
			e.setAttribute("write", u.getValue().write.toString());
			root.addContent(e);
			}

		}

	
	public boolean canLogin(String user, String password)
		{
		User u=users.get(user);
		if(u!=null)
			{
			try
				{
				return HashPassword.SHA1(password).equals(u.passwd);
				}
			catch (NoSuchAlgorithmException e)
				{
				e.printStackTrace();
				}
			catch (UnsupportedEncodingException e)
				{
				e.printStackTrace();
				}
			}
		return false;
		}

	
	
	//this is imserv specific!! how to auth?
	
	private ImservImpl getImserv(BioservDaemon daemon)
		{
		for(BioservModule module:daemon.modules)
			if(module instanceof ImservImpl)
				return (ImservImpl)module;
		return null;
		}
	
	public void canRead(BioservDaemon daemon, String user, Map<String,DataIF> map)
		{
		User u=users.get(user);
		if(u!=null)
			{
			TagExpr item=TagExpr.makeOr(u.read, u.write);
			item.filter(getImserv(daemon), map);
			}
		else
			map.clear();
		}
	public void canWrite(BioservDaemon daemon, String user, Map<String,DataIF> map)
		{
		User u=users.get(user);
		if(u!=null)
			u.write.filter(getImserv(daemon), map);
		else
			map.clear();
		}
	
	
	/**
	 * Add user
	 */
	private User addUser(String user)
		{
		User u=new User();
		users.put(user, u);
		return u;
		}

	
	
	
	}
