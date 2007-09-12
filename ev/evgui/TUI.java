package evgui;

import java.util.*;

import evplugin.ev.*;
import evplugin.script.*;

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
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			}

		}
	}
