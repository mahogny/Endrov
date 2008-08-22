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
		new Start().run(new String[]{"OSTdaemon.GUI"});
		}
	}
