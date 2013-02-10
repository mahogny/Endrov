/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.JSpinnerSimpleInteger;
import endrov.util.EvBrowserUtil;

/**
 * Widget for recording settings: Channel settings
 * @author Johan Henriksson
 *
 */
public class RecWidgetChannelDialog extends JDialog implements ActionListener
	{
	private static final long serialVersionUID = 1L;


	//Stack settings
	private final JSpinnerSimpleInteger spZinc=new JSpinnerSimpleInteger(0,0,100,1);
	private final JSpinnerSimpleInteger spZfirst=new JSpinnerSimpleInteger(0,0,1000,1); //z0
	private final JSpinnerSimpleInteger spZlast=new JSpinnerSimpleInteger(0,0,1000,1);
	private final JCheckBox cbLastZ=new JCheckBox("Last slice to capture:");
	
	//Frame settings
	private final JSpinnerSimpleInteger spTinc=new JSpinnerSimpleInteger(0,0,100,1);
	private final JSpinnerSimpleInteger spTfirst=new JSpinnerSimpleInteger(0,0,100000,1);
	private final JSpinnerSimpleInteger spTlast=new JSpinnerSimpleInteger(0,0,100000,1);
	private final JCheckBox cbLastT=new JCheckBox("Last frame to capture:");
	
	//Acquisition settings
	private final JSpinnerSimpleInteger spAveraging=new JSpinnerSimpleInteger(1,1,10,1);
	private final JCheckBox cbAdjustExposureTime=new JCheckBox("Adjust exposure time dynamically to expand dynamic range");

	private final JButton bCancel=new JButton("Cancel");
	private final JButton bOk=new JButton("OK");
	private final JButton bHelp=new JButton("Help");
	
	
	
	private RecSettingsChannel.OneChannel settings;
	
	public RecWidgetChannelDialog(RecSettingsChannel.OneChannel settings)
		{
		this.settings=settings;
		
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
		bHelp.addActionListener(this);
		
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutCompactVertical(
				EvSwingUtil.withTitledBorder("Stack settings", 
						EvSwingUtil.layoutEvenVertical(
								EvSwingUtil.withLabel("First slice to capture:", spZfirst),
								EvSwingUtil.layoutLCR(cbLastZ, spZlast, null),
								EvSwingUtil.withLabel("Skip slices between each slice captured:", spZinc)
								)
						),

				EvSwingUtil.withTitledBorder("Frame settings", 
						EvSwingUtil.layoutEvenVertical(
								EvSwingUtil.withLabel("First frame to capture:", spTfirst),
								EvSwingUtil.layoutLCR(cbLastT, spTlast, null),
								EvSwingUtil.withLabel("Skip frames between each frame captured:", spTinc)
								)
						),

				EvSwingUtil.withTitledBorder("Capture settings", 
						EvSwingUtil.layoutEvenVertical(
								EvSwingUtil.withLabel("Frame averaging:", spAveraging),
								cbAdjustExposureTime
								)
						),

				EvSwingUtil.layoutEvenHorizontal(bHelp, bCancel, bOk)
				),BorderLayout.CENTER);
		

		loadSettings();
		setTitle("Channel settings");
		pack();
		setModal(true);
		setLocationRelativeTo(null);
		setVisible(true);
		}
	
	/**
	 * Update controls from settings
	 */
	private void loadSettings()
		{
		spAveraging.setIntValue(settings.averaging);

		cbAdjustExposureTime.setSelected(settings.adjustRangeByExposure);
		
		spZfirst.setIntValue(settings.zFirst);
		if(settings.zLast!=null)
			spZlast.setIntValue(settings.zLast);
		cbLastZ.setSelected(settings.zLast!=null);

		spTfirst.setIntValue(settings.tFirst);
		if(settings.tLast!=null)
			spTlast.setIntValue(settings.tLast);
		cbLastT.setSelected(settings.tLast!=null);
		
		spZinc.setIntValue(settings.zIncrement-1);
		spTinc.setIntValue(settings.tIncrement-1);
		}
	
	
	
	/**
	 * Store control settings into the settings object
	 */
	private void storeSettings()
		{
		settings.adjustRangeByExposure=cbAdjustExposureTime.isSelected();

		settings.zFirst=spZfirst.getIntValue();
		if(cbLastZ.isSelected())
			settings.zLast=spZlast.getIntValue();
		else
			settings.zLast=null;
		
		settings.tFirst=spTfirst.getIntValue();
		if(cbLastT.isSelected())
			settings.tLast=spTlast.getIntValue();
		else
			settings.tLast=null;
		
		settings.zIncrement=spZinc.getIntValue()+1;
		settings.tIncrement=spTinc.getIntValue()+1;
		}
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bOk)
			{
			storeSettings();
			dispose();
			}
		else if(e.getSource()==bCancel)
			{
			dispose();
			}
		else if(e.getSource()==bHelp)
			{
			EvBrowserUtil.openWikiArticle("The_multi-dimensional_acquisition_window");
			}
		}
	
	
	public static void main(String[] args)
		{
		new RecWidgetChannelDialog(new RecSettingsChannel.OneChannel());
		}
	}
