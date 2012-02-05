package util2.paperCeExpression;

import java.io.File;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;

public class ProveDynamicRange
	{

	/**
	 * Assumes movies are already created - then puts them all in the right directory for publication
	 */
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
	
		File finfile=new File("/Volumes/TBU_main06/ost4dgood/BC15177_070605.ost");
	
		EvData data=EvData.loadFile(finfile);
		EvChannel ch=(EvChannel)data.getChild("im").getChild("GFP");
		
		StringBuilder sb=new StringBuilder();
		
		for(EvDecimal f:ch.getFrames())
			{
			double max=0;
			EvStack stack=ch.getStack(f);
			for(int az=0;az<stack.getDepth();az++)
				{
				EvPixels p=stack.getInt(az).getPixels(null).convertToDouble(true);
				for(double val:p.getArrayDouble())
					if(val>max)
						max=val;
				}
			sb.append(f+"\t"+max+"\n");
			System.out.println(f+"\t"+max);
			}
		
		System.out.println(sb.toString());
		
		
		
		
		System.exit(0);
		
		
		}
	
	}
