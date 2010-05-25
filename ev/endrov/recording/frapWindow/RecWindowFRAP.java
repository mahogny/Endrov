/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.frapWindow;


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
import endrov.recording.RecordingResource;
import endrov.recording.recmetBurst.EvBurstAcquisition;
import endrov.roi.ROI;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

/**
 * FRAP acquisition
 * @author Johan Henriksson 
 */
public class RecWindowFRAP extends BasicWindow implements ActionListener, EvFRAPAcquisition.Listener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
	
	//private JCheckBox cDuration=new JCheckBox("Duration");
	private JButton bStartStop=new JButton("Start");
	private SpinnerSimpleEvDecimal spRecoveryTime=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spBleachTime=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spExpTime=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spRate=new SpinnerSimpleEvDecimal();
	
	/*
	private JComboBox cDurationUnit=new JComboBox(new Object[]{"Frames","Seconds"});
	private JComboBox cRateUnit=new JComboBox(new Object[]{"Hz","ms"});
	private JCheckBox cSwapEarly=new JCheckBox("Early swap to disk"); 
	*/
	private JLabel labelStatus=new JLabel("Status: Stopped");

	private EvFRAPAcquisition acq=new EvFRAPAcquisition();
	private EvFRAPAcquisition.AcqThread thread;
	
	private EvComboObject objectCombo=new EvComboObject(new LinkedList<EvObject>(), true, false)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return cont instanceof EvContainer;
			}
		};

		
	private EvComboObject roiCombo=new EvComboObject(new LinkedList<EvObject>(), true, false)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return cont instanceof ROI;
			}
		};
	
	private JTextArea tStoreName=new JTextArea("ch");

	
	public RecWindowFRAP()
		{
		this(new Rectangle(300,120));
		}
	
	public RecWindowFRAP(Rectangle bounds)
		{
		
		roiCombo.setRoot(RecordingResource.getData());
		
//		acq.addListener(this);
		
		//cDuration.setToolTipText("Limit duration or run indefinetely");
		
		spRate.setDecimalValue(new EvDecimal(10));
		spBleachTime.setDecimalValue(new EvDecimal(10));
		
		///////////////// Acquire ///////////////////////////////////////

		
		
		//Create new data
		//Select root
		//Select name of channel - only if not RGB
		
		
			
		//tChannelName.setToolTipText("Name of channel - Used as a prefix if the camera does RGB");
		
		
		////////////////////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutEvenVertical(
				
				//Camera
				//ROI
				//Bleach time
				//Tot time
				//Interval
				
				
				
				EvSwingUtil.layoutLCR(
						new JLabel("ROI"),
						roiCombo,
						null
						),

				EvSwingUtil.layoutLCR(
						new JLabel("Bleach time"),
						spRate,
						new JLabel("s")
						),

				EvSwingUtil.layoutLCR(
						new JLabel("Recovery time"),
						spRecoveryTime,
						new JLabel("s")
						),

				EvSwingUtil.layoutLCR(
						new JLabel("Exposure time"),
						spExpTime,
						new JLabel("ms")
						),
				
				EvSwingUtil.layoutLCR(
						new JLabel("Sampling intervals"),
						spRate,
						new JLabel("ms")
						),
						
				EvSwingUtil.layoutLCR(
						null,
						labelStatus,
						null
						),
				
				EvSwingUtil.layoutLCR(
						objectCombo,
						tStoreName,
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
				/*
				acq.channelName=tStoreName.getText();
				
				
				if(cDuration.isSelected())
					{
					acq.duration=spDuration.getDecimalValue();
					acq.durationUnit=(String)cDurationUnit.getSelectedItem();
					}
				
				acq.rate=spRate.getDecimalValue();
				acq.rateUnit=(String)cRateUnit.getSelectedItem();
				
				acq.earlySwap=cSwapEarly.isSelected();
				*/
				acq.container=objectCombo.getSelectedObject();
		
				thread=acq.startAcquisition();
				//thread.startAcquisition();
				}
			
			}
			
		
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		
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
		new RecWindowFRAP();
		
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
					JMenuItem mi=new JMenuItem("FRAP acquisition",new ImageIcon(getClass().getResource("tangoCamera.png")));
					mi.addActionListener(this);
					BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowFRAP();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		
		
		}
	
	
	
	}
