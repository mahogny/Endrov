/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv;

import endrov.starter.Start;

/**
 * Start ImServ
 * @author Johan Henriksson
 */
public class StartImServ
	{
	public static void main(String[] args)
		{
		Start start=new Start();
		start.mainClass="bioserv.BioservGUI";
		start.run(args);
		}
	}
