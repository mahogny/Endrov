package evplugin.data.cmd;
import java.util.*;

import evplugin.script.*;
import evplugin.basicWindow.*;
import evplugin.data.EvData;

/**
 * List data
 * @author Johan Henriksson
 */
public class CmdDUNL extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Exp e=arg.get(1);
		if(e instanceof ExpVal)
			{
			ExpVal v=(ExpVal)e;

			int id=0;
			if(v.o instanceof Integer)
				id=(Integer)v.o;
			else if(v.o instanceof String)
				id=getByString((String)v.o);
			else
				throw new Exception("Incompatible type");
			EvData.metadata.remove(id);
			if(EvData.selectedMetadataId>id)
				EvData.selectedMetadataId--;
			BasicWindow.updateWindows();
			return null;
			}
		else
			throw new Exception("Incompatible type");
		}
	
	
	private int getByString(String name)
		{
		for(int i=0;i<EvData.metadata.size();i++)
			{
			EvData m=EvData.metadata.get(i);
			if(m!=null && m.getMetadataName().equals(name))
				return i;
			}
		return -1;
		}
	
	}
