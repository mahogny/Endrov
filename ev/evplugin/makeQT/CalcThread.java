package evplugin.makeQT;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;

import quicktime.*;
import quicktime.io.*;
import quicktime.qd.*;
import quicktime.std.*;
import quicktime.std.image.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.*;
import quicktime.util.*;
import java.util.*;

import evplugin.ev.*;
import evplugin.imageset.*;


/**
 * The thread for doing calculations
 */
public final class CalcThread extends BatchThread 
	{
	private final Imageset rec;
	
	private final int startFrame, endFrame;
	private final int z;

	private final int oneW;
	private final Vector<String> channelNames;
	
	public CalcThread(Imageset rec, int startFrame, int endFrame, int z, Vector<String> channelNames, int oneW)
		{
		this.rec=rec;
		this.startFrame=startFrame;
		this.endFrame=endFrame;
		this.z=z;
		this.channelNames=channelNames;
		this.oneW=oneW;
		}
	
	public String getBatchName()
		{
		return rec.getMetadataName();
		}
	
	
	
	
	
	/**
	 * Interface to quicktime. Encapsulates all shitty commands.
	 */
	private class MovieMaker implements StdQTConstants
		{
		static final int KEY_FRAME_RATE = 30;
		static final int TIME_SCALE = 600;
		
		public final String[] codecs = {"Cinepak", "Animation", "H.263", "Sorenson", "Sorenson 3", "MPEG-4"};
		public final int[] codecTypes = {kCinepakCodecType, kAnimationCodecType, kH263CodecType, kSorensonCodecType, 0x53565133, 0x6d703476};
		
		private final String[] qualityStrings = {"Low", "Normal", "High", "Maximum"};
		private final int[] qualityConstants = {codecLowQuality, codecNormalQuality, codecHighQuality, codecMaxQuality};

		private final RawEncodedImage compressedImage;
		private final ImageDescription imgDesc;
		private final Movie movie;
		private final VideoMedia videoMedia;
		private final Track videoTrack;
		private final QTFile movFile;
		private final QDGraphics gw;
		private final CSequence seq;
		private final QDRect bounds;
		private final QTHandle imageHandle;
		private int[] pixelsNativeOrder = null;
		
		private final int finalWidth;
		private final int finalHeight;
		private final int rate;
		private int codecType = kSorensonCodecType;
		private int codecQuality = codecNormalQuality;
		private int timeScale = TIME_SCALE; //units per second
		
		/**
		 * Start making a movie
		 */
		public MovieMaker(String path, int finalWidth, int finalHeight, String codec, String quality) throws Exception
			{
			this.finalWidth=finalWidth;
			this.finalHeight=finalHeight;

			//Choose codec from string description
			for (int i=0; i<codecs.length; i++) 
				if (codec.equals(codecs[i]))
					codecType = codecTypes[i];
			for (int i=0; i<qualityStrings.length; i++) 
				if (quality.equals(qualityStrings[i]))
					codecQuality = qualityConstants[i];
			
			double fps = 7.0;
			//if (fps<0.1) fps = 0.1;
			//if (fps>100.0) fps = 100.0;
			rate = (int)(TIME_SCALE/fps);
			
			//Prepare making a movie
			QTSession.open();
			movFile = new QTFile(new File(path));
			movie = Movie.createMovieFile(movFile, kMoviePlayer, createMovieFileDeleteCurFile|createMovieFileDontCreateResFile);
			videoTrack = movie.addTrack (finalWidth, finalHeight, 0);
			videoMedia = new VideoMedia(videoTrack, timeScale);
			videoMedia.beginEdits();
			ImageDescription imgDesc2 = new ImageDescription(QDConstants.k32ARGBPixelFormat); //Packed into one java int
			imgDesc2.setWidth(finalWidth);
			imgDesc2.setHeight(finalHeight);
			gw = new QDGraphics(imgDesc2, 0);
			bounds = new QDRect (0, 0, finalWidth, finalHeight);
			int rawImageSize = QTImage.getMaxCompressionSize(gw, bounds, gw.getPixMap().getPixelSize(), codecQuality, codecType, CodecComponent.anyCodec);
			imageHandle = new QTHandle(rawImageSize, true);
			imageHandle.lock();
			compressedImage = RawEncodedImage.fromQTHandle(imageHandle);
			seq = new CSequence(gw, bounds, gw.getPixMap().getPixelSize(), codecType, CodecComponent.bestFidelityCodec, 
					codecQuality, codecQuality, KEY_FRAME_RATE, null, 0);
			imgDesc = seq.getDescription();
			}
		
		/**
		 * Finish up movie making
		 */
		public void done() throws Exception
			{
			videoMedia.endEdits();
			videoTrack.insertMedia (0, 0, videoMedia.getDuration(), 1);
			OpenMovieFile omf = OpenMovieFile.asWrite (movFile);
			movie.addResource(omf, movieInDataForkResID, movFile.getName());
			}
		
		/**
		 * Encode another frame
		 */
		public void addFrame(BufferedImage im) throws Exception
			{
			
			//Extract image pixels
			int pixels[] = new int[im.getWidth()*im.getHeight()];
			im.getRaster().getSamples(0,0,im.getWidth(),im.getHeight(),0,pixels);
			for(int i=0;i<pixels.length;i++)
				pixels[i]=pixels[i] | (pixels[i]<<8) | (pixels[i]<<16); //Packed pixel format

			//Fix byte order. Put pixels in pixelData.
			RawEncodedImage pixelData = gw.getPixMap().getPixelData();
			int intsPerRow = pixelData.getRowBytes()/4;
			if (pixelsNativeOrder==null) 
				pixelsNativeOrder = new int[intsPerRow*finalHeight];
			if (EndianOrder.isNativeLittleEndian()) 
				{
				//System.out.println("Native little endian");
				int offset1, offset2;
				for (int y=0; y<finalHeight; y++) 
					{
					offset1 = y*finalWidth;
					offset2 = y*intsPerRow;
					for (int x=0; x<finalWidth; x++)
						pixelsNativeOrder[offset2++] = EndianOrder.flipBigEndianToNative32(pixels[offset1++]);
					}
				} 
			else
				{
				//System.out.println("Not native little endian");
				for (int i=0; i<finalHeight; i++)
					System.arraycopy(pixels, i*finalWidth, pixelsNativeOrder, i*intsPerRow, finalWidth);
				}
			pixelData.copyFromArray(0, pixelsNativeOrder, 0, intsPerRow*finalHeight);

			//Add information about frame
			CompressedFrameInfo cfInfo = seq.compressFrame (gw, bounds, codecFlagUpdatePrevious, compressedImage);
			boolean syncSample = cfInfo.getSimilarity()==0; // see developer.apple.com/qa/qtmcc/qtmcc20.html
			videoMedia.addSample (imageHandle, 0, cfInfo.getDataSize(), rate, imgDesc, 1, syncSample?0:mediaSampleNotSync);
			}
		}

	
	/**
	 * Run batch thread
	 */
	public void run()
		{
		try
			{
			//Check that channel list is ok
			if(channelNames.isEmpty())
				throw new Exception("Missing channels");
			for(String s:channelNames)
				if(rec.getChannel(s)==null)
					throw new Exception("Missing channels");
			
			//Decide name of movie file
			if(rec.datadir()==null)
				{
				batchLog("Error: imageset does not support a data directory");
				batchDone();
				return;
				}
			String lastpart=rec.getMetadataName()+"-"+channelNames.get(0);
			for(int i=1;i<channelNames.size();i++)
				lastpart+="_"+channelNames.get(i);
			File moviePath=new File(rec.datadir(),lastpart+".mov");
			//moviePath=new File("/tmp/out.mov");
			if(moviePath.exists())
				{
				batchLog("Skipping. Movie already exists");
				batchDone();
				return;
				}

			//Quicktime movie to be made
			MovieMaker movieMaker=null;
			
			//For all frames
			int curframe=rec.getChannel(channelNames.get(0)).closestFrame(startFrame);
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
				Vector<MovieChannel> mc=new Vector<MovieChannel>();
				boolean allImloadOk=true;
				for(String cName:channelNames)
					{
					Imageset.ChannelImages ch=rec.getChannel(cName);
					int frame=ch.closestFrame(curframe);
					int tz=ch.closestZ(frame, z);
					EvImage imload=ch.getImageLoader(frame, tz);
					if(imload==null)
						{
						allImloadOk=false;
						return;
						}
					else
						mc.add(new MovieChannel(imload.getJavaImage(), cName));
					}
				if(allImloadOk)
					{
					//Combine images
					BufferedImage c=combine(mc, curframe);

					//Start making movie when size of channels known
					if(movieMaker==null)
						movieMaker=new MovieMaker(moviePath.getAbsolutePath(), c.getWidth(), c.getHeight(), "", "");

					//Encode frame
					movieMaker.addFrame(c);
					}

				//Go to next frame. End if there are no more frames.
				int newcurframe=rec.getChannel(channelNames.get(0)).closestFrameAfter(curframe);
				if(newcurframe==curframe)
					break;
				curframe=newcurframe;
				}

			//Finish encoding
			if(movieMaker!=null)
				movieMaker.done();
			
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
    
	
	private class MovieChannel
		{
		public BufferedImage im;
		public String name;
		public double scale;
		public MovieChannel(BufferedImage im, String name)
			{
			this.im=im; this.name=name;
			}
		}

	
	/**
	 * Put channels together
	 */
	private BufferedImage combine(Vector<MovieChannel> mc, int frame)
		{
		//Need renormalization TODO
		int h=0;
		for(MovieChannel ch:mc)
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
			MovieChannel ch=mc.get(i);
			
			AffineTransform trans=new AffineTransform();
			trans.translate(oneW*i, 0);
			trans.scale(ch.scale, ch.scale);
			g.drawImage(ch.im, trans, null);
			
			g.setColor(Color.WHITE);
			if(i==0)
				g.drawString(ch.name+" ("+frame+")", oneW*i, h-1);
			else
				g.drawString(ch.name, oneW*i, h-1);
			}
		
		return c;
		}
	
	
    
	}
