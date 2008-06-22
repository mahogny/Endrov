package evplugin.imagesetImserv.service;

import java.awt.GridLayout;
import java.net.InetAddress;

import javax.swing.JFrame;

public class ClientGUI extends JFrame
	{
	public static final long serialVersionUID=0;
	ImservClientPane pane;
	
	public ClientGUI(ImservConnection conn)
		{
		ImservClientPane pane=new ImservClientPane(conn);
		setLayout(new GridLayout(1,1));
		add(pane);
		pack();
		setBounds(0, 0, 500,400);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		}
	
	
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
			new ClientGUI(ImservConnection.connect(InetAddress.getLocalHost().getHostName(), Daemon.PORT));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		}
	
	}
