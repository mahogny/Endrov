package endrov.starter;

import endrov.starter.Start;

/**
 * Start lw C.E viewer
 * @author Johan Henriksson
 */
public class StartCEviewer
	{
	public static void main(String[] args)
		{
		Start start=new Start();
		start.mainClass="endrov.starter.CEviewer";
		start.run(args);
		}
	}
