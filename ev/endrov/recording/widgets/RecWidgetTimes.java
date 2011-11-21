/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.widgets;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import endrov.basicWindow.SpinnerSimpleEvFrame;
import endrov.basicWindow.SpinnerSimpleInteger;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.recording.HWTrigger;
import endrov.util.EvSwingUtil;

/**
 * Widget for recording settings: Time settings
 * @author Johan Henriksson
 *
 */
public class RecWidgetTimes extends JPanel
	{
	private static final long serialVersionUID = 1L;
	
	private SpinnerSimpleEvFrame spFreqDt=new SpinnerSimpleEvFrame();
	
	private JRadioButton rbFreqDt=new JRadioButton("∆t", true);
	private JRadioButton rbMaxSpeed=new JRadioButton("Maximum rate");
	private JRadioButton rbOnTrigger=new JRadioButton("On trigger");
	
	private JRadioButton rbNumFrames=new JRadioButton("#f");
	private JRadioButton rbTotT=new JRadioButton("∑∆t");
	private JRadioButton rbOneT=new JRadioButton("Once",true);
	private ButtonGroup bgTotalTimeGroup=new ButtonGroup();
	private ButtonGroup bgRateGroup=new ButtonGroup();
	private SpinnerSimpleInteger spNumFrames=new SpinnerSimpleInteger(1,1,10000000,1);
	private SpinnerSimpleEvFrame spSumTime=new SpinnerSimpleEvFrame();
		
	private RecWidgetComboDevice comboTriggerDevice=new RecWidgetComboDevice()
		{
		private static final long serialVersionUID = 1L;
		protected boolean includeDevice(EvDevicePath path, EvDevice device)
			{
			return device instanceof HWTrigger;
			}
		};
	
	
	public RecWidgetTimes()
		{
		rbFreqDt.setToolTipText("Time between frames");
		rbMaxSpeed.setToolTipText("Images are acquired as fast as possible");
		rbOnTrigger.setToolTipText("Images are captured when triggered externally");
		rbNumFrames.setToolTipText("Specifies number of frames to capture");
		rbTotT.setToolTipText("Images are acquired until total time has passed");
		rbOneT.setToolTipText("Acquire a single time point");
		
		
		bgRateGroup.add(rbFreqDt);
		bgRateGroup.add(rbMaxSpeed);
		bgRateGroup.add(rbOnTrigger);
		
		bgTotalTimeGroup.add(rbNumFrames);
		bgTotalTimeGroup.add(rbTotT);
		bgTotalTimeGroup.add(rbOneT);
		
		spFreqDt.setFrame("1s");

		setLayout(new GridLayout(1,1));
		
		add(
				EvSwingUtil.withTitledBorder("Time",
						EvSwingUtil.layoutCompactVertical(
								new JLabel("Frequency:"),
								EvSwingUtil.layoutTableCompactWide(
										rbFreqDt, spFreqDt
								),
								rbMaxSpeed,
								rbOnTrigger,
								comboTriggerDevice,
								new JLabel("Number:"),
								EvSwingUtil.layoutTableCompactWide(
										rbNumFrames, spNumFrames,
										rbTotT, spSumTime
								),
								rbOneT
						)));
		}
	
	
	/**
	 * Get settings from widget
	 */
	public RecSettingsTimes getSettings()
		{
		RecSettingsTimes settings=new RecSettingsTimes();
		if(rbNumFrames.isSelected())
			{
			settings.tType=RecSettingsTimes.TimeType.NUMT;
			settings.numT=spNumFrames.getIntValue();
			}
		else if(rbTotT.isSelected())
			{
			settings.tType=RecSettingsTimes.TimeType.SUMT;
			settings.sumTime=spSumTime.getDecimalValue();
			}
		else
			{
			settings.tType=RecSettingsTimes.TimeType.ONET;
			settings.numT=1;
			}
		
		if(rbFreqDt.isSelected())
			settings.freq=spFreqDt.getDecimalValue();

		return settings;
		}
	
	
	
	}
