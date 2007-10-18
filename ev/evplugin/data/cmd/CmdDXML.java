package evplugin.data.cmd;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.data.EvData;
import evplugin.data.EvDataXML;
import evplugin.script.*;

/**
 * Open XML
 * @author Johan Henriksson
 */
public class CmdDXML extends Command
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
