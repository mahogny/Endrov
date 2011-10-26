/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;
import java.io.*;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.*;
import endrov.imageset.EvChannel;
import endrov.imageset.Imageset;
import endrov.util.EvXmlUtil;


/**
 * Clean up TBU data
 * @author Johan Henriksson
 */
public class CleanTBU
	{
	public static void makeOST(File file)
		{
		if(file.getName().endsWith(".ost"))
			{
			System.out.println("----- "+file);
			EvData data=EvData.loadFile(file);
			
			boolean changed=false;
			for(Map.Entry<EvPath, EvChannel> e:data.getIdObjectsRecursive(EvChannel.class).entrySet())
				{
				EvChannel ch=e.getValue();
				if(ch.getFrames().isEmpty() && ch.metaObject.isEmpty())
					{
					System.out.println("Would delete "+e.getKey());
					EvContainer cont=e.getKey().getParent().getObject();
					
					cont.metaObject.remove(e.getKey().getLeafName());
					changed=true;
					
					}
				
				
				for(String s:new String[]{
						"endtime",
						"uniblitz",
					})
					if(ch.metaOther.containsKey(s))
						{
						changed=true;
						ch.metaOther.remove(s);
						}
				
				}
			
			if(data.getObjects(Imageset.class).isEmpty())
				return;
			
			
			Imageset im=data.getObjects(Imageset.class).iterator().next();
			
			String oldDesc=im.metaOther.get("description");
			if(oldDesc!=null)
				im.metaOther.put("tbu_old_desc",oldDesc);
			
			try
				{
				File imservFile=new File(new File(file,"data"),"imserv.txt");
				File newImservFile=new File(new File(file,"data"),"imserv.txt.old");
				Document imserv=EvXmlUtil.readXML(imservFile);
				changed=true;
				
				Element root=imserv.getRootElement();
				
				for(Object o:root.getChildren())
					{
					Element e=(Element)o;
					String name=e.getAttributeValue("name");
					String value=e.getAttributeValue("value");

					if(name.equals("author"))
						{
						im.metaOther.put("authorID", value);
						}
					else if(name.equals("desc"))
						{
						if(oldDesc==null)
							oldDesc=value;
						else
							oldDesc=oldDesc+" | "+value;
						im.metaOther.put("description", oldDesc);
						}
					else if(name.equals("entry_date"))
						{
						im.metaOther.put("tbu_entryDate", value);
						}
					else if(value==null)
						{
						im.tags.add(name);
						}
					else
						{
						System.out.println("abort: "+name+" "+value);
						System.exit(1);
						}
					}
				imservFile.renameTo(newImservFile);
				}
			catch (Exception e1)
				{
				}
			
			
			for(String s:new String[]{
					"openlabtimeout",
					"objfactors",
					"objpixels",
					"maxtotalstacks",
					"framestart",
					"opvarfactors",
					"openlabautomation",
					"ORSversion",
					"slicenum"})
				if(im.metaOther.containsKey(s))
					{
					changed=true;
					im.metaOther.remove(s);
					}
			
			
			
			
			if(changed)
				data.saveData();
			}
		
		
		}
	
	public static void main(String[] arg)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();

		
//		if(arg.length==0)
		arg=new String[]{
				
				"/Volumes/TBU_main01/ost3dfailed/","/Volumes/TBU_main01/ost4dfailed/",
				"/Volumes/TBU_main01/ost3dgood/","/Volumes/TBU_main01/ost4dgood",
				
				"/Volumes/TBU_main02/ost3dfailed/","/Volumes/TBU_main02/ost4dfailed/",
				"/Volumes/TBU_main02/ost3dgood/","/Volumes/TBU_main02/ost4dgood",

				
				"/Volumes/TBU_main03/ost3dfailed/","/Volumes/TBU_main03/ost4dfailed/",
				"/Volumes/TBU_main03/ost3dgood/","/Volumes/TBU_main03/ost4dgood",

				"/Volumes/TBU_main04/ost3dfailed/","/Volumes/TBU_main04/ost4dfailed/",
				"/Volumes/TBU_main04/ost3dgood/","/Volumes/TBU_main04/ost4dgood",
				
			};
		for(String s:arg)
			if(new File(s).isDirectory())
				for(File file:(new File(s)).listFiles())
					if(file.isDirectory())
						makeOST(file);
		System.exit(0);
		}

	}
