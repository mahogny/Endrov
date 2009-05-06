package endrov.keyBinding;

import endrov.keyBinding.JInputManager.EvJinputButtonEvent;
import endrov.keyBinding.JInputManager.EvJinputStatus;

/**
 * Handler of Jinput events
 * @author Johan Henriksson
 *
 */
public interface JinputListener
	{
	public void bindAxisPerformed(EvJinputStatus status);
	public void bindKeyPerformed(EvJinputButtonEvent e);
	}