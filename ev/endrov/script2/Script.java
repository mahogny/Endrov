package endrov.script2;

import bsh.*;

public class Script
	{
	public Interpreter bsh=new Interpreter();
	
	public static void initPlugin(){}
	public Script()
		{
		//bsh.setStrictJava(true);
		
		
		
//	bsh.setErr
//	bsh.setOut
		}
	
	public Object eval(String s) throws EvalError
		{
		return bsh.eval(s);
		}
	
	
	
	}
