package endrov.nuc.cmd;
import java.util.*;

import endrov.basicWindow.*;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import endrov.script.*;

/**
 * Rename nucleus
 * @author Johan Henriksson
 */
public class CmdNucren extends Command
	{
	
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Exp e=arg.get(1);
		if(e instanceof ExpVal && ((ExpVal)e).o instanceof String)
			{
			String newName=((ExpVal)e).stringValue();
			if(NucLineage.selectedNuclei.size()==1)
				{
				NucPair nucPair=NucLineage.selectedNuclei.iterator().next();
				NucLineage lin=nucPair.fst();
				if(!lin.renameNucleus(nucPair.snd(), newName))
					throw new Exception("Failed to rename");
				NucLineage.selectedNuclei.remove(nucPair);
				NucLineage.selectedNuclei.add(new NucPair(lin,newName));
				BasicWindow.updateWindows();
				return null;
				}
			else
				throw new Exception("Wrong number of nuclei selected: "+NucLineage.selectedNuclei.size());
			}
		else
			throw new Exception("Incompatible type");
		}
	
	}