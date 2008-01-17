package evplugin.nuc;
import java.util.*;

import evplugin.script.*;

/**
 * Rename nucleus
 * @author Johan Henriksson
 */
public class CmdNucrend extends Command
	{
	
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		RenameDialog.run(null);
		return null;
		}
	
	}
