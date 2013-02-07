package endrov.util.mathExpr;

public interface MathExpr
	{
	
	
	public static class EvalException extends Exception
	{
	private static final long serialVersionUID = 1L;
	public EvalException(String message)
		{
		super(message);
		}
	}

	public Object evalExpr(MathExprEnvironment env) throws MathExpr.EvalException;

	}
