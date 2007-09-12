package evplugin.script.cmd;
import java.util.*;
import evplugin.script.*;

public class CmdMap extends Command
	{
	public int numArg()	{return 2;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		ExpApp func=Command.expApp(arg.get(1));
		Object list=Command.expVal(arg.get(2));
		
		if(list instanceof Vector)
			{
			Vector<Exp> vl=(Vector<Exp>)list; //TODO unchecked cast from Object to vector warning
			Vector<Exp> out=new Vector<Exp>();
			for(int i=0;i<vl.size();i++)
				{
				ExpApp applied=func.apply(vl.get(i));
				Exp appliedOut=Script.evalExp(applied);
				if(appliedOut!=null)
					out.add(appliedOut);
				}
			return new ExpVal(out);
			}
		else
			throw new Exception("Map on non-vector");
		}
	}
