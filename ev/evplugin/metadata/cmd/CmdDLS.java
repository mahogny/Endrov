package evplugin.metadata.cmd;
import java.util.*;

import evplugin.ev.Log;
import evplugin.metadata.Metadata;
import evplugin.script.*;

/**
 * List data
 * @author Johan Henriksson
 */
public class CmdDLS extends Command
	{
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		for(int i=0;i<Metadata.metadata.size();i++)
			Log.printLog(""+i+": "+Metadata.metadata.get(i).getMetadataName());
		return null;
		}
	
	}
