package endrov.nuc.cmd;
import java.util.*;

import endrov.basicWindow.*;
import endrov.nuc.NucLineage;
import endrov.script.*;

/**
 * Select/Deselect nuc
 * @author Johan Henriksson
 */
public class CmdNucda extends Command
	{
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		NucLineage.selectedNuclei.clear();
		BasicWindow.updateWindows();
		return null;
		}
	
	public void select(Exp e) throws Exception
		{
		}
	
	}
