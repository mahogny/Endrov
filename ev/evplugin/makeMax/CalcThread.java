package evplugin.makeMax;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.ev.*;
import evplugin.imageset.*;
import java.awt.image.*;
import javax.imageio.*;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


/**
 * The thread for doing calculations
 */
public final class CalcThread extends BatchThread
	{
	private final Imageset rec;
	
	private final int startFrame, endFrame;
	private final String channel;
	private final double quality;
	
	public CalcThread(Imageset rec, int startFrame, int endFrame, String channel, double quality)
		{
		this.rec=rec;
		this.startFrame=startFrame;
		this.endFrame=endFrame;
		this.channel=channel;
		this.quality=quality;
		}
	
	public String getBatchName()
		{
		return "MakeMax "+rec.getMetadataName();
		}

	
	public void run()
		{
		try
			{
			if(!(rec instanceof OstImageset))
				{
				batchError("Only OST supported right now");
				batchDone();
				return;
				}

			//TODO: once we have writable images, then remove this assumption and use ordinary save system
			OstImageset ost=(OstImageset)rec;
			ost.invalidateDatabaseCache();
			
			Imageset.ChannelImages chfrom=rec.getChannel(channel);
			
			if(chfrom==null)
				throw new Exception("Missing channel");
			
			
			//For all frames
			int curframe=chfrom.closestFrame(startFrame);
			while(curframe<=endFrame)
				{
				//Tell about progress
				batchLog(""+curframe);

				int z=chfrom.closestZ(curframe, 0);
				try
					{
					int[][] maxim=null;

					File outfile=ost.buildImagePath(channel+"max", curframe, 0, "");
	//				File outfile2=ost.buildImagePath(channel+"max", curframe, 0, ".jpg");
//					if(!outfile2.exists())
						for(;;)
							{
							//Check for premature stop
							if(die)
								{
								batchDone();
								return;
								}
	
							//Load image
							ImageLoader imload=chfrom.getImageLoader(curframe, z);
							if(imload==null)
								break;
							BufferedImage bufi=imload.loadImage();
							if(bufi==null)
								throw new Exception("Could not load image");
							
							//Update max
							Raster r=bufi.getData();
							final int w=bufi.getWidth();
							final int h=bufi.getHeight();									
							if(maxim==null)
								maxim=new int[h][w];
							final int pixel[]=new int[r.getNumBands()];
							for(int ay=0;ay<h;ay++)
								for(int ax=0;ax<w;ax++)
									{
									r.getPixel(ax,ay,pixel);
									final int p=pixel[0];
									if(p>maxim[ay][ax])
										maxim[ay][ax]=p;
									}
	
							//Go to next z
							final int nz=chfrom.closestZAbove(curframe, z);
							if(nz==z)
								break;
							z=nz;
							}

					//Write out max image
					if(maxim!=null)
						{
						outfile.getParentFile().mkdirs();
						saveImageJpgGray(outfile.getAbsolutePath(), maxim, quality);
						}
					
					
					}
				catch(Exception e)
					{
					Log.printError(null, e);
					}

				//Go to next frame. End if there are no more frames.
				int newcurframe=chfrom.closestFrameAfter(curframe);
				if(newcurframe==curframe)
					break;
				curframe=newcurframe;
				}
			
			//Normal exit
			batchLog("Done");
			}
		catch (Exception e)
			{
			batchLog("Failure: "+e.getMessage());
			e.printStackTrace();
			}
		rec.buildDatabase();
		BasicWindow.updateWindows();
		batchDone();
		}
    

	/**
	 * Save image
	 */  
	public static void saveImageJpgGray(String filename, int im[][], double quality) throws IOException
		{
		int w=im[0].length;
		int h=im.length;
		BufferedImage wim=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster=wim.getRaster();
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				raster.setSample(ax,ay,0,im[ay][ax]);

		saveImage(wim, new File(filename), (float)quality);
		}
	
	
	/**
	 * Save an image to disk
	 */
	private static void saveImage(BufferedImage im, File toFile, float quality) throws IOException
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
	 * Save image, many colors
	 */  
	/*
	public static void saveImageJpgGray(String filename, int im[][]) throws IOException
		{

		int w=im[0].length;
		int h=im.length;
		BufferedImage wim=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		WritableRaster raster=wim.getRaster();
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				raster.setSample(ax,ay,0,im[ay][ax]);
				raster.setSample(ax,ay,1,im[ay][ax]);
				raster.setSample(ax,ay,2,im[ay][ax]); //setting all three? sounds wasteful.
				}
	...
		}
		*/
	
	}

