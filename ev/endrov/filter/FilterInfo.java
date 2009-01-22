package endrov.filter;

/**
 * Information about an available filter/image processor
 * @author Johan Henriksson
 */
public abstract class FilterInfo
	{
	public abstract String getCategory();
	public abstract String getMetaName();
	public abstract String getReadableName();

	public abstract boolean hasFilterROI();
	public abstract FilterROI filterROI();

	//public abstract Filter readXML(Element e); 
	}
