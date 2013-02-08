/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.starter;

import java.util.*;
import bsh.*;

import endrov.core.*;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.script.*;

/**
 * Text user interface / console
 * @author Johan Henriksson
 */
public class TUI
	{

	/**
	 * Entry point
	 * @param args Command line arguments
	 */
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());

		EndrovCore.loadPlugins();
		@SuppressWarnings("resource")
		Scanner in=new Scanner(System.in);

		Script script=new Script();
		
		for(;;)
			{

			EndrovCore.userShouldConfirmQuit=false;
			
			System.out.print("EV> ");
			String line=in.nextLine();

			try
				{
				
				
				try
					{
					Object e=script.eval(line);
					if(e==null)
						System.out.println("<>");
					else
						System.out.println(""+e);
					}
				catch (EvalError e)
					{
					System.out.println(e);
					}
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}

		}
	}
