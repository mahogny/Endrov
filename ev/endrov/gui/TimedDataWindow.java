package endrov.gui;


import endrov.data.EvContainer;
import endrov.util.EvDecimal;

/**
 * Interface that should go away once image and model window are merged
 * 
 * @author Johan Henriksson
 *
 */
public interface TimedDataWindow
	{
	public EvDecimal getFrame();
	
	public EvContainer getSelectedData();
	}
