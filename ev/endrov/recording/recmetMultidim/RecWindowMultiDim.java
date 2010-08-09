/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetMultidim;


import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.recording.EvAcquisition;
import endrov.recording.widgets.RecWidgetChannel;
import endrov.recording.widgets.RecWidgetOrder;
import endrov.recording.widgets.RecWidgetPositions;
import endrov.recording.widgets.RecWidgetRecDesc;
import endrov.recording.widgets.RecWidgetSlices;
import endrov.recording.widgets.RecWidgetTimes;
import endrov.util.EvSwingUtil;

/**
 * Multi-dimension acquisition
 * @author Johan Henriksson 
 */
public class RecWindowMultiDim extends BasicWindow
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
		public boolean getAcquisitionSettings()
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
		this(new Rectangle(800,660));
		}
	
	public RecWindowMultiDim(Rectangle bounds)
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
		setBoundsEvWindow(800, null);
		setVisibleEvWindow(true);
		}
	
	
	
	/**
	 * also: compensate for light loss in Z?
	 * 
	 * 
	 * lacking, from micromanager:
	 * 
	 * 
	 * 
	 * 
	 * positions: 
	 * 		edit XY list
	 * 
	 * slices:
	 * 		relative/absolute z
	 * 		[] keep shutter open
	 * 
	 * 
	 * autofocus:
	 * 		skip frame (...   some numeric editor
	 * 		options
	 * 
	 * 
	 * save images
	 * 		dir root
	 * 		display, all+other options
	 * 		name prefix
	 * 		comment
	 * 
	 * save/load
	 * 
	 * 
	 * this is how it got things:
	 * 
	 * 
	 * Channels
	 * 		channel group=
	 *	and table
	 *		configuration
	 *		exposure
	 *		z-offset
	 *		z-stack 
	 *
	 * time points
	 * 		number
	 * 		interval #
	 * 		interval unit		
	 * 
	 * 
	 * 
	 * 
	 * **
	 * mm has group editor. each property can be selected to be included.
	 * hard to edit afterwards IMO, must select all properties right the first time. 
	 * 
	 * 
	 * 
	 */
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		wchans.dataChangedEvent();
		wpos.dataChangedEvent();
		wacq.dataChangedEvent();
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		
		} 
	public void freeResources()
		{
		}
	
	public static void main(String[] args)
		{
		new RecWindowMultiDim();
		
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
					JMenuItem mi=new JMenuItem("Multi-dim acquisition",new ImageIcon(getClass().getResource("jhMultidimWindow.png")));
					mi.addActionListener(this);
					BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowMultiDim();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		}
	
	
	}
