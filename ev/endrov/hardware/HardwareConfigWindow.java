/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;


import java.awt.BorderLayout;
import java.awt.GridLayout;
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

/**
 * Hardware Configuration window
 * @author Johan Henriksson 
 */
public class HardwareConfigWindow extends BasicWindow
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
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
				new HardwareConfigWindow();
				}

			public void buildMenu(BasicWindow w){}
			}
			});
		
		
		
		
/*		EV.personalConfigLoaders.put("consolewindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
					int x=e.getAttribute("x").getIntValue();
					int y=e.getAttribute("y").getIntValue();
					int w=e.getAttribute("w").getIntValue();
					int h=e.getAttribute("h").getIntValue();
					new ConsoleWindow(x,y,w,h);
					}
				catch (DataConversionException e1)
					{
					e1.printStackTrace();
					}
				}
			public void savePersonalConfig(Element e){}
			});
			*/
		}
	
	
	//JButton bAdd=new JButton("Add");
	//JButton bRemove=new JButton("Remove");
	JButton bImport=new JButton("Import");
	JButton bExport=new JButton("Export");
	JButton bConfig=new JButton("Config");
	//JButton bAutodetect=new JButton("Autodetect");
	
	
	private static class HWListItem
		{
		DevicePath name;
		public String toString()
			{
			return name+" :: "+EvHardware.getDevice(name).getDescName();
			}
		}
	
	public HardwareConfigWindow()
		{
		this(new Rectangle(400,300));
		}
	
	public HardwareConfigWindow(Rectangle bounds)
		{
		Vector<HWListItem> hwNames=new Vector<HWListItem>();
		for(DevicePath hw:EvHardware.getDeviceList())
			{
			HWListItem item=new HWListItem();
			item.name=hw;
			hwNames.add(item);
			}
		
		final JList hwList=new JList(hwNames);
		JScrollPane spane=new JScrollPane(hwList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		JPanel bpu=new JPanel(new GridLayout(1,3));
		bpu.add(bImport);
		bpu.add(bExport);
		bpu.add(bConfig);
		/*
		JPanel bpl=new JPanel(new GridLayout(1,3));
		bpl.add(bLoad);
		bpl.add(bSave);
		bpl.add(bAutodetect);
*/
		JPanel bp=new JPanel(new GridLayout(1,1));
		bp.add(bpu);
		//bp.add(bpl);
		
		setLayout(new BorderLayout());
		add(spane,BorderLayout.CENTER);
		add(bp,BorderLayout.SOUTH);
		
		
		
		bConfig.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e)
			{
			HWListItem o=(HWListItem)hwList.getSelectedValue();
			Device dev=o.name.getDevice();
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		// TODO Auto-generated method stub
		
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		// TODO Auto-generated method stub
		
		} 
	public void freeResources(){}
	
	}
