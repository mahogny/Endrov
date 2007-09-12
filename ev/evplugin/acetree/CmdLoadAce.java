package evplugin.acetree;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.metadata.MetaObject;
import evplugin.metadata.Metadata;
import evplugin.metadata.XmlMetadata;
import evplugin.script.*;

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
				Metadata m=new XmlMetadata();
				MetaObject ob=a.getMeta();
				m.addMetaObject(ob);
				
				Metadata.metadata.add(m);
				
				BasicWindow.updateWindows();
				return new ExpVal(m); //really m?
				}
			else
				return null;
			//TODO: print error?
			
			}
		else
			throw new Exception("Incompatible type");
		}
	
	}
