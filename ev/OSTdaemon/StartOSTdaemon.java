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
