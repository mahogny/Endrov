package endrov.nuc.cmd;
import java.util.*;

import endrov.nuc.NucRenameDialog;
import endrov.script.*;

/**
 * Rename nucleus
 * @author Johan Henriksson
 */
public class CmdNucrend extends Command
	{
	
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		NucRenameDialog.run(null);
		return null;
		}
	
	}
