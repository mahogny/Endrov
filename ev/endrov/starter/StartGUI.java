package endrov.starter;


/**
 * Start Endrov GUI
 * @author Johan Henriksson
 */
public class StartGUI
	{
	public static void main(String[] args)
		{
		if(args.length==0)
			new Start().run(new String[]{"endrov.starter.MW"});
		else
			new Start().run(args);
		}
	}
