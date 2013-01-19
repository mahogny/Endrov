/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.keybinding;

import endrov.gui.keybinding.JInputManager.EvJinputButtonEvent;
import endrov.gui.keybinding.JInputManager.EvJinputStatus;

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
