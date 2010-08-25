/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import endrov.data.EvData;
import endrov.ev.BatchThread;
import endrov.ev.CompleteBatch;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowProjection.EvOpProjectMaxZ;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.makeMovie.EvMovieMakerFactory;
import endrov.makeMovie.MakeMovieThread;
import endrov.util.EvDecimal;

/**
 * Go through all imagesets in a directory and make movies
 * @author Johan Henriksson
 */
public class BatchMovie
	{
	public static void makeMovie(File file)
		{
		//File outfile=new File(file.getParent(),file.getName()+".mov");
		//File outfile=new File(file.getParent(),file.getName()); //file ending added automatically
		File outfile=new File(new File(file,"data"),"thumbnail"); //file ending added automatically
		File outfile2=new File(new File(file,"data"),"thumbnail.avi");
		if(outfile2.exists())
			return;
		
		System.out.println("Doing imageset "+file.getPath());
		EvData ost=EvData.loadFile(file);
	
		if(ost==null)
			{
			System.out.println("Cannot load "+file);
			return;
			}
		else
			{
			//EvMovieMakerFactory factory=EvMovieMakerFactory.getFactory("QT: h.264 (MPEG-4)");
			EvMovieMakerFactory factory=EvMovieMakerFactory.getFactory("Mencoder");
			if(factory==null)
				{
				System.out.println("Cannot get movie maker");
				for(EvMovieMakerFactory f:EvMovieMakerFactory.makers)
					System.out.println(">"+f.getName());
				return;
				}
			//String quality="Maximum";
			String quality="Default";
	
	
	
			List<MakeMovieThread.MovieChannel> channelNames=new LinkedList<MakeMovieThread.MovieChannel>();
	
			//Get the imageset
			if(ost.getIdObjectsRecursive(Imageset.class).isEmpty())
				return;
			Imageset imset=ost.getIdObjectsRecursive(Imageset.class).values().iterator().next();
			
			//Generate max channels
			EvChannel chGFP=(EvChannel)imset.metaObject.get("GFP");
			if(chGFP!=null)
				imset.metaObject.put("GFPmax", new EvOpProjectMaxZ().exec1(chGFP));
			EvChannel chRFP=(EvChannel)imset.metaObject.get("RFP");
			if(chRFP!=null)
				imset.metaObject.put("RFPmax", new EvOpProjectMaxZ().exec1(chRFP));
			

			//Add channels that should be in the movie. Figure out best width (original width)
			int width=336;
			for(String name:new String[]{"DIC","GFPmax","RFPmax"})
				if(imset.metaObject.containsKey(name) && !((EvChannel)imset.metaObject.get(name)).imageLoader.isEmpty())
					{
					String desc="<channel/>";
					if(name.equals("DIC"))
						desc="<channel/> (<frame/>)";
					channelNames.add(new MakeMovieThread.MovieChannel(name,desc));
	
					System.out.println(name);
					//Get original image size
					EvChannel ch=(EvChannel)imset.metaObject.get(name);
					EvStack stack=ch.getFirstStack();
					EvPixels p=stack.getFirstImage().getPixels();
					width=p.getWidth();
					}
	
			System.out.println("Now making movie");
	
			BatchThread c=new MakeMovieThread(imset, EvDecimal.ZERO, new EvDecimal("1000000"), new EvDecimal(17), 
					channelNames, width, quality, outfile, factory);
	
			new CompleteBatch(c); 
			System.out.println("Movie done");
			}
		}
	
	public static void main(String[] arg)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
	
	
		if(arg.length==0)
			{
			arg=new String[]{
					"/Volumes/TBU_main06/ost4dgood/",
					};
			for(String s:arg)
				for(File file:(new File(s)).listFiles())
					if(!file.getName().startsWith(".") && !file.getName().contains("celegans"))
						makeMovie(file);
			
			
			}
		else
			{
			for(String s:arg)
				makeMovie(new File(s));
			}
		/*
					if(!file.getName().endsWith(".mov"))
						if(file.isFile())
							{
							System.out.println(file);
							//				long currentTime=System.currentTimeMillis();
							makeMovie(file);
							//			System.out.println(" timeY "+(System.currentTimeMillis()-currentTime));
							}
	*/
		System.exit(0);
	
		}
	
	
	
	}
