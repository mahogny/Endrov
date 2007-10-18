package evplugin.data.cmd;
import java.util.*;

import evplugin.data.EvData;
import evplugin.script.*;

/**
 * List data
 * @author Johan Henriksson
 */
public class CmdDSEL extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Exp e=arg.get(1);
		if(e instanceof ExpVal)
			{
			ExpVal v=(ExpVal)e;

			if(v.o instanceof Integer)
				EvData.selectedMetadataId=(Integer)v.o;
			else if(v.o instanceof String)
				EvData.selectedMetadataId=getByString((String)v.o);
			else
				throw new Exception("Incompatible type");
			return new ExpVal(EvData.getSelectedMetadata());
//			return null;
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
