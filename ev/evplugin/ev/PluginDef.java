package evplugin.ev;

/**
 * Definition of a plugin
 * @author Johan Henriksson
 */
public abstract class PluginDef
	{
	public abstract String getPluginName();
	public abstract String getAuthor();
	public abstract String[] requires();
	public abstract Class<?>[] getInitClasses();
	public abstract String cite();
	public abstract boolean systemSupported();
	}
