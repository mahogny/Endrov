/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetFLIP;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.*;

import org.jdom.*;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.EvComboObject;
import endrov.gui.component.JSpinnerSimpleEvDecimal;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.recording.EvAcquisition;
import endrov.recording.RecordingResource;
import endrov.recording.recmetMultidim.RecWidgetAcquire;
import endrov.roi.ROI;
import endrov.util.math.EvDecimal;

/**
 * FLIP acquisition
 * @author Johan Henriksson 
 */
public class RecWindowFLIP extends EvBasicWindow 
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	private JSpinnerSimpleEvDecimal spNumRepeats=new JSpinnerSimpleEvDecimal();
	private JSpinnerSimpleEvDecimal spBleachTime=new JSpinnerSimpleEvDecimal();
	private JSpinnerSimpleEvDecimal spRate=new JSpinnerSimpleEvDecimal();

	private EvFLIPAcquisition acq=new EvFLIPAcquisition();
	
	private EvComboObject roiBleachCombo=new EvComboObject(new LinkedList<EvObject>(), true, false)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return cont instanceof ROI;
			}
		};

	private EvComboObject roiObserveCombo=new EvComboObject(new LinkedList<EvObject>(), true, true)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return cont instanceof ROI;
			}
		};

	private RecWidgetAcquire wAcq=new RecWidgetAcquire()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public boolean getAcquisitionSettings()
					{
					acq.bleachTime=spBleachTime.getDecimalValue();
					acq.rate=spRate.getDecimalValue();
					acq.recoveryTime=spNumRepeats.getDecimalValue();
					acq.roiBleach=(ROI)roiBleachCombo.getSelectedObject();
					acq.roiObserve=(ROI)roiObserveCombo.getSelectedObject();
					acq.numRepeats=spNumRepeats.getDecimalValue().intValue();
					
					if(acq.container==null)
						{
						showErrorDialog("Need to select a place to store the acquisition (e.g. File -> New)");
						return false;
						}
					else if(acq.roiBleach==null)
						{
						showErrorDialog("Need to select a ROI to bleach");
						return false;
						}
					else
						return true;
					}
				
				@Override
				public EvAcquisition getAcquisition()
					{
					return acq;
					}
			};

	
	
	public RecWindowFLIP()
		{
		
		roiBleachCombo.setRoot(RecordingResource.getData());
		roiObserveCombo.setRoot(RecordingResource.getData());
		
		wAcq.setStoreName("flip");
		
		spNumRepeats.setDecimalValue(new EvDecimal(10));
		spRate.setDecimalValue(new EvDecimal(2));
		spBleachTime.setDecimalValue(new EvDecimal("0.1"));
		
		////////////////////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutCompactVertical(
				
				EvSwingUtil.withTitledBorder("Settings", 
						EvSwingUtil.layoutCompactVertical(
								EvSwingUtil.layoutLCR(
										new JLabel("Bleach ROI"),
										roiBleachCombo,
										null
										),
										
								EvSwingUtil.layoutLCR(
										new JLabel("Observe ROI"),
										roiObserveCombo,
										null
										),

								EvSwingUtil.layoutLCR(
										new JLabel("Bleach time"),
										spBleachTime,
										new JLabel("[s]")
										),

								EvSwingUtil.layoutLCR(
										new JLabel("Num.repeats"),
										spNumRepeats,
										new JLabel("[-]")
										),

								EvSwingUtil.layoutLCR(
										new JLabel("Sampling intervals"),
										spRate,
										new JLabel("[s]")
										)
								)
						
						),
				
				wAcq
				),
				BorderLayout.CENTER);
		
		//Window overall things
		setTitleEvWindow("FLIP acquisition");
		packEvWindow();
		setVisibleEvWindow(true);
		}
	
	
	
	public void dataChangedEvent()
		{
		roiBleachCombo.updateList();
		roiObserveCombo.updateList();
		wAcq.dataChangedEvent();
		}

	public void windowEventUserLoadedFile(EvData data){}
	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}
	public void windowFreeResources()
		{
		}
	

	@Override
	public String windowHelpTopic()
		{
		return null;
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
					JMenuItem mi=new JMenuItem("Acquire: FLIP",new ImageIcon(getClass().getResource("tangoCamera.png")));
					mi.addActionListener(this);
					EvBasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowFLIP();
					}
	
				public void buildMenu(EvBasicWindow w){}
				}
			});
		
		
		
		}
	
	
	}
