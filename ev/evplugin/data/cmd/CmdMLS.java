package evplugin.data.cmd;
import java.util.*;


import evplugin.data.*;
import evplugin.ev.*;
import evplugin.script.*;

/**
 * List metaobjects
 * 
 * @author Johan Henriksson
 */
public class CmdMLS extends Command
	{
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		EvData m=EvData.getSelectedMetadata();
		if(m!=null)
			{
			for(String i:m.metaObject.keySet())
				{
				EvObject ob=m.metaObject.get(i);
				Log.printLog(i+": "+ob.getMetaTypeDesc());
				}
			}
		else
			Log.printLog("No data selected");
		return null;
		}
	
	}
