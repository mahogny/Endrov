package endrov.acetree;
import java.util.*;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.data.EvDataXML;
import endrov.data.EvObject;
import endrov.script.*;

/**
 * Open OST
 * @author Johan Henriksson
 */
public class CmdLoadAce extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Exp e=arg.get(1);
		if(e instanceof ExpVal)
			{
			ExpVal v=(ExpVal)e;

			AceTree a=new AceTree();
			if(a.load(v.stringValue()))
				{
				EvData m=new EvDataXML();
				EvObject ob=a.getMeta();
				m.addMetaObject(ob);
				
				EvData.metadata.add(m);
				
				BasicWindow.updateWindows();
				return new ExpVal(m); //really m?
				}
			else
				return null;
			}
		else
			throw new Exception("Incompatible type");
		}
	
	}
