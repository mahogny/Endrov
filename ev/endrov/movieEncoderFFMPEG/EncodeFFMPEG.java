/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.movieEncoderFFMPEG;

import java.io.File;
import java.util.List;
import java.util.Vector;

import endrov.movieEncoder.EvMovieEncoder;
import endrov.movieEncoder.EvMovieEncoderFactory;

/**
 * Encode movies using FFMPEG
 * @author Johan Henriksson
 */
public class EncodeFFMPEG
	{
	public static File program=null;
	
	public static Vector<String> formats=new Vector<String>(); 
	
	public static final String formatFLV1="Lossless FLV1";
	public static final String formatHighQualMp4="High-quality MPEG4";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()	{}
	static
		{
		File programLinux=new File("/usr/bin/ffmpeg");
		File programMacPorts=new File("/opt/local/bin/ffmpeg");
		if(programLinux.exists())
			program=programLinux;
		if(programMacPorts.exists())
			program=programMacPorts;
		
		if(program!=null)
			EvMovieEncoderFactory.makers.add(new EvMovieEncoderFactory()
			{
				public EvMovieEncoder getInstance(File path, int w, int h, String quality) throws Exception
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
					return formats;
					}

				public String getDefaultQuality()
					{
					return formatHighQualMp4;
					}
				
				
			});
		
		formats.add(formatHighQualMp4);
		formats.add(formatFLV1);
		//formats.add("mpeg4");
		//formats.add("ffv1");
		}
	
	
	
	}
