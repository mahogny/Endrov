package imserv;


//-Dsun.io.serialization.extendedDebugInfo=true

//javac *.java
//rmic HelloImpl
//java -Djava.security.policy=policy HelloImpl
//java HelloClient (run in another window)

//-Djavax.net.ssl.keyStore=testkeys -Djavax.net.ssl.keyStorePassword=passphrase


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.*;


/**
 * Interface to imserv
 * 
 * @author Johan Henriksson
 *
 */
public class ClientGUI extends JFrame implements ActionListener, ListSelectionListener
	{
	public static final long serialVersionUID=0;
	
	public ImservConnection conn;
	
	public JList tagList=new JList(new String[]{});
	public ImservDataPane pane;
	public JTextField searchField=new JTextField();
	public JButton bHelp=new JButton("Help");
	public JLabel status=new JLabel("");
	private Timer timer=new Timer(1000,this);
	private Date lastUpdate=new Date();

	
	private static JComponent borderLR(JComponent left, JComponent center, JComponent right)
		{
		JPanel p=new JPanel(new BorderLayout());
		if(left!=null)   p.add(left,BorderLayout.WEST);
		if(center!=null) p.add(center,BorderLayout.CENTER);
		if(right!=null)  p.add(right,BorderLayout.EAST);
		return p;
		}

	
	
	
	public void updateTagList()
		{
		try
			{
			final ArrayList<Object> list=new ArrayList<Object>();
			list.add(ListDescItem.makeMatchAll());
			for(String s:conn.imserv.getTags())
				list.add(ListDescItem.makeTag(s));
			for(String s:conn.imserv.getObjects())
				list.add(ListDescItem.makeObj(s));
			for(String s:conn.imserv.getChannels())
				list.add(ListDescItem.makeChan(s));
			
			ListModel lm=new ListModel(){
				public void addListDataListener(ListDataListener l){}
				public void removeListDataListener(ListDataListener l){}
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
	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==searchField)
			pane.setFilter(searchField.getText());
		else if(e.getSource()==timer)
			{
			try
				{
				Date lastUpdate=conn.imserv.getLastUpdate();
				if(!lastUpdate.equals(this.lastUpdate))
					{
					this.lastUpdate=lastUpdate;
					updateTagList();
					pane.update();
					updateCount();
					}
				}
			catch (Exception e1){}
			}
		}
	
	
	
	public void valueChanged(ListSelectionEvent e)
		{
		StringBuffer crit=new StringBuffer();
		Object[] sels=tagList.getSelectedValues();
		for(Object sel:sels)
			{
			ListDescItem item=(ListDescItem)sel;
			if(crit.length()!=0)
				crit.append(" and ");
			crit.append(item.toString());
			}
		if(sels.length==0)
			crit.append("*");
		searchField.setText(crit.toString());
		pane.setFilter(crit.toString());
		updateCount();
		try{lastUpdate=conn.imserv.getLastUpdate();}
		catch (Exception e1){}
		}
	
	
	
	
	private void updateCount()
		{
		status.setText(""+pane.obList.size());
		}
	
	
	
	public ClientGUI(ImservConnection conn)
		{
		this.conn=conn;
		pane=new ImservDataPane(conn);
		
		searchField.addActionListener(this);
		tagList.addListSelectionListener(this);
		
		
		JPanel right=new JPanel(new BorderLayout());
		right.add(borderLR(status,searchField,bHelp),BorderLayout.SOUTH);
		right.add(pane,BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(new JScrollPane(tagList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.WEST);
		add(right,BorderLayout.CENTER);
		
		
		updateTagList();
		updateCount();
		
		pack();
		setBounds(0, 0, 500,400);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		timer.start();
		
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
