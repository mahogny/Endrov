package evplugin.metadata.cmd;
import java.util.*;

import evplugin.metadata.Metadata;
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
				Metadata.selectedMetadataId=(Integer)v.o;
			else if(v.o instanceof String)
				Metadata.selectedMetadataId=getByString((String)v.o);
			else
				throw new Exception("Incompatible type");
			return new ExpVal(Metadata.getSelectedMetadata());
//			return null;
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
