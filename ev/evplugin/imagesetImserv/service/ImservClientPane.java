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
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import evplugin.ev.BrowserControl;
import evplugin.ev.EV;
import evplugin.ev.EvSwingTools;
import evplugin.ev.Tuple;
import evplugin.imagesetImserv.service.TagListPane.TagListListener;


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
	
	public DataIconPane pane;
	public JTextField searchField=new JTextField();
	public JButton bHelp=new JButton("Help");
	public JButton bNewTag=new JButton("New Tag");
	public JButton bToTrash=new JButton("=> Trash");
	public JLabel status=new JLabel("");
	private javax.swing.Timer timer=new javax.swing.Timer(1000,this);
	private Date lastUpdate=new Date();
	
	private TagListPane taglist=new TagListPane();

	
	String theStar="*";
	
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
		taglist.addTagListListener(this);
		
		JPanel listBottom=new JPanel(new GridLayout(3,1));
		listBottom.add(bNewTag);
		listBottom.add(bHelp);
		listBottom.add(bToTrash);
		
		JPanel right=new JPanel(new BorderLayout());
		right.add(EvSwingTools.borderLR(status,searchField,null),BorderLayout.SOUTH);
		right.add(pane,BorderLayout.CENTER);

		JPanel left=new JPanel(new BorderLayout());
		left.add(new JScrollPane(taglist,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
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
		revalidate();
		}
	
	
	/**
	 * Update the list of tags by querying the server
	 */
	public void updateTagList()
		{
		try
			{
			final ArrayList<String> list=new ArrayList<String>();
			final Set<String> virtualTag=new HashSet<String>();
			list.add(theStar);
			if(conn!=null)
				{
				//tags, virtualtags
				Tuple<String[], String[]> tup=conn.imserv.getTags();
				for(String tag:tup.fst())
					list.add(tag);
				for(String tag:tup.snd())
					virtualTag.add(tag);
				}
			taglist.setList(list,virtualTag);
			revalidate();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	private void setTag(String tag, String value, boolean enable)
		{
		if(!pane.selectedId.isEmpty())
			{
			try
				{
				for(String s:pane.selectedId)
					{
					DataIF data=conn.imserv.getData(s);
					data.setTag(tag, value,enable);
//					System.out.println("set tag");
					}
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			pane.update();
			updateCount();
			revalidate();
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
			{
			pane.setFilter(searchField.getText());
			revalidate();
			}
		else if(e.getSource()==bNewTag)
			{
			String tag=JOptionPane.showInputDialog(this,"Enter new tag");
			if(tag!=null)
				setTag(tag,null,true);
			revalidate();
			}
		else if(e.getSource()==bToTrash)
			{
			if(JOptionPane.showConfirmDialog(this,  "Do you really want to move the selected datasets to trash?", "Confirm", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
				setTag("trash",null,true);
			revalidate();
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
					revalidate();
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
	
	
	
	
	
	
	public void tagListAddRemove(Tag item, boolean toAdd)
		{
		setTag(item.name,null,toAdd);
		}


	public void tagListSelect()
		{
		StringBuffer crit=new StringBuffer();
		TagExpr[] sels=taglist.getSelectedValues();
		
		boolean trashSelected=false;
		for(TagExpr item:sels)
			if(item.type==TagExpr.TAG && item.name.equals("trash"))
				trashSelected=true;
		if(!trashSelected)
			crit.append("not trash");
		
		for(TagExpr item:sels)
			{
			if(crit.length()!=0)
				crit.append(" and ");
			crit.append(item.toString());
			}
		searchField.setText(crit.toString());
		pane.setFilter(crit.toString());
		updateCount();
		revalidate();
		try{lastUpdate=conn.imserv.getLastUpdate();}
		catch (Exception e1){}
		}


	private void updateCount()
		{
		status.setText(""+pane.obList.size());
		}
	
	
	
	
	
	
	
	
	
	}
