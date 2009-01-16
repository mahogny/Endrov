package endrov.imageset;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;


import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;

/**
 * Object combo. with children disabled it also works as a EvData selector.
 * New objects can be created as well.
 * @author Johan Henriksson
 *
 */
public class EvComboChannel extends JPanel implements ActionListener
	{
	static final long serialVersionUID=0;
	private boolean allowNoSelection;	
	private EvContainer root;	
	private JComboBox combo=new JComboBox();
	public String lastSelectChannel=null;
	
	public EvComboChannel(EvContainer root, boolean allowNoSelection)
		{
		this.allowNoSelection=allowNoSelection;
		//TODO listen on object data changes
		this.root=root;
		updateList();
		setLayout(new BorderLayout());
		add(combo,BorderLayout.CENTER);
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
	
	
	//Empty item. This will allow comparison by pointer
	private ComboItem emptyItem=new ComboItem(new LinkedList<String>(),null,null,null);
	
	/**
	 * One entry in the combo box. The object it points to should only be null
	 * if this is the empty object.
	 */
	private class ComboItem
		{
		//another path interface in HW
		String path[];
 		private WeakReference<EvContainer> con; 
		private WeakReference<EvData> data; 
		public String channelName; 
				
		public ComboItem(List<String> path, String channelName, EvContainer obj, EvData data)
			{
			this.path=path.toArray(new String[0]);
			this.con=new WeakReference<EvContainer>(obj);
			this.data=new WeakReference<EvData>(data);
			this.channelName=channelName;
//			System.out.println("new, path: "+path);
			}
		
		public EvContainer getCon()
			{
			return con.get();
			}
		public EvData getData()
			{
			return data.get();
			}
		
		public StringBuffer getObjectPath()
			{
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<path.length;i++)
				{
				sb.append(path[i]);
				if(i!=path.length-1)
					sb.append("/");
				}
			return sb;
			}
		
		public String toString()
			{
			StringBuffer sb=getObjectPath();
			if(channelName!=null)
				{
				if(path.length!=0)
					sb.append("-");
				sb.append(channelName);
				}
			return sb.toString();
			}
		
		}
	
	
	/**
	 * Update list of objects
	 */
	public void updateList()
		{
		//Remember selection
		ComboItem currentItem=(ComboItem)combo.getSelectedItem();
		EvContainer currentCont=currentItem==null ? null : currentItem.getCon();
		String currentChannel=currentItem==null ? null : currentItem.channelName;
		
		combo.removeActionListener(this);
		combo.removeAllItems();

		boolean hasEmpty=false;
		if(allowNoSelection)
			{
			hasEmpty=true;
			combo.addItem(emptyItem);
			}

		//Build list. Depending on if there is a root it will be used or all data will be listed
		if(root==null)
			{
			//List EvData
			for(EvData data:EvData.metadata)
				{
				LinkedList<String> paths=new LinkedList<String>();
				paths.add(data.getMetadataName());
				updateListRec(data, paths, data);
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
			{
			hasEmpty=true;
			combo.addItem(emptyItem);
			}

		//TODO: more informed decision
		if(currentItem==emptyItem && !hasEmpty)
			{
			currentCont=((ComboItem)combo.getItemAt(0)).getCon();
			currentChannel=null;
			}
		
		//If null-selection not allowed then reselect any item in the list
		if(currentItem==null || (currentItem==emptyItem && !allowNoSelection) || !getItemMap().containsKey(currentCont))// ||
			//	(currentItem==emptyItem && !hasEmpty)) //This special case, select preferred channel TODO 
			{
			currentCont=((ComboItem)combo.getItemAt(0)).getCon();
			currentChannel=null;
			}
		
		//Reselect last item
		setSelectedObject(currentCont, currentChannel);
		
	//	System.out.println("selected "+combo.getSelectedIndex());
	//	System.out.println("sesected2 "+combo.getSelectedItem());
		combo.addActionListener(this);
		}

	private void addChannelsFor(EvContainer thisObj, LinkedList<String> contPath, EvData data)
		{
		if(thisObj instanceof Imageset)
			{
			Imageset im=(Imageset)thisObj;
			for(String chanName:im.channelImages.keySet())
				combo.addItem(new ComboItem(contPath,chanName,thisObj,data));
			}
		}
	
	private void updateListRec(EvContainer root, LinkedList<String> contPath, EvData data)
		{
		addChannelsFor(root, contPath, data);
		
		for(Map.Entry<String, EvObject> entry:root.metaObject.entrySet())
			{
			EvContainer thisCont=entry.getValue();
			contPath.addLast(entry.getKey());
		//	System.out.println("Checking to include "+thisCont+ " "+includeObject(thisCont));

			updateListRec(thisCont, contPath, data);
			contPath.removeLast();
			}
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		ComboItem ci=(ComboItem)combo.getSelectedItem();
//		if(ci.channelName!=null)
		lastSelectChannel=ci.channelName;
		emitListener();
		
		
		}
	
	
	
	
	
	
	
	
	private List<ActionListener> aListeners=new LinkedList<ActionListener>();
	
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
			ComboItem ci=(ComboItem)combo.getSelectedItem();
			return ci.getData();
			}
		else
			return null;
		}
	
	/**
	 * Return currently selected object or null
	 */
	public Imageset getImageset()
		{
		ComboItem ci=(ComboItem)combo.getSelectedItem();
		return (Imageset)ci.getCon();
//		 * TODO bug: getImageset has once been asked before it was ready
		//ci was null
		}

	/**
	 * Return currently selected object or null
	 */
	public Imageset getImagesetNotNull()
		{
		ComboItem ci=(ComboItem)combo.getSelectedItem();
		Imageset im=(Imageset)ci.getCon();
		return im==null ? new Imageset() : im;
		}

	/**
	 * Get a map of the current entries
	 */
	private Map<EvContainer,Map<String,ComboItem>> getItemMap()
		{
		Map<EvContainer,Map<String,ComboItem>> itemMap=new HashMap<EvContainer, Map<String,ComboItem>>();
		for(int ci=0;ci<combo.getItemCount();ci++)
			{
			ComboItem item=(ComboItem)combo.getItemAt(ci);
			Map<String,ComboItem> chanMap=itemMap.get(item.getCon());
			if(chanMap==null)
				itemMap.put(item.getCon(),chanMap=new HashMap<String, ComboItem>());
			chanMap.put(item.channelName,item);
			}
		return itemMap;
		}
	
	/**
	 * Set which object should be selected
	 */
	public void setSelectedObject(EvContainer c, String wantChannel)
		{
		//System.out.println("select "+c+" "+wantChannel);
		Map<EvContainer,Map<String,ComboItem>> itemMap=getItemMap();
		//Ugly hack to fix null. does it work?
/*		if(itemMap.get(c)==null)
			{
			updateList();
			itemMap=getItemMap();
			}*/
		Map<String,ComboItem> chanMap=itemMap.get(c); //has been null
		ComboItem ci;
		if(chanMap.containsKey(wantChannel))
			ci=chanMap.get(wantChannel);
		else if(chanMap.containsKey(lastSelectChannel))
			ci=chanMap.get(lastSelectChannel);
		else
			ci=chanMap.values().iterator().next();
		combo.setSelectedItem(ci);
		//TODO should this emit an event?
		}
	
	/**
	 * Get selected channel or null
	 */
	public String getChannel()
		{
		ComboItem ci=(ComboItem)combo.getSelectedItem();
		return ci.channelName;
		}
	
	public String getSelectedObjectPath()
		{
		ComboItem ci=(ComboItem)combo.getSelectedItem();
		return ci.getObjectPath().toString();
		}
	}
