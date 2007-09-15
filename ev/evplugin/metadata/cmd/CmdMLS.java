package evplugin.metadata.cmd;
import java.util.*;


import evplugin.ev.*;
import evplugin.metadata.*;
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
		Metadata m=Metadata.getSelectedMetadata();
		if(m!=null)
			{
			for(int i:m.metaObject.keySet())
				{
				MetaObject ob=m.metaObject.get(i);
				Log.printLog(""+i+": "+ob.getMetaTypeDesc());
				}
			}
		else
			Log.printLog("No data selected");
		return null;
		}
	
	}
