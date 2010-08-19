/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;


import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.BasicWindowExtension;
import endrov.basicWindow.BasicWindowHook;
import endrov.data.EvData;
import endrov.util.EvSwingUtil;

/**
 * Hardware Configuration window
 * @author Johan Henriksson 
 */
public class EvHardwareConfigWindow extends BasicWindow
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
	private JList hwList;
	
	
	//JButton bAdd=new JButton("Add");
	//JButton bRemove=new JButton("Remove");
	//private JButton bImport=new JButton("Import settings");
	//private JButton bExport=new JButton("Export settings");
	private JButton bConfig=new JButton("Configure device");
	//JButton bAutodetect=new JButton("Autodetect");
	
	
	private static class HWListItem 
		{
		EvDevicePath name;
		public String toString()
			{
			return name+" :: "+EvHardware.getDevice(name).getDescName();
			}
		
		@Override
		public boolean equals(Object obj)
			{
			if(obj instanceof HWListItem)
				{
				HWListItem o=(HWListItem)obj;
				return o.name.equals(name);
				}
			else
				return false;
			}
		
		@Override
		public int hashCode()
			{
			return name.hashCode();
			}
		
		
		}
	
	
	public EvHardwareConfigWindow()
		{
		this(new Rectangle(400,300));
		}
	
	public EvHardwareConfigWindow(Rectangle bounds)
		{
		hwList=new JList();
		updateHardwareList();
		
		JScrollPane spane=new JScrollPane(hwList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		setLayout(new BorderLayout());
		add(spane,BorderLayout.CENTER);
		add(
				EvSwingUtil.layoutEvenVertical
				(
						//bImport, bExport,
						bConfig
				),
				BorderLayout.SOUTH);

		bConfig.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e)
			{
			HWListItem o=(HWListItem)hwList.getSelectedValue();
			EvDevice dev=o.name.getDevice();
			if(dev!=null && dev.hasConfigureDialog())
				dev.openConfigureDialog();
			}
		});
		
		//Window overall things
		setTitleEvWindow("Hardware Configuration");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		}
	
	
	private Vector<HWListItem> getHardwareList()
		{
		Vector<HWListItem> hwNames=new Vector<HWListItem>();
		for(EvDevicePath hw:EvHardware.getDeviceList())
			{
			HWListItem item=new HWListItem();
			item.name=hw;
			hwNames.add(item);
			}
		return hwNames;
		}
	
	/**
	 * Some listener would be better
	 */
	private void updateHardwareList()
		{
		Vector<HWListItem> curItems=new Vector<HWListItem>();
		for(int i=0;i<hwList.getModel().getSize();i++)
			curItems.add((HWListItem)hwList.getModel().getElementAt(i));

		Vector<HWListItem> newItems=getHardwareList();
		if(!newItems.equals(curItems))
			{
			hwList.setListData(newItems);
			hwList.repaint();
			}
		
		}
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		updateHardwareList();
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		} 
	public void freeResources(){}

	

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
				JMenuItem mi=new JMenuItem("Hardware manager",new ImageIcon(getClass().getResource("gnomeHardwareCard.png")));
				mi.addActionListener(this);
				w.addMenuWindow(mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new EvHardwareConfigWindow();
				}

			public void buildMenu(BasicWindow w){}
			}
			});
		
		
		
		}
	}
