package endrov.gui;


import endrov.data.EvContainer;
import endrov.util.math.EvDecimal;

/**
 * Interface that should go away once image and model window are merged
 * 
 * @author Johan Henriksson
 *
 */
public interface TimedDataWindowInterface
	{
	public EvDecimal getFrame();
	
	public EvContainer getSelectedData();
	}
