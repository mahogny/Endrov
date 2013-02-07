package endrov.util.mathExpr;

public class MathExprSymbol implements MathExpr
	{
	public String val;
	
	public MathExprSymbol(String next)
		{
		val=next;
		}

	@Override
	public String toString()
		{
		return val;
		}
	
	
	public Object evalExpr(MathExprEnvironment env) throws MathExpr.EvalException
		{
		return env.getSymbolValue(val);
		}
	

	}
