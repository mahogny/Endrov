package endrov.makeMax;

//need to update batch script

import endrov.basicWindow.*;
import endrov.ev.*;
import endrov.imageset.*;
import endrov.util.EvDecimal;

import java.awt.image.*;


/**
 * The thread for doing calculations
 */
public final class CalcThread extends BatchThread
	{
	private final Imageset rec;
	
	private final int startFrame, endFrame;
	private final String channel;
	
	public CalcThread(Imageset rec, int startFrame, int endFrame, String channel)
		{
		this.rec=rec;
		this.startFrame=startFrame;
		this.endFrame=endFrame;
		this.channel=channel;
		}
	
	public String getBatchName()
		{
		return "MakeMax";
		}

	
	public void run()
		{
		try
			{
			//TODO: once we have writable images, then remove this assumption and use ordinary save system
			//ost.invalidateDatabaseCache();

			//Get channel to process
			EvChannel chfrom=rec.getChannel(channel);
			if(chfrom==null)
				throw new Exception("Missing channel");
			
			//Get channel to write
			String maxChannel=channel+"max";
			EvChannel chto=rec.getChannel(maxChannel);
			if(chto==null)
				{
				//Channel does not exist before. Create it
				chto=rec.getCreateChannel(maxChannel);
				
				//should anything else be copied? copy entire meta? TODO
				chto.chBinning=chfrom.chBinning;
				chto.dispX=chfrom.dispX;
				chto.dispY=chfrom.dispY;
				}
			else
				throw new Exception("Max-channel already exists");
			
			//For all frames
			EvDecimal curframe=chfrom.closestFrame(new EvDecimal(startFrame));
			while(curframe.lessEqual(new EvDecimal(endFrame)))
				{
				//Tell about progress
				batchLog(""+curframe);

				EvDecimal z=chfrom.closestZ(curframe, EvDecimal.ZERO);
				double resX=1,resY=1,dispX=0,dispY=0,binning=1;
				try
					{
					int[][] maxim=null;

						for(;;)
							{
							//Check for premature stop
							if(die)
								{
								batchDone();
								return;
								}
	
							//Load image
							EvStack stack=chfrom.imageLoader.get(curframe);
							EvImage imload=chfrom.getImageLoader(curframe, z);
							if(imload==null)
								break;
							resX=stack.resX;
							resY=stack.resY;
							dispX=stack.dispX;
							dispY=stack.dispY;
							binning=stack.binning;
							
							
							BufferedImage bufi=imload.getJavaImage();
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
							final EvDecimal nz=chfrom.closestZAbove(curframe, z);
							if(nz.equals(z))
								break;
							z=nz;
							}

					//Write out max image
					if(maxim!=null)
						{
						EvStack stack=new EvStack();
						
						EvImage toim=new EvImage();
						toim.setImage(makeBI(maxim));
						stack.resX=resX;
						stack.resY=resY;
						stack.dispX=dispX;
						stack.dispY=dispY;
						stack.binning=binning;
						
						stack.put(EvDecimal.ZERO,toim);
						
						chto.imageLoader.put(curframe,stack);
						
						//chto.setImage(curframe, EvDecimal.ZERO, toim);
						BasicWindow.updateWindows();
						}
					
					
					}
				catch(Exception e)
					{
					Log.printError(null, e);
					}

				//Go to next frame. End if there are no more frames.
				EvDecimal newcurframe=chfrom.closestFrameAfter(curframe);
				if(newcurframe.equals(curframe))
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
		BasicWindow.updateWindows();
		batchDone();
		}

	
	/**
	 * Turn array into buffered image
	 */  
	private static BufferedImage makeBI(int im[][]) 
		{
		int w=im[0].length;
		int h=im.length;
		BufferedImage wim=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster=wim.getRaster();
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				raster.setSample(ax,ay,0,im[ay][ax]);
		return wim;
		}


	
	
	}

