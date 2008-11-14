package endrov.flow;

import org.jdom.Element;

public abstract class FlowUnitDeclarationTrivial extends FlowUnitDeclaration
	{
	public FlowUnitDeclarationTrivial(String category, String name)
		{
		super(category,name,name);
		}
	public FlowUnitDeclarationTrivial(String category, String name, String metadata)
		{
		super(category,name,metadata);
		}
	
	public FlowUnit fromXML(Element e)
		{
		return createInstance();
		}
	}
