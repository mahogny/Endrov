package evplugin.metadata;
import org.jdom.*;

public class MetaObjectUnknown extends MetaObject
	{
	public final Element xml;
	public final String metaType;
	
	public MetaObjectUnknown(Element xml)
		{
		this.xml=xml;
		metaType=xml.getName();
		}
	
	public String getMetaTypeDesc()
		{
		return "unknown("+metaType+")";
		}

	public void saveMetadata(Element e)
		{
		e.setName(xml.getName());
		for(Object o:xml.getChildren())
			xml.addContent((Element)o);
		}
	}
