package evplugin.nuc;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.script.*;

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
