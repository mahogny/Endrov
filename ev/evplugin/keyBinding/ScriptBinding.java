package evplugin.keyBinding;

import java.awt.event.*;
import java.util.*;
import evplugin.script.*;
import evplugin.ev.*;

public class ScriptBinding
	{
	public static Vector<ScriptBinding> list=new Vector<ScriptBinding>(); 
	
	
	public static boolean runScriptKey(KeyEvent e)
		{
		for(ScriptBinding b:list)
			if(b.key.typed(e))
				{
				try
					{
					Script.evalExp(b.script);
					}
				catch (Exception e1)
					{
					Log.printError("ScriptBinding", e1);
					}
				return true;
				}
		return false;
		}
	
	
	static
	{
	ScriptBinding b=new ScriptBinding();
	list.add(b);
	}
	
	public String script="";
	public KeyBinding key=new KeyBinding("","",0,0);
	}
