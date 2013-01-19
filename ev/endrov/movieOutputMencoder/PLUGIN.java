/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.movieOutputMencoder;
import endrov.core.*;

public class PLUGIN extends EvPluginDefinition
	{
	public String getPluginName()
		{
		return "Movie output: Mencoder";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return EncodeMencoder.program.exists();
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
		return new Class[]{EncodeMencoder.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
