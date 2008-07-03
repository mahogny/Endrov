package endrov.data.cmd;
import java.util.*;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.data.EvDataXML;
import endrov.script.*;

/**
 * Open XML
 * @author Johan Henriksson
 */
public class CmdLoadOSTXML extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Exp e=arg.get(1);
		if(e instanceof ExpVal)
			{
			ExpVal v=(ExpVal)e;

			EvData m=new EvDataXML(v.stringValue());
			EvData.metadata.add(m);
			
			BasicWindow.updateWindows();
			
			//TODO: print error?
			
			return new ExpVal(m);
			}
		else
			throw new Exception("Incompatible type");
		}
	
	}
