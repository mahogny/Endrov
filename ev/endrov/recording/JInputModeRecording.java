package endrov.recording;

import java.util.HashMap;
import java.util.Map;

import endrov.keyBinding.JInputManager;
import endrov.keyBinding.JInputMode;


/**
 * Use input to control hardware
 * 
 * @author Johan Henriksson
 *
 */
public class JInputModeRecording implements JInputMode
	{
	/**
	 * Axis name TO 
	 */
	Map<String, String> gpMap=new HashMap<String, String>();
	
	
	public void bindAxisPerformed(JInputManager.EvJinputStatus status)
		{
		}
	
	
	public void bindKeyPerformed(JInputManager.EvJinputButtonEvent e)
		{
		}

	
	
	}
