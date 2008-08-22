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
		new Start().run(new String[]{"bioserv.BioservGUI"});
		}
	}
