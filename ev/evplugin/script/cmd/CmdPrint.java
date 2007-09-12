package evplugin.script.cmd;
import java.util.*;
import evplugin.script.*;
import evplugin.ev.*;

public class CmdPrint extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Object e=Command.expVal(arg.get(1));
		EV.printLog(""+e);
		return null;
		}
	}
