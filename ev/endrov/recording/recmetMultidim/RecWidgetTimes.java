package endrov.recording.recmetMultidim;

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
	
	private SpinnerSimpleEvFrame spDt=new SpinnerSimpleEvFrame();
	
	private JRadioButton rbDt=new JRadioButton("∆t",true);
	private JRadioButton rbMaxSpeed=new JRadioButton("min t");
	
	private JRadioButton rbNumFrames=new JRadioButton("#t");
	private JRadioButton rbTotT=new JRadioButton("∑∆t");
	private JRadioButton rbOneT=new JRadioButton("Once",true);
	private ButtonGroup bgDtGroup=new ButtonGroup();
	private SpinnerSimpleInteger spNumFrames=new SpinnerSimpleInteger();
	private SpinnerSimpleEvFrame spTotTime=new SpinnerSimpleEvFrame();
		
	public RecWidgetTimes()
		{
		rbNumFrames.setToolTipText("Specify number of frames to capture");
		rbTotT.setToolTipText("Specify total acquisition time");
		rbOneT.setToolTipText("Acquire a single time point");
		bgDtGroup.add(rbNumFrames);
		bgDtGroup.add(rbTotT);
		bgDtGroup.add(rbOneT);
		spDt.setFrame("1s");

		
		rbDt.setToolTipText("Time between frames");
		
		setLayout(new GridLayout(1,1));
		
		
		add(
				EvSwingUtil.withTitledBorder("Time",
						EvSwingUtil.layoutCompactVertical(
								new JLabel("Frequency:"),
								EvSwingUtil.layoutTableCompactWide(
										rbDt, spDt,
										rbMaxSpeed, new JLabel("")
								),
								new JLabel("Number:"),
								EvSwingUtil.layoutTableCompactWide(
										rbNumFrames, spNumFrames,
										rbTotT, spTotTime,
										rbOneT, new JLabel("")
								)
						)));
		}
	}
