/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.starter;


/**
 * Start Endrov GUI
 * @author Johan Henriksson
 */
public class StartGUI
	{
	public static void main(String[] args)
		{
		Start start=new Start();
		start.mainClass="endrov.starter.MW";
		start.run(args);
		}
	}
