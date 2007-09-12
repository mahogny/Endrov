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
		return "Val:"+o;
		}
	}
