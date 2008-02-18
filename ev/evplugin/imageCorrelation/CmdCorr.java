package evplugin.imageCorrelation;
import java.util.*;
import evplugin.script.*;
import evplugin.script.cmd.CmdAskString;
import evplugin.script.cmd.CmdDiv;
import evplugin.script.cmd.CmdExit;
import evplugin.script.cmd.CmdMap;
import evplugin.script.cmd.CmdMinus;
import evplugin.script.cmd.CmdMul;
import evplugin.script.cmd.CmdPlus;
import evplugin.script.cmd.CmdPrint;
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
