package util2.paperCeExpression.compare;

import java.io.File;

import util2.paperCeExpression.collectData.PaperCeExpressionUtil;
import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;

public class DebugMakeGraph
	{

	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
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
