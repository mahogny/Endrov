package evplugin.imagesetOST;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.data.EvData;
import evplugin.imageset.Imageset;
import evplugin.script.*;

/**
 * Open OST
 * @author Johan Henriksson
 */
public class CmdLoadOST extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Exp e=arg.get(1);
		if(e instanceof ExpVal)
			{
			ExpVal v=(ExpVal)e;

			Imageset rec=new OstImageset(v.stringValue());
			EvData.metadata.add(rec);
			
			BasicWindow.updateWindows();
			
			
			return new ExpVal(rec);
			}
		else
			throw new Exception("Incompatible type");
		}
	
	}
