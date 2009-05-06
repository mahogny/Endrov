package endrov.keyBinding;


/**
 * Manager for gamepad/joystick events
 * @author Johan Henriksson
 *
 */
public interface JInputMode
	{	
	public void bindAxisPerformed(JInputManager.EvJinputStatus status);
	public void bindKeyPerformed(JInputManager.EvJinputButtonEvent e);
	}
