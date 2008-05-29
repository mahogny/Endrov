package evplugin.basicWindow;

import java.awt.event.*;
import javax.swing.*;

import evplugin.data.*;
import evplugin.imageset.*;

/**
 * A combobox with all channels
 * @author Johan Henriksson
 */
public class ChannelCombo extends JComboBox
	{
	static final long serialVersionUID=0;

	private final boolean addEmptyChannel;
	private Imageset imagesetExternal; //if an imageset is provided externally

	/** The last channel that was selected. This is only used to remember the channel between times when imageset==null */
	public String lastSelectChannel="";
	

	private ActionListener listeners[]=null;
	
	/** Temporarily disable action listeners */
	private void disableActionListeners()
		{
		listeners=getActionListeners();
		for(ActionListener l:listeners)
			removeActionListener(l);
		}
	
	/** Re-enable action listeners */
	private void enableActionListeners()
		{
		for(ActionListener l:listeners)
			addActionListener(l);
		}
	
	
	
	/**
	 * Construct new channel combo, needs access to global data
	 */
	public ChannelCombo(Imageset imagesetExternal, boolean addEmptyChannel)
		{
		this.imagesetExternal=imagesetExternal;
		this.addEmptyChannel=addEmptyChannel;
		addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				if(getItemCount()!=0)
					lastSelectChannel=getChannel();
				}
			});
		updateChannelList();
		}
	

	public void setExternalImageset(Imageset rec)
		{
		imagesetExternal=rec;
		updateChannelList();
		}
	
	public void setImageset(Imageset rec)
		{
		updateChannelList(rec,null);
		}
	
	
	private class Alternative
		{
		final public Imageset imageset;
		final public String channel;
		public Alternative(Imageset imageset, String channel)
			{
			this.imageset=imageset;
			this.channel=channel;
			}
		public String toString()
			{
			if(imagesetExternal!=null)
				return channel;
			else
				return imageset.getMetadataName()+"-"+channel;
			}
		}

	
	public void updateChannelList()
		{
		updateChannelList(null,null);
		}
	
	/**
	 * Update the combobox with channels from the record
	 */
	public void updateChannelList(Imageset curImageset, String curChannel)
		{
		if(curImageset==null)
			curImageset=getImageset();
		if(curChannel==null)
			curChannel=getChannel();
		//Remember what is selected in the list right now
		Imageset lastImageset=curImageset;
		String lastChannel=curChannel;
		
		disableActionListeners();
		buildList();
		
		//Make sure a channel is selected unless the imageset is empty
		if((curChannel==null || (!curChannel.equals("") && curImageset.getChannel(curChannel)==null)) && 
				!curImageset.channelImages.isEmpty())
			{
			curChannel=curImageset.channelImages.keySet().iterator().next();
			System.out.println("setcurchannel "+curImageset.channelImages.keySet().iterator().next());
			}
		
		//Reselect old item in list
		for(int i=0;i<getItemCount();i++)
			{
			Alternative a=(Alternative)getItemAt(i);
			if(a.imageset==curImageset && a.channel.equals(curChannel))
				{
				setSelectedIndex(i);
				//System.out.println("sel "+i);
				}
			}
		enableActionListeners();
		
		//Update listeners
		if(lastImageset!=curImageset || curChannel!=lastChannel)
			{
			for(ActionListener l:getActionListeners())
				l.actionPerformed(new ActionEvent(this,0,"")); //bad ID?
			}
		}

	
	/**
	 * Fill up list with channels
	 */
	private void buildList()
		{
		removeAllItems();
		if(addEmptyChannel)
			addItem(new Alternative(new EmptyImageset(),""));
		if(imagesetExternal!=null)
			{
			//Add Imageset imageset
			for(String channel:imagesetExternal.channelImages.keySet())
				addItem(new Alternative(imagesetExternal,channel));
			}
		else
			{
			//Add other metadata
			for(EvData thisMeta:EvData.metadata)
				{
				if(thisMeta instanceof Imageset)
					{
					Imageset im=(Imageset)thisMeta;
					for(String channel:im.channelImages.keySet())
						addItem(new Alternative(im,channel));
					}
				}
			}
		}
	
	/**
	 * Get the selected channel
	 * @return Channel or null
	 */
	public String getChannel()
		{
		Alternative a=(Alternative)getSelectedItem();
		if(a==null)
			return null;
		else
			return a.channel;
		}
	public String getChannelNotNull()
		{
		String ch=getChannel();
		return ch==null ? "" : ch;
		}
	
	
	/**
	 * Get the selected imageset
	 * @return Imageset, never null
	 */
	public Imageset getImageset()
		{		
		Alternative a=(Alternative)getSelectedItem();
		return a==null ? new EmptyImageset() : a.imageset; 
		}
	
	/**
	 * Get the selected imageset
	 * @return Imageset or null
	 */
	public Imageset getImagesetNull()
		{		
		Alternative a=(Alternative)getSelectedItem();
		if(a.imageset instanceof EmptyImageset)
			return null; //TODO check if this breaks anything
		else
			return a.imageset;
		}
	
	public Alternative createAlternative(Imageset imageset, String channel)
		{
		return new Alternative(imageset, channel);
		}
	
	}
