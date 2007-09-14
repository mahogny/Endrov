package evplugin.script;

/**
 * Expression: Value
 * @author Johan Henriksson
 */
public class ExpVal extends Exp
	{
	public Object o;
	
	public ExpVal(){}
	public ExpVal(Object o)
		{
		this.o=o;
		}
	public String toString()
		{
		if(o==null)
			return "(null)";
		else if(o instanceof String)
			return "\""+o+"\"";
		else if(o instanceof Integer)
			return "int: "+o;
		else if(o instanceof Double)
			return "double: "+o;
		else
			{
			return o.getClass().getName()+": "+o;
			}
//		return "Val:"+o;
		}
	}
