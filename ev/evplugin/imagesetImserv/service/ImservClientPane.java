package evplugin.imagesetImserv.service;


//-Dsun.io.serialization.extendedDebugInfo=true

//javac *.java
//rmic HelloImpl
//java -Djava.security.policy=policy HelloImpl
//java HelloClient (run in another window)

//-Djavax.net.ssl.keyStore=testkeys -Djavax.net.ssl.keyStorePassword=passphrase


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.*;

import evplugin.ev.BrowserControl;
import evplugin.ev.EV;
import evplugin.ev.EvSwingTools;
import evplugin.imagesetImserv.service.TagList.TagListListener;


/**
 * Interface to imserv: a viewer with capability to select and load . Meant to be integrated in 
 * 
 * @author Johan Henriksson
 *
 */
public class ImservClientPane extends JPanel implements ActionListener,TagListListener
	{
	public static final long serialVersionUID=0;
	
	public ImservConnection conn;
	
	//public JList tagList=new JList(new String[]{});
	public DataIconPane pane;
	public JTextField searchField=new JTextField();
	public JButton bHelp=new JButton("Help");
	public JButton bNewTag=new JButton("New Tag");
	public JButton bToTrash=new JButton("=> Trash");
	public JLabel status=new JLabel("");
	private Timer timer=new Timer(1000,this);
	private Date lastUpdate=new Date();
	
	private TagList taglist=new TagList();

	
	ListDescItem theStar=ListDescItem.makeMatchAll();
	
	/**
	 * Constructor
	 */
	public ImservClientPane(ImservConnection conn)
		{
		this.conn=conn;
		pane=new DataIconPane(conn);


		bHelp.addActionListener(this);
		bToTrash.addActionListener(this);
		bNewTag.addActionListener(this);
		searchField.addActionListener(this);
		//tagList.addListSelectionListener(this);
		taglist.addTagListListener(this);
		
		JPanel listBottom=new JPanel(new GridLayout(3,1));
		listBottom.add(bNewTag);
		listBottom.add(bHelp);
		listBottom.add(bToTrash);
		
		JPanel right=new JPanel(new BorderLayout());
		right.add(EvSwingTools.borderLR(status,searchField,null),BorderLayout.SOUTH);
		right.add(pane,BorderLayout.CENTER);

		//JPanel foo=new JPanel(new GridLayout(1,1));
	//	foo.add(table);
		
		JPanel left=new JPanel(new BorderLayout());
//		left.add(new JScrollPane(tagList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
		left.add(new JScrollPane(taglist,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
//		left.add(new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
//		left.add(new JScrollPane(foo,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
//		left.add(table,BorderLayout.CENTER);
		left.add(listBottom,BorderLayout.SOUTH);
		
		setLayout(new BorderLayout());
		add(left,BorderLayout.WEST);
		add(right,BorderLayout.CENTER);

		
		setConnection(conn);
		tagListSelect();
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
			final ArrayList<ListDescItem> list=new ArrayList<ListDescItem>();
			list.add(theStar);
			if(conn!=null)
				{
				for(String s:conn.imserv.getTags())
					list.add(ListDescItem.makeTag(s));
				for(String s:conn.imserv.getObjects())
					list.add(ListDescItem.makeObj(s));
				for(String s:conn.imserv.getChannels())
					list.add(ListDescItem.makeChan(s));
				}
			taglist.setList(list);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	private void setTag(String tag, boolean enable)
		{
		if(!pane.selectedId.isEmpty())
			{
			try
				{
				for(String s:pane.selectedId)
					{
					DataIF data=conn.imserv.getData(s);
					data.setTag(tag, enable);
					}
				
//		  public void setTag(String[] obs, String tag, boolean enable) throws Exception;

//				conn.imserv.setTag(pane.selectedId.toArray(new String[]{}), tag, enable);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			pane.update();
			updateCount();
			}
		else
			JOptionPane.showMessageDialog(this, "No datasets selected");
		}
	
	
	/**
	 * Handle events
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bHelp)
			BrowserControl.displayURL(EV.website+"Organizing_with_ImServ");
		else if(e.getSource()==searchField)
			pane.setFilter(searchField.getText());
		else if(e.getSource()==bNewTag)
			{
			String tag=JOptionPane.showInputDialog(this,"Enter new tag");
			if(tag!=null)
				setTag(tag,true);
			}
		else if(e.getSource()==bToTrash)
			{
			if(JOptionPane.showConfirmDialog(this,  "Do you really want to move the selected datasets to trash?", "Confirm", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
				setTag("trash",true);
			}
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
		
		}
	
	
	
	
	
	
	public void tagListAddRemove(ListDescItem item, boolean toAdd)
		{
		if(item.type==ListDescItem.TAG)
			setTag(item.name,toAdd);
		}


	public void tagListSelect()
		{
		StringBuffer crit=new StringBuffer();
		ListDescItem[] sels=taglist.getSelectedValues();
		
		boolean trashSelected=false;
		for(ListDescItem item:sels)
			if(item.type==ListDescItem.TAG && item.name.equals("trash"))
				trashSelected=true;
		if(!trashSelected)
			crit.append("not tag:trash");
		
		for(ListDescItem item:sels)
			{
			if(crit.length()!=0)
				crit.append(" and ");
			crit.append(item.toString());
			}
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
