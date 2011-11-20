/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.propertyWindow;


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.*;

import org.jdom.*;


import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;
import endrov.hardware.EvHardwareConfigGroup;

/**
 * Property window - shows every property available
 * @author Johan Henriksson 
 */
public class PropertyWindow extends BasicWindow implements ActionListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	private JComponent createControlFor(EvDevice device, String property)
		{
		
		
		
		//TODO
		
		
		return new JLabel(device.getPropertyValue(property));
		}
	
	
	private JPanel allPropertyPanel=new JPanel();
	private JPanel defaultPropertyPanel=new JPanel();
	private JTabbedPane tabs=new JTabbedPane();
	private JButton bNewGroup=new JButton("New group");
	
	public PropertyWindow()
		{
		this(new Rectangle(300,120));
		}
	
	public PropertyWindow(Rectangle bounds)
		{
		updateAllPanel(allPropertyPanel);
		updateDefaultPanel(defaultPropertyPanel);
		
		tabs.addTab("All",new JScrollPane(allPropertyPanel , JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		tabs.addTab("Default",new JScrollPane(defaultPropertyPanel , JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		
		bNewGroup.addActionListener(this);
		
		////////////////////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
	
		
		//Window overall things
		setTitleEvWindow("Control2");
		packEvWindow();
		setVisibleEvWindow(true);
		//setBoundsEvWindow(bounds);
		}
	
	
	
	

	private void updateDefaultPanel(JPanel thisPanel)
		{
		thisPanel.removeAll();

		JPanel propertyPanel=new JPanel();
		
		propertyPanel.setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;

		for(Map.Entry<String, EvHardwareConfigGroup> e:EvHardwareConfigGroup.groups.entrySet())
			{
			EvHardwareConfigGroup hwg=e.getValue();
			String groupName=e.getKey();

			c.gridx=0;
			c.weightx=0;
			propertyPanel.add(new JLabel(groupName),c);

			c.gridx++;
			if(hwg.propsToInclude.size()==1)
				{
				//If it is a single device then allow direct control of it
				EvDevice device=hwg.propsToInclude.iterator().next().getDevice();
				String property=hwg.propsToInclude.iterator().next().getProperty();
				propertyPanel.add(createControlFor(device, property));
				}
			else
				{
				//Allow user to switch config groups
				NewConfigGroupPanel groupPanel=new NewConfigGroupPanel(groupName);

				//TODO would it be useful to try and detect the current state?
				//maybe it should be done every time a property is changed. need to listen for changes

				propertyPanel.add(groupPanel);
				}

			c.gridy++;
			}


		//Button for adding new groups
		thisPanel.setLayout(new BorderLayout());
		thisPanel.add(propertyPanel,BorderLayout.CENTER);
		thisPanel.add(bNewGroup,BorderLayout.SOUTH);
		}
	
	private void updateAllPanel(JPanel thisPanel)
		{
		
		thisPanel.removeAll();
		
		//devname, property, value
		//could be: devname#property, value
		
		thisPanel.setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;
		
		
		for(Map.Entry<EvDevicePath, EvDevice> e:EvHardware.getDeviceMap().entrySet())
			{

			EvDevice device=e.getValue();

			for(String propName:device.getPropertyTypes().keySet())
				{
				c.gridx=0;
				c.weightx=0;
				thisPanel.add(new JLabel(e.getKey().toString()),c);
				
				c.gridx++;
				thisPanel.add(new JLabel(propName),c);
				
				c.gridx++;
				c.weightx=1;
				thisPanel.add(createControlFor(device, propName));
				}
			c.gridy++;
			
			}
	
		
		
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bNewGroup)
			{
			new DialogNewConfigGroup();
			}
		}
	
	
	
	public void dataChangedEvent()
		{
//		objectCombo.updateList();
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		
		} 
	public void freeResources()
		{
		}
	
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
			public void newBasicWindow(BasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(),new Hook());
				}
			class Hook implements BasicWindowHook, ActionListener
				{
				public void createMenus(BasicWindow w)
					{
					JMenuItem mi=new JMenuItem("Control2",new ImageIcon(getClass().getResource("iconWindow.png")));
					mi.addActionListener(this);
					BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new PropertyWindow();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		
		
		}
	
	
	
	}
