package util2.paperCeExpression.collectData;

import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;

public class ClearCache
	{

	
	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
		new PaperCeExpressionUtil(); //Get password right away so it doesn't stop later
		
		PaperCeExpressionUtil.removeAllTags();
		
		System.exit(0);
		
		
		}
	
	}
