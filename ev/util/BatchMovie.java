/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import endrov.core.EndrovCore;
import endrov.core.batch.BatchThread;
import endrov.core.batch.CompleteBatch;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.flowProjection.EvOpProjectMaxZ;
import endrov.movieEncoder.EvMovieEncoderFactory;
import endrov.movieEncoder.EncodeMovieThread;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvStack;
import endrov.typeImageset.Imageset;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;

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
		File outfile=new File(new File(file,"data"),"thumbnailMPEG4"); //file ending added automatically
		File outfile2=new File(new File(file,"data"),"thumbnailMPEG4.avi");
		if(outfile2.exists())
			{
			System.out.println("Skipping "+file);
			return;
			}
		
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
			//EvMovieMakerFactory factory=EvMovieMakerFactory.getFactory("Mencoder");
//			EvMovieMakerFactory factory=EvMovieMakerFactory.getFactory("Mencoder");
			EvMovieEncoderFactory factory=EvMovieEncoderFactory.getFactory("FFMPEG");
			if(factory==null)
				{
				System.out.println("Cannot get movie maker");
				for(EvMovieEncoderFactory f:EvMovieEncoderFactory.makers)
					System.out.println(">"+f.getName());
				return;
				}
			//String quality="Maximum";
			//String quality="Default";
			String quality="High-quality MPEG4";
	
			ProgressHandle ph=new ProgressHandle(); 
	
			List<EncodeMovieThread.MovieChannel> channelList=new LinkedList<EncodeMovieThread.MovieChannel>();
	
			//Get the imageset
			if(ost.getIdObjectsRecursive(Imageset.class).isEmpty())
				return;
			Imageset imset=ost.getIdObjectsRecursive(Imageset.class).values().iterator().next();
			
			//Generate max channels
			EvChannel chGFP=(EvChannel)imset.metaObject.get("GFP");
			if(chGFP!=null)
				imset.metaObject.put("GFPmax", new EvOpProjectMaxZ().exec1(ph, chGFP));
			EvChannel chRFP=(EvChannel)imset.metaObject.get("RFP");
			if(chRFP!=null)
				imset.metaObject.put("RFPmax", new EvOpProjectMaxZ().exec1(ph, chRFP));
			

			//Add channels that should be in the movie. Figure out best width (original width)
			int width=336;
			EvDecimal z=new EvDecimal(17);
			for(String name:new String[]{"DIC","GFPmax","RFPmax"})
				if(imset.metaObject.containsKey(name) && !((EvChannel)imset.metaObject.get(name)).getFrames().isEmpty())
					{
					String desc="<channel/>";
					if(name.equals("DIC"))
						desc="<channel/> (<frame/>)";
					channelList.add(new EncodeMovieThread.MovieChannel(name,(EvChannel)imset.metaObject.get(name), desc,z));
	
					System.out.println(name);
					//Get original image size
					EvChannel ch=(EvChannel)imset.metaObject.get(name);
					EvStack stack=ch.getFirstStack(null);
					EvPixels p=stack.getFirstPlane().getPixels(ph);
					width=p.getWidth();
					}
	
			System.out.println("Now making movie");
	
			BatchThread c=new EncodeMovieThread(EvDecimal.ZERO, new EvDecimal("1000000"), 
					channelList, width, factory, quality, outfile);
	
			new CompleteBatch(c); 
			System.out.println("Movie done");
			}
		}
	
	public static void main(String[] arg)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
	
	
		if(arg.length==0)
			{
			arg=new String[]{
					"/Volumes/TBU_main06/ost4dgood/",
					//"/pimai/TBU_main01b/ost4dgood/",
					"/pimai/TBU_extra05/ost4dpaper/"
					//"/pimai/TBU_main01b/daemon/output/",
					};
			for(String s:arg)
				for(File file:(new File(s)).listFiles())
					if(!file.getName().startsWith(".") && !file.getName().contains("celegans"))
						try
							{
							makeMovie(file);
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}
			
			
			}
		else
			{
			for(String s:arg)
				try
					{
					makeMovie(new File(s));
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
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
