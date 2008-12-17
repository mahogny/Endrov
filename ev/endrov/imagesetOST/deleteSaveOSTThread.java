package endrov.imagesetOST;

import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import com.sun.image.codec.jpeg.*;

import endrov.ev.*;
import endrov.imageset.*;
import endrov.util.EvDecimal;


/**
 * Convert an imageset to native format
 * @author Johan Henriksson
 */
public class deleteSaveOSTThread extends BatchThread
	{
	private final Imageset rec;
	private final String imagesetFilename;
	
	
	/**
	 * Create calculation
	 * @param rec Record to convert
	 * @param filename Path where images will be stored (includes imageset name)
	 * @param quality Between 0 and 1 (1 for lossless)
	 */
	public deleteSaveOSTThread(Imageset rec, String filename)
		{
		this.rec=rec;
		this.imagesetFilename=filename;
		}
	
	public String getBatchName()
		{
		return "SaveOST "+imagesetFilename;
		}
	
	/**
	 * Save an image to disk
	 */
	private void saveImage(BufferedImage im, File toFile, int quality) throws Exception
		{
		if(quality<100)
			{
			toFile=new File(toFile.getAbsoluteFile()+".jpg");
	    FileOutputStream toStream = new FileOutputStream(toFile); 
	    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(toStream); 
	    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(im); 
	    param.setQuality((float)(quality/100.0), false); 
	    encoder.setJPEGEncodeParam(param); 
	    encoder.encode(im); 
			}
		else
			{
			try
				{
			toFile=new File(toFile.getAbsoluteFile()+".png");
			ImageIO.write(im, "png", toFile);
	//		System.out.println("save "+im+" "+im.getWidth()+" "+im.getHeight()+" "+im.getRaster().getNumBands());
//			System.exit(1);
			
			//Leica
			//save BufferedImage@4c7a98: type = 0 ColorModel: #pixelBits = 8 numComponents = 4 color space = java.awt.color.ICC_ColorSpace@4a96a transparency = 3 has alpha = true isAlphaPre = false sun.awt.image.SunWritableRaster@e920f 256 256 1

			
				}
			catch (Exception e)
				{
				System.out.println(e.getMessage());
				e.printStackTrace();
				}
			
			}
		}
	
	
	/**
	 * The thread entry point
	 */
	public void run()
		{
		
		try
			{
			File imagesetPath=new File(imagesetFilename+".ost");
			imagesetPath.mkdirs();
			
			//Meta data
			
			//TODO
		//TODO
		//TODO
		//TODO
		//TODO
			
//			rec.saveMeta(new File(imagesetPath, "rmd.ostxml"));

			//TODO
			//TODO
			//TODO
			//TODO
			//TODO

			
			//Image data
			for(Imageset.ChannelImages channel:rec.channelImages.values())
				{
				File channelPath=new File(imagesetPath, "ch-"+channel.getMeta().name);
				channelPath.mkdir();
								
				for(EvDecimal frame:channel.imageLoader.keySet())
					{
					File framePath=new File(channelPath, EV.pad(frame,8).toString());
					framePath.mkdir();
					TreeMap<EvDecimal, EvImage> slices=channel.imageLoader.get(frame);
					for(EvDecimal slice:slices.keySet())
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
						saveImage(im, toFile, channel.getMeta().compression);
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
