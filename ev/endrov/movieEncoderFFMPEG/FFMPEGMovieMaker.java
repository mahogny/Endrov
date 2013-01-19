/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.movieEncoderFFMPEG;

import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

import endrov.core.log.EvLog;
import endrov.gui.window.EvBasicWindow;
import endrov.movieEncoder.EvMovieEncoder;
import endrov.util.io.EvFileUtil;

/**
 * Interface to FFMPEG
 */
public class FFMPEGMovieMaker implements EvMovieEncoder
	{
	private File path;
	
	private int curframe=1;

	private File tempFile;

	private String quality;
	
	public FFMPEGMovieMaker(File path, int w, int h, String quality)
		{
		this.path=path;
		this.quality=quality;
		try
			{
			tempFile=File.createTempFile("ev_ffmpeg", "");
			tempFile.delete(); //Not that great
			tempFile.mkdirs();
			}
		catch (IOException e)
			{
			EvLog.printError("Couldn't generate movies", e);
			throw new RuntimeException("Couldn't generate movie");
			}
		}
	
	public void addFrame(BufferedImage im) throws Exception
		{
		File thisFile=new File(tempFile,curframe+".png");
		curframe++;

		if(im.getWidth()%2==1 || im.getHeight()%2==1)
			{
			//Must be divisible by 2 or there will be problems later
			BufferedImage newBim=new BufferedImage((im.getWidth()/2)*2, (im.getHeight()/2)*2, BufferedImage.TYPE_3BYTE_BGR);
			newBim.getGraphics().drawImage(im, 0,0,null);
			im=newBim;
			}
		
		ImageIO.write(im, "png", thisFile);
		System.out.println(thisFile);
		}

	public void done() throws Exception
		{
		File output;
		
		output=new File(path.toString()+".avi");
		
		output.delete();
		System.out.println("Output to "+output);
		//System.out.println(""+tempFile);
		
		//ffv1 is FFMPEGs lossless format
		
		
		int keyframeEveryFrame=20;
		
		
		if(quality.equals(EncodeFFMPEG.formatFLV1))	
			runUntilQuit(EncodeFFMPEG.program.toString(),"-i",tempFile+"/"+"%d.png","-vcodec","ffv1","-g",""+keyframeEveryFrame,output.toString());
		else if(quality.equals(EncodeFFMPEG.formatHighQualMp4))
		//	runUntilQuit(EncodeFFMPEG.program.toString(),"-i",tempFile+"/"+"%d.png","-vcodec","mpeg4","-b","7000k","-g",""+keyframeEveryFrame,output.toString());
			runUntilQuit(EncodeFFMPEG.program.toString(),"-i",tempFile+"/"+"%d.png","-vcodec","libx264","-b","7000k","-g",""+keyframeEveryFrame,output.toString());
		
		// ffmpeg -i %08d.png out.mpg
		// ffmpeg -i %08d.png out.avi
		
		if(!output.exists() || output.length()==0)
			{
			EvBasicWindow.showErrorDialog(
					"File was not created due to FFMPEG error. Likely missing codec support.\n" +
					"Intermediate files in "+tempFile);
			}
		else
			EvFileUtil.deleteRecursive(tempFile);
		
			

		/*
		 * -v format
		 * 
		 * 3.14 Which are good parameters for encoding high quality MPEG-4?
'-mbd rd -flags +4mv+aic -trellis 2 -cmp 2 -subcmp 2 -g 300 -pass 1/2', things to try: '-bf 2', '-flags qprd', '-flags mv0', '-flags skiprd'.

3.15 Which are good parameters for encoding high quality MPEG-1/MPEG-2?
'-mbd rd -trellis 2 -cmp 2 -subcmp 2 -g 100 -pass 1/2' but beware the '-g 100' might cause problems with some decoders. Things to try: '-bf 2', '-flags qprd', '-flags mv0', '-flags skiprd. 
		 * 
		 * 
		`-r fps'
	  Set frame rate (Hz value, fraction or abbreviation), (default = 25). 
	  */
		
		
		}	
	
	
	public static void runUntilQuit(String... arg)
		{
		for(String s:arg)
			System.out.println(s);
				
		try
			{
			Process proc=Runtime.getRuntime().exec(arg);
			BufferedReader os=new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			while((line=os.readLine())!=null)
				System.out.println(line);
			while(os.readLine()!=null);
			proc.waitFor();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		
		}
	
	}
