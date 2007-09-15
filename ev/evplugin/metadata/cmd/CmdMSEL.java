package evplugin.metadata.cmd;
import java.util.*;

import evplugin.ev.Log;
import evplugin.metadata.Metadata;
import evplugin.script.*;

/**
 * Select metaobject
 * @author Johan Henriksson
 */
public class CmdMSEL extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Metadata m=Metadata.getSelectedMetadata();
		Exp e=arg.get(1);
		if(e instanceof ExpVal && ((ExpVal)e).o instanceof Integer)
			{
			ExpVal v=(ExpVal)e;

			if(m!=null)
				{
				m.selectedMetaobjectId=(Integer)v.o;
				return new ExpVal(m.getSelectedMetaobject());
				}
			else
				{
				Log.printLog("No data selected");
				return null;
				}

			}
		else
			throw new Exception("Incompatible type");
		}
	
	
	}
