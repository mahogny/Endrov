package evplugin.metadata.cmd;
import java.util.*;

import evplugin.metadata.Metadata;
import evplugin.script.*;
import evplugin.basicWindow.*;

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
			Metadata.metadata.remove(id);
			if(Metadata.selectedMetadataId>id)
				Metadata.selectedMetadataId--;
			BasicWindow.updateWindows();
			return null;
			}
		else
			throw new Exception("Incompatible type");
		}
	
	
	private int getByString(String name)
		{
		for(int i=0;i<Metadata.metadata.size();i++)
			{
			Metadata m=Metadata.metadata.get(i);
			if(m!=null && m.getMetadataName().equals(name))
				return i;
			}
		return -1;
		}
	
	}
