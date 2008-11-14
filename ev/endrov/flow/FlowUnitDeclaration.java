package endrov.flow;

import org.jdom.Element;

public abstract class FlowUnitDeclaration
	{
	public final String name, category, metadata;
	public FlowUnitDeclaration(String category, String name, String metadata)
		{
		this.name=name;
		this.category=category;
		this.metadata=metadata;
		}
	
	
	
	public abstract FlowUnit createInstance();
	public String toString()
		{
		return name;
		}
	
	public abstract FlowUnit fromXML(Element e);
	}
