package bioserv;

import java.rmi.server.UnicastRemoteObject;

import javax.swing.JComponent;

import org.jdom.Element;

/**
 * Module extendng bioserv
 * @author Johan Henriksson
 * 
 */
public abstract class BioservModule extends UnicastRemoteObject 
	{
	protected BioservModule() throws Exception 
		{
		super(BioservDaemon.PORT, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory());
		}
	
	public abstract String getBioservModuleName();
	
	public abstract JComponent getBioservModuleSwingComponent(BioservGUI gui);
	
	public abstract void loadConfig(Element e);
	public abstract void saveConfig(Element e);
	
	public abstract void start(BioservDaemon daemon);
	
	}
