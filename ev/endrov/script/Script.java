package endrov.script;

import java.util.*;

import endrov.script.cmd.*;
import endrov.script.parser.*;
import java.io.*;

/**
 * Main class of the scripting/console language
 * @author Johan Henriksson
 */
public class Script
	{
	
	public static HashMap<String,Vector<Command>> command=new HashMap<String,Vector<Command>>();
	
	public static void initPlugin() {}
	static
		{
		addCommand("exit", new CmdExit());
		addCommand("quit", new CmdExit());
		addCommand("print", new CmdPrint());
		addCommand("map", new CmdMap());
		addCommand("askstring", new CmdAskString());
		
		addCommand("+", new CmdPlus());
		addCommand("-", new CmdMinus());
		addCommand("*", new CmdMul());
		addCommand("/", new CmdDiv());
		
		//PC
		//MakeParent
		//SelectSample
		//...in oldexec
		}
	

	/**
	 * Add an available command
	 * @param name Name of command
	 * @param c Handler
	 */
	public static void addCommand(String name, Command c)
		{
		Vector<Command> list=command.get(name);
		if(list==null)
			{
			list=new Vector<Command>();
			command.put(name, list);
			}
		list.add(c);
		}

	
	
	/**
	 * Evaluate an expression
	 */
	public static Exp evalExp(String line) throws Exception
		{
		if(line.equals(""))
			return null;
		return evalExp(parseExp(line));
		}

	
	/**
	 * Evaluate an expression
	 */
	public static Exp evalExp(Exp e) throws Exception
		{
		if(e instanceof ExpApp)
			{
			ExpApp app=(ExpApp)e;
			
			//Evaluate all parts
			for(int i=0;i<app.expr.size();i++)
				app.expr.set(i, evalExp(app.expr.get(i)));
			
			//Try to apply function
			if(app.expr.get(0) instanceof ExpSym/* || app.expr.get(0) instanceof ExpVal*/)
				{
				String sym=app.expr.get(0).stringValue();
				Vector<Command> cmds=command.get(sym);
				
				if(cmds==null)
					{
					/*System.out.println("evald "+sym);
					if(app.expr.size()==1)
						return new ExpVal(sym);
					else*/
						throw new Exception("Function not found: "+sym);
					}
				
				//Evaluate function if it is time
				if(cmds.get(0).numArg()==app.expr.size()-1)
					return cmds.get(0).exec(app.expr);
				}
			else if(app.expr.size()==1)
				return app.expr.get(0);
			else
//				return new ExpVal(app.expr.get(0).stringValue());
				throw new Exception("Tried to use non-symbol as function");
			
			return e;
			}
		else
			return e;
		}
	
	
	/**
	 * Parse an expression
	 */
	public static Exp parseExp(String line) throws Exception
		{
		StringReader sr=new StringReader(line);
	  Parser parser = new Parser(sr);
 		return parser.Input();
		}
	

	//Script listener?
	
	
	
	}

