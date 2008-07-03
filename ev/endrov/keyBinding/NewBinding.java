package endrov.keyBinding;

import java.util.HashMap;
import java.util.WeakHashMap;

public class NewBinding
	{

	public static class EvBindStatus
		{
		public HashMap<String, Float> values=new HashMap<String, Float>();
		
		}

	
	public static class EvBindKeyEvent 
		{
		public EvBindKeyEvent(String src, float value, EvBindStatus status)
			{
			srcName=src;
			srcValue=value;
			this.status=status;
			}
		
		public String srcName;
		public EvBindStatus status;
		public float srcValue;
		
		}

	public static WeakHashMap<EvBindListener,Object> bindListeners=new WeakHashMap<EvBindListener,Object>();

	public static void attachBindAxisListener(EvBindListener listener)
		{
		bindListeners.put(listener,null);
		}
	
	public interface EvBindListener
		{
		public void bindAxisPerformed(EvBindStatus status);
		public void bindKeyPerformed(EvBindKeyEvent e);
		}
	
	
	}


/*

if(KeyBinding.get(KEY_GETCONSOLE).typed(e))
	
	vs
	
	if(KeyBinding.get(KEY_GETCONSOLE).typed(e)) //e now our own type of event?

		
		
		KeyListener is hidden in attached BindKeyListener
		
		
		*/