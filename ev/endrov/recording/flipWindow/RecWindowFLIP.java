/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.flipWindow;


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
import endrov.roi.ROI;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

/**
 * FRAP acquisition
 * @author Johan Henriksson 
 */
public class RecWindowFLIP extends BasicWindow implements ActionListener, EvFLIPAcquisition.Listener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	private JButton bStartStop=new JButton("Start");
	private SpinnerSimpleEvDecimal spRecoveryTime=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spBleachTime=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spRate=new SpinnerSimpleEvDecimal();

	private EvFLIPAcquisition acq=new EvFLIPAcquisition();
	private EvFLIPAcquisition.AcqThread thread;
	
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
	
	private JTextField tStoreName=new JTextField("frap");

	
	public RecWindowFLIP()
		{
		this(new Rectangle(300,120));
		}
	
	public RecWindowFLIP(Rectangle bounds)
		{
		
		roiCombo.setRoot(RecordingResource.getData());
		
		acq.addListener(this);
		
		//cDuration.setToolTipText("Limit duration or run indefinetely");
		
		spRecoveryTime.setDecimalValue(new EvDecimal(10));
		spRate.setDecimalValue(new EvDecimal(1));
		spBleachTime.setDecimalValue(new EvDecimal(1));
//		spExpTime.setDecimalValue(new EvDecimal(100));
		
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
						spBleachTime,
						new JLabel("s")
						),

				EvSwingUtil.layoutLCR(
						new JLabel("Recovery time"),
						spRecoveryTime,
						new JLabel("s")
						),

				EvSwingUtil.layoutLCR(
						new JLabel("Sampling intervals"),
						spRate,
						new JLabel("s")
						),

				EvSwingUtil.layoutLCR(
						new JLabel("Store in"),
						objectCombo,
						new JLabel("ms")
						),

				
				EvSwingUtil.layoutLCR(
						new JLabel("Object name"),
						tStoreName,
						bStartStop
						)//,
				
				//labelStatus
				
				),
				BorderLayout.CENTER);
		
		bStartStop.addActionListener(this);
		
		//Window overall things
		setTitleEvWindow("FRAP acquisition");
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
				
				acq.setBleachTime(spBleachTime.getDecimalValue());
				acq.setContainer(objectCombo.getSelectedObject());
				acq.setContainerStoreName(tStoreName.getText());
				//acq.setExpTime(spExpTime.getDecimalValue());
				acq.setRate(spRate.getDecimalValue());
				acq.setRecoveryTime(spRecoveryTime.getDecimalValue());
				acq.setRoi((ROI)roiCombo.getSelectedObject());
				
				thread=acq.startAcquisition();

				}
			
			}
			
		
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		roiCombo.updateList();
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
		new RecWindowFLIP();
		
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
					new RecWindowFLIP();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		
		
		}
	
	
	
	}
