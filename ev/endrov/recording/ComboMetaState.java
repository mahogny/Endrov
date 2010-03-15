package endrov.recording;

import java.util.Vector;

import javax.swing.JComboBox;

import endrov.hardware.EvHardwareConfigGroup;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class ComboMetaState extends JComboBox
	{
	private static final long serialVersionUID = 1L;

	public ComboMetaState()
		{
		super(new Vector<String>(EvHardwareConfigGroup.groups.keySet()));
		}
	
	}
