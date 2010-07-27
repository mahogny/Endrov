/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.movieOutputMencoder;

import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

import endrov.basicWindow.BasicWindow;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.makeMovie.EvMovieMaker;
import endrov.util.EvFileUtil;

/**
 * Interface to Mencoder
 */
public class MencoderMovieMaker implements EvMovieMaker
	{
	private File path;
	//private int w;
	//private int h;
	
	private int curframe=1;

	private File tempFile;

	public MencoderMovieMaker(File path, int w, int h, String quality)
		{
		this.path=path;
		try
			{
			tempFile=File.createTempFile("ev_mencoder", "");
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
		File thisFile=new File(tempFile,EV.pad(curframe,8)+".png");
		curframe++;

		ImageIO.write(im, "png", thisFile);
		//System.out.println(thisFile);
		}

	public void done() throws Exception
		{
		File output=new File(path.toString()+".avi");
		output.delete();
		System.out.println("Output to "+output);
		//System.out.println(""+tempFile);
		
		
		//   mencoder mf://*.png -mf w=800:h=600:fps=5:type=png -ovc lavc     -lavcopts vcodec=mpeg4:mbd=2:trell -oac copy -o output.avi
		
		runUntilQuit(EncodeMencoder.program.toString(),"mf://"+EvFileUtil.getFileFromURL(tempFile.toURI().toURL())+"/*.png",
				"-mf","w=800:h=600:fps=5:type=png","-ovc","lavc","-lavcopts","vcodec=mpeg4:mbd=2:trell","-oac","copy","-o",output.toString());
		
		if(!output.exists() || output.length()==0)
			{
			BasicWindow.showErrorDialog(
					"File was not created due to Mencoder error. Likely missing codec support.\n" +
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
			System.out.print(s+" ");
		System.out.println();		
		
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
