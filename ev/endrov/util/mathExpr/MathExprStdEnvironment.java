package endrov.util.mathExpr;

public abstract class MathExprStdEnvironment implements MathExprEnvironment
	{
	private static void checkNumArg(String name, Object[] arg, int n) throws MathExpr.EvalException
		{
		if(arg.length!=n)
			throw new MathExpr.EvalException("The function "+name+" expects "+n+" arguments");
		}
	
	public Object evaluateFunction(String name, Object[] arg) throws MathExpr.EvalException
		{
		if(name.equals("+"))
			{
			checkNumArg(name, arg, 2);
			return ((Number)arg[0]).doubleValue() + ((Number)arg[1]).doubleValue();
			}
		else if(name.equals("-"))
			{
			checkNumArg(name, arg, 2);
			return ((Number)arg[0]).doubleValue() - ((Number)arg[1]).doubleValue();
			}
		else if(name.equals("*"))
			{
			checkNumArg(name, arg, 2);
			return ((Number)arg[0]).doubleValue() * ((Number)arg[1]).doubleValue();
			}
		else if(name.equals("/"))
			{
			checkNumArg(name, arg, 2);
			return ((Number)arg[0]).doubleValue() / ((Number)arg[1]).doubleValue();
			}
		
		
		else if(name.equals("!"))
			{
			checkNumArg(name, arg, 1);
			return !(Boolean)arg[0];
			}
		else if(name.equals(">"))
			{
			checkNumArg(name, arg, 2);
			return ((Number)arg[0]).doubleValue() > ((Number)arg[1]).doubleValue();
			}
		else if(name.equals("<"))
			{
			checkNumArg(name, arg, 2);
			return ((Number)arg[0]).doubleValue() < ((Number)arg[1]).doubleValue();
			}
		else if(name.equals("<=") || name.equals("=<"))
			{
			checkNumArg(name, arg, 2);
			return ((Number)arg[0]).doubleValue() <= ((Number)arg[1]).doubleValue();
			}
		else if(name.equals("==") || name.equals("="))
			{
			checkNumArg(name, arg, 2);
			return ((Number)arg[0]).doubleValue() == ((Number)arg[1]).doubleValue();
			}
		
		
		else if(name.equals("||") || name.equals("|"))
			{
			checkNumArg(name, arg, 2);
			return ((Boolean)arg[0]) || ((Boolean)arg[1]);
			}
		else if(name.equals("&&") || name.equals("&"))
			{
			checkNumArg(name, arg, 2);
			return ((Boolean)arg[0]) && ((Boolean)arg[1]);
			}
		else
			throw new MathExpr.EvalException("Function not defined: "+name);
		}
	}
