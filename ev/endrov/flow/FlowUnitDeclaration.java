package endrov.flow;

public abstract class FlowUnitDeclaration
	{
	public abstract String getName();
	public abstract String getCategory();
	public abstract FlowUnit createInstance();
	public String toString()
		{
		return getName();
		}
	
	//TODO function to deserialize
	
	}
