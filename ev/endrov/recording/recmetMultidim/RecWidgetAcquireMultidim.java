package endrov.recording.recmetMultidim;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import endrov.basicWindow.icon.BasicIcon;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;


public class RecWidgetAcquireMultidim extends JPanel
	{

	//TODO where to save it down?
	//TODO color cameras?
	//TODO autoshutter
	//TODO autofocus, have it with position settings?
	
	
	JButton bAcquire=new JImageButton(BasicIcon.iconButtonRecord,"Start acquisition");
	JLabel labelToAcq=new JLabel("");
	JLabel labelAcqStat=new JLabel("");
	
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
	
	}
