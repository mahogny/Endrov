package evplugin.imageCorrelation;
import java.util.*;
import evplugin.script.*;
import evplugin.ev.*;

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
