/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.movieEncoder;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;

import endrov.core.batch.BatchThread;
import endrov.typeImageset.*;
import endrov.util.math.EvDecimal;


/**
 * Thread: Encode a movie
 */
public final class EncodeMovieThread extends BatchThread 
	{
	private final EvDecimal startFrame, endFrame;

	private final int oneW;
	private final java.util.List<MovieChannel> channels;
	private final String inputQuality;
	private final File moviePath;
	private final EvMovieEncoderFactory mf;
	
	public static class MovieChannel
		{
		public String name;
		public EvChannel chan;
		public EncodeMovieDescriptionFormat desc;
		public EvDecimal z;
		public MovieChannel(String name, EvChannel chan,  String desc, EvDecimal z)
			{
			this.name=name;
			this.chan=chan;
			this.desc=new EncodeMovieDescriptionFormat(desc);
			this.z=z;
			}
		}
	
	public EncodeMovieThread(EvDecimal startFrame, EvDecimal endFrame, List<MovieChannel> channelNames, int oneW, EvMovieEncoderFactory mf, String quality,
			File movieFile)
		{
		this.startFrame=startFrame;
		this.endFrame=endFrame;
		this.channels=channelNames;
		this.oneW=oneW;
		inputQuality=quality;
		this.moviePath=movieFile;
		this.mf=mf;
		}
	
	public String getBatchName()
		{
		return moviePath.toString();
		}
	
	

	
	/**
	 * Run batch thread
	 */
	public void run()
		{
		try
			{
			//Check that channel list is ok
			if(channels.isEmpty())
				throw new Exception("Missing channels");

			//Quicktime movie to be made
			EvMovieEncoder movieMaker=null;
			
			//For all frames
			EvDecimal curframe=channels.get(0).chan/*rec.getChannel(channels.get(0).name)*/.closestFrame(startFrame);
			while(curframe.lessEqual(endFrame))
				{
				//Check for premature stop
				if(die)
					{
					batchDone();
					return;
					}

				//Tell about progress
				batchLog(""+curframe);

				//Fetch all images
				LinkedList<MovieChannelImage> mc=new LinkedList<MovieChannelImage>();
				boolean allImloadOk=true;
				for(MovieChannel movieChan:channels)
					{
					EvChannel ch=movieChan.chan;//rec.getChannel(cName.name);
					EvDecimal frame=ch.closestFrame(curframe);
					EvStack stack=ch.getStack(frame);
					int tz=stack.getClosestPlaneIndex(movieChan.z.doubleValue());
					if(tz<0)
						System.out.println("Error for channel "+movieChan.name+"   "+stack.getDepth()+"  "+movieChan.z+"   ");
					EvImagePlane imload=stack.getPlane(tz);
					if(imload==null)
						{
						batchError("Failure: Could not collect EvImage for ch:"+movieChan.name+" f:"+frame+" z:"+tz);
						allImloadOk=false;
						}
					else
						{
						BufferedImage ji=imload.getPixels(progh).quickReadOnlyAWT();
						if(ji==null)
							{
							batchError("Failure: Could not collect EvPixels for ch:"+movieChan.name+" f:"+frame+" z:"+tz);
							allImloadOk=false;
							}
						else
							mc.add(new MovieChannelImage(ji));
						}
					}
				if(allImloadOk)
					{
					//Combine images
					BufferedImage c=combine(mc, curframe);

					//Start making movie when size of channels known
					if(movieMaker==null)
						movieMaker=mf.getInstance(moviePath, c.getWidth(), c.getHeight(), inputQuality);

					//Encode frame
					movieMaker.addFrame(c);
					}

				//Go to next frame. End if there are no more frames.
				EvDecimal newcurframe=channels.get(0).chan.closestFrameAfter(curframe);
				if(newcurframe.equals(curframe))
					break;
				curframe=newcurframe;
				}

			//Finish encoding
			if(movieMaker!=null)
				movieMaker.done();
			else
				System.out.println("No movie maker to destroy");
			//Normal exit
			batchDone();
			}
		catch (Exception e)
			{
			batchError("Failure: "+e.getMessage());
			e.printStackTrace();
			}
		batchDone();
		}
    
	
	private class MovieChannelImage
		{
		public final BufferedImage im;
		public double scale;
		public MovieChannelImage(BufferedImage im)
			{
			this.im=im;
			}
		}

	
	/**
	 * Put channels together
	 */
	private BufferedImage combine(List<MovieChannelImage> mc, EvDecimal frame)
		{
		int h=0;
		for(MovieChannelImage ch:mc)
			{
			ch.scale=oneW/(double)ch.im.getWidth();
			int th=(int)(ch.im.getHeight()*ch.scale);
			if(h<th)
				h=th;
			}

		//int fontH=g.getFontMetrics().getHeight();
		int fontH=20;
		h+=fontH+2;

		BufferedImage c=new BufferedImage(oneW*mc.size(),h,BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g=(Graphics2D)c.getGraphics();
		for(int i=0;i<mc.size();i++)
			{
			MovieChannelImage ch=mc.get(i);
			
			AffineTransform trans=new AffineTransform();
			trans.translate(oneW*i, 0);
			trans.scale(ch.scale, ch.scale);
			g.drawImage(ch.im, trans, null);
			
			g.setColor(Color.WHITE);
			
			g.drawString(channels.get(i).desc.formatString(channels.get(i).name,frame), oneW*i, h-1);
			}
		


		
		return c;
		}




    
	}
