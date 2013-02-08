/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;


import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvPath;
import endrov.data.gui.EvDataGUI;
import endrov.gui.EvSwingUtil;
import endrov.gui.icon.BasicIcon;
import endrov.gui.window.EvBasicWindow;
import endrov.util.EvStringUtil;

/**
 * Object combo. with children disabled it also works as a EvData selector.
 * New objects can be created as well.
 * @author Johan Henriksson
 *
 */
public abstract class EvComboObject extends JPanel implements ActionListener
	{
	static final long serialVersionUID=0;
	private boolean showChildren;
	private boolean allowNoSelection;
	private List<ActionListener> aListeners=new LinkedList<ActionListener>();
	private EvContainer root;
	
	//If channels were objects then this component could be used for that as well
	//add blobID to EvObject? this would fix the rename issue
	
	private JComboBox combo=new JComboBox/*<ComboItem>*/();

	//Empty item. This will allow comparison by pointer
	private final ComboItem emptyItem=new ComboItem(null);

	/**
	 * This item might not be needed but might solve a race condition. Sometimes this widget
	 * is updated and the current object taken. it has been null. I think the GUI might have
	 * to be updated before some calls are working.
	 */
	private ComboItem currentItem=null;
	
	
	
	public EvComboObject(List<EvObject> creators, boolean showChildren, boolean allowNoSelection)
		{
		this.showChildren=showChildren;
		this.allowNoSelection=allowNoSelection;
		
		//TODO listen on object data changes
		//TODO allow delete as well?
		
		JPanel pCreate=new JPanel(new GridLayout(1,creators.size()));
		for(final EvObject ob:creators)
			{
			final JImageButton b=new JImageButton(BasicIcon.iconMenuNew,"Create new "+ob.getMetaTypeDesc());
			pCreate.add(b);
			b.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
					{
					try
						{
						NewObjectWindow w=new NewObjectWindow(ob.getClass().newInstance());
						w.setLocation(b.getLocationOnScreen());
						//w.setLocationRelativeTo(b);
						//Could be out of screen, move it back in that case
						Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
						if(w.getLocation().y+w.getHeight()>dim.height)
							w.setLocation(w.getLocation().x, dim.height-w.getHeight());
						if(w.getLocation().x+w.getWidth()>dim.width)
							w.setLocation(dim.width-w.getWidth(),w.getLocation().y);
						w.setVisible(true);
						}
					catch (Exception e1)
						{
						e1.printStackTrace();
						}
					}
			});
			

			
			//hm. should have used class instead
			//TODO
			}

		updateList();
		
		setLayout(new BorderLayout());
		add(combo,BorderLayout.CENTER);
		add(pCreate,BorderLayout.WEST); //which is best? east? chance of miss-clicking but better grouping
		}

	public void setRoot(EvContainer c)
		{
		root=c;
		updateList();
		}
	public EvContainer getRoot()
		{
		return root;
		}
	
	
	
	/**
	 * One entry in the combo box. The object it points to should only be null
	 * if this is the empty object.
	 */
	private static class ComboItem
		{
		private EvPath path;
		
		public EvContainer getObject()
			{
			if(path==null)
				return null;
			else
				return path.getObject();
			}
		public EvData getData()
			{
			if(path==null)
				return null;
			else
				return (EvData)path.getRoot();
			}
		public EvPath getPath()
			{
			return path;
			}
		public ComboItem(EvPath evpath)
			{
			this.path=evpath;
			}
		
		public String toString()
			{
			if(path==null)
				return "";
			else
				return path.toString(true);
			}
		
		
		
		}
	
	/**
	 * Update list of objects
	 */
	public void updateList()
		{
		//Remember selection
		EvContainer currentCont=currentItem==null ? null : currentItem.getObject();
		
		combo.removeActionListener(this);
		combo.removeAllItems();

		if(allowNoSelection)
			combo.addItem(emptyItem);

		//Build list. Depending on if there is a root it will be used or all data will be listed
		if(root==null)
			{
			//List EvData
			for(EvData data:EvDataGUI.openedData)
				{
				if(includeObject(data))
					combo.addItem(new ComboItem(new EvPath(data)));
				if(showChildren)
					updateListRec(data, new LinkedList<String>(), data);
				}
			}
		else
			{
			LinkedList<String> paths=new LinkedList<String>();
			if(root instanceof EvData)
				updateListRec(root, paths, (EvData)root);
			else
				updateListRec(root, paths, null);
			}
		
		if(combo.getItemCount()==0)
			combo.addItem(emptyItem);

		//If null-selection not allowed then reselect any item in the list
		if(currentItem==null || (currentItem==emptyItem && !allowNoSelection) || !getItemMap().containsKey(currentCont))
			currentCont=((ComboItem)combo.getItemAt(0)).getObject();
		
		//Reselect last item
		setSelectedObject(currentCont);
		
		combo.addActionListener(this);
		}

	
	public abstract boolean includeObject(EvContainer cont);
	
	private void updateListRec(EvContainer parent, LinkedList<String> contPath, EvData data)
		{
		TreeSet<String> reorderObjects=new TreeSet<String>(EvStringUtil.getNaturalComparator());
		reorderObjects.addAll(parent.metaObject.keySet());
		
		
		//for(Map.Entry<String, EvObject> entry:parent.metaObject.entrySet())
		for(String key:reorderObjects)
			{
			EvContainer thisCont=parent.metaObject.get(key);
			//EvContainer thisCont=entry.getValue();
			contPath.addLast(key);
			if(includeObject(thisCont))
				combo.addItem(new ComboItem(new EvPath(data, contPath)));
			if(showChildren)
				updateListRec(thisCont, contPath, data);
			contPath.removeLast();
			}
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		currentItem=(ComboItem)combo.getSelectedItem();
		emitListener();
		}
	
	
	public void addActionListener(ActionListener list)
		{
		aListeners.add(list);
		}
	
	private void emitListener()
		{
		for(ActionListener a:aListeners)
			a.actionPerformed(new ActionEvent(this,0,""));
		}
	
	
	/**
	 * Get a pointer directly to the EvData container of the selected object or null
	 * @return Object or null
	 */
	public EvData getData()
		{
		if(root==null)
			{
			ComboItem ci=currentItem;
			return ci.getData();
			}
		else
			return null;
		}
	
	/**
	 * Return currently selected object or null
	 */
	public EvContainer getSelectedObject()
		{
		ComboItem ci=currentItem;
		//bug: I think ci was once null after unloading data. was nothing reselected?
		if(ci==null)
			{
			System.out.println("ci null "+combo.getItemCount());
			}
		return ci.getObject();
		}
	
	public EvContainer getSelectObjectParent()
		{
		ComboItem ci=currentItem;
		//bug: I think ci was once null after unloading data. was nothing reselected?
		if(ci==null)
			{
			System.out.println("ci null "+combo.getItemCount());
			}
		EvPath path=ci.getPath();
		if(path==null)
			return null;
		else
			return path.getParent().getObject();
		}
	
	
	public EvPath getSelectedPath()
		{
		ComboItem ci=currentItem;
		//bug: I think ci was once null after unloading data. was nothing reselected?
		if(ci==null)
			{
			System.out.println("ci null "+combo.getItemCount());
			}
		
		return ci.getPath();
		}
	
	private Map<EvContainer,ComboItem> getItemMap()
		{
		Map<EvContainer,ComboItem> itemMap=new HashMap<EvContainer, ComboItem>();
		for(int ci=0;ci<combo.getItemCount();ci++)
			{
			ComboItem item=(ComboItem)combo.getItemAt(ci);
			itemMap.put(item.getObject(),item);
			}
		return itemMap;
		}
	
	public void setSelectedObject(EvContainer c)
		{
		combo.setSelectedItem(getItemMap().get(c));
		
		currentItem=getItemMap().get(c);
		//TODO should this emit an event?
		}
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	//use case 1: just list Datas.
	//use case 2: list objects for all data. want Data, Path to object, Object
	//(use case 3: as a channel selector)
	//use case 3: list objects for one data. data is the root

	//extended object can be parameterized. get method without cast can then be introduced
	
	
	public class NewObjectWindow extends JFrame implements ActionListener, WindowFocusListener
		{
		static final long serialVersionUID=0;
		private EvComboObjectTree tree=new EvComboObjectTree();
		private JTextField fName=new JTextField();
		private JButton bOk=new JButton("Ok");
		private JButton bCancel=new JButton("Cancel");
		private EvObject newObject;
		
		//TODO esc key
		
		public NewObjectWindow(EvObject newObject)
			{
			this.newObject=newObject;
			setLayout(new BorderLayout());
			JScrollPane scroll=new JScrollPane(tree,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			JPanel topPanel=new JPanel(new BorderLayout());
			JPanel bottomPanel=new JPanel(new GridLayout(1,2));
			topPanel.add(new JLabel("Name:"),BorderLayout.WEST);
			topPanel.add(fName,BorderLayout.CENTER);
			bottomPanel.add(bOk);
			bottomPanel.add(bCancel);
			add(scroll,BorderLayout.CENTER);
			add(topPanel,BorderLayout.NORTH);
			add(bottomPanel,BorderLayout.SOUTH);
			bOk.addActionListener(this);
			bCancel.addActionListener(this);
			fName.addActionListener(this);
			setTitle("Create new "+newObject.getMetaTypeDesc());
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setUndecorated(true);
			pack();
			int minh=150;
//			if(getHeight()<minh)
			setSize(getWidth(), minh);
			addWindowFocusListener(this);
			
			bOk.setEnabled(canOk());
			EvSwingUtil.textAreaChangeListener(fName, new ChangeListener(){
				public void stateChanged(ChangeEvent e){bOk.setEnabled(canOk());}
				});
			
			tree.addTreeSelectionListener(new TreeSelectionListener(){
				public void valueChanged(TreeSelectionEvent e){bOk.setEnabled(canOk());}
				});
			}

		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bCancel)
				dispose();
			else if(e.getSource()==bOk || e.getSource()==fName)
				{
				EvContainer con=tree.getSelectedContainer();
				String name=fName.getText();
				if(canOk())
					{
					dispose();
					con.metaObject.put(name,newObject);
					updateList();
					setSelectedObject(newObject);
					EvBasicWindow.updateWindows();
					}
				}
				
			}

		public boolean canOk()
			{
			EvContainer con=tree.getSelectedContainer();
			String name=fName.getText();
			return (con!=null && !name.equals("") && con.getMetaObject(name)==null);
			}
		
		public void windowGainedFocus(WindowEvent arg0)
			{
			}

		public void windowLostFocus(WindowEvent arg0)
			{
			dispose();
			}
		
		
		
		}
	
	
	}
