/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.bindingMac;

import endrov.core.EvPluginDefinition;
import endrov.starter.EvSystemUtil;

public class PLUGIN extends EvPluginDefinition
	{
	public boolean isDefaultEnabled()
		{
		return true;
		}

	public String getPluginName()
		{
		return "Mac binding";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return EvSystemUtil.isMac();
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
		try
			{
			return new Class[]{Class.forName("endrov.bindingMac.OSXAdapter"),Class.forName("endrov.bindingMac.EncodeQT")};
			}
		catch (ClassNotFoundException e)
			{
			e.printStackTrace();
			return new Class[]{};
			}
		//return new Class[]{OSXAdapter.class, EncodeQT.class};
		}
	
	
	}
