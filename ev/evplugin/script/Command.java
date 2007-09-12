package evplugin.script;
import java.util.*;

public abstract class Command
	{
	/**
	 * Run command. Throws BadParameterException if the arguments does not fit.
	 * If something else goes wrong, then another exception will be sent.
	 * Note that the name of this command will be the first argument, hence it should
	 * be skipped
	 */
	public abstract Exp exec(Vector<Exp> arg) throws Exception; //no guarantees on 1st arg?
	
	public abstract int numArg();

	public static boolean allNumber(Exp a, Exp b) throws Exception
		{
		return expVal(a) instanceof Number && expVal(b) instanceof Number;
		}

	public static boolean anyDouble(Exp a, Exp b) throws Exception
		{
		return expVal(a) instanceof Double || expVal(b) instanceof Double;
		}

	
	//////////////////// phase out below
	
	/**
	 * Get the symbol of a symbol expression
	 */
	public static String expSym(Exp e) throws Exception
		{
		ExpSym v=(ExpSym)e;
		return v.sym;
		}

	public static ExpApp expApp(Exp e) throws Exception
		{
		if(e instanceof ExpApp)
			return (ExpApp)e;
		else
			{
			ExpApp app=new ExpApp();
			app.expr.add(e);
			return app;
			}
		}

	public static Integer expInteger(Exp e) throws Exception
		{
		return ((Number)expVal(e)).intValue();
		}

	public static Double expDouble(Exp e) throws Exception
		{
		return ((Number)expVal(e)).doubleValue(); //hmm
		}

	/**
	 * Get the value of a value expression
	 */
	public static Object expVal(Exp e) throws Exception
		{
		ExpVal v=(ExpVal)e;
		return v.o;
		}
	
	/////////phase out above functions
	
	}
