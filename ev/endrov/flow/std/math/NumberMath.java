package endrov.flow.std.math;


/**
 * Scalar with upconversion semantics
 * @author Johan Henriksson
 *
 */
public class NumberMath
	{

	
	
	public static int typeIndex(Number a)
		{
		if(a instanceof Byte)
			return 0;
		else if(a instanceof Short)
			return 1;
		else if(a instanceof Integer)
			return 2;
		else if(a instanceof Float)
			return 3;
		else if(a instanceof Double)
			return 4;
		else if(a instanceof Long)
			return 5;
		else
			throw new RuntimeException("Unsupported Number");
		}

	/**
	 * Generic +
	 */
	public static Number plus(Number a, Number b)
		{
		int type=commonIndex(a, b);
		if(type==0)
			return a.byteValue()+b.byteValue();
		else if(type==1)
			return a.shortValue()+b.shortValue();
		else if(type==2)
			return a.intValue()+b.intValue();
		else if(type==3)
			return a.floatValue()+b.floatValue();
		else if(type==4)
			return a.doubleValue()+b.doubleValue();
		else if(type==5)
			return a.longValue()+b.longValue();
		else
			throw new RuntimeException("Unsupported Number");
		}
	
	
	/**
	 * Generic -
	 */
	public static Number minus(Number a, Number b)
		{
		int type=commonIndex(a, b);
		if(type==0)
			return a.byteValue()-b.byteValue();
		else if(type==1)
			return a.shortValue()-b.shortValue();
		else if(type==2)
			return a.intValue()-b.intValue();
		else if(type==3)
			return a.floatValue()-b.floatValue();
		else if(type==4)
			return a.doubleValue()-b.doubleValue();
		else if(type==5)
			return a.longValue()-b.longValue();
		else
			throw new RuntimeException("Unsupported Number");
		}
	
	
	
	/**
	 * Generic * 
	 */
	public static Number mul(Number a, Number b)
		{
		int type=commonIndex(a, b);
		if(type==0)
			return a.byteValue()*b.byteValue();
		else if(type==1)
			return a.shortValue()*b.shortValue();
		else if(type==2)
			return a.intValue()*b.intValue();
		else if(type==3)
			return a.floatValue()*b.floatValue();
		else if(type==4)
			return a.doubleValue()*b.doubleValue();
		else if(type==5)
			return a.longValue()*b.longValue();
		else
			throw new RuntimeException("Unsupported Number");
		}
	
	
	/**
	 * Generic /
	 */
	public static Number div(Number a, Number b)
		{
		int type=commonIndex(a, b);
		if(type==0)
			return a.byteValue()/b.byteValue();
		else if(type==1)
			return a.shortValue()/b.shortValue();
		else if(type==2)
			return a.intValue()/b.intValue();
		else if(type==3)
			return a.floatValue()/b.floatValue();
		else if(type==4)
			return a.doubleValue()/b.doubleValue();
		else if(type==5)
			return a.longValue()/b.longValue();
		else
			throw new RuntimeException("Unsupported Number");
		}
	
	
	
	
	
	
	
	private static int commonIndex(Number a, Number b)
		{
		return Math.max(typeIndex(a), typeIndex(b));
		}
	
	
	}
