package OSTdaemon;

import endrov.starter.StartGUI;

/**
 * Start OST daemon
 * @author Johan Henriksson
 */
public class StartOSTdaemon
	{
	public static void main(String[] args)
		{
		new StartGUI().run(new String[]{"OSTdaemon.GUI"});
		}
	}
