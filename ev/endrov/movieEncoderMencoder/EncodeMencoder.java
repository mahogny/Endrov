/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.movieEncoderMencoder;

import java.io.File;
import java.util.List;
import java.util.Vector;

import endrov.movieEncoder.EvMovieEncoder;
import endrov.movieEncoder.EvMovieEncoderFactory;

/**
 * Encode movies using Mencoder
 * @author Johan Henriksson
 */
public class EncodeMencoder
	{
	public static File program=new File("/usr/bin/mencoder");
	
	public static Vector<String> formats=new Vector<String>(); 
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()	{}
	static
		{
		if(program.exists())
			EvMovieEncoderFactory.makers.add(new EvMovieEncoderFactory()
			{
				public EvMovieEncoder getInstance(File path, int w, int h, String quality) throws Exception
					{
					return new MencoderMovieMaker(path,w,h,quality);
					}

				public String getName() 
					{
					return "Mencoder";
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
