package util;

//delete once all conversion done

import java.io.*;

public class FixOST15
	{

	public static void main(String[] arg)
		{
		File start=new File("/Volumes/TBU_xeon01_500GB01/akrams_VWB/");
		
		for(File imagesetFile:start.listFiles())
			if(imagesetFile.isDirectory())
				{
				System.out.println("Currently in "+imagesetFile.getPath());
				for(File channelFile:imagesetFile.listFiles())
					if(!channelFile.getName().endsWith("data") && channelFile.isDirectory())
						{
						System.out.println("	Currently2 in "+channelFile.getPath()+" with "+channelFile.listFiles().length+" files");
						String imagesetName=channelFile.getName();
						imagesetName=imagesetName.substring(imagesetName.lastIndexOf('-')+1);
						
						
						for(File frameFile:channelFile.listFiles())
							{
							if(frameFile.isDirectory() && frameFile.getName().indexOf('-')==-1)
								{
								//rename
								String framefileName=frameFile.getName();
								String framenum=framefileName.substring(framefileName.length()-8);
								
								
								
								File newFrameFile=new File(channelFile, imagesetName+"-"+framenum);
								System.out.println("		Rename "+frameFile.getPath()+" to "+newFrameFile.getPath());
								frameFile.renameTo(newFrameFile);
								}
							}
						}
				}
		}
	}
