/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.widgets;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import endrov.gui.EvSwingUtil;
import endrov.gui.component.JImageButton;
import endrov.gui.component.JSpinnerSimpleEvDecimal;
import endrov.gui.icon.BasicIcon;
import endrov.recording.widgets.RecSettingsChannel.OneChannel;
import endrov.util.math.EvDecimal;

/**
 * Widget for recording settings: Channel settings
 * 
 * @author Johan Henriksson
 *
 */
public class RecWidgetChannel extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	
	/**
	 * * current settings, what to call channel?
	 * * auto exposure?
	 * * z-scan while recording? a la emilie
	 */
	

	private ArrayList<OneChannelWidget> entrylist=new ArrayList<OneChannelWidget>();
	
	private JComponent p=new JPanel();
	private JButton bAdd=new JImageButton(BasicIcon.iconAdd,"Add channel to list");


	private RecWidgetComboConfigGroup cConfigGroup=new RecWidgetComboConfigGroup();

	
	private class OneChannelWidget implements ActionListener
		{
		RecSettingsChannel.OneChannel ch=new RecSettingsChannel.OneChannel();

		RecWidgetComboConfigGroupStates comboChannel=new RecWidgetComboConfigGroupStates();
		JSpinnerSimpleEvDecimal spExposure=new JSpinnerSimpleEvDecimal(new EvDecimal(100));
		JButton bUp=new JImageButton(BasicIcon.iconButtonUp,"Move toward first in order");
		JButton bDown=new JImageButton(BasicIcon.iconButtonDown,"Move toward end in order");
		
		JButton bRemove=new JImageButton(BasicIcon.iconRemove,"Remove channel from list");
		
		JButton bMoreSettings=new JButton("More settings");
		
		public OneChannelWidget()
			{
			bUp.addActionListener(this);
			bDown.addActionListener(this);
			bRemove.addActionListener(this);
			bMoreSettings.addActionListener(this);
			}
		
	
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bRemove)
				{
				entrylist.remove(this);
				makeLayout();
				}
			else if(e.getSource()==bUp)
				{
				int pos=entrylist.indexOf(this);
				if(pos!=0)
					{
					entrylist.remove(pos);
					entrylist.add(pos-1, this);
					makeLayout();
					}
				}
			else if(e.getSource()==bDown)
				{
				int pos=entrylist.indexOf(this);
				if(pos!=entrylist.size()-1)
					{
					entrylist.remove(pos);
					entrylist.add(pos+1, this);
					makeLayout();
					}
				}
			else if(e.getSource()==bMoreSettings)
				{
				new RecWidgetChannelDialog(ch);
				}
			}



		public OneChannel getSettings()
			{
			
			ch.name=comboChannel.getConfigGroupName();
			ch.exposure=spExposure.getDecimalValue();

			return ch;
			}
		
		
		}
	
	
	
	public RecWidgetChannel()
		{
		setBorder(BorderFactory.createTitledBorder("Channnels"));
		setLayout(new BorderLayout());
		add(EvSwingUtil.withLabel("Config group: ", cConfigGroup),BorderLayout.NORTH);
		
		add(p,BorderLayout.CENTER);
		addChannel();
		makeLayout();
		
		bAdd.addActionListener(this);
		}
	
	
	public void dataChangedEvent()
		{
		cConfigGroup.makeLayout();
		}
	
	
	private void addChannel()
		{
		OneChannelWidget row=new OneChannelWidget();
		cConfigGroup.registerWeakMetastateGroup(row.comboChannel);
		entrylist.add(row);
		}
	
	private void makeLayout()
		{
		p.removeAll();
		p.setLayout(new GridBagLayout());

		//For each line and description
		for(int i=-1;i<entrylist.size();i++)
			{
			GridBagConstraints c=new GridBagConstraints();
			c.gridy=i+1;
			c.gridx=0;
			
			OneChannelWidget row=null;
			if(i!=-1) row=entrylist.get(i);
			
			if(i==-1) p.add(bAdd,c);
			else p.add(row.bRemove,c);
			c.gridx++;

			if(i==-1) ;
			else p.add(row.bUp,c);
			c.gridx++;

			if(i==-1) ;
			else p.add(row.bDown,c);
			c.gridx++;

			c.fill=GridBagConstraints.HORIZONTAL;
			c.weightx=1;
			if(i==-1) p.add(new JLabel("Channel "),c);
			else p.add(row.comboChannel,c);
			c.gridx++;
			
			if(i==-1)
				{
				JLabel lab=new JLabel("Exposure [ms]");
				p.add(lab,c);
				}
			else p.add(row.spExposure,c);
			c.weightx=0;
			c.gridx++;

			if(i==-1)
				{
				JLabel lab=new JLabel("");
				p.add(lab,c);
				}
			else p.add(row.bMoreSettings,c);
			c.gridx++;
			}
		
		revalidate();
		}


	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bAdd)
			{
			addChannel();
			makeLayout();
			}
		}
	
	
	/**
	 * Get channel settings
	 */
	public RecSettingsChannel getSettings() throws Exception
		{
		RecSettingsChannel settings=new RecSettingsChannel();
		settings.configGroup=cConfigGroup.getConfigGroupName();
		if(settings.configGroup==null)
			throw new Exception("No config group specified");
		for(OneChannelWidget e:entrylist)
			settings.channels.add(e.getSettings());
		return settings;
		}

	}
