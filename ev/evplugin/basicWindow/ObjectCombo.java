package evplugin.basicWindow;

import java.awt.event.*;
import javax.swing.*;

import evplugin.data.*;
import evplugin.imageset.*;

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
	private EvData curMeta=null; //BIG CHANGE
	private String curId=null;

	
	
	
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
	public ObjectCombo(comboFilterMetaObject filterObject, boolean addEmpty)
		{
		this.addEmpty=addEmpty;
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
			if(curMeta==a.meta && (curId!=null && a.id!=null && curId.equals(a.id))) //added stuff so equals does not die 080516
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
		//Make sure curmeta exists, otherwise get rid of it. This speeds up GC
		boolean curmetaok=curMeta instanceof EvDataEmpty;
		for(int i=0;i<getItemCount();i++)
			{
			Alternative a=(Alternative)getItemAt(i);
			if(a.meta==curMeta)
				curmetaok=true;
			}
		if(!curmetaok)
			curMeta=null;
		
		
		
		//If this list does not allow that no imageset is selected then just take one
		if(!addEmpty)
			{
			if(curMeta instanceof EvDataEmpty && getItemCount()>0)
				{
				curMeta=((Alternative)getItemAt(0)).meta;
				curId=null;
				}

			//Make sure a channel is selected unless the imageset is empty
			if((curId==null || (curMeta!=null && curMeta.getMetaObject(curId)==null)) && 
					(curMeta!=null && !curMeta.metaObject.isEmpty()))
				curId=curMeta.metaObject.keySet().iterator().next();
			}

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
				if(a.meta==curMeta && a.id.equals(curId))
					setSelectedIndex(i);
				}
		}
	
	/**
	 * Build list of items
	 */
	private void buildList()
		{
		removeAllItems();
		if(addEmpty)
			addItem(new Alternative(null,null, null, null));
		//Add other metadata
		for(EvData thisMeta:EvData.metadata)
			{
			for(String id:thisMeta.metaObject.keySet())
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
	public String getObjectID()
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
	public EvObject getObject()
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
		public boolean comboFilterMetaObjectCallback(EvObject ob);
		public Alternative[] comboAddObjectAlternative(ObjectCombo combo, EvData meta);
		public Alternative[] comboAddAlternative(ObjectCombo combo);
		}
	
	public static class Alternative
		{
		public final EvData meta;
		public final String id;
		public final String special;
		public final ActionListener listener;
		public Alternative(EvData meta, String id, String special, ActionListener listener)
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
				EvObject o=meta.getMetaObject(id);
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
