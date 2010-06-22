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

	private JButton bStartStop=new JButton("Start");
	private SpinnerSimpleEvDecimal spRecoveryTime=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spBleachTime=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spRate=new SpinnerSimpleEvDecimal();

	private EvFRAPAcquisition acq=new EvFRAPAcquisition();
	private EvFRAPAcquisition.AcqThread thread;
	
	private JLabel labelStatus=new JLabel(" ");
	
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

	
	public RecWindowFRAP()
		{
		this(new Rectangle(300,120));
		}
	
	public RecWindowFRAP(Rectangle bounds)
		{
		
		roiCombo.setRoot(RecordingResource.getData());
		
		acq.addListener(this);
		
		//cDuration.setToolTipText("Limit duration or run indefinetely");
		
		spRecoveryTime.setDecimalValue(new EvDecimal(10));
		spRate.setDecimalValue(new EvDecimal(1));
		spBleachTime.setDecimalValue(new EvDecimal(1));
//		spExpTime.setDecimalValue(new EvDecimal(100));
		
		labelStatus.setBorder(BorderFactory.createTitledBorder("Status"));
		
		
		////////////////////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutEvenVertical(
				
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
						),

				EvSwingUtil.layoutLCR(
						new JLabel("Store in"),
						objectCombo,
						null
						),
				
				EvSwingUtil.layoutLCR(
						new JLabel("Object name"),
						tStoreName,
						bStartStop
						),
				
				labelStatus
				
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
				
				acq.bleachTime=spBleachTime.getDecimalValue();
				acq.container=objectCombo.getSelectedObject();
				acq.containerStoreName=tStoreName.getText();
				//acq.setExpTime(spExpTime.getDecimalValue());
				acq.rate=spRate.getDecimalValue();
				acq.recoveryTime=spRecoveryTime.getDecimalValue();
				acq.roi=(ROI)roiCombo.getSelectedObject();
				
				if(acq.container==null)
					showErrorDialog("Need to select a place to store the acquisition (e.g. File -> New)");
				else if(acq.roi==null)
					showErrorDialog("Need to select a ROI");
				else
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
		new RecWindowFRAP();
		
		}

	

	public void acqStopped()
		{
		SwingUtilities.invokeLater(new Runnable()
			{
			public void run()
				{
				bStartStop.setText("Start");
				thread=null;
				labelStatus.setText(" ");
				}
			});
		}
	
	public void newStatus(final String s)
		{
		SwingUtilities.invokeLater(new Runnable()
			{
			public void run()
				{
				labelStatus.setText(s);
				}
			});
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
