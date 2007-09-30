package util;

import java.util.*;
import java.io.File;

import evplugin.ev.*;
import evplugin.imageset.*;
import evplugin.imagesetOST.OstImageset;
import evplugin.metadata.*;
import evplugin.shell.*;
import evplugin.sliceSignal.*;

/**
 * Go through all imagesets in a directory and run slice/signal
 * @author Johan Henriksson
 */
public class BatchSS
	{
	public static Vector<Shell> getShell(Metadata rec)
		{
		Vector<Shell> out=new Vector<Shell>();
		for(MetaObject ob:rec.metaObject.values())
			if(ob instanceof Shell)
				out.add((Shell)ob);
		return out;
		}
	
	public static void makeGraph(File file)
		{
		System.out.println("Imageset "+file.getPath());
		OstImageset ost=new OstImageset(file.getPath());
		double stripeVar=0.001;
		String svar=""+stripeVar;
		if(svar.startsWith("0."))
			svar="0_"+svar.substring(2);
		int numStripes=50;
		String channel="GFP";
		
		Vector<Shell> shells=getShell(ost);
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
	 * @param args Command line arguments
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
