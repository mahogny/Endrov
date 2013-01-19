/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowMakeMaxProjection;

//need to update batch script

import endrov.core.batch.BatchThread;
import endrov.core.log.EvLog;
import endrov.data.EvContainer;
import endrov.flowProjection.EvOpProjectMaxZ;
import endrov.gui.window.EvBasicWindow;
import endrov.typeImageset.*;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;


/**
 * The thread for doing calculations
 */
public final class CalcThread extends BatchThread
	{
	private final EvContainer rec;
	
	private final int startFrame, endFrame;
	private final String channel;
	
	public ProgressHandle ph=new ProgressHandle(); //TODO connect handle
	
	public CalcThread(EvContainer rec, int startFrame, int endFrame, String channel)
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
			EvChannel chfrom=(EvChannel)rec.metaObject.get(channel);
			if(chfrom==null)
				throw new Exception("Missing channel: "+channel);
			
			//Get channel to write
			String maxChannel=channel+"max";
			EvChannel chto=(EvChannel)rec.metaObject.get(maxChannel);
			if(chto==null)
				{
				//Channel does not exist before. Create it
				chto=new EvChannel();
				rec.metaObject.put(maxChannel, chto);
				
				//should anything else be copied? copy entire meta? TODO
				chto.chBinning=chfrom.chBinning;
				
//				chto.defaultDispX=chfrom.defaultDispX;
//				chto.defaultDispY=chfrom.defaultDispY;
				}
			else
				throw new Exception("Max-channel already exists");
			
			//For all frames
			EvDecimal curframe=chfrom.closestFrame(new EvDecimal(startFrame));
			while(curframe.lessEqual(new EvDecimal(endFrame)))
				{
				//Tell about progress
				batchLog(""+curframe);

				
//				EvDecimal z=chfrom.closestZ(curframe, EvDecimal.ZERO);
	//			double resX=1,resY=1,dispX=0,dispY=0,binning=1;
				try
					{
		//			int[][] maxim=null;

					//Check for premature stop
					if(die)
						{
						batchDone();
						return;
						}
					
					EvStack s=chfrom.getStack(ph, curframe);
					EvStack max=new EvOpProjectMaxZ().project(ph, s);
					
					//Pick out only one slice - in case we resave to disk
					/*EvStack maxOneLayer=new EvStack();
					maxOneLayer.getMetaFrom(max);
					max.putInt(0, max.getInt(0));*/


					/*
						for(;;)
							{
							

							
	
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
							
							EvPixels pix=imload.getPixels();
							BufferedImage bufi=pix.quickReadOnlyAWT();
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

						*/
						
					/*
					//Write out max image
					if(maxim!=null)
						{
						EvStack stack=new EvStack();
						
						EvImage toim=new EvImage();
						
						toim.setPixelsReference(new EvPixels(makeBI(maxim)));
						stack.resX=resX;
						stack.resY=resY;
						stack.dispX=dispX;
						stack.dispY=dispY;
						stack.binning=binning;
						
						stack.put(EvDecimal.ZERO,toim);
						*/
						chto.putStack(curframe,max);
						
						//chto.setImage(curframe, EvDecimal.ZERO, toim);
						EvBasicWindow.updateWindows();
//						}
					
					
					}
				catch(Exception e)
					{
					EvLog.printError(null, e);
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
		EvBasicWindow.updateWindows();
		batchDone();
		}

	
	/**
	 * Turn array into buffered image
	 */  
	/*
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
*/

	
	
	}

