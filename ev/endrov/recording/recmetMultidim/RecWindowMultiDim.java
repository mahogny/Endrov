/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetMultidim;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.jdom.*;

import endrov.data.EvData;
import endrov.gui.EvSwingUtil;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.recording.EvAcquisition;
import endrov.recording.widgets.RecWidgetChannel;
import endrov.recording.widgets.RecWidgetOrder;
import endrov.recording.widgets.RecWidgetPositions;
import endrov.recording.widgets.RecWidgetRecDesc;
import endrov.recording.widgets.RecWidgetSlices;
import endrov.recording.widgets.RecWidgetTimes;

/**
 * Multi-dimension acquisition
 * @author Johan Henriksson 
 */
public class RecWindowMultiDim extends EvBasicWindow
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	private EvMultidimAcquisition acq=new EvMultidimAcquisition();

	
	private RecWidgetSlices wslices=new RecWidgetSlices();
	private RecWidgetTimes wtimes=new RecWidgetTimes();
	private RecWidgetChannel wchans=new RecWidgetChannel();
	private RecWidgetOrder worder=new RecWidgetOrder();
	private RecWidgetPositions wpos=new RecWidgetPositions();
	private RecWidgetRecDesc wdesc=new RecWidgetRecDesc();
	private RecWidgetAcquire wacq=new RecWidgetAcquire()
		{
		private static final long serialVersionUID = 1L;
		
		@Override
		public EvAcquisition getAcquisition()
			{
			return acq;
			}
		@Override
		public boolean getAcquisitionSettings() throws Exception
			{
			acq.order=worder.getSettings();
			acq.channel=wchans.getSettings();
			acq.desc=wdesc.getSettings();
			acq.slices=wslices.getSettings();
			acq.times=wtimes.getSettings();
			acq.positions=wpos.getSettings();
			return true;
			}
		};
	
	public RecWindowMultiDim()
		{
		setLayout(new BorderLayout());

		add(
				EvSwingUtil.layoutCompactVertical(
					wslices,wtimes,worder,wacq
					),
				BorderLayout.EAST);
		add(
				EvSwingUtil.layoutACB(
					EvSwingUtil.layoutCompactVertical(wchans,wpos), 
					wdesc, 
					null),
				BorderLayout.CENTER);
		
		
		//Window overall things
		setTitleEvWindow("Multidimensional acquisition");
		packEvWindow();
		setBoundsEvWindow(700, null);
		setVisibleEvWindow(true);
		}
	
	
	
	
	public void dataChangedEvent()
		{
		wchans.dataChangedEvent();
		wpos.dataChangedEvent();
		wacq.dataChangedEvent();
		}

	public void windowEventUserLoadedFile(EvData data){}
	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}
	public void windowFreeResources()
		{
		}
	
	public static void main(String[] args)
		{
		new RecWindowMultiDim();
		
		}

	

	@Override
	public String windowHelpTopic()
		{
		return "The multi-dimensional acquisition window";
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
					JMenuItem mi=new JMenuItem("Acquire: Multi-dim",new ImageIcon(getClass().getResource("jhMultidimWindow.png")));
					mi.addActionListener(this);
					EvBasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowMultiDim();
					}
	
				public void buildMenu(EvBasicWindow w){}
				}
			});
		
		}
	
	
	}
