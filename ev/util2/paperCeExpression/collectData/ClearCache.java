package util2.paperCeExpression.collectData;

import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;

public class ClearCache
	{

	
	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
		new PaperCeExpressionUtil(); //Get password right away so it doesn't stop later
		
		PaperCeExpressionUtil.removeAllTags();
		
		System.exit(0);
		
		
		}
	
	}
