/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware.gui;


import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jdom.*;

import endrov.data.EvData;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;

/**
 * Hardware Configuration window
 * @author Johan Henriksson 
 */
public class EvHardwareConfigWindow extends EvBasicWindow
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	private JTable hwTable=new JTable();
	private JButton bConfig=new JButton("Configure device");

	
	public EvHardwareConfigWindow()
		{
		this(new Rectangle(400,300));
		}
	
	public EvHardwareConfigWindow(Rectangle bounds)
		{
		updateHardwareList();
		
		JScrollPane spane=new JScrollPane(hwTable,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		setLayout(new BorderLayout());
		add(spane,BorderLayout.CENTER);
		add(bConfig,BorderLayout.SOUTH);

		bConfig.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e)
			{
			int row=hwTable.getSelectedRow();
			if(row!=-1)
				{
				DefaultTableModel model=(DefaultTableModel)hwTable.getModel();
				String s=(String)model.getValueAt(row, 0);

				EvDevice dev=new EvDevicePath(s).getDevice();
				if(dev!=null && dev.hasConfigureDialog())
					dev.openConfigureDialog();
				}

			}
		});
		
		//Window overall things
		setTitleEvWindow("Hardware Configuration");
		packEvWindow();
		setBoundsEvWindow(bounds);
		setVisibleEvWindow(true);
		}
	
	
	/**
	 * Update the list of hardware
	 */
	private void updateHardwareList()
		{
		DefaultTableModel model=new DefaultTableModel(new String[][]{}, new String[]{"Device","Description"}){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column)
				{
				return false;
				}
			};
		for(EvDevicePath hw:EvHardware.getDeviceList())
			{
			model.addRow(new String[]{
				hw.toString(),
				hw.getDevice().getDescName()
			});
			}
		hwTable.setModel(model);
		}
	
	
	
	public void dataChangedEvent()
		{
		updateHardwareList();
		}

	public void windowEventUserLoadedFile(EvData data){}
	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}
	public void windowFreeResources(){}

	@Override
	public String windowHelpTopic()
		{
		return "The hardware manager";
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
				w.basicWindowExtensionHook.put(this.getClass(),new Hook());
				}
			class Hook implements EvBasicWindowHook, ActionListener
			{
			public void createMenus(EvBasicWindow w)
				{
				JMenuItem mi=new JMenuItem("Hardware manager",new ImageIcon(getClass().getResource("gnomeHardwareCard.png")));
				mi.addActionListener(this);
				w.addMenuWindow(mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new EvHardwareConfigWindow();
				}

			public void buildMenu(EvBasicWindow w){}
			}
			});
		
		
		
		}
	}
