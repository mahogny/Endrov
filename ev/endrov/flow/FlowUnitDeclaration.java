package endrov.flow;

public abstract class FlowUnitDeclaration
	{
	public final String name, category;
	public FlowUnitDeclaration(String category, String name)
		{
		this.name=name;
		this.category=category;
		}
	
	
	
	public abstract FlowUnit createInstance();
	public String toString()
		{
		return name;
		}
	
	//TODO function to deserialize
	
	}
