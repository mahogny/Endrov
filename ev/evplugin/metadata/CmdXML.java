package evplugin.metadata;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.metadata.Metadata;
import evplugin.script.*;

/**
 * Open XML
 * @author Johan Henriksson
 */
public class CmdXML extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Exp e=arg.get(1);
		if(e instanceof ExpVal)
			{
			ExpVal v=(ExpVal)e;

			Metadata m=new XmlMetadata(v.stringValue());
			Metadata.metadata.add(m);
			
			BasicWindow.updateWindows();
			
			//TODO: print error?
			
			return new ExpVal(m);
			}
		else
			throw new Exception("Incompatible type");
		}
	
	}
