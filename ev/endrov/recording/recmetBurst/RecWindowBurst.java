/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetBurst;


import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

/**
 * Burst acquisition
 * @author Johan Henriksson 
 */
public class RecWindowBurst extends BasicWindow implements ActionListener, EvBurstAcquisition.Listener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
	private JCheckBox cDuration=new JCheckBox("Duration");
	private SpinnerSimpleEvDecimal spDuration=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spRate=new SpinnerSimpleEvDecimal();
	private JComboBox cDurationUnit=new JComboBox(new Object[]{"Frames","Seconds"});
	private JComboBox cRateUnit=new JComboBox(new Object[]{"Hz","ms"});
	private JButton bStartStop=new JButton("Start");
	private JCheckBox cSwapEarly=new JCheckBox("Early swap to disk"); 
	private JLabel labelStatus=new JLabel("Status: Stopped");
	
	private EvBurstAcquisition acq=new EvBurstAcquisition();
	private EvBurstAcquisition.AcqThread thread;

	private EvComboObject objectCombo=new EvComboObject(new LinkedList<EvObject>(), true, false)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return cont instanceof EvData;
			}
		};
	
	private JTextField tChannelName=new JTextField("ch");

	
	public RecWindowBurst()
		{
		this(new Rectangle(300,120));
		}
	
	public RecWindowBurst(Rectangle bounds)
		{
		acq.addListener(this);
		
		cSwapEarly.setToolTipText("Helps for longer recordings, otherwise might affect performance negatively");
		cDuration.setToolTipText("Limit duration or run indefinetely");
		
		spRate.setDecimalValue(new EvDecimal(10));
		spDuration.setDecimalValue(new EvDecimal(10));
		
		///////////////// Acquire ///////////////////////////////////////

		
		
		//Create new data
		//Select root
		//Select name of channel - only if not RGB
		
		
			
		tChannelName.setToolTipText("Name of channel - Used as a prefix if the camera does RGB");
		
		
		////////////////////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutEvenVertical(
				
				
				
				EvSwingUtil.layoutLCR(
						cDuration,
						spDuration,
						cDurationUnit
						),

				EvSwingUtil.layoutLCR(
						new JLabel("Rate"),
						spRate,
						cRateUnit
						),
						
				cSwapEarly,
				
				EvSwingUtil.layoutLCR(
						null,
						labelStatus,
						null
						),
				
				EvSwingUtil.layoutLCR(
						objectCombo,
						tChannelName,
						bStartStop
						)
				
				
				),
				BorderLayout.CENTER);
		
		bStartStop.addActionListener(this);
		
		//Window overall things
		setTitleEvWindow("Burst acquisition");
		packEvWindow();
		setVisibleEvWindow(true);
		//setBoundsEvWindow(bounds);
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		
		if(e.getSource()==bStartStop)
			{
			if(thread!=null)
				{
				thread.tryStop();
				//System.out.println("----stopping acquisition----");
				}
			else
				{
				bStartStop.setText("Stop");
				acq.channelName=tChannelName.getText();
				
				if(cDuration.isSelected())
					{
					acq.duration=spDuration.getDecimalValue();
					acq.durationUnit=(String)cDurationUnit.getSelectedItem();
					}
				
				acq.rate=spRate.getDecimalValue();
				acq.rateUnit=(String)cRateUnit.getSelectedItem();
				
				acq.earlySwap=cSwapEarly.isSelected();
				acq.container=objectCombo.getSelectedObject();
		
				thread=acq.startAcquisition();
				//thread.startAcquisition();
				}
			
			}
		
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		objectCombo.updateList();
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
		new RecWindowBurst();
		
		}

	

	public void acqStopped()
		{
		bStartStop.setText("Start");
		thread=null;
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
					JMenuItem mi=new JMenuItem("Acquire: Burst",new ImageIcon(getClass().getResource("tangoCamera.png")));
					mi.addActionListener(this);
					BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowBurst();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		
		
		}
	
	
	
	}
