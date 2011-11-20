/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.bleachWindow;


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
 * Just bleach an area
 * @author Johan Henriksson 
 */
public class RecWindowQuickBleach extends BasicWindow implements ActionListener, QuickBleach.Listener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
	private JButton bStartStop=new JButton("Start");
	private SpinnerSimpleEvDecimal spBleachTime=new SpinnerSimpleEvDecimal();

	private QuickBleach acq=new QuickBleach();
	private QuickBleach.AcqThread thread;
	
	private JLabel labelStatus=new JLabel(" ");
	
	private EvComboObject roiCombo=new EvComboObject(new LinkedList<EvObject>(), true, false)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return cont instanceof ROI;
			}
		};
	
	public RecWindowQuickBleach()
		{
		this(new Rectangle(300,120));
		}
	
	public RecWindowQuickBleach(Rectangle bounds)
		{
		
		roiCombo.setRoot(RecordingResource.getData());
		
		acq.addListener(this);
		
		spBleachTime.setDecimalValue(new EvDecimal(1));
	
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
						
				bStartStop,

				labelStatus
				
				),
				BorderLayout.CENTER);
		
		bStartStop.addActionListener(this);
		
		//Window overall things
		setTitleEvWindow("Quick bleach");
		packEvWindow();
		setVisibleEvWindow(true);
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bStartStop)
			{
			if(thread!=null)
				{
				thread.tryStop();
				}
			else
				{
				bStartStop.setText("Stop");
				
				acq.setBleachTime(spBleachTime.getDecimalValue());
				acq.setRoi((ROI)roiCombo.getSelectedObject());
				
				labelStatus.setText("Bleaching");
				
				thread=acq.startAcquisition();

				}
			}
		}
	
	
	public void dataChangedEvent()
		{
		roiCombo.updateList();
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		
		} 
	public void freeResources()
		{
		}
	
	public void acqStopped()
		{
		bStartStop.setText("Start");
		labelStatus.setText("");
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
					JMenuItem mi=new JMenuItem("Quick bleach",new ImageIcon(getClass().getResource("iconBleach.png")));
					mi.addActionListener(this);
					BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowQuickBleach();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		
		
		}
	
	
	
	}
