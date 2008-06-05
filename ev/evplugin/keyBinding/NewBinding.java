package evplugin.keyBinding;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class NewBinding
	{

	public static class EvBindStatus
		{
		public HashMap<String, Float> values=new HashMap<String, Float>();
		
		}

	
	public static class EvBindKeyEvent 
		{
		
		
		
		}

	public static List<EvBindListener> bindListeners=new LinkedList<EvBindListener>();

	public static void attachBindAxisListener(EvBindListener listener)
		{
		bindListeners.add(listener);
		}
	
	public interface EvBindListener
		{
		public void bindAxisPerformed(EvBindStatus status);
		public void bindKeyPerformed(EvBindKeyEvent e, EvBindStatus status);
		}
	
	
	}


/*

if(KeyBinding.get(KEY_GETCONSOLE).typed(e))
	
	vs
	
	if(KeyBinding.get(KEY_GETCONSOLE).typed(e)) //e now our own type of event?

		
		
		KeyListener is hidden in attached BindKeyListener
		
		
		*/