/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.script;

import bsh.*;

public class Script
	{
	public Interpreter bsh=new Interpreter();
	
	
	
	public Object eval(String s) throws EvalError
		{
		return bsh.eval(s);
		}
	
	


	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin(){}
	public Script()
		{
		//bsh.setStrictJava(true);
//	bsh.setErr
//	bsh.setOut
		
		bsh.setClassLoader(Script.class.getClassLoader());
		
		}
	}
