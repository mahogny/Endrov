/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.movieEncoderImageSeries;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import endrov.movieEncoder.EvMovieEncoder;
import endrov.movieEncoder.EvMovieEncoderFactory;

/**
 * Encode movies as a series of files
 * @author Johan Henriksson
 */
public class EncodeImageSeries
	{
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()	{}
	static
		{
			EvMovieEncoderFactory.makers.add(new EvMovieEncoderFactory()
			{
				public EvMovieEncoder getInstance(File path, int w, int h, String quality) throws Exception
					{
					return new ImageSeriesMovieMaker(path,w,h,quality);
					}

				public String getName() 
					{
					return "PNG images";
					}

				public String toString()
					{
					return getName();
					}
				
				public List<String> getQualities() 
					{
					return Arrays.asList("Lossless");
					}

				public String getDefaultQuality()
					{
					return "Lossless";
					}
				
				
			});
		}
	
	
	
	}
