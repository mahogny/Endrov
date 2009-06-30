package endrov.makeMovieFFMPEG;

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

	File tempFile;

	public FFMPEGMovieMaker(File path, int w, int h, String quality)
		{
		this.path=path;
		try
			{
			tempFile=File.createTempFile("", "");
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
		}

	public void done() throws Exception
		{
		runUntilQuit(EncodeFFMPEG.program.toString(),"-i","%d.png",path.toString()+".avi");
		// ffmpeg -i %08d.png out.mpg
		// ffmpeg -i %08d.png out.avi
		EvFileUtil.deleteRecursive(tempFile);
		}	
	
	
	public static void runUntilQuit(String... arg)
		{
		try
			{
			Process proc=Runtime.getRuntime().exec(arg);
			BufferedReader os=new BufferedReader(new InputStreamReader(proc.getInputStream()));
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