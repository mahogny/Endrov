package util2.misc;

import java.util.*;
import java.io.File;

import endrov.data.*;
import endrov.ev.*;
import endrov.imagesetOST.OstImageset;
import endrov.shell.*;
import endrov.sliceSignal.*;

/**
 * Go through all imagesets in a directory and run slice/signal
 * @author Johan Henriksson
 */
public class BatchSS
	{
	public static List<Shell> getShell(EvData rec)
		{
		return rec.getObjects(Shell.class);
		}
	
	public static void makeGraph(File file)
		{
		System.out.println("Imageset "+file.getPath());
		OstImageset ost=new OstImageset(file);
		double stripeVar=0.001;
		String svar=""+stripeVar;
		if(svar.startsWith("0."))
			svar="0_"+svar.substring(2);
		int numStripes=50;
		String channel="GFP";
		
		List<Shell> shells=getShell(ost);
		for(int i=0;i<shells.size();i++)
			{
			File outfile=new File(ost.datadir(),ost.getMetadataName()+"-"+numStripes+"sl"+svar+"v."+i+".ss.txt");
			String signalFilename=outfile.getAbsolutePath();
			if(!outfile.exists())
				{
				BatchThread c=new CalcThread(ost, shells.get(i), stripeVar, numStripes, channel, 0, 100000, signalFilename);
				new CompleteBatch(c);
				}
			else
				System.out.println("Skipping "+i);
			}
		
		
		}
	
	/**
	 * Entry point
	 * @param arg Command line arguments
	 */
	public static void main(String[] arg)
		{
		EV.loadPlugins();

		if(arg.length==0)
			arg=new String[]{"/Volumes/TBU_xeon01_500GB01/final_recordings/"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					makeGraph(file);
		}
	}
