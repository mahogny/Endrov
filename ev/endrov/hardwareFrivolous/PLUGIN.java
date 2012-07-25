/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareFrivolous;
import endrov.ev.PluginDef;
import endrov.hardwareFrivolous.devices.FrivolousDeviceProvider;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Frivolous hardware driver";
		}

	public String getAuthor()
		{
		return "David Johansson <jdavid@kth.se>\n" +
				"Arvid Johansson <arvidjo@kth.se>";
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
		return new Class[]{FrivolousDeviceProvider.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
