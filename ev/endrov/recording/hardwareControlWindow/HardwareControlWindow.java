/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.hardwareControlWindow;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdom.*;


import endrov.data.EvData;
import endrov.gui.component.JNumericField;
import endrov.gui.component.JSmartToggleCombo;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;
import endrov.hardware.EvHardwareConfigGroup;
import endrov.util.EvStringUtil;

/**
 * Property window - shows every property available
 * @author Johan Henriksson 
 */
public class HardwareControlWindow extends EvBasicWindow implements ActionListener, EvHardwareConfigGroup.GroupsChangedListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	private JPanel allPropertyPanel=new JPanel();
	private JPanel defaultPropertyPanel=new JPanel();
	private JTabbedPane tabs=new JTabbedPane();
	private JButton bNewGroup=new JButton("New group");
	
	
	public HardwareControlWindow()
		{
		updateAllPanel(allPropertyPanel);
		updatePalettePanel(defaultPropertyPanel);
		
		tabs.addTab("All",new JScrollPane(allPropertyPanel , JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		tabs.addTab("My properties",new JScrollPane(defaultPropertyPanel , JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		tabs.setSelectedIndex(1);
		
		bNewGroup.addActionListener(this);
		
		////////////////////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
	
		
		EvHardwareConfigGroup.groupsChangedListeners.addWeakListener(this);
		
		//Window overall things
		setTitleEvWindow("Hardware control");
		packEvWindow();
		Rectangle bounds=new Rectangle(600,300);
		if(bounds.width<getWidth())
			bounds.width=getWidth();
		setBoundsEvWindow(bounds);
		setVisibleEvWindow(true);		
		}
	
	
	
	
	/**
	 * Create panel for chosen properties and config groups
	 * @param thisPanel
	 */
	private void updatePalettePanel(JPanel thisPanel)
		{
		thisPanel.removeAll();

		JPanel propertyPanel=new JPanel();
		
		propertyPanel.setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridy=0;

		for(Map.Entry<String, EvHardwareConfigGroup> e:EvHardwareConfigGroup.getConfigGroups().entrySet())
			{
			String groupName=e.getKey();

			c.gridx=0;
			c.weightx=0;
			propertyPanel.add(new JLabel(groupName+" "),c);

			JComponent groupComponent;
			
			ConfigGroupPanel groupPanel=new ConfigGroupPanel(groupName);
			groupComponent=groupPanel;
			
			c.gridx++;
			c.weightx=1;
			propertyPanel.add(groupComponent,c);

			c.gridy++;
			}


		//Button for adding new groups
		thisPanel.setLayout(new BorderLayout());
		thisPanel.add(propertyPanel,BorderLayout.NORTH);
		thisPanel.add(bNewGroup,BorderLayout.SOUTH);
		}
	
	
	
	/**
	 * Layout components for "All"
	 */
	private void updateAllPanel(JPanel thisPanel)
		{
		
		thisPanel.removeAll();
		
		//devname, property, value
		//could be: devname#property, value
		
		thisPanel.setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridy=0;
		
		for(Map.Entry<EvDevicePath, EvDevice> e:EvHardware.getDeviceMap().entrySet())
			{
			EvDevice device=e.getValue();
			for(String propName:device.getPropertyTypes().keySet())
				{
				c.gridx=0;
				c.weightx=0;
				thisPanel.add(new JLabel(e.getKey().toString()+" "),c);
				
				c.gridx++;
				thisPanel.add(new JLabel(propName+" "),c);
				
				c.gridx++;
				c.weightx=1;
				JComponent propertyComponent=createComponentForProperty(device, propName);
				//System.out.println("sddsad "+e.getKey()+" "+propName+" "+propertyComponent.getClass());
				thisPanel.add(propertyComponent,c);
				
				c.gridy++;
				}
			}
	
		
		
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bNewGroup)
			{
			new NewConfigGroupWindow();
			}
		}
	
	
	
	public void dataChangedEvent()
		{
//		objectCombo.updateList();
		}

	public void windowEventUserLoadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}
	public void windowFreeResources()
		{
		}
	

	public void hardwareGroupsChanged()
		{
		updateAllPanel(allPropertyPanel);
		updatePalettePanel(defaultPropertyPanel);
		setVisible(true);
		revalidate();
		repaint();
		}
	
	
	

	/**
	 * Create a GUI component to set a property
	 */
	public static JComponent createComponentForProperty(final EvDevice hw, final String propName)
		{
		final DevicePropertyType pt = hw.getPropertyTypes().get(propName);
		if (pt.readOnly)
			{
			return new JLabel(hw.getPropertyValue(propName));
			}
		else if (pt.isBoolean)
			{
			JCheckBox b = new JCheckBox();
			b.setSelected(hw.getPropertyValueBoolean(propName));
			b.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					hw.setPropertyValue(propName, ((JCheckBox) e.getSource()).isSelected());
					}
				});
			return b;
			}
		else if (!pt.categories.isEmpty())
			{
			Vector<String> cats=new Vector<String>(pt.categories);
			Collections.sort(cats, EvStringUtil.getNaturalComparatorAscii());
			JSmartToggleCombo c = new JSmartToggleCombo(cats);
			c.setSelectedItem(hw.getPropertyValue(propName));
			c.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					hw.setPropertyValue(propName, (String) ((JSmartToggleCombo) e.getSource()).getSelectedItem());
					}
				});
			return c;
			}
		else if(pt.isString)
			{
			final JTextField b=new JTextField(hw.getPropertyValue(propName));
			final Color defaultBG=b.getBackground();
			
			b.getDocument().addDocumentListener(new DocumentListener()
				{
				public void changedUpdate(DocumentEvent e){sendAction();}
				public void insertUpdate(DocumentEvent e){sendAction();}
				public void removeUpdate(DocumentEvent e){sendAction();}
				private void sendAction()
					{
					b.setBackground(Color.YELLOW);
					}
				});	
			
			b.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					b.setBackground(defaultBG);
					String value = ((JTextField) e.getSource()).getText();
					hw.setPropertyValue(propName, value);
					}
				});
			return b;
			}
		else //if (pt.hasRange)
			{
			double doubleval=0;
			try
				{
				doubleval=Double.parseDouble(hw.getPropertyValue(propName));
				}
			catch (NumberFormatException e1)
				{
				System.out.println("Could not parse value "+e1.getMessage());
				//e1.printStackTrace();
				}
			
			final JNumericField b=new JNumericField(doubleval);
			final Color defaultBG=b.getBackground();
			// Override: Exposure, Gain

			b.getDocument().addDocumentListener(new DocumentListener()
				{
				public void changedUpdate(DocumentEvent e){sendAction();}
				public void insertUpdate(DocumentEvent e){sendAction();}
				public void removeUpdate(DocumentEvent e){sendAction();}
				private void sendAction()
					{
					b.setBackground(Color.YELLOW);
					}
				});	
			
			b.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					b.setBackground(defaultBG);
					String value = ((JNumericField) e.getSource()).getText();
					double d;
					try
						{
						d = Double.parseDouble(value);
						} 
					catch(NumberFormatException ex) 
						{
						((JNumericField) e.getSource()).setText(hw.getPropertyValue(propName));
						return;
						}
					
					if(pt.hasRange)
						{
						if(pt.rangeLower<=d && pt.rangeUpper >=d)
							hw.setPropertyValue(propName, value);
						else 
							{ 
							((JNumericField) e.getSource()).setText(hw.getPropertyValue(propName));
							return;
							}
						}
					else
						{
						hw.setPropertyValue(propName, value);
						}
					}
				});
			return b;
			}
		
		}
	
	
	@Override
	public String windowHelpTopic()
		{
		return "Controlling the microscope";
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
			{
			public void newBasicWindow(EvBasicWindow w)
				{
				w.addHook(this.getClass(),new Hook());
				}
			class Hook implements EvBasicWindowHook, ActionListener
				{
				public void createMenus(EvBasicWindow w)
					{
					JMenuItem mi=new JMenuItem("Hardware control",new ImageIcon(getClass().getResource("iconWindow.png")));
					mi.addActionListener(this);
					EvBasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new HardwareControlWindow();
					}
	
				public void buildMenu(EvBasicWindow w){}
				}
			});
		
		
		
		}
	
	
	
	
	}
