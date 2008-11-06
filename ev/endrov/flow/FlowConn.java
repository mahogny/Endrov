package endrov.flow;

/**
 * Connection between two flow units
 * @author Johan Henriksson
 *
 */
public class FlowConn
	{
	public final FlowUnit fromUnit, toUnit;
	public final String fromArg, toArg;
	
	public FlowConn(FlowUnit fromUnit, String fromArg, FlowUnit toUnit, String toArg)
		{
		this.fromUnit=fromUnit;
		this.toUnit=toUnit;
		this.fromArg=fromArg;
		this.toArg=toArg;
		}
	
	
	public String toString()
		{
		return ""+fromUnit+":"+fromArg+"  --  "+toUnit+":"+toArg;
		}


	public boolean equals(Object obj)
		{
		if(obj instanceof FlowConn && obj!=null)
			{
			FlowConn c=(FlowConn)obj;
			return fromUnit==c.fromUnit && toUnit==c.toUnit && fromArg.equals(c.fromArg) && toArg.equals(c.toArg);
			}
		else
			return false;
		}
	
	
	
	}
