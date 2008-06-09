package evplugin.keyBinding;

import java.awt.event.*;
import java.util.*;

import org.jdom.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.ev.EV;
import evplugin.ev.PersonalConfig;

//TODO: support gamepad repeat type

/**
 * Handle key bindings
 * @author Johan Henriksson
 */
public class KeyBinding implements Comparable<KeyBinding>
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static HashMap<Integer,KeyBinding> bindings=new HashMap<Integer,KeyBinding>();
	private static int nextId=0;

	static JInputManager jinputManager=new JInputManager();

	
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
			KeyBinding b=new KeyBinding(plugin, desc,null);
			
			for(Object neo:e.getChildren())
				{
				Element ne=(Element)neo;
				if(ne.getName().equals("char"))
					b.types.add(new TypeChar(ne.getAttributeValue("char").charAt(0)));
				else if(ne.getName().equals("keycode"))
					b.types.add(new TypeKeycode(ne.getAttribute("keyCode").getIntValue(),ne.getAttribute("modifier").getIntValue()));
				else if(ne.getName().equals("jinput"))
					b.types.add(new TypeJInput(ne.getAttributeValue("ident"),ne.getAttribute("value").getFloatValue()));
				
				
				}

			//backwards compatibility
			if(e.getAttribute("key")!=null)
				{
				TypeChar kb=new TypeChar(e.getAttributeValue("key").charAt(0));
				b.types.add(kb);
				}
			else if(e.getAttribute("keyCode")!=null)
				{
				TypeKeycode kb=new TypeKeycode(e.getAttribute("keyCode").getIntValue(),e.getAttribute("modifier").getIntValue());
				b.types.add(kb);
				}
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
	 *                               Binding handlers                                                     *
	 *****************************************************************************************************/
	public static interface KeyBindingType
		{
		public boolean typed(KeyEvent e, NewBinding.EvBindKeyEvent je);
		public boolean held(KeyEvent e, NewBinding.EvBindStatus je);
		
		public void writeXML(Element e);
		public String keyDesc();
		public float getAxis(NewBinding.EvBindStatus status);
		}
	
	/**
	 * "Char" keybinding
	 */
	public static class TypeChar implements KeyBindingType
		{
		private char key;
		public TypeChar(char key){this.key=key;}
		public boolean typed(KeyEvent e, NewBinding.EvBindKeyEvent je)
			{
			return e!=null && e.getKeyChar()==key;
			}
		public boolean held(KeyEvent e, NewBinding.EvBindStatus je)
			{
			return false; //TODO
			}
		public void writeXML(Element e)
			{
			Element ne=new Element("char");
			ne.setAttribute("char",""+key);
			e.addContent(ne);
			}
		public String keyDesc()
			{
			return (""+key).toLowerCase();
			}
		public float getAxis(NewBinding.EvBindStatus status)
			{
			return 0;
			}
		}

	/**
	 * Keycode keybinding
	 */
	public static class TypeKeycode implements KeyBindingType
		{
		private int keyCode;
		private int modifierEx;
		public TypeKeycode(int keyCode, int modifierEx){this.keyCode=keyCode;this.modifierEx=modifierEx;}
		public boolean typed(KeyEvent e, NewBinding.EvBindKeyEvent je)
			{
			//TODO: use modifier
			return e!=null && e.getKeyCode()==keyCode;
			}
		public boolean held(KeyEvent e, NewBinding.EvBindStatus je)
			{
			return false; //TODO
			}
		/*
		public boolean typedNew(KeyEvent e,NewBinding.EvBindStatus status)
			{
			return false;
			}
		*/
		public void writeXML(Element e)
			{
			Element ne=new Element("keycode");
			ne.setAttribute("keyCode",""+keyCode);
			ne.setAttribute("modifier",""+modifierEx);
			e.addContent(ne);
			}
		public String keyDesc()
			{
			String r=KeyEvent.getKeyModifiersText(modifierEx);
			if(!r.equals(""))
				r=r+"+";
			return (r+KeyEvent.getKeyText(keyCode)).toLowerCase();
			}
		public float getAxis(NewBinding.EvBindStatus status)
			{return 0;}
		}

	
	
	/**
	 * JInput keybinding
	 */
	public static class TypeJInput implements KeyBindingType
		{
		private String ident;
		private float value;
		public TypeJInput(String ident,float value)
			{
			this.ident=ident;
			this.value=value;
			}
		public boolean typed(KeyEvent e, NewBinding.EvBindKeyEvent je)
			{
			if(je!=null && je.srcName.equals(ident))
				return je.srcValue==1;
			return false;
//			return status.values.get(ident)==value;
			}
		public boolean held(KeyEvent e, NewBinding.EvBindStatus status)
			{
			if(status!=null)
				{
				Float v=status.values.get(ident);
				return v==null ? false : v==value;
				}
			return false;
			}
		public void writeXML(Element e)
			{
			Element ne=new Element("jinput");
			ne.setAttribute("ident",ident);
			ne.setAttribute("value",""+value);
			e.addContent(ne);
			}
		public String keyDesc()
			{
			return "JI:"+ident+"="+value;
			}
		public float getAxis(NewBinding.EvBindStatus status)
			{
//			System.out.println("getaxis "+ident+" "+status.values.get(ident)+" "+status.values.get("x"));
			Float v=status.values.get(ident);
			return v==null ? 0 : v;
			} 
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public final String pluginName, description;

	public Vector<KeyBindingType> types=new Vector<KeyBindingType>();
	
	
	public int compareTo(KeyBinding o)
		{
		int v=pluginName.compareTo(o.pluginName);
		if(v==0)
			return description.compareTo(o.description);
		else
			return v;
		}



	/**
	 * Key binding
	 */
	public KeyBinding(String plugin, String description, KeyBindingType kb)
		{
		this.pluginName=plugin;
		this.description=description;
		if(kb!=null)
			types.add(kb);
		}
	
	/**
	 * Key binding for special call
	 */
	public KeyBinding(String plugin, String description, char key)
		{
		this.pluginName=plugin;
		this.description=description;
		TypeChar kb=new TypeChar(key);
		types.add(kb);
		}

	
	/**
	 * Key binding for special call
	 */
	public KeyBinding(String plugin, String description, int keyCode, int modifierEx)
		{
		this.pluginName=plugin;
		this.description=description;
		TypeKeycode kb=new TypeKeycode(keyCode,modifierEx);
		types.add(kb);
		}
	
	
	
	/**
	 * Write keybinding info to an element
	 */
	public void writeXML(Element e)
		{
		e.setAttribute("plugin",pluginName);
		e.setAttribute("desc",description);
		
		for(KeyBindingType t:types)
			t.writeXML(e);
		}
	
		
	/**
	 * Has key been typed?
	 */
	public boolean typed(KeyEvent e)
		{
		for(KeyBindingType kb:types)
			if(kb.typed(e,null))
				return true;
		return false;
		}

	/**
	 * Has key been typed?
	 */
	public boolean held(NewBinding.EvBindStatus status)
		{
		for(KeyBindingType kb:types)
			if(kb.held(null,status))
				return true;
		return false;
		}

	
	/**
	 * Textual description of key
	 */
	public String keyDesc()
		{
		if(types.isEmpty())
			return "";
		else
			return types.get(0).keyDesc();
		
		//TODO: GUI need to handle several keys
		
		}
	
	
	
	
	
	
	///////////////// to be merged later /////////////////////////////////
	
	
	/** Get axis value. NULL only on exception */
	public float getAxis(NewBinding.EvBindStatus status)
		{
		float v=0;
		for(KeyBindingType kb:types)
			v+=kb.getAxis(status);
		return v;
		}
	
	public boolean typed(NewBinding.EvBindKeyEvent e)
		{
		for(KeyBindingType kb:types)
			if(kb.typed(null,e)) //TODO: any need to separate here?
				return true;
		return false;
		}
	
	
	
	}
