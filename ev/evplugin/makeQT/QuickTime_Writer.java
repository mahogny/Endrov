package evplugin.makeQT;

import quicktime.*;
import quicktime.io.*;
import quicktime.qd.*;
import quicktime.std.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.*;
import quicktime.std.image.*;
import quicktime.util.*;
import java.awt.image.*;
import java.io.*;

//This plugin uses QuickTime for Java to save the current stack as a QuickTime movie.
//It is based on the VideoSampleBuilder example from chapter 8 of "QuickTime for Java: 
//A Developer's Notebook" by Chris Adamson (www.oreilly.com/catalog/quicktimejvaadn/).
public class QuickTime_Writer implements StdQTConstants 
	{
	static final int KEY_FRAME_RATE = 30;
	static final int TIME_SCALE = 600;
	
	static String[] codecs = {"Cinepak", "Animation", "H.263", "Sorenson", "Sorenson 3", "MPEG-4"};
	static int[] codecTypes = {kCinepakCodecType, kAnimationCodecType, kH263CodecType, kSorensonCodecType, 0x53565133, 0x6d703476};
	
	static String codec = "Sorenson";
	static String[] qualityStrings = {"Low", "Normal", "High", "Maximum"};
	static int[] qualityConstants = {codecLowQuality, codecNormalQuality, codecHighQuality, codecMaxQuality};
	static String quality = "Normal";

	public static void foo() 
		{
		int codecType = kSorensonCodecType;
		for (int i=0; i<codecs.length; i++) 
			if (codec.equals(codecs[i]))
				codecType = codecTypes[i];
		int codecQuality = codecNormalQuality;
		for (int i=0; i<qualityStrings.length; i++) 
			if (quality.equals(qualityStrings[i]))
				codecQuality = qualityConstants[i];
		
		double fps = 7.0;
		//if (fps<0.1) fps = 0.1;
		//if (fps>100.0) fps = 100.0;
		int rate = (int)(TIME_SCALE/fps);
		
	
		try 
			{
			QTSession.open();
			//writemovie
			writeMovie("/foo.mov", codecType, codecQuality, rate);
			} 
		catch (Exception e) 
			{
			e.printStackTrace();
			} 
		QTSession.close();
		}

	
	
	public static void writeMovie(String path, int codecType, int codecQuality, int rate) throws QTException, IOException 
		{
		int finalWidth=0;
		int finalHeight=0;
		int frames=0;
		int timeScale = TIME_SCALE; // 100 units per second
		
		
		QTFile movFile = new QTFile (new File(path));
		Movie movie = Movie.createMovieFile(movFile, kMoviePlayer, createMovieFileDeleteCurFile|createMovieFileDontCreateResFile);

		Track videoTrack = movie.addTrack (finalWidth, finalHeight, 0);
		VideoMedia videoMedia = new VideoMedia(videoTrack, timeScale);
		videoMedia.beginEdits();
		
		ImageDescription imgDesc2 = new ImageDescription(QDConstants.k32ARGBPixelFormat);
		imgDesc2.setWidth(finalWidth);
		imgDesc2.setHeight(finalHeight);
		
		QDGraphics gw = new QDGraphics(imgDesc2, 0);
		QDRect bounds = new QDRect (0, 0, finalWidth, finalHeight);
		
		int rawImageSize = QTImage.getMaxCompressionSize(gw, bounds, gw.getPixMap().getPixelSize(), codecQuality, codecType, CodecComponent.anyCodec);

		QTHandle imageHandle = new QTHandle (rawImageSize, true);
		imageHandle.lock();
		RawEncodedImage compressedImage = RawEncodedImage.fromQTHandle(imageHandle);
		
		CSequence seq = new CSequence(gw, bounds, gw.getPixMap().getPixelSize(), codecType, CodecComponent.bestFidelityCodec, 
				codecQuality, codecQuality, KEY_FRAME_RATE, null, 0);
		ImageDescription imgDesc = seq.getDescription();
		
		int[] pixelsNativeOrder = null;
		for (int frame=1; frame<=frames; frame++)
			{
		//processor
	
	//		ImageProcessor ip = stack.getProcessor(frame);
//			ip = ip.convertToRGB();
			
			
			//Get image pixels
			BufferedImage im=new BufferedImage(0,0,0);//todo
			int pixels[] = new int[im.getWidth()*im.getHeight()];
			im.getRaster().getSamples(0,0,im.getWidth(),im.getHeight(),0,pixels);
			
			//Fix byte order. Put pixels in pixelData.
			RawEncodedImage pixelData = gw.getPixMap().getPixelData();
			int intsPerRow = pixelData.getRowBytes()/4;
			if (pixelsNativeOrder==null) 
				pixelsNativeOrder = new int[pixels.length];
			if (EndianOrder.isNativeLittleEndian()) 
				{
				int offset1, offset2;
				for (int y=0; y<finalHeight; y++) 
					{
					offset1 = y*finalWidth;
					offset2 = y*intsPerRow;
					for (int x=0; x<finalWidth; x++)
						pixelsNativeOrder[offset2++] = EndianOrder.flipBigEndianToNative32(pixels[offset1++]);
					}
				pixelData.copyFromArray(0, pixelsNativeOrder, 0, intsPerRow*finalHeight);
				} 
			else
				{
				for (int i=0; i<finalHeight; i++)
					System.arraycopy(pixels, i*finalWidth, pixelsNativeOrder, i*intsPerRow, finalWidth);
				pixelData.copyFromArray(0, pixels, 0, intsPerRow*finalHeight);
				}
			
			//Add information about frame
			CompressedFrameInfo cfInfo = seq.compressFrame (gw, bounds, codecFlagUpdatePrevious, compressedImage);
			boolean syncSample = cfInfo.getSimilarity()==0; // see developer.apple.com/qa/qtmcc/qtmcc20.html
			videoMedia.addSample (imageHandle, 0, cfInfo.getDataSize(), rate, imgDesc, 1, syncSample?0:mediaSampleNotSync);
			}
		
		//Done with frames. Finish up movie making
		videoMedia.endEdits();
		videoTrack.insertMedia (0, 0, videoMedia.getDuration(), 1);
		OpenMovieFile omf = OpenMovieFile.asWrite (movFile);
		movie.addResource(omf, movieInDataForkResID, movFile.getName());
		}
		
	
	}
