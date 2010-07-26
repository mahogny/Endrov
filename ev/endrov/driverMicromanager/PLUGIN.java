/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverMicromanager;
import endrov.ev.EV;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Micro-manager";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		try
			{
			Class.forName("mmcorej.CMMCore");
//			return EV.isLinux() || EV.isMac();
			return false;
			}
		catch (ClassNotFoundException e)
			{
			System.out.println("Micro-manager not found");
			return false;
			}
		
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
		return new Class[]{MicroManager.class};
		//return new Class[]{};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
