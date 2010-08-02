/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.widgets;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import endrov.basicWindow.SpinnerSimpleEvDecimal;
import endrov.basicWindow.SpinnerSimpleInteger;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;

/**
 * Widget for recording settings: Slice settings
 * @author Johan Henriksson
 *
 */
public class RecWidgetSlices extends JPanel
	{
	private static final long serialVersionUID = 1L;
	
	public static final ImageIcon iconSetFromStage=new ImageIcon(RecWidgetSlices.class.getResource("jhSetFromZ.png"));

	
	private SpinnerSimpleEvDecimal spStartZ=new SpinnerSimpleEvDecimal();
	private SpinnerSimpleEvDecimal spEndZ=new SpinnerSimpleEvDecimal();
	private JRadioButton rbTotSlices=new JRadioButton("#Z");
	private JRadioButton rbDZ=new JRadioButton("∆Z");
	private JRadioButton rbOneZ=new JRadioButton("Single slice",true);
	private ButtonGroup bgDZgroup=new ButtonGroup();
	private JButton bSetStartZ=new JImageButton(iconSetFromStage,"Set position using current stage position");
	private JButton bSetEndZ=new JImageButton(iconSetFromStage,"Set position using current stage position");
	private SpinnerSimpleInteger spNumZ=new SpinnerSimpleInteger();
	private SpinnerSimpleEvDecimal spDZ=new SpinnerSimpleEvDecimal();
	
	public RecWidgetSlices()
		{
		rbTotSlices.setToolTipText("Specify number of slices in Z-direction");
		rbDZ.setToolTipText("Specify spacing beetween slices in micrometer");
		rbOneZ.setToolTipText("Do not acquire multiple slices");
		bgDZgroup.add(rbTotSlices);
		rbTotSlices.add(rbDZ);
		rbTotSlices.add(rbOneZ);

		
		setLayout(new GridLayout(1,1));
		add(
			EvSwingUtil.withTitledBorder("Slices",
					EvSwingUtil.layoutCompactVertical(
						EvSwingUtil.layoutTableCompactWide(
							new JLabel("Z start"), EvSwingUtil.layoutLCR(null, spStartZ, bSetStartZ),
							new JLabel("Z end"), EvSwingUtil.layoutLCR(null, spEndZ, bSetEndZ)
							),
						EvSwingUtil.layoutTableCompactWide(
							rbTotSlices, spNumZ,
							rbDZ, spDZ
					),
					rbOneZ
					)				
			));
	
		}
	
	
	
	public static class SettingsSlices
		{
		
		
		
		public String author, comment, sample;
		}
	
	
	public SettingsSlices getSettings()
		{
		SettingsSlices settings=new SettingsSlices();
		
		
		
		
		return settings;
		}
		
	}
