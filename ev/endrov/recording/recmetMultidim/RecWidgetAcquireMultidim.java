/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetMultidim;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import endrov.basicWindow.icon.BasicIcon;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;


/**
 * Widget for recording settings: Acquire button
 * @author Johan Henriksson
 *
 */
public class RecWidgetAcquireMultidim extends JPanel
	{
	private static final long serialVersionUID = 1L;

	//TODO where to save it down?
	//TODO color cameras?
	//TODO autoshutter
	//TODO autofocus, have it with position settings?
	
	
	
	private JButton bAcquire=new JImageButton(BasicIcon.iconButtonRecord,"Start acquisition");
	private JLabel labelToAcq=new JLabel("");
	private JLabel labelAcqStat=new JLabel("");
	
	public RecWidgetAcquireMultidim()
		{
		setBorder(BorderFactory.createTitledBorder("Acquire"));
		setLayout(new GridLayout(1,1));
		add(
				EvSwingUtil.layoutCompactVertical(
						EvSwingUtil.layoutTableCompactWide(
								new JLabel("To acquire:"), labelToAcq,
								new JLabel("Progress:"), labelAcqStat),
								bAcquire
				)
		);

		}

	
	public void setToAcquire(String s)
		{
		labelToAcq.setText(s);
		}
	
	public void setStatus(String s)
		{
		labelAcqStat.setText(s);
		}
	
	
	}
