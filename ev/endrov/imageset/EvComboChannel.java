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
	
	public EvComboChannel(List<EvObject> creators, boolean allowNoSelection)
		{
		this.allowNoSelection=allowNoSelection;
		//TODO listen on object data changes
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
		
		public EvContainer getCon()
			{
			return con.get();
			}
		public EvData getData()
			{
			return data.get();
			}
		
		public ComboItem(List<String> path, String channelName, EvContainer obj, EvData data)
			{
			this.path=path.toArray(new String[0]);
			this.con=new WeakReference<EvContainer>(obj);
			this.data=new WeakReference<EvData>(data);
			this.channelName=channelName;
//			System.out.println("new, path: "+path);
			}
		
		public String toString()
			{
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<path.length;i++)
				{
				sb.append(path[i]);
				if(i!=path.length-1)
					sb.append("/");
				if(channelName!=null)
					{
					if(path.length!=0)
						sb.append("-");
					sb.append(channelName);
					}
				}
			return sb.toString();
			}
		
		
		
		}
	
	private String lastChannel=null;
	
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

		if(allowNoSelection)
			combo.addItem(emptyItem);

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
			combo.addItem(emptyItem);

		//If null-selection not allowed then reselect any item in the list
		if(currentItem==null || (currentItem==emptyItem && !allowNoSelection))
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
	
	
	public boolean includeObject(EvContainer cont)
		{
		return cont instanceof Imageset;
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
		lastChannel=ci.channelName;
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
	public EvContainer getSelectedObject()
		{
		ComboItem ci=(ComboItem)combo.getSelectedItem();
		return ci.getCon();
		}
	
	public void setSelectedObject(EvContainer c, String wantChannel)
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
		Map<String,ComboItem> chanMap=itemMap.get(c);
		ComboItem ci;
		if(chanMap.containsKey(wantChannel))
			ci=chanMap.get(wantChannel);
		else if(chanMap.containsKey(lastChannel))
			ci=chanMap.get(lastChannel);
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
	}
