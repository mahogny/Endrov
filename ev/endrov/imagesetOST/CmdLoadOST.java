package endrov.imagesetOST;
import java.io.File;
import java.util.*;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.imageset.Imageset;
import endrov.script.*;

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

			Imageset rec=new OstImageset(new File(v.stringValue()));
			EvData.metadata.add(rec);
			
			BasicWindow.updateWindows();
			
			
			return new ExpVal(rec);
			}
		else
			throw new Exception("Incompatible type");
		}
	
	}
