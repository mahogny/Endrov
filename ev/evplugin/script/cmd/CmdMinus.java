package evplugin.script.cmd;
import java.util.*;
import evplugin.script.*;

public class CmdMinus extends Command
	{
	public int numArg()	{return 2;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Exp a=arg.get(1);
		Exp b=arg.get(2);
		if(allNumber(a, b))
			{
			if(anyDouble(a, b))
				return new ExpVal(expDouble(a) - expDouble(b));
			else
				return new ExpVal(expInteger(a) - expInteger(b));
			}
		else
			throw new Exception("Incompatible types");
		}
	}
