package evplugin.makeQT;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;

import evplugin.ev.*;
import evplugin.filter.FilterSeq;
import evplugin.imageset.*;


/**
 * Thread: Encode a quicktime movie
 */
public final class CalcThread extends BatchThread 
	{
	private final Imageset rec;
	
	private final int startFrame, endFrame;
	private final int z;

	private final int oneW;
	private final java.util.List<MovieChannel> channels;
	private final String inputCodec;
	private final String inputQuality;
	
	
	public static class MovieChannel
		{
		public final String name;
		public final FilterSeq fs;
		public final MovieDescString desc;
		public MovieChannel(String nane, FilterSeq fs, String desc)
			{
			this.name=nane;
			this.fs=fs;
			this.desc=new MovieDescString(desc);
			}
		}
	
	public CalcThread(Imageset rec, int startFrame, int endFrame, int z, List<MovieChannel> channelNames, int oneW, String codec, String quality)
		{
		this.rec=rec;
		this.startFrame=startFrame;
		this.endFrame=endFrame;
		this.z=z;
		this.channels=channelNames;
		this.oneW=oneW;
		inputCodec=codec;
		inputQuality=quality;
		}
	
	public String getBatchName()
		{
		return rec.getMetadataName();
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
			for(MovieChannel s:channels)
				if(rec.getChannel(s.name)==null)
					throw new Exception("Missing channels");
			
			//Decide name of movie file
			if(rec.datadir()==null)
				{
				batchLog("Error: imageset does not support a data directory");
				batchDone();
				return;
				}
			String lastpart=rec.getMetadataName()+"-"+channels.get(0).name;
			for(int i=1;i<channels.size();i++)
				lastpart+="_"+channels.get(i).name;
			File moviePath=new File(rec.datadir(),lastpart+".mov");
			
			System.out.println(" "+moviePath);
			
			if(moviePath.exists())
				{
				batchLog("Skipping. Movie already exists");
				batchDone();
				return;
				}

			//Quicktime movie to be made
			QTMovieMaker movieMaker=null;
			
			//For all frames
			int curframe=rec.getChannel(channels.get(0).name).closestFrame(startFrame);
			while(curframe<=endFrame)
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
				Vector<MovieChannelImage> mc=new Vector<MovieChannelImage>();
				boolean allImloadOk=true;
				for(MovieChannel cName:channels)
					{
					Imageset.ChannelImages ch=rec.getChannel(cName.name);
					int frame=ch.closestFrame(curframe);
					int tz=ch.closestZ(frame, z);
					EvImage imload=ch.getImageLoader(frame, tz);
					if(imload==null)
						allImloadOk=false;
					else
						{
						imload=cName.fs.applyReturnImage(imload);
						BufferedImage ji=imload.getJavaImage();
						if(ji==null)
							allImloadOk=false;
						else
							mc.add(new MovieChannelImage(ji, cName.name));
						}
					}
				if(allImloadOk)
					{
					//Combine images
					BufferedImage c=combine(mc, curframe);

					//Start making movie when size of channels known
					if(movieMaker==null)
						movieMaker=new QTMovieMaker(moviePath.getAbsolutePath(), c.getWidth(), c.getHeight(), inputCodec, inputQuality);

					//Encode frame
					movieMaker.addFrame(c);
					}
				else
					batchError("Failure: not all images could be collected");

				//Go to next frame. End if there are no more frames.
				int newcurframe=rec.getChannel(channels.get(0).name).closestFrameAfter(curframe);
				if(newcurframe==curframe)
					break;
				curframe=newcurframe;
				}

			//Finish encoding
			if(movieMaker!=null)
				movieMaker.done();
			else
				System.out.println("No movie maker to destroy");
			//Normal exit
			batchLog("Done");
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
		public BufferedImage im;
		public String name;
		public double scale;
		public MovieChannelImage(BufferedImage im, String name)
			{
			this.im=im; this.name=name; 
			}
		}

	
	/**
	 * Put channels together
	 */
	private BufferedImage combine(Vector<MovieChannelImage> mc, int frame)
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

		BufferedImage c=new BufferedImage(oneW*mc.size(),h,BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g=(Graphics2D)c.getGraphics();
		for(int i=0;i<mc.size();i++)
			{
			MovieChannelImage ch=mc.get(i);
			
			AffineTransform trans=new AffineTransform();
			trans.translate(oneW*i, 0);
			trans.scale(ch.scale, ch.scale);
			g.drawImage(ch.im, trans, null);
			
			g.setColor(Color.WHITE);
			
			g.drawString(
			channels.get(i).desc.decode(rec, channels.get(i).name,frame),
			 oneW*i, h-1);
			/*
			if(i==0)
				g.drawString(ch.name+" ("+frame+")", oneW*i, h-1);
			else
				g.drawString(ch.name, oneW*i, h-1);*/
			}
		
		return c;
		}




    
	}
