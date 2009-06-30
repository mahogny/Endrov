package endrov.movieOutputFFMPEG;

import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

import endrov.ev.EvLog;
import endrov.makeMovie.EvMovieMaker;
import endrov.util.EvFileUtil;

/**
 * Interface to FFMPEG
 */
public class FFMPEGMovieMaker implements EvMovieMaker
	{
	private File path;
	//private int w;
	//private int h;
	
	private int curframe=1;

	private File tempFile;

	public FFMPEGMovieMaker(File path, int w, int h, String quality)
		{
		this.path=path;
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

		ImageIO.write(im, "png", thisFile);
		System.out.println(thisFile);
		}

	public void done() throws Exception
		{
		File output=new File(path.toString()+".avi");
		output.delete();
		System.out.println("Output to "+output);
		//System.out.println(""+tempFile);
		runUntilQuit(EncodeFFMPEG.program.toString(),"-i",tempFile+"/"+"%d.png",output.toString());
		// ffmpeg -i %08d.png out.mpg
		// ffmpeg -i %08d.png out.avi
		EvFileUtil.deleteRecursive(tempFile);
		

		/*
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
			//String line;
			//while((line=os.readLine())!=null)
				//System.out.println(line);
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