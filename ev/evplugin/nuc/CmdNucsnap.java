package evplugin.nuc;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.script.*;


/**
 * Snap to line
 * @author Johan Henriksson
 */
public class CmdNucsnap extends Command
	{
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		for(NucPair nucPair:NucLineage.selectedNuclei)
			{
			NucLineage lin=nucPair.getLeft();
			NucLineage.Nuc n=lin.nuc.get(nucPair.getRight());
			
			NucLineage.NucPos pos=n.pos.get(n.pos.firstKey());
			
			}
		BasicWindow.updateWindows();
		return null;
		}	
	}
