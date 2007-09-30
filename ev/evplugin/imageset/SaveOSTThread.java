package evplugin.imageset;

import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import com.sun.image.codec.jpeg.*;

import evplugin.ev.*;


/**
 * Convert an imageset to native format
 * @author Johan Henriksson
 */
public class SaveOSTThread extends BatchThread
	{
	private final Imageset rec;
	private final String imagesetFilename;
	private final double quality;
	
	/**
	 * Create calculation
	 * @param rec Record to convert
	 * @param filename Path where images will be stored (includes imageset name)
	 * @param quality Between 0 and 1 (1 for lossless)
	 */
	public SaveOSTThread(Imageset rec, String filename, double quality)
		{
		this.rec=rec;
		this.imagesetFilename=filename;
		this.quality=quality;
		}
	
	public String getBatchName()
		{
		return "SaveOST "+imagesetFilename;
		}
	
	/**
	 * Save an image to disk
	 */
	private void saveImage(BufferedImage im, File toFile, float quality) throws Exception
		{
		if(quality<1)
			{
			toFile=new File(toFile.getAbsoluteFile()+".jpg");
	    FileOutputStream toStream = new FileOutputStream(toFile); 
	    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(toStream); 
	    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(im); 
	    param.setQuality(quality, false); 
	    encoder.setJPEGEncodeParam(param); 
	    encoder.encode(im); 
			}
		else
			{
			toFile=new File(toFile.getAbsoluteFile()+".png");
			ImageIO.write(im, "png", toFile);
			}
		}
	
	
	/**
	 * The thread entry point
	 */
	public void run()
		{
		
		try
			{
			File imagesetPath=new File(imagesetFilename);
			imagesetPath.mkdirs();
			String imageset=imagesetPath.getName();
			
			//Meta data
			rec.saveMeta(new File(imagesetPath, "rmd.xml"));
			
			//Image data
			for(Imageset.ChannelImages channel:rec.channelImages.values())
				{
				File channelPath=new File(imagesetPath, imageset+"-"+channel.getMeta().name);
				channelPath.mkdir();
								
//				int numFrames=channel.imageLoader.size();
//				int doneFrames=0;
				for(int frame:channel.imageLoader.keySet())
					{
					File framePath=new File(channelPath, EV.pad(frame,8));
					framePath.mkdir();
					TreeMap<Integer, EvImage> slices=channel.imageLoader.get(frame);
					for(int slice:slices.keySet())
						{
						//Check for premature stop
						if(die)
							{
							batchDone();
							return;
							}

						//Save image
						batchLog(channel.getMeta().name+"/"+frame+"/"+slice);
						EvImage loader=slices.get(slice);
						BufferedImage im=loader.getJavaImage();
						File toFile=new File(framePath, EV.pad(slice,8));
						saveImage(im, toFile, (float)quality);
						}
					}
				}
			batchLog("Done");
			}
		catch (Exception e)
			{
			batchLog("Error: "+e.getMessage());
			e.printStackTrace();
			}
		batchDone();
		}
	}
