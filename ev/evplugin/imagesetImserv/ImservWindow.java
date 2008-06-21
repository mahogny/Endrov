package evplugin.imagesetImserv;

import evplugin.basicWindow.BasicWindow;
import evplugin.data.EvData;
import evplugin.imagesetImserv.service.*;
import java.awt.GridLayout;
import java.awt.Rectangle;

import org.jdom.Element;


/**
 * Endrov specific ImServ connection window
 * @author Johan Henriksson
 */
public class ImservWindow extends BasicWindow 
	{
	public static final long serialVersionUID=0;
	ImservClientPane pane;
	
	public ImservWindow()
		{
		this(new Rectangle(100,100,600,600));
		}
	
	public ImservWindow(Rectangle bounds)
		{
		System.setProperty("javax.net.ssl.keyStore",ImservClientPane.class.getResource("imservkeys").getFile());
		System.setProperty("javax.net.ssl.keyStorePassword","passphrase");
		System.setProperty("javax.net.ssl.trustStore",ImservClientPane.class.getResource("cacerts").getFile());
		System.setProperty("javax.net.ssl.trustStorePassword","changeit");

		
		
		ImservClientPane pane=new ImservClientPane(null);
		
		setLayout(new GridLayout(1,1));
		add(pane);

		
		setTitleEvWindow("ImServ");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		}

	
	
	public void dataChangedEvent()
		{
		}

	public void loadedFile(EvData data)
		{
		}

	public void windowPersonalSettings(Element e)
		{
		}
	
	
	
	
	
	/*
	public static void main(String[] arg)
		{
		//start daemon, convenience
		GUI.main(arg);

		
		try{Thread.sleep(1000);}
		catch (InterruptedException e){}

		
		System.setProperty("javax.net.ssl.keyStore",ImservClientPane.class.getResource("imservkeys").getFile());
		System.setProperty("javax.net.ssl.keyStorePassword","passphrase");
		System.setProperty("javax.net.ssl.trustStore",ImservClientPane.class.getResource("cacerts").getFile());
		System.setProperty("javax.net.ssl.trustStorePassword","changeit");

		
		
		//daemon: -Djava.security.policy=imserv/policy
		
		
		try
			{
			new ImservWindow(ImservConnection.connect(InetAddress.getLocalHost().getHostName(), Daemon.PORT));
			}
		catch (UnknownHostException e)
			{
			e.printStackTrace();
			}
		
		}*/
	
	}
