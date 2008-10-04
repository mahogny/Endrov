package bioserv;

import java.io.File;



/**
 * ImServ server-side TUI
 * 
 * @author Johan Henriksson
 */
public class BioservTUI implements BioservDaemon.DaemonListener
	{
	static final long serialVersionUID=0;
	BioservDaemon daemon=new BioservDaemon();
	
	/**
	 * Construct GUI
	 */
	public BioservTUI(File config)
		{
		if(config!=null)
			Config.configfile=config;
		
		//Load configuration
		Config.readConfig(daemon);

		//Start up daemon
		daemon.addListener(this);
//		daemon.start();
		
		}
	
	
	public void log(final String s)
		{
		System.out.println(s);
		}
	
	

	
	/**
	 * Callback: session list updated
	 */
	public void sessionListUpdated()
		{
		}

	
	







	public static void main(String args[]) 
		{
		BioservTUI ui=new BioservTUI(null);
		ui.daemon.start();
		}
	}
