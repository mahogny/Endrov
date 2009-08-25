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
	
	public static boolean first=true;
	public static String getchdesc()
		{
		String s=first?"<channel/> (<frame/>)" : "<channel/>";
		first=false;
		return s;
		}
	
	public static void makeMovie(File file)
		{
		//first=true;
		EvData ost=EvData.loadFile(file);

		if(ost==null)
			return;
		else
			{
		
			System.out.println("Imageset "+file.getPath());
			
			List<MakeMovieThread.MovieChannel> channelNames=new LinkedList<MakeMovieThread.MovieChannel>();
			
			
			Imageset imset=ost.getIdObjectsRecursive(Imageset.class).values().iterator().next();
	
			for(String name:new String[]{"GFPmax","ch0","DIC"})
				if(imset.metaObject.containsKey(name))
					channelNames.add(new MakeMovieThread.MovieChannel(name,""));
			
			System.out.println("Now making movie");
			
			BatchThread c=new MakeMovieThread(imset, EvDecimal.ZERO, new EvDecimal("1000000"), 15, 
					channelNames, 336, "Maximum", new File(file.getParent(),file.getName()+".mov"),
					EvMovieMakerFactory.getFactory("h.264 (MPEG-4)"));
			
			new CompleteBatch(c); 
			System.out.println("Movie done");
			}
		}
	
	public static void main(String[] arg)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		
		
		
		if(arg.length==0)
			arg=new String[]{
					"/Volumes/TBU_xeon01_500GB01/ost4dgood/",
					"/Volumes/TBU_xeon01_500GB02/ost3dgood/",
					"/Volumes/TBU_xeon01_500GB02/ost4dgood/"
					
		};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					{
					long currentTime=System.currentTimeMillis();
					makeMovie(file);
					System.out.println(" timeY "+(System.currentTimeMillis()-currentTime));
					}
		}
	
	}
