/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.widgets;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import endrov.gui.EvSwingUtil;
import endrov.gui.component.JImageButton;
import endrov.gui.component.JSpinnerSimpleEvDecimal;
import endrov.gui.component.JSpinnerSimpleInteger;
import endrov.recording.RecordingResource;
import endrov.util.math.EvDecimal;

/**
 * Widget for recording settings: Slice settings
 * @author Johan Henriksson
 *
 */
public class RecWidgetSlices extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	
	public static final ImageIcon iconSetFromStage=new ImageIcon(RecWidgetSlices.class.getResource("jhSetFromZ.png"));

	
	private JSpinnerSimpleEvDecimal spStartZ=new JSpinnerSimpleEvDecimal();
	private JSpinnerSimpleEvDecimal spEndZ=new JSpinnerSimpleEvDecimal();
	private JRadioButton rbNumSlices=new JRadioButton("#Z");
	private JRadioButton rbDZ=new JRadioButton("∆Z");
	private JRadioButton rbOneZ=new JRadioButton("Single slice",true);
	private ButtonGroup bgDZgroup=new ButtonGroup();
	private JButton bSetStartZ=new JImageButton(iconSetFromStage,"Set position using current stage position");
	private JButton bSetEndZ=new JImageButton(iconSetFromStage,"Set position using current stage position");
	private JSpinnerSimpleInteger spNumZ=new JSpinnerSimpleInteger(1,1,1000,1);
	private JSpinnerSimpleEvDecimal spDZ=new JSpinnerSimpleEvDecimal();
	
	public RecWidgetSlices()
		{
		rbNumSlices.setToolTipText("Specify number of slices in Z-direction");
		rbDZ.setToolTipText("Specify spacing beetween slices [um]");
		rbOneZ.setToolTipText("Do not acquire multiple slices");
		bgDZgroup.add(rbNumSlices);
		bgDZgroup.add(rbDZ);
		bgDZgroup.add(rbOneZ);
		rbNumSlices.add(rbDZ);
		rbNumSlices.add(rbOneZ);

		
		setLayout(new GridLayout(1,1));
		add(
			EvSwingUtil.withTitledBorder("Slices",
					EvSwingUtil.layoutCompactVertical(
						EvSwingUtil.layoutTableCompactWide(
							new JLabel("Z start"), EvSwingUtil.layoutLCR(null, spStartZ, bSetStartZ),
							new JLabel("Z end"), EvSwingUtil.layoutLCR(null, spEndZ, bSetEndZ)
							),
						EvSwingUtil.layoutTableCompactWide(
							rbNumSlices, spNumZ,
							rbDZ, spDZ
					),
					rbOneZ
					)				
			));
		
		
		bSetStartZ.addActionListener(this);
		bSetEndZ.addActionListener(this);
		}
	
	
	
	/**
	 * Get settings from widget
	 */
	public RecSettingsSlices getSettings()
		{
		RecSettingsSlices settings=new RecSettingsSlices();
		if(rbNumSlices.isSelected())
			{
			settings.zType=RecSettingsSlices.ZType.NUMZ;
			settings.numZ=spNumZ.getIntValue();
			}
		else if(rbDZ.isSelected())
			{
			settings.zType=RecSettingsSlices.ZType.DZ;
			settings.dz=spDZ.getDecimalValue();
			}
		else
			{
			settings.zType=RecSettingsSlices.ZType.ONEZ;
			settings.numZ=1;
			}

		settings.start=spStartZ.getDecimalValue();
		settings.end=spEndZ.getDecimalValue();
		
		
		return settings;
		}

	private EvDecimal getStagePos()
		{
		double pos=RecordingResource.getCurrentStageZ();
		return new EvDecimal(new BigDecimal(pos*1000).divideToIntegralValue(new BigDecimal(1000)));
		}

	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bSetEndZ)
			{
			spEndZ.setDecimalValue(getStagePos());
			}
		else if(e.getSource()==bSetStartZ)
			{
			spStartZ.setDecimalValue(getStagePos());
			}
		
		}
	
	
	
		
	}
