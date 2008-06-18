package imserv;


//-Dsun.io.serialization.extendedDebugInfo=true

//javac *.java
//rmic HelloImpl
//java -Djava.security.policy=policy HelloImpl
//java HelloClient (run in another window)

//-Djavax.net.ssl.keyStore=testkeys -Djavax.net.ssl.keyStorePassword=passphrase


import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


/**
 * Interface to imserv
 * 
 * @author Johan Henriksson
 *
 */
public class ClientGUI extends JFrame
	{
	public static final long serialVersionUID=0;
	
	public ImservConnection conn;
	
	public JList tagList=new JList(new String[]{"foo","bar"});
	public ImservDataPane pane;
	public JTextField searchField=new JTextField();
	public JButton bHelp=new JButton("Help");
	
	
	private static JComponent borderLR(JComponent left, JComponent center, JComponent right)
		{
		JPanel p=new JPanel(new BorderLayout());
		if(left!=null)   p.add(left,BorderLayout.WEST);
		if(center!=null) p.add(center,BorderLayout.CENTER);
		if(right!=null)  p.add(right,BorderLayout.EAST);
		return p;
		}
	
	
	public ClientGUI(ImservConnection conn)
		{
		this.conn=conn;
		pane=new ImservDataPane(conn);
		
		JPanel right=new JPanel(new BorderLayout());
		right.add(borderLR(null,searchField,bHelp),BorderLayout.SOUTH);
		right.add(pane,BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(new JScrollPane(tagList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.WEST);
		add(right,BorderLayout.CENTER);
		
		
		pack();
		setBounds(0, 0, 300,300);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		
		
		//Update pane with list of objects
		
		}
	
	public static void main(String[] arg)
		{
		//start daemon, convenience
		GUI.main(arg);
		
		
		System.setProperty("javax.net.ssl.keyStore",ClientGUI.class.getResource("imservkeys").getFile());
		System.setProperty("javax.net.ssl.keyStorePassword","passphrase");
		System.setProperty("javax.net.ssl.trustStore",ClientGUI.class.getResource("cacerts").getFile());
		System.setProperty("javax.net.ssl.trustStorePassword","changeit");

		
		
		//daemon: -Djava.security.policy=imserv/policy
		
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
