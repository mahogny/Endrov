package endrov.movieOutputFFMPEG;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import endrov.makeMovie.EvMovieMaker;
import endrov.makeMovie.EvMovieMakerFactory;

/**
 * Encode movies using FFMPEG
 * @author Johan Henriksson
 */
public class EncodeFFMPEG
	{
	public static File program=new File("/usr/bin/ffmpeg");
	
	public static Vector<String> formats=new Vector<String>(); 
	
	public static void initPlugin()	{}
	static
		{
		if(program.exists())
			EvMovieMakerFactory.makers.add(new EvMovieMakerFactory()
			{
				public EvMovieMaker getInstance(File path, int w, int h, String quality) throws Exception
					{
					return new FFMPEGMovieMaker(path,w,h,quality);
					}

				public String getName() 
					{
					return "FFMPEG";
					}

				public String toString()
					{
					return getName();
					}
				
				public List<String> getQualities() 
					{
					return formats;//Arrays.asList("Default");
					}

				public String getDefaultQuality()
					{
					return "Default";
					}
				
				
			});
		
		formats.add("Default");
		//formats.add("mpeg4");
		//formats.add("ffv1");
		}
	
	
	
	}
