package evplugin.filter;

/**
 * Information about an available filter/image processor
 * @author Johan Henriksson
 */
public abstract class FilterInfo
	{
	public abstract String getCategory();
	public abstract String getName();

	public abstract boolean hasFilterROI();
	public abstract FilterROI filterROI();
	}
