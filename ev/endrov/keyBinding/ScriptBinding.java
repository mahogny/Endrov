package endrov.keyBinding;

import java.awt.event.*;
import java.util.*;
import bsh.*;

import org.jdom.Element;

import endrov.ev.*;
import endrov.script2.*;

public class ScriptBinding
	{
	public static Vector<ScriptBinding> list=new Vector<ScriptBinding>(); 
	
	public static void initPlugin() {}
	static
		{
		EV.personalConfigLoaders.put("scriptBinding",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				ScriptBinding b=new ScriptBinding();
				b.script=e.getAttributeValue("script");
				b.key=KeyBinding.readXML(e);
				list.add(b);
				}
			public void savePersonalConfig(Element root)
				{
				for(ScriptBinding b:list)
					{
					Element e=new Element("scriptBinding");
					b.key.writeXML(e);
					e.setAttribute("script",b.script);
					root.addContent(e);
					}
				}
			});
		}

	
	
	public static boolean runScriptKey(KeyEvent e)
		{
		for(ScriptBinding b:list)
			if(b.key.typed(e))
				{
				try
					{
					Script script=new Script();
					script.eval(b.script);
					//TODO Print to console?
					}
				catch (EvalError e1)
					{
					EvLog.printError("ScriptBinding", e1);
					}
				return true;
				}
		return false;
		}
	
	
	public String script="";
	public KeyBinding key=new KeyBinding("","",0,0);
	}
