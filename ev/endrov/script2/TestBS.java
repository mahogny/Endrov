/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.script2;

import bsh.*;

import java.util.*;

//BS docs: functions are fast, only parsed once
//so for scripting, declare a function. arguments obvious.
//mixes easily with java compiler API

//To document: how to load files using command line
// * how to rename a nuc. how to hide and show. 
// snap to line command
//replace: AskString. system.out.println.
//remove from site: acetree

public class TestBS
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		Interpreter bsh = new Interpreter();
		bsh.setStrictJava(true);
		
		
		
//		bsh.setErr
//		bsh.setOut
		
    try
			{
			// Evaluate statements and expressions
			bsh.eval("double foo=Math.sin(0.5)");
			bsh.eval("double bar=foo*5; bar=Math.cos(bar);");
			// same as above using java syntax and apis only
			bsh.eval("for(int i=0; i<10; i++) { System.out.println(\"hello\"); }");

			// Use set() and get() to pass objects in and out of variables
			bsh.set( "date", new Date() );
			Date date = (Date)bsh.get( "date" );
			// This would also work:
			//Date date = (Date)bsh.eval( "date" );

			bsh.eval("int year = date.getYear()");
			Integer year = (Integer)bsh.get("year");  // primitives use wrappers

			System.out.println(""+date+" "+year);
			
			// With Java1.3+ scripts can implement arbitrary interfaces...
			// Script an awt event handler (or source it from a file, more likely)
			bsh.eval( "public void actionPerformed(ActionEvent e ) { System.out.print( e ); }");
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
