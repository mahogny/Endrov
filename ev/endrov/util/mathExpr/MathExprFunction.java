package endrov.util.mathExpr;

import java.util.LinkedList;

/**
 * Mathematical expression:
 * 
 * func(a,b,c,d...)
 * 
 * @author Johan Henriksson
 *
 */
public class MathExprFunction implements MathExpr
	{
	public String function;
	public MathExpr[] arg;
	
	public MathExprFunction(String n, MathExpr... a)
		{
		function=n;
		arg=a;
		}
	
	@Override
	public String toString()
		{
		StringBuilder sb=new StringBuilder();
		sb.append(function);
		sb.append("(");
		
		boolean first=true;
		for(MathExpr e:arg)
			{
			if(!first)
				sb.append(" ");
			sb.append(e.toString());
			first=false;
			}
		
		sb.append(")");
		return sb.toString();
		}
	
	
	
	public Object evalExpr(MathExprEnvironment env) throws MathExpr.EvalException
		{
		LinkedList<Object> evalArg=new LinkedList<Object>();
		for(MathExpr e:arg)
			evalArg.add(e.evalExpr(env));
		return env.evaluateFunction(function, evalArg.toArray());
		}
	

	}
