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
