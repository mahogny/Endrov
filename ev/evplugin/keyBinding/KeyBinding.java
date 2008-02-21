package evplugin.keyBinding;

import java.awt.event.*;
import java.util.*;

import org.jdom.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.ev.EV;
import evplugin.ev.PersonalConfig;

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
		
		EV.personalConfigLoaders.put("keyBinding",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{register(readXML(e),true);}
			public void savePersonalConfig(Element root)
				{
				for(KeyBinding b:bindings.values())
					{
					Element e=new Element("keyBinding");
					b.writeXML(e);
					root.addContent(e);
					}
				}
			});
		}

	/**
	 * Make a keybinding out of an XML element
	 */
	public static KeyBinding readXML(Element e)
		{
		try
			{
			String plugin=e.getAttributeValue("plugin");
			String desc=e.getAttributeValue("desc");
			int keyCode=e.getAttribute("keyCode").getIntValue();
			int modifier=e.getAttribute("modifier").getIntValue();
			KeyBinding b=new KeyBinding(plugin, desc, keyCode, modifier);
			String cs=e.getAttributeValue("key");
			if(cs!=null)
				b.key=cs.charAt(0);
			return b;
			}
		catch (DataConversionException e1)
			{
			e1.printStackTrace();
			return null;
			}
		}

	
	
	/**
	 * Register key binding. If it already exists, then it will not be readded (this allows overriding)
	 */
	public static int register(KeyBinding b)
		{
		return register(b, false);
		}
	public static int register(KeyBinding b, boolean forceUpdate)
		{
		//Search for binding if it already exists
		for(Integer id:bindings.keySet())
			{
			KeyBinding tb=bindings.get(id);
			if(tb.pluginName.equals(b.pluginName) && tb.description.equals(b.description))
				{
				if(forceUpdate)
					bindings.put(id, b);
				return id;
				}
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

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public final String pluginName, description;
	public Character key; //unsure?
	public int keyCode;
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
	 * Write keybinding info to an element
	 */
	public void writeXML(Element e)
		{
		e.setAttribute("plugin",pluginName);
		e.setAttribute("desc",description);
		e.setAttribute("keyCode",""+keyCode);
		e.setAttribute("modifier",""+modifierEx);
		if(key!=null)
			e.setAttribute("key",""+key);
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
			return (""+key).toLowerCase();
		else
			{
			String r=KeyEvent.getKeyModifiersText(modifierEx);
			if(!r.equals(""))
				r=r+"+";
			return (r+KeyEvent.getKeyText(keyCode)).toLowerCase();
			}
		}
	
	
	
	}
