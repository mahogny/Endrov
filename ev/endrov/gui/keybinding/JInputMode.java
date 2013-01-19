/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.keybinding;


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
