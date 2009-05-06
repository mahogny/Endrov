package endrov.basicWindow;

import endrov.keyBinding.JInputManager;
import endrov.keyBinding.JInputMode;
import endrov.keyBinding.JinputListener;

/**
 * Send input to the currently active BasicWindow
 * 
 * @author Johan Henriksson
 *
 */
public class JInputModeBasicWindow implements JInputMode
	{
	public static BasicWindow getWindow()
		{
		return BasicWindow.windowManager.getFocusWindow();
		}
	
	public void bindAxisPerformed(JInputManager.EvJinputStatus status)
		{
		BasicWindow w=getWindow();
		if(w!=null)
			{
			for(JinputListener listener:w.jinputListeners.keySet())
				listener.bindAxisPerformed(status);
			}
		}
	
	
	public void bindKeyPerformed(JInputManager.EvJinputButtonEvent e)
		{
		BasicWindow w=getWindow();
		if(w!=null)
			{
			for(JinputListener listener:w.jinputListeners.keySet())
				listener.bindKeyPerformed(e);
			}
		}

	
	
	}
