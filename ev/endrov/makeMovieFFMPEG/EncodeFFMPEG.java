package endrov.makeMovieFFMPEG;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import endrov.makeMovie.EvMovieMaker;
import endrov.makeMovie.EvMovieMakerFactory;

/**
 * Encode movies using FFMPEG
 * @author Johan Henriksson
 */
public class EncodeFFMPEG
	{
	public static File program=new File("/usr/bin/ffmpeg");
	
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
					return Arrays.asList("Default");
					}

				public String getDefaultQuality()
					{
					return "Default";
					}
				
				
			});
		}
	
	
	
	}
