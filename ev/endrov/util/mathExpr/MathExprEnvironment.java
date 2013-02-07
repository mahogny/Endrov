package endrov.util.mathExpr;

public interface MathExprEnvironment
	{
	public Object getSymbolValue(String name) throws MathExpr.EvalException;
	public Object evaluateFunction(String name, Object[] arg) throws MathExpr.EvalException;
	}
