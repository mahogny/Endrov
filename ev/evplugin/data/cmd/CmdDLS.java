package evplugin.data.cmd;
import java.util.*;

import evplugin.data.EvData;
import evplugin.ev.Log;
import evplugin.script.*;

/**
 * List metadata
 * @author Johan Henriksson
 */
public class CmdDLS extends Command
	{
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		for(int i=0;i<EvData.metadata.size();i++)
			Log.printLog(""+i+": "+EvData.metadata.get(i).getMetadataName());
		return null;
		}
	
	}
