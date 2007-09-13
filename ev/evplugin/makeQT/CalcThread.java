package evplugin.makeQT;

import java.io.*;

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
	
	public CalcThread(Imageset rec, int startFrame, int endFrame, int z)
		{
		this.rec=rec;
		this.startFrame=startFrame;
		this.endFrame=endFrame;
		this.z=z;
		}
	
	public String getBatchName()
		{
		return rec.getMetadataName();
		}
	
	public void run()
		{
		try
			{
			Imageset.ChannelImages ch1=rec.getChannel("DIC");
			Imageset.ChannelImages ch2=rec.getChannel("GFPmax");
			
			if(ch1==null || ch2==null)
				throw new Exception("Missing channels");
			
			//Create temp movie directory
			if(rec.datadir()==null)
				{
				batchLog("Error: imageset does not support a data directory");
				batchDone();
				return;
				}
			File moviePath=new File(rec.datadir(),rec.getMetadataName()+"-"+ch1.getMeta().name+"_"+ch2.getMeta().name+".mov");
			if(moviePath.exists())
				{
				batchLog("Skipping. Movie already exists");
				batchDone();
				return;
				}

			File movieTempDir=new File(rec.datadir(),"tempmovie");
			movieTempDir.mkdirs();

			int movieframe=0;
			
			File firstFile=null;
			
			//For all frames
			int curframe=ch1.closestFrame(startFrame);
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

				int frame1=ch1.closestFrame(curframe);
				int frame2=ch2.closestFrame(curframe);
				int z1=ch1.closestZ(frame1, (ch1.closestZ(frame1, -10000)+ch1.closestZ(frame1,1000000))/2);
				int z2=ch2.closestZ(frame2, z);
				
				//Load image
				ImageLoader imload1=ch1.getImageLoader(frame1, z1);
				ImageLoader imload2=ch2.getImageLoader(frame2, z2);
				if(imload1!=null && imload2!=null)
					{
					String file1=imload1.sourceName();
					String file2=imload2.sourceName();

					Log.printDebug("1: "+file1);
					Log.printDebug("2: "+file2);
					
					//Made movie frame
					File outfile=new File(movieTempDir,EV.pad(movieframe, 8)+".png");
					
					//Invoke 
					if(!outfile.exists())
						makeComboIm(ch1, ch2, file1, file2, outfile.getPath(), ""+curframe);

					//Remember first file for QT
					if(firstFile==null)
						firstFile=outfile;
					
					movieframe++;
					}

				//Go to next frame. End if there are no more frames.
				int newcurframe=ch2.closestFrameAfter(curframe);
				if(newcurframe==curframe)
					break;
				curframe=newcurframe;
				}

			//Run QT
			Process p=Runtime.getRuntime().exec(
					new String[]{"/usr/bin/osascript", 
							(new File("evplugin/makeQT/MakeQT.applescript")).getAbsolutePath(), 
							firstFile.getAbsolutePath(), 
							moviePath.getAbsolutePath()});
			waitProcess(p);
			
			//Delete temp directory
			removeDir(movieTempDir);
//			movieTempDir.delete();
			
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
    
	private static String quote(String s)
		{
		return "\""+s+"\"";
		}
	
	private static String singleQuote(String s)
		{
		return "'"+s+"'";
		}
	
	private static void removeDir(File dir)
		{
		for(File f:dir.listFiles())
			f.delete();
		dir.delete();
		}
	
	private static void waitProcess(Process p) throws IOException
		{
		BufferedReader stdInput = new BufferedReader(new 
        InputStreamReader(p.getInputStream()));
    while ((stdInput.readLine()) != null);
		}
	
	/**
	 * Put two images beside each other
	 */
	private void makeComboIm(Imageset.ChannelImages ch1, Imageset.ChannelImages ch2, String image1, String image2, String out, String framestring) throws IOException
		{
		int w=336;
		int h=254;
		String MAGICK_HOME="/Volumes/TBU_G5_500GB01/applications/ImageMagick-6.3.4";
		String imp= "export MAGICK_HOME="+quote(MAGICK_HOME)+"; export PATH; PATH=\"$MAGICK_HOME/bin:$PATH\"; export DYLD_LIBRARY_PATH=\"$MAGICK_HOME/lib\";";
		String fontpath="/Volumes/TBU_G5_500GB01/fonts/tahoma.ttf";
		
		String exec1=imp+"convert "+quote(image1)+" -normalize "+quote(out);
		waitProcess(Runtime.getRuntime().exec(new String[]{"/bin/bash","-c",exec1}));
		
		String exec2=imp+"montage -background Black -fill White -font "+quote(fontpath)+
		" -pointsize 10 -label "+singleQuote(ch1.getMeta().name+" frame "+framestring)+
		" "+quote(out)+
		" -label "+singleQuote(ch2.getMeta().name)+
		" "+quote(image2)+
		" -geometry "+w+"x"+h+"+2+2 "+quote(out);
		waitProcess(Runtime.getRuntime().exec(new String[]{"/bin/bash","-c",exec2}));
		}
    
	}

/*
private static String doubleQuote(String s)
	{
	return "\""+s.replaceAll("\"", "\\\\\"")+"\"";
	}
*/
