package evplugin.keyBinding;

import java.awt.event.*;
import java.util.*;

import evplugin.basicWindow.BasicWindow;

/**
 * Handle key bindings
 * @author Johan Henriksson
 */
public class KeyBinding
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static HashMap<Integer,KeyBinding> bindings=new HashMap<Integer,KeyBinding>();
	private static int nextId=0;

	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicKeyBinding());
		}

	
	/**
	 * Register key binding. If it already exists, then it will not be readded (this allows overriding)
	 */
	public static int register(KeyBinding b)
		{
		//Search for binding if it already exists
		for(Integer id:bindings.keySet())
			{
			KeyBinding tb=bindings.get(id);
			if(tb.pluginName.equals(b.pluginName) && tb.description.equals(b.description))
				return id;
			}
		//Generate new binding
		nextId++;
		bindings.put(nextId, b);
		return nextId;
		}
	
	/**
	 * Get a key binding
	 */
	public static KeyBinding get(int id)
		{
		return bindings.get(id);
		}

	
	public static void loadBindings()
		{
		//TODO
		
	/*	
		KeyBinding.register(new KeyBinding("Image Window","Step back",'a'));
		KeyBinding.register(new KeyBinding("Image Window","Step forward",'e'));
		KeyBinding.register(new KeyBinding("Image Window","Step up",'o'));
		KeyBinding.register(new KeyBinding("Image Window","Step down",'ä'));
*/
		}
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public final String pluginName, description;
	public Character key; //unsure?
	public Integer keyCode;
	public int modifierEx;


	
	/**
	 * Key binding for special call
	 */
	public KeyBinding(String plugin, String description, Character key)
		{
		this.pluginName=plugin;
		this.description=description;
		this.key=key;
		this.modifierEx=0;
		}

	
	/**
	 * Key binding for special call
	 */
	public KeyBinding(String plugin, String description, int keyCode, int modifierEx)
		{
		this.pluginName=plugin;
		this.description=description;
		this.key=null;
		this.keyCode=keyCode;
		this.modifierEx=modifierEx;
		}
	
		
	/**
	 * Has key been typed?
	 */
	public boolean typed(KeyEvent e)
		{
		if(key!=null)
			return e.getKeyChar()==key;
		else
			{
			//todo: use modifier
			return e.getKeyCode()==keyCode;
			}
		}
	
	
	/**
	 * Textual description of key
	 */
	public String keyDesc()
		{
		if(key!=null)
			return ""+key;
		else
			{
			String r=KeyEvent.getKeyModifiersText(modifierEx);
			if(!r.equals(""))
				r=r+"+";
			return r+KeyEvent.getKeyText(keyCode);
			}
		}
	
	
	
	}
