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
	
	private JRadioButton rbFreqDt=new JRadioButton("∆t",true);
	private JRadioButton rbMaxSpeed=new JRadioButton("Maximum rate");
	
	private JRadioButton rbNumFrames=new JRadioButton("#t");
	private JRadioButton rbTotT=new JRadioButton("∑∆t");
	private JRadioButton rbOneT=new JRadioButton("Once",true);
	private ButtonGroup bgTotalTimeGroup=new ButtonGroup();
	private ButtonGroup bgRateGroup=new ButtonGroup();
	private SpinnerSimpleInteger spNumFrames=new SpinnerSimpleInteger(1,1,10000000,1);
	private SpinnerSimpleEvFrame spSumTime=new SpinnerSimpleEvFrame();
		
	public RecWidgetTimes()
		{
		rbNumFrames.setToolTipText("Specify number of frames to capture");
		rbTotT.setToolTipText("Specify total acquisition time");
		rbOneT.setToolTipText("Acquire a single time point");
		bgTotalTimeGroup.add(rbNumFrames);
		bgTotalTimeGroup.add(rbTotT);
		bgTotalTimeGroup.add(rbOneT);
		bgRateGroup.add(rbFreqDt);
		bgRateGroup.add(rbMaxSpeed);
		spFreqDt.setFrame("1s");

		
		rbFreqDt.setToolTipText("Time between frames");
		
		setLayout(new GridLayout(1,1));
		
		
		add(
				EvSwingUtil.withTitledBorder("Time",
						EvSwingUtil.layoutCompactVertical(
								new JLabel("Frequency:"),
								EvSwingUtil.layoutTableCompactWide(
										rbFreqDt, spFreqDt
								),
								rbMaxSpeed,
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
