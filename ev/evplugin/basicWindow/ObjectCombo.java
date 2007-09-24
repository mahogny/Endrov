package evplugin.basicWindow;

import java.awt.event.*;
import javax.swing.*;

import evplugin.imageset.*;
import evplugin.metadata.*;

/**
 * A combobox with all channels
 * @author Johan Henriksson
 */
public class ObjectCombo extends JComboBox implements ActionListener
	{
	static final long serialVersionUID=0;

	private final boolean addEmpty;
	private ActionListener saveListeners[]=null;
	private final comboFilterMetaObject filterObject;
	
	//Needed to unselect special alternatives after user selected them
//	private Metadata curMeta=new EmptyMetadata();
	private Metadata curMeta=null; //BIG CHANGE
	private Integer curId=null;

	
	
	
	/** Temporarily disable action listeners */
	private void disableActionListeners()
		{
		saveListeners=getActionListeners();
		for(ActionListener l:saveListeners)
			removeActionListener(l);
		}
	
	/** Re-enable action listeners */
	private void enableActionListeners()
		{
		for(ActionListener l:saveListeners)
			addActionListener(l);
		}
	
	
	

	
	/**
	 * Construct new channel combo, needs access to global data
	 */
	public ObjectCombo(comboFilterMetaObject filterObject, boolean addEmptyChannel)
		{
		this.addEmpty=addEmptyChannel;
		this.filterObject=filterObject;
		addActionListener(this);
		
		
		updateObjectList();
		}
	
	
	public void actionPerformed(ActionEvent e) //something else
		{
		Alternative a=(Alternative)getSelectedItem();
		if(a!=null)
			{
			//Check if this is still the old alternative. If so, ignore this event.
			if(curMeta==a.meta && curId==a.id)
				return;
			
			if(a.special==null)
				{
				//Remember selection unless it is a special alternative
				curMeta=a.meta;
				curId=a.id;
				}
			else if(a.listener!=null)
				{
				//Execute special action
				disableActionListeners();
				setSelection();
				enableActionListeners();
				
				//System.out.println("+ "+getSelectedIndex());
				
				a.listener.actionPerformed(e);
				BasicWindow.updateWindows();
				}
			}
		}
	
	
	
	/**
	 * Update the combobox with objects from the record
	 */
	public void updateObjectList()
		{
		actionPerformed(null);
		
		disableActionListeners();
		buildList();
		setSelection();
	
		enableActionListeners();
		}

	private void setSelection()
		{
//		System.out.println("a "+getSelectedIndex());
		//If this list does not allow that no imageset is selected then just take one
		if(!addEmpty)
			{
			if(curMeta instanceof EmptyMetadata && getItemCount()>0)
				{
				curMeta=((Alternative)getItemAt(0)).meta;
				curId=null;
				}

//			System.out.println("b "+getSelectedIndex());

			//Make sure a channel is selected unless the imageset is empty
			if((curId==null || (curMeta!=null && curMeta.getMetaObject(curId)==null)) && 
					(curMeta!=null && !curMeta.metaObject.isEmpty()))
				curId=curMeta.metaObject.keySet().iterator().next();
			}
//		System.out.println("c "+getSelectedIndex()+ " "+curMeta);

		//Reselect old item in list
		if(curMeta==null)
			{
			if(getItemCount()>0)
				setSelectedIndex(0);
			}
		else
			for(int i=0;i<getItemCount();i++)
				{
				Alternative a=(Alternative)getItemAt(i);
				if(a.meta==curMeta && a.id==curId)
					setSelectedIndex(i);
				}
//		System.out.println("d "+getSelectedIndex());

		}
	
	/**
	 * Build list of items
	 */
	private void buildList()
		{
		removeAllItems();
		if(addEmpty)
			addItem(new Alternative(null,0, null, null));
		//Add other metadata
		for(Metadata thisMeta:Metadata.metadata)
			{
			for(int id:thisMeta.metaObject.keySet())
				if(filterObject.comboFilterMetaObjectCallback(thisMeta.getMetaObject(id)))
					addItem(new Alternative(thisMeta,id, null, null));
			for(Alternative a:filterObject.comboAddObjectAlternative(this, thisMeta))
				addItem(a);
			}
		}
	
	
	/**
	 * Get the selected channel
	 * @return Channel or null
	 */
	public Integer getObjectID()
		{
		Alternative a=(Alternative)getSelectedItem();
		if(a==null)
			return null;
		else
			return a.id;
		}
	
	/**
	 * Get a pointer directly to the meta object
	 * @return Object or null
	 */
	public MetaObject getObject()
		{
		Alternative a=(Alternative)getSelectedItem();
		if(a==null || a.meta==null || a.id==null)
			return null;
		else
			return a.meta.getMetaObject(a.id);
		}
	
	/**
	 * Get the selected imageset
	 * @return Imageset or null
	 */
	public Imageset getImageset()
		{		
		Alternative a=(Alternative)getSelectedItem();
		if(a==null)
			return new EmptyImageset();
		else
			return (Imageset)a.meta;
		}
	

	
	
	public static interface comboFilterMetaObject
		{
		public boolean comboFilterMetaObjectCallback(MetaObject ob);
		public Alternative[] comboAddObjectAlternative(ObjectCombo combo, Metadata meta);
		public Alternative[] comboAddAlternative(ObjectCombo combo);
		}
	
	public static class Alternative
		{
		public final Metadata meta;
		public final Integer id;
		public final String special;
		public final ActionListener listener;
		public Alternative(Metadata meta, Integer id, String special, ActionListener listener)
			{
			this.meta=meta;
			this.id=id;
			this.special=special;
			this.listener=listener;
			}
		public boolean isEmpty()
			{
			return meta==null;
			}
		public String toString()
			{
			if(isEmpty())
				{
				if(special!=null)
					return special;
				else
					return "";
				}
			else if(special==null)
				{
				MetaObject o=meta.getMetaObject(id);
				if(o==null)
					return meta.getMetadataName()+": "+id;
				else
					return meta.getMetadataName()+": "+id+" - "+o.getMetaTypeDesc();
				}
			else
				return meta.getMetadataName()+": "+special;
			}
		}

	
	}
