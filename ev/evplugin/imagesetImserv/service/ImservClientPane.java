package evplugin.imagesetImserv.service;


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

import evplugin.ev.BrowserControl;
import evplugin.ev.EV;
import evplugin.ev.EvSwingTools;


/**
 * Interface to imserv: a viewer with capability to select and load . Meant to be integrated in 
 * 
 * @author Johan Henriksson
 *
 */
public class ImservClientPane extends JPanel implements ActionListener, ListSelectionListener
	{
	public static final long serialVersionUID=0;
	
	public ImservConnection conn;
	
	public JList tagList=new JList(new String[]{});
	public DataIconPane pane;
	public JTextField searchField=new JTextField();
	public JButton bHelp=new JButton("Help");
	public JLabel status=new JLabel("");
	private Timer timer=new Timer(1000,this);
	private Date lastUpdate=new Date();


	/**
	 * Constructor
	 */
	public ImservClientPane(ImservConnection conn)
		{
		this.conn=conn;
		pane=new DataIconPane(conn);
		
		bHelp.addActionListener(this);
		searchField.addActionListener(this);
		tagList.addListSelectionListener(this);
		
		JPanel right=new JPanel(new BorderLayout());
		right.add(EvSwingTools.borderLR(status,searchField,bHelp),BorderLayout.SOUTH);
		right.add(pane,BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(new JScrollPane(tagList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.WEST);
		add(right,BorderLayout.CENTER);
		
		setConnection(conn);
		timer.start();
		}
	
	
	public void setConnection(ImservConnection conn)
		{
		this.conn=conn;
		pane.setConn(conn);
		updateTagList();
		updateCount();
		}
	
	
	/**
	 * Update the list of tags by querying the server
	 */
	public void updateTagList()
		{
		try
			{
			final ArrayList<Object> list=new ArrayList<Object>();
			list.add(ListDescItem.makeMatchAll());
			if(conn!=null)
				{
				for(String s:conn.imserv.getTags())
					list.add(ListDescItem.makeTag(s));
				for(String s:conn.imserv.getObjects())
					list.add(ListDescItem.makeObj(s));
				for(String s:conn.imserv.getChannels())
					list.add(ListDescItem.makeChan(s));
				}
			
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
	
	
	/**
	 * Handle events
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bHelp)
			BrowserControl.displayURL(EV.website+"Organizing_with_ImServ");
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
	
	
	/**
	 * Handle selections in tag list
	 */
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
	
	
	
	
	
	
	
	
	
	}
