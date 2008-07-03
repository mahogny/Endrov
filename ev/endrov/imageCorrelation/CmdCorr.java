package endrov.imageCorrelation;
import java.util.*;

import endrov.ev.*;
import endrov.script.*;

public class CmdCorr extends Command
	{
	public static void initPlugin() {}
	static
		{
		Script.addCommand("corr", new CmdCorr());
		
		}
	
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		
		
		
		Object e=Command.expVal(arg.get(1));
		
		
		
		Log.printLog(""+e);
		return null;
		}
	}
