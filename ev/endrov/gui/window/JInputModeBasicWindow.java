/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.window;

import endrov.gui.keybinding.JInputManager;
import endrov.gui.keybinding.JInputMode;
import endrov.gui.keybinding.JinputListener;

/**
 * Send input to the currently active BasicWindow
 * 
 * @author Johan Henriksson
 *
 */
public class JInputModeBasicWindow implements JInputMode
	{
	public static EvBasicWindow getWindow()
		{
		return EvBasicWindow.windowManager.getFocusWindow();
		}
	
	public void bindAxisPerformed(JInputManager.EvJinputStatus status)
		{
		EvBasicWindow w=getWindow();
		if(w!=null)
			{
			for(JinputListener listener:w.jinputListeners.keySet())
				listener.bindAxisPerformed(status);
			}
		}
	
	
	public void bindKeyPerformed(JInputManager.EvJinputButtonEvent e)
		{
		EvBasicWindow w=getWindow();
		if(w!=null)
			{
			for(JinputListener listener:w.jinputListeners.keySet())
				listener.bindKeyPerformed(e);
			}
		}

	
	
	}
