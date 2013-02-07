package endrov.util.mathExpr;

public class MathExprNumber implements MathExpr
	{
	public String val;
	
	public MathExprNumber(String next)
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
		return Double.parseDouble(val);
		}
	

	}
