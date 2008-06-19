package imserv;


//-Dsun.io.serialization.extendedDebugInfo=true

//javac *.java
//rmic HelloImpl
//java -Djava.security.policy=policy HelloImpl
//java HelloClient (run in another window)

//-Djavax.net.ssl.keyStore=testkeys -Djavax.net.ssl.keyStorePassword=passphrase


import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;


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
	
	public JList tagList=new JList(new String[]{"*"});
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

	
	private static class TagListTag
		{
		String tag;
		public TagListTag(String tag){this.tag=tag;};
		public String toString(){return "tag:"+tag;}
		}
	private static class TagListObject
		{
		String tag;
		public TagListObject(String tag){this.tag=tag;};
		public String toString(){return "obj:"+tag;}
		}
	private static class TagListChannel
		{
		String tag;
		public TagListChannel(String tag){this.tag=tag;};
		public String toString(){return "chan:"+tag;}
		}
	
	public void updateTagList()
		{
		try
			{
			final ArrayList<Object> list=new ArrayList<Object>();
			list.add("*");
			for(String s:conn.imserv.getTags())
				list.add(new TagListTag(s));
			for(String s:conn.imserv.getObjects())
				list.add(new TagListObject(s));
			for(String s:conn.imserv.getChannels())
				list.add(new TagListChannel(s));
			
			ListModel lm=new ListModel(){
				public void addListDataListener(ListDataListener l)
					{
					}
				public void removeListDataListener(ListDataListener l)
					{
					}

				public Object getElementAt(int index){return list.get(index);}
				public int getSize(){return list.size();}
			};
			tagList.setModel(lm);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
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
		
		
		updateTagList();
		
		pack();
		setBounds(0, 0, 500,400);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		
		
		//Update pane with list of objects
		
		}
	
	public static void main(String[] arg)
		{
		//start daemon, convenience
		GUI.main(arg);

		try{Thread.sleep(1000);}
		catch (InterruptedException e){}

		
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
