package endrov.imageCalc;

//need to update batch script

import endrov.basicWindow.*;
import endrov.ev.*;
import endrov.imageset.*;

import java.awt.image.*;
import javax.media.jai.*;
import java.awt.image.renderable.ParameterBlock;


/**
 * The thread for doing calculations
 */
public final class CalcThread extends BatchThread
	{
	private final Imageset rec;
	
	private final String channel1, channel2, operator;
	private final int startFrame, endFrame;
	
	public CalcThread(Imageset rec, String channel1, String operator, String channel2, int startFrame, int endFrame )
		{
		this.rec=rec;
		this.channel1=channel1;
		this.channel2=channel2;
		this.operator=operator;
		this.startFrame=startFrame;
		this.endFrame=endFrame;
		}
	
	public String getBatchName()
		{
		return "Image Calculator Operation: " + operator + " " +rec.getMetadataName();
		}

	
	public void run()
		{
		try
			{
			//TODO: once we have writable images, then remove this assumption and use ordinary save system
			
			//Get channel to process
			Imageset.ChannelImages ch1from=rec.getChannel(channel1);
			if(ch1from==null)
				throw new Exception("Missing channel");
			Imageset.ChannelImages ch2from=rec.getChannel(channel2);
			if(ch1from==null)
				throw new Exception("Missing channel");
			
			//Get channel to write
			String calcChannel=channel1 + operator + channel2;
			Imageset.ChannelImages chto=rec.getChannel(calcChannel);
			
			if(chto==null)
				{
				//Channel does not exist before. Create it
				chto=rec.createChannel(calcChannel);
				
				//should anything else be copied? copy entire meta? TODO
				chto.getMeta().chBinning=ch1from.getMeta().chBinning;
				chto.getMeta().dispX=ch1from.getMeta().dispX;
				chto.getMeta().dispY=ch1from.getMeta().dispY;
				}
			else
				throw new Exception(calcChannel + " already exists");
			
			//For all frames
			int curframe=ch1from.closestFrame(startFrame);
			while(curframe<=endFrame)
				{
				//Tell about progress
				batchLog(""+curframe);

				int z=ch1from.closestZ(curframe, 0);
				try
					{


						for(;;)
							{
							//Check for premature stop
							if(die)
								{
								batchDone();
								return;
								}
							//Load images
							EvImage imload1=ch1from.getImageLoader(curframe, z);
							if(imload1==null)
								break;
							BufferedImage bufi_ch1=imload1.getJavaImage();
							if(bufi_ch1==null)
								throw new Exception("Could not load image");
							
							EvImage imload2=ch2from.getImageLoader(curframe, z);
							if(imload2==null)
								break;
							BufferedImage bufi_ch2=imload2.getJavaImage();
							if(bufi_ch2==null)
								throw new Exception("Could not load image");
							
							//create outputimage
							ParameterBlock pb = new ParameterBlock();
							pb.addSource(bufi_ch1);
							pb.addSource(bufi_ch2);
							
							RenderedOp output = JAI.create(operator, pb);
							BufferedImage BI_output = output.getAsBufferedImage();
							//Write output to calc channel
							if(output!=null)
								{
								EvImage toim=chto.createImageLoader(curframe, z);
								toim.setImage(BI_output);
								BasicWindow.updateWindows();
								}

							//Go to next z
							final int nz=ch1from.closestZAbove(curframe, z);
							if(nz==z)
								break;
							z=nz;
							}
					
					}
				catch(Exception e)
					{
					Log.printError(null, e);
					}

				//Go to next frame. End if there are no more frames.
				int newcurframe=ch1from.closestFrameAfter(curframe);
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
		BasicWindow.updateWindows();
		batchDone();
		}

	}

