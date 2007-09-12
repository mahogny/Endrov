package evplugin.basicWindow;

import java.awt.event.*;
import javax.swing.*;

import evplugin.imageset.*;
import evplugin.metadata.*;

/**
 * A combobox with all channels
 * @author Johan Henriksson
 */
public class MetaCombo extends JComboBox implements ActionListener
	{
	static final long serialVersionUID=0;

	private final boolean addEmpty;
	private ActionListener saveListeners[]=null;
	private final comboFilterMetadata filter;
	
	//Needed to unselect special alternatives after user selected them
	private Metadata curMeta=new EmptyMetadata();

	
	
	
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
	public MetaCombo(comboFilterMetadata filter, boolean addEmptyChannel)
		{
		this.addEmpty=addEmptyChannel;
		this.filter=filter;
		addActionListener(this);
		updateList();
		}
	
	
	public void actionPerformed(ActionEvent e) //something else
		{
		Alternative a=(Alternative)getSelectedItem();
		if(a!=null)
			{
//			if(a.special==null)
				{
				//Remember selection unless it is a special alternative
				curMeta=a.meta;
				}/*
			else
				{
				//Execute special action
				setSelection();
				a.listener.actionPerformed(e);
				}*/
			}
		}
	
	
	
	/**
	 * Update the combobox with channels from the record
	 */
	public void updateList()
		{
		actionPerformed(null);
		
		disableActionListeners();
		buildList();
		setSelection();
	
		enableActionListeners();
		}

	private void setSelection()
		{
		//If this list does not allow that no imageset is selected then just take one
		if(!addEmpty && curMeta instanceof EmptyMetadata && getItemCount()>0)
			curMeta=((Alternative)getItemAt(0)).meta;
		
		//Reselect old item in list
		for(int i=0;i<getItemCount();i++)
			{
			Alternative a=(Alternative)getItemAt(i);
			if(a.meta==curMeta)
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
			addItem(new Alternative(null));
		//Add other metadata
		for(Metadata thisMeta:Metadata.metadata)
			if(filter==null || filter.comboFilterMetadataCallback(thisMeta))
				addItem(new Alternative(thisMeta));
		}
	
	
	
	/**
	 * Get a pointer directly to the meta object
	 * @return Object or null
	 */
	public Metadata getMeta()
		{
		Alternative a=(Alternative)getSelectedItem();
		if(a==null)
			return null;
		else
			return a.meta;
		}
	
	/**
	 * Get the selected imageset
	 * @return Some imageset, never null
	 */
	public Imageset getImageset()
		{		
		Metadata rec=getImagesetNull();
		if(rec==null)
			return new EmptyImageset();
		else
			return (Imageset)rec;
		}
	

	/**
	 * Get the selected imageset
	 * @return The imageset or null
	 */
	public Imageset getImagesetNull()
		{
		Alternative a=(Alternative)getSelectedItem();
		if(a==null)
			return null;
		else
			return (Imageset)a.meta;
		}
	
	public static interface comboFilterMetadata
		{
		public boolean comboFilterMetadataCallback(Metadata meta);
		}
	
	public static class Alternative
		{
		public final Metadata meta;
		public Alternative(Metadata meta)
			{
			this.meta=meta;
			}
		public String toString()
			{
			return meta.getMetadataName();
			}
		}

	
	}
