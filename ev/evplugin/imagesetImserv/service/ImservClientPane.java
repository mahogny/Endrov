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

import evplugin.basicWindow.BasicWindow;
import evplugin.ev.BrowserControl;
import evplugin.ev.EV;
import evplugin.ev.EvSwingTools;
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
	
	private static ImageIcon iconSearchAttr=new ImageIcon(ImservClientPane.class.getResource("iconSearchAttr.png"));
	private static ImageIcon iconNewTag=new ImageIcon(ImservClientPane.class.getResource("iconNewTag.png"));

	
	public ImservConnection conn;
	
	public DataIconPane pane;
	public JTextField searchField=new JTextField();
	public JButton bHelp=new JButton(BasicWindow.getIconHelp());
	public JButton bNewTag=new JButton(iconNewTag);
	public JButton bNewAttrSearch=new JButton(iconSearchAttr);
	public JButton bToTrash=new JButton(BasicWindow.getIconTrash());
	public JLabel status=new JLabel("");
	private javax.swing.Timer timer=new javax.swing.Timer(1000,this);
	private Date lastUpdate=new Date();
	
	private TagListPane taglist=new TagListPane();

	private JPanel attrPanes=new JPanel(new GridLayout(1,0));
	private Vector<AttrPane> attrPanesList=new Vector<AttrPane>();

	private Map<String,AttrEditPane> attrEditList=new TreeMap<String, AttrEditPane>();
	private JPanel attrEditPane=new JPanel();

	
	private void addAttrPanel()
		{
		attrPanesList.add(new AttrPane());
		updateTagList();
		attrPanelLayout();
		}
	private void attrPanelLayout()
		{
		attrPanes.removeAll();
		attrPanes.setLayout(new GridLayout(attrPanesList.size(),1));
		for(AttrPane p:attrPanesList)
			attrPanes.add(p);
		attrPanes.revalidate();
		}
	
	/**
	 * Constructor
	 */
	public ImservClientPane(ImservConnection conn)
		{
		this.conn=conn;
		pane=new DataIconPane(conn);

		addAttrPanel();

		bHelp.setToolTipText("Help");
		bToTrash.setToolTipText("Mark as trash");
		bNewAttrSearch.setToolTipText("Add attribute search field");
		bNewTag.setToolTipText("Add new tag to selected");
		
		bHelp.addActionListener(this);
		bToTrash.addActionListener(this);
		bNewTag.addActionListener(this);
		bNewAttrSearch.addActionListener(this);
		searchField.addActionListener(this);
		taglist.addTagListListener(this);
		
		JPanel listBottom=new JPanel(new GridLayout(1,4));
		listBottom.add(bNewTag);
		listBottom.add(bNewAttrSearch);
		listBottom.add(bHelp);
		listBottom.add(bToTrash);
		

		JPanel rightBottom=new JPanel(new BorderLayout());
		rightBottom.add(attrEditPane,BorderLayout.CENTER);
		rightBottom.add(EvSwingTools.borderLR(status,searchField,null),BorderLayout.SOUTH);
		
		JPanel right=new JPanel(new BorderLayout());
		right.add(rightBottom,BorderLayout.SOUTH);
		right.add(pane,BorderLayout.CENTER);
		
		JPanel leftBottom=new JPanel(new BorderLayout());
		leftBottom.add(attrPanes,BorderLayout.CENTER);
		leftBottom.add(listBottom,BorderLayout.SOUTH);
		
		JPanel left=new JPanel(new BorderLayout());
		left.add(new JScrollPane(taglist,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
		left.add(leftBottom,BorderLayout.SOUTH);
		
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
			final LinkedList<String> list=new LinkedList<String>();
			if(conn!=null)
				for(String tag:conn.imserv.getTags())
					list.add(tag);
			List<String> nostarlist=new ArrayList<String>();
			nostarlist.add("");
			nostarlist.addAll(list);
			
			for(AttrPane p:attrPanesList)
				p.updateTagList(nostarlist);

			list.addFirst("*");
			taglist.setList(list);

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
		else if(e.getSource()==bNewAttrSearch)
			addAttrPanel();
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
	
	
	
	
	
	
	public void tagListAddRemove(String item, boolean toAdd)
		{
		setTag(item,null,toAdd);
		}


	public void tagListSelect()
		{
		StringBuffer crit=new StringBuffer();
		Set<String> sels=taglist.getSelectedValues();
		
		boolean trashSelected=sels.contains("trash");
		if(!trashSelected)
			crit.append("not trash");
		
		for(String item:sels)
			{
			if(crit.length()!=0)
				crit.append(" and ");
			crit.append(item.toString());
			}
		for(AttrPane p:attrPanesList)
			{
			String name=(String)p.cTag.getSelectedItem();
			if(!name.equals(""))
				{
				String value=p.tValue.getText();
				crit.append(" and "+name+"=.*"+value+".*");
				}
			}
		searchField.setText(crit.toString());
		pane.setFilter(crit.toString());
		updateCount();
		updateEditableAttrList();
		revalidate();
		try{lastUpdate=conn.imserv.getLastUpdate();}
		catch (Exception e1){}
		}


	private void updateCount()
		{
		status.setText(""+pane.obList.size());
		}
	
	
	
	
	/**
	 *	
	 * /////////////////////////////////////////////////////////////////////////////
	 */
	public class AttrPane extends JPanel implements ActionListener
		{
		public static final long serialVersionUID=0;
		
		public JComboBox cTag=new JComboBox(new Object[]{""});
		public JTextField tValue=new JTextField();
		public JButton bRemove=new JButton(BasicWindow.getIconDelete());
		
		
		public void updateTagList(final List<String> list)
			{
			String oldsel=(String)cTag.getSelectedItem();
			if(oldsel==null)
				oldsel=null;
			final String oldsel2=oldsel;
			ComboBoxModel model=new ComboBoxModel(){
				Object sel=oldsel2;
				public Object getSelectedItem(){return sel;}
				public void setSelectedItem(Object anItem){sel=anItem;}
				public Object getElementAt(int i){return list.get(i);}
				public int getSize(){return list.size();}
				public void addListDataListener(ListDataListener arg0){}
				public void removeListDataListener(ListDataListener arg0){}
			};
			cTag.setModel(model);
			cTag.repaint();
			}
		
		public AttrPane()
			{
			cTag.addActionListener(this);
			tValue.addActionListener(this);
			setLayout(new GridLayout(2,1));
			JPanel p=new JPanel(new BorderLayout());
			p.add(cTag,BorderLayout.CENTER);
			p.add(bRemove,BorderLayout.EAST);
			add(p);
			add(tValue);
			bRemove.addActionListener(this);
			}

		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bRemove)
				{
				attrPanesList.remove(this);
				attrPanelLayout();
				}
			else
				tagListSelect();
			}
		
		}
	
	
	
	
	
	
	/**
	 * Adapt the list of editable attributes
	 */
	private void updateEditableAttrList()
		{
		boolean updated=false;
		
		Set<String> selected=pane.selectedId;
		
		if(selected.size()!=1)
			{
			if(!attrEditList.isEmpty())
				{
				updated=false;
				attrEditList.clear();
				attrEditPane.removeAll();
				}
			}
		else
			{
			//null-entry: editable name
			if(!attrEditList.containsKey(""))
				{
				updated=true;
				attrEditList.put(null, new AttrEditPane(""));
				}
			
			try
				{
				//Normal entries
				DataIF data=conn.imserv.getData(selected.iterator().next());
				Tag[] tags=data.getTags();
				Map<String,String> attr=new TreeMap<String, String>();
				for(Tag t:tags)
					if(t.value!=null)
						attr.put(t.name,t.value);

				//Any new?
				for(Map.Entry<String, String> ae:attr.entrySet())
					{
					AttrEditPane pane=attrEditList.get(ae.getKey());
					if(pane==null)
						{
						pane=new AttrEditPane(ae.getKey());
						attrEditList.put(ae.getKey(),pane);
						updated=true;
						}
					else if(!pane.tValue.equals(ae.getValue()))
						{
						updated=true;
						pane.tValue.setText(ae.getValue());
						}
					}
					
				//Any removed
				List<String> lastKeys=new LinkedList<String>(attrEditList.keySet());
				for(String key:lastKeys)
					if(!attr.containsKey(key))
						{
						attrEditList.remove(key);
						updated=true;
						}
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		
		
		//do layout
		if(updated)
			{
			attrEditPane.removeAll();
			attrEditPane.setLayout(new GridLayout(attrEditList.size(),1));
			for(Map.Entry<String, AttrEditPane> entry:attrEditList.entrySet())
				{
				attrEditPane.add(entry.getValue());
				
				}
			revalidate();
			}
		}
	
	/**
	 *	
	 * /////////////////////////////////////////////////////////////////////////////
	 */
	public class AttrEditPane extends JPanel implements ActionListener
		{
		public static final long serialVersionUID=0;
		
		public JTextField tName=new JTextField();
		public JTextField tValue=new JTextField();
		public JButton bRemove=new JButton(BasicWindow.getIconDelete());
		String name;
		
		public AttrEditPane(String name)
			{
			this.name=name;
			setLayout(new BorderLayout());
			if(name!=null)
				{
				add(new JLabel(name),BorderLayout.WEST);
				add(bRemove,BorderLayout.EAST);
				}
			else
				{
				add(tName,BorderLayout.WEST);
				
				}
			add(tValue,BorderLayout.CENTER);
			tName.addActionListener(this);
			tValue.addActionListener(this);
			bRemove.addActionListener(this);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bRemove)
				{
				setTag(name,null,false);
				//update TODO
				}
			else if(e.getSource()==tValue || e.getSource()==tName)
				{
				String tname=name;
				if(tname==null)
					tname=tName.getText();
				setTag(tname,tValue.getText(),true);
				if(name==null)
					{
					attrEditList.remove(null);
					updateEditableAttrList();
					}
				}
			}
		
		
		}
	
	
	}
