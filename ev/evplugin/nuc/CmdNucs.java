package evplugin.nuc;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.script.*;

/**
 * Select/Deselect nuc
 * @author Johan Henriksson
 */
public class CmdNucs extends Command
	{
	public final boolean reset, select;
	
	public CmdNucs(boolean reset, boolean select)
		{
		this.reset=reset;
		this.select=select;
		}
	
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		if(this.reset)
			NucLineage.selectedNuclei.clear();
		
		Exp e=arg.get(1);
		if(e instanceof ExpVal)
			{
			ExpVal v=(ExpVal)e;
			if(v.o instanceof Vector)
				for(Object o:(Vector)v.o) //TODO vector should be parameterized
					select((Exp)o);
			else
				select(e);
			BasicWindow.updateWindows();
			return null;
			}
		else
			throw new Exception("Incompatible type");
		}
	
	public void select(Exp e) throws Exception
		{
		if(select)
			NucLineage.selectedNuclei.add(e.stringValue());
		else
			NucLineage.selectedNuclei.remove(e.stringValue());
		}
	
	}
