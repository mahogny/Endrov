package endrov.script2;

import bsh.*;

public class Script
	{
	Interpreter bsh=new Interpreter();
	
	public Script()
		{
		bsh.setStrictJava(true);
		
		
		
//	bsh.setErr
//	bsh.setOut
		}
	
	public Object eval(String s) throws EvalError
		{
		return bsh.eval(s);
		}
	
	}
