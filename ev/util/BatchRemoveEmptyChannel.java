package util;
import java.io.*;
import java.util.Map;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.*;
import endrov.imageset.EvChannel;


/**
 * Delete empty channels in all recordings
 * @author Johan Henriksson
 */
public class BatchRemoveEmptyChannel
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
				if(ch.imageLoader.isEmpty() && ch.metaObject.isEmpty())
					{
					System.out.println("Would delete "+e.getKey());
					
					
					EvContainer cont=e.getKey().getParent().getContainer(data);
					
					cont.metaObject.remove(e.getKey().getLeafName());
					changed=true;
					
					}
//				else
	//				System.out.println("Would keep "+e.getKey());
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
				
/*
				"/Volumes/TBU_main02/ost3dfailed/","/Volumes/TBU_main02/ost4dfailed/",
				"/Volumes/TBU_main02/ost3dgood/","/Volumes/TBU_main02/ost4dgood",

				
				"/Volumes/TBU_main03/ost3dfailed/","/Volumes/TBU_main03/ost4dfailed/",
				"/Volumes/TBU_main03/ost3dgood/","/Volumes/TBU_main03/ost4dgood",

				"/Volumes/TBU_main04/ost3dfailed/","/Volumes/TBU_main04/ost4dfailed/",
				"/Volumes/TBU_main04/ost3dgood/","/Volumes/TBU_main04/ost4dgood",
				*/
				
			};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
//				if(file.isDirectory())
					makeOST(file);
		System.exit(0);
		}

	}
