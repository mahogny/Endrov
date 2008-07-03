package util2;

import endrov.ev.EV;
import endrov.imageset.*;
import endrov.imagesetOST.OstImageset;

import java.io.File;
import java.util.*;

public class StripXML
	{
	
	
	/**
	 * Entry point
	 * @param arg Command line arguments
	 */
	public static void main(String[] arg)
		{
		EV.loadPlugins();

		String filename="/Volumes/TBU_xeon01_500GB01/x";
		OstImageset rec=new OstImageset(new File(filename));
		for(String chan:rec.meta.channelMeta.keySet())
			{
			ImagesetMeta.Channel m=rec.meta.channelMeta.get(chan);
			Vector<Integer> bah=new Vector<Integer>();
			for(int frame:m.metaFrame.keySet())
				if(frame<2500 || frame>3005)
					bah.add(frame);
			for(int frame:bah)
				m.metaFrame.remove(frame);
			}
		rec.saveMeta();
		}
	}
