/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package OSTdaemon;

import endrov.starter.Start;

/**
 * Start OST daemon
 * @author Johan Henriksson
 */
public class StartOSTdaemon
	{
	public static void main(String[] args)
		{
		Start start=new Start();
		start.mainClass="OSTdaemon.GUI";
		start.run(args);
		}
	}
