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
				for(Object o:(Vector<?>)v.o)
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
		NucLineage lin=NucLineage.getSelectedLineage();
		if(lin!=null)
			{
			if(select)
				NucLineage.selectedNuclei.add(new NucPair(lin,e.stringValue()));
			else
				NucLineage.selectedNuclei.remove(new NucPair(lin, e.stringValue()));
			}
		}
	
	}
