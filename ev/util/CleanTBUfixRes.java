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
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.util.EvDecimal;
import endrov.util.EvXmlUtil;


/**
 * Fix TBU data
 * @author Johan Henriksson
 */
public class CleanTBUfixRes
	{
	public static void makeOST(File file)
		{
		if(file.getName().endsWith(".ost"))
			{
			
			System.out.println("----- "+file);
			EvData data=EvData.loadFile(file);
			
			boolean changed=false;
			
			if(data.getObjects(Imageset.class).isEmpty())
				return;
			
			
			Imageset im=data.getObjects(Imageset.class).iterator().next();
			

			/**
			 * Renaming slices to b* would make it a lot easier to change resolution.
			 * it would be in-meta only.
			 * 
			 * 
			 * 
			 */
			
			
			 
			EvChannel ch=(EvChannel)im.metaObject.get("DIC");
			if(ch!=null)
				{
				EvStack stack=ch.getFirstStack();
				
				
				if(stack.lastZ().greater(new EvDecimal(40)))
					{
					System.out.println("Resolution detected wrong");
					
					}
				
				}
			
			
			
			
			if(changed)
				data.saveData();
			}
		
		
		}
	
	public static void main(String[] arg)
		{
		EvLog.listeners.add(new EvLogStdout());
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
			if(s.contains("4d"))
				if(new File(s).isDirectory())
					for(File file:(new File(s)).listFiles())
						if(file.getName().startsWith("TB"))
							if(file.isDirectory())
								makeOST(file);
		System.exit(0);
		}

	}
