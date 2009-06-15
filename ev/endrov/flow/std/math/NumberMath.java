package endrov.flow.std.math;

import endrov.util.EvDecimal;


/**
 * Scalar with upconversion semantics
 * @author Johan Henriksson
 *
 */
public class NumberMath
	{

	
	/**
	 * Order of types. One type fits in the next type
	 */
	private static int typeIndex(Number a)
		{
		if(a instanceof Byte)
			return 0;
		else if(a instanceof Short)
			return 1;
		else if(a instanceof Integer)
			return 2;
		else if(a instanceof Long)
			return 3;
		else if(a instanceof Float)
			return 4;
		else if(a instanceof Double)
			return 5;
		else if(a instanceof EvDecimal)
			return 5; //TODO
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
			return a.longValue()+b.longValue();
		else if(type==4)
			return a.floatValue()+b.floatValue();
		else if(type==5)
			return a.doubleValue()+b.doubleValue();
		else if(type==6)
			return ((EvDecimal)a).add((EvDecimal)b);
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
			return a.longValue()-b.longValue();
		else if(type==4)
			return a.floatValue()-b.floatValue();
		else if(type==5)
			return a.doubleValue()-b.doubleValue();
		else if(type==6)
			return ((EvDecimal)a).subtract((EvDecimal)b);
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
			return a.longValue()*b.longValue();
		else if(type==4)
			return a.floatValue()*b.floatValue();
		else if(type==5)
			return a.doubleValue()*b.doubleValue();
		else if(type==6)
			return ((EvDecimal)a).multiply((EvDecimal)b);
		else
			throw new RuntimeException("Unsupported Number");
		}
	
	
	/**
	 * Generic max
	 */
	public static Number max(Number a, Number b)
		{
		int type=commonIndex(a, b);
		if(type==0)
			return (byte)Math.max(a.byteValue(),b.byteValue());
		else if(type==1)
			return (short)Math.max(a.shortValue(),b.shortValue());
		else if(type==2)
			return Math.max(a.intValue(),b.intValue());
		else if(type==3)
			return Math.max(a.longValue(),b.longValue());
		else if(type==4)
			return Math.max(a.floatValue(),b.floatValue());
		else if(type==5)
			return Math.max(a.doubleValue(),b.doubleValue());
		else if(type==6)
			return ((EvDecimal)a).max((EvDecimal)b);
		else
			throw new RuntimeException("Unsupported Number");
		}
	
	public static boolean greaterThan(Number a, Number b)
		{
		int type=commonIndex(a, b);
		if(type==0)
			return a.byteValue()>b.byteValue();
		else if(type==1)
			return a.shortValue()>b.shortValue();
		else if(type==2)
			return a.intValue()>b.intValue();
		else if(type==3)
			return a.longValue()>b.longValue();
		else if(type==4)
			return a.floatValue()>b.floatValue();
		else if(type==5)
			return a.doubleValue()>b.doubleValue();
		else if(type==6)
			return ((EvDecimal)a).greater((EvDecimal)b);
		else
			throw new RuntimeException("Unsupported Number");
		}
	
	
	/**
	 * Generic min
	 */
	public static Number min(Number a, Number b)
		{
		int type=commonIndex(a, b);
		if(type==0)
			return (byte)Math.min(a.byteValue(),b.byteValue());
		else if(type==1)
			return (short)Math.min(a.shortValue(),b.shortValue());
		else if(type==2)
			return Math.min(a.intValue(),b.intValue());
		else if(type==3)
			return Math.min(a.longValue(),b.longValue());
		else if(type==4)
			return Math.min(a.floatValue(),b.floatValue());
		else if(type==5)
			return Math.min(a.doubleValue(),b.doubleValue());
		else if(type==6)
			return ((EvDecimal)a).min((EvDecimal)b);
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
			return a.longValue()/b.longValue();
		else if(type==4)
			return a.floatValue()/b.floatValue();
		else if(type==5)
			return a.doubleValue()/b.doubleValue();
		else if(type==6)
			return ((EvDecimal)a).divide((EvDecimal)b);
		else
			throw new RuntimeException("Unsupported Number");
		}
	
	
	
	
	
	
	
	private static int commonIndex(Number a, Number b)
		{
		return Math.max(typeIndex(a), typeIndex(b));
		}
	
	
	}
