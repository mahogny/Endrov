package evplugin.keyBinding;

import java.util.*;

public class ScriptBinding
	{
	public static Vector<ScriptBinding> list=new Vector<ScriptBinding>(); 
	
	
	static
	{
	ScriptBinding b=new ScriptBinding();
	list.add(b);
	}
	
	public String script="";
	public KeyBinding key=new KeyBinding("","",0,0);
	}
