/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui;
import endrov.core.EvPluginDefinition;
import endrov.gui.keybinding.KeyBinding;
import endrov.gui.keybinding.ScriptBinding;
import endrov.gui.undo.EvUndo;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvRegistrationDialog;

public class PLUGIN extends EvPluginDefinition
	{
	public String getPluginName()
		{
		return "Endrov GUI";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return true;
		}
	
	public String cite()
		{
		return "";
		}
	
	public String[] requires()
		{
		return new String[]{};
		}
	
	public Class<?>[] getInitClasses()
		{
		return new Class[]{
				EvBasicWindow.class,
				EvUndo.class,
				KeyBinding.class, 
				ScriptBinding.class,
				EvRegistrationDialog.class,
				};
		}

	public boolean isDefaultEnabled(){return true;};
	}
