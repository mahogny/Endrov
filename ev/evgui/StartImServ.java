package evgui;

/**
 * Start ImServ
 * @author Johan Henriksson
 */
public class StartImServ
	{
	public static void main(String[] args)
		{
		new StartGUI().run(new String[]{"evplugin.imagesetImserv.service.GUI"});
		}
	}
