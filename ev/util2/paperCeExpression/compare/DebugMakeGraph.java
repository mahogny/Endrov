package util2.paperCeExpression.compare;

import java.io.File;

import util2.paperCeExpression.collectData.PaperCeExpressionUtil;
import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;

public class DebugMakeGraph
	{

	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		new PaperCeExpressionUtil(); //Get password right away so it doesn't stop later

		for(String s:args)
			{
			File in=new File(s);
			EvData data=EvData.loadFile(in);
			String chanName="GFP";
			CompareAll.doGraphsFor(in, data, chanName);
			}
		
		}
	}
