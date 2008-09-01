package endrov.nuc.cmd;
import java.util.*;

import endrov.basicWindow.*;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import endrov.script.*;

/**
 * Hide nucleus
 * @author Johan Henriksson
 */
public class CmdNuchide extends Command
	{
	
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		for(NucPair p:NucLineage.selectedNuclei)
			NucLineage.hiddenNuclei.add(p);
		BasicWindow.updateWindows();
		return null;
		}
	
	}
