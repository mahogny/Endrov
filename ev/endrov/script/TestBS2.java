/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.script;

import bsh.*;

import java.util.*;


public class TestBS2
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		Interpreter bsh = new Interpreter();

    try
			{
			// Evaluate statements and expressions
			bsh.eval("foo=Math.sin(0.5)");
			bsh.eval("bar=foo*5; bar=Math.cos(bar);");
			bsh.eval("for(i=0; i<10; i++) { print(\"hello\"); }");
			// same as above using java syntax and apis only
			bsh.eval("for(int i=0; i<10; i++) { System.out.println(\"hello\"); }");

			// Source from files or streams
			//bsh.source("myscript.bsh");  // or bsh.eval("source(\"myscript.bsh\")");

			// Use set() and get() to pass objects in and out of variables
			bsh.set( "date", new Date() );
			Date date = (Date)bsh.get( "date" );
			// This would also work:
			//Date date = (Date)bsh.eval( "date" );

			bsh.eval("year = date.getYear()");
			Integer year = (Integer)bsh.get("year");  // primitives use wrappers

			System.out.println(""+date+" "+year);
			
			// With Java1.3+ scripts can implement arbitrary interfaces...
			// Script an awt event handler (or source it from a file, more likely)
			bsh.eval( "actionPerformed( e ) { print( e ); }");
			// Get a reference to the script object (implementing the interface)
//			ActionListener scriptedHandler = 
//			        (ActionListener)bsh.eval("return (ActionListener)this");
			// Use the scripted event handler normally...
			//new JButton.addActionListener( script );
			}
		catch (EvalError e)
			{
			e.printStackTrace();
			}
    
    
		}

	}
