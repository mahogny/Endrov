/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetFRAP;


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
import endrov.recording.EvAcquisition;
import endrov.recording.RecordingResource;
import endrov.recording.recmetMultidim.RecWidgetAcquire;
import endrov.roi.ROI;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

/**
 * FRAP acquisition
 * @author Johan Henriksson 
 */
public class RecWindowFRAP extends BasicWindow 
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	private SpinnerSimpleEvDecimal spRecoveryTime=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spBleachTime=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spRate=new SpinnerSimpleEvDecimal();

	private EvFRAPAcquisition acq=new EvFRAPAcquisition();

	RecWidgetAcquire wAcq=new RecWidgetAcquire()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean getAcquisitionSettings()
				{
				
				acq.bleachTime=spBleachTime.getDecimalValue();
				acq.rate=spRate.getDecimalValue();
				acq.recoveryTime=spRecoveryTime.getDecimalValue();
				acq.roi=(ROI)roiCombo.getSelectedObject();
				
				if(acq.roi==null)
					{
					showErrorDialog("Need to select a ROI");
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

		
	private EvComboObject roiCombo=new EvComboObject(new LinkedList<EvObject>(), true, false)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return cont instanceof ROI;
			}
		};

	
	public RecWindowFRAP()
		{
		this(new Rectangle(300,120));
		}
	
	public RecWindowFRAP(Rectangle bounds)
		{
		
		roiCombo.setRoot(RecordingResource.getData());
		
		spRecoveryTime.setDecimalValue(new EvDecimal(10));
		spRate.setDecimalValue(new EvDecimal(1));
		spBleachTime.setDecimalValue(new EvDecimal(1));
		wAcq.setStoreName("frap");
		
		
		////////////////////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutCompactVertical(
				
				EvSwingUtil.withTitledBorder("Settings",
						EvSwingUtil.layoutEvenVertical(
								EvSwingUtil.layoutLCR(
										new JLabel("ROI"),
										roiCombo,
										null
										),

								EvSwingUtil.layoutLCR(
										new JLabel("Bleach time"),
										spBleachTime,
										new JLabel("[s]")
										),

								EvSwingUtil.layoutLCR(
										new JLabel("Recovery time"),
										spRecoveryTime,
										new JLabel("[s]")
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
		setTitleEvWindow("FRAP acquisition");
		packEvWindow();
		setVisibleEvWindow(true);
		//setBoundsEvWindow(bounds);
		}
	
	
	
	
	public void dataChangedEvent()
		{
		roiCombo.updateList();
		wAcq.dataChangedEvent();
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
					JMenuItem mi=new JMenuItem("Acquire: FRAP",new ImageIcon(getClass().getResource("tangoCamera.png")));
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
