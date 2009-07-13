package endrov.recording.recmetMultidim;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import endrov.util.EvSwingUtil;

/**
 * Widget for recording settings: Position settings
 * @author Johan Henriksson
 *
 */
public class RecWidgetPositions extends JPanel
	{
	private static final long serialVersionUID = 1L;
	
	JCheckBox cbAutofocus=new JCheckBox("Autofocus");
	//which device here
	//MM: switch of hw autofocus while moving xy
	//MM: switch of hw autofocus while moving z
	
	
	public RecWidgetPositions()
		{


		setLayout(new GridLayout(1,1));
		add(
			EvSwingUtil.withTitledBorder("Positions",
					EvSwingUtil.layoutCompactVertical(
					cbAutofocus,
					new JLabel("TODO")
					
					)
					/*
				EvSwingUtil.layoutTableCompactWide(
						new JLabel("dt"), spDt,
						rbNumFrames, spNumFrames,
						rbTotT, spTotTime,
						rbOneT, new JLabel("")
			)
						*/
			));
		}
	}
