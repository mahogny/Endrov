package imserv;


//-Dsun.io.serialization.extendedDebugInfo=true

//javac *.java
//rmic HelloImpl
//java -Djava.security.policy=policy HelloImpl
//java HelloClient (run in another window)

//-Djavax.net.ssl.keyStore=testkeys -Djavax.net.ssl.keyStorePassword=passphrase


import java.awt.GridLayout;

import javax.swing.JFrame;

public class ClientGUI extends JFrame
	{
	public static final long serialVersionUID=0;
	
	public ImservConnection conn;
	
	public ClientGUI(ImservConnection conn)
		{
		this.conn=conn;
		ImservDataPane pane=new ImservDataPane(conn);
		setLayout(new GridLayout(1,1));
		add(pane);
		setBounds(0, 0, 300,300);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		//Update pane with list of objects
		
		}
	
	public static void main(String[] arg)
		{
		
		ImservConnection conn=ImservConnection.connect();
		
		/*
		try 
			{
			DataIF data=conn.imserv.getData("foo");
			data.print();
			} 
		catch (Exception e) 
			{
			System.out.println("HelloClient exception: " + e.getMessage());
			e.printStackTrace();
			}
		*/
		
		new ClientGUI(conn);
		}
	
	
	
	}
