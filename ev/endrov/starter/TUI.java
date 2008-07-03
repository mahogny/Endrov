package endrov.starter;

import java.util.*;

import endrov.ev.*;
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
		Log.listeners.add(new StdoutLog());

		EV.loadPlugins();
		Scanner in=new Scanner(System.in);

		for(;;)
			{

			EV.confirmQuit=false;
			
			System.out.print("EV> ");
			String line=in.nextLine();

			try
				{
				Exp e=Script.evalExp(line);
				
				if(e==null)
					System.out.println("<>");
				else
					System.out.println(""+e);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}

		}
	}
