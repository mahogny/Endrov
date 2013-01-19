/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.bindingMac;

import java.awt.image.*;
import java.io.*;

import endrov.makeMovie.EvMovieMaker;

/**
 * Interface to quicktime. Encapsulates all shitty commands.
 */
public class QTMovieMaker implements StdQTConstants, EvMovieEncoder
	{	
	/** Available codecs */
	public static final String[] codecs = {"Cinepak", "Animation", "H.263", "Sorenson", "Sorenson 3", "h.264 (MPEG-4)"};
	private static final int[] codecTypes = {StdQTConstants.kCinepakCodecType, StdQTConstants.kAnimationCodecType, StdQTConstants.kH263CodecType, 
			StdQTConstants.kSorensonCodecType, StdQTConstants6.kSorenson3CodecType, 0x6d703476};	//0x53565133, 0x6d703476 (MP4v)
	
	/** Available compression levels */
	public static final String[] qualityStrings = {"Low", "Normal", "High", "Maximum"};
	private static final int[] qualityConstants = {StdQTConstants.codecLowQuality, StdQTConstants.codecNormalQuality, 
			StdQTConstants.codecHighQuality, StdQTConstants.codecMaxQuality};

	private int qualityToInt(String quality) throws Exception
		{
		for (int i=0; i<qualityStrings.length; i++) 
			if (quality.equals(qualityStrings[i]))
				return qualityConstants[i];
		throw new Exception("Quality not found");
		}
	
	private int codecToInt(String codec) throws Exception
		{
		for (int i=0; i<codecs.length; i++) 
			if (codec.equals(codecs[i]))
				return codecTypes[i];
		throw new Exception("Codec not found");
		}

	
	
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
	
	
	private final int movieWidth, movieHeight;
	private final int rate;
	private int timeScale = 600; //units per second
	private final int keyFrameRate = 30;
	
	
	private File tempFile;
	private File finalFile;
	
	/**
	 * Start making a movie
	 */
	public QTMovieMaker(File path, int finalWidth, int finalHeight, String codec, String quality) throws Exception
		{
		//Make sure path is valid
		if(!path.getName().endsWith(".mov"))
			path=new File(path.getParentFile(),path.getName()+".mov");
			
		int codecType=codecToInt(codec);
		int codecQuality=qualityToInt(quality);

		this.movieWidth=finalWidth;
		this.movieHeight=finalHeight;
		
		double fps = 7.0;
		//if (fps<0.1) fps = 0.1;
		//if (fps>100.0) fps = 100.0;
		rate = (int)(timeScale/fps);
		
		//Prepare making a movie
		CodecComponent cc=CodecComponent.bestFidelityCodec;
		QTSession.open();
		System.out.println("Target movie file: "+path);
		
		finalFile=path;
		tempFile=new File(finalFile.getParentFile(),"_temp.mov");
		
		movFile = new QTFile(tempFile);
		movie = Movie.createMovieFile(movFile, kMoviePlayer, createMovieFileDeleteCurFile|createMovieFileDontCreateResFile);
		videoTrack = movie.addTrack (finalWidth, finalHeight, 0);//w,h,z
		videoMedia = new VideoMedia(videoTrack, timeScale);
		videoMedia.beginEdits();
		
		ImageDescription imgDesc2 = new ImageDescription(QDConstants.k32ARGBPixelFormat); //Packed into one java int
		imgDesc2.setWidth(finalWidth);
		imgDesc2.setHeight(finalHeight);
		
		gw = new QDGraphics(imgDesc2, 0);
		bounds = new QDRect(0, 0, finalWidth, finalHeight);
		int rawImageSize = QTImage.getMaxCompressionSize(gw, bounds, gw.getPixMap().getPixelSize(), codecQuality, codecType, cc);
		imageHandle = new QTHandle(rawImageSize, true);
		imageHandle.lock();
		compressedImage = RawEncodedImage.fromQTHandle(imageHandle);
		seq = new CSequence(gw, bounds, gw.getPixMap().getPixelSize(), codecType, cc, codecQuality, codecQuality, keyFrameRate, null, 0);
		imgDesc = seq.getDescription();
		}
	
	
	
	/**
	 * Finish up movie making
	 */
	public void done() throws Exception
		{
		videoMedia.endEdits();
		videoTrack.insertMedia(0, 0, videoMedia.getDuration(), 1);
		
		//Save resource in file
		OpenMovieFile omf = OpenMovieFile.asWrite(movFile);
		movie.addResource(omf, movieInDataForkResID, movFile.getName());
		omf.close(); //my addition, fixes bug on the net
		
		tempFile.renameTo(finalFile);
		
		System.out.println("done movie");
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
			pixelsNativeOrder = new int[intsPerRow*movieHeight];
		if (EndianOrder.isNativeLittleEndian()) 
			{
			//System.out.println("Native little endian");
			int offset1, offset2;
			for (int y=0; y<movieHeight; y++) 
				{
				offset1 = y*movieWidth;
				offset2 = y*intsPerRow;
				for (int x=0; x<movieWidth; x++)
					pixelsNativeOrder[offset2++] = EndianOrder.flipBigEndianToNative32(pixels[offset1++]);
				}
			} 
		else
			{
			//System.out.println("Not native little endian");
			for (int i=0; i<movieHeight; i++)
				System.arraycopy(pixels, i*movieWidth, pixelsNativeOrder, i*intsPerRow, movieWidth);
			}
		pixelData.copyFromArray(0, pixelsNativeOrder, 0, intsPerRow*movieHeight);

		//Add information about frame
		CompressedFrameInfo cfInfo = seq.compressFrame (gw, bounds, codecFlagUpdatePrevious, compressedImage);
		boolean syncSample = cfInfo.getSimilarity()==0; // see developer.apple.com/qa/qtmcc/qtmcc20.html
		videoMedia.addSample(imageHandle, 0, cfInfo.getDataSize(), rate, imgDesc, 1, syncSample?0:mediaSampleNotSync);
		}
	
	
	
	}
