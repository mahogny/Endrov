package endrov.script;

import java.util.*;

/**
 * Expression: Application
 * @author Johan Henriksson
 */
public class ExpApp extends Exp
	{
	public Vector<Exp> expr=new Vector<Exp>();
	
	public String toString()
		{
		String s="(";
		for(Exp e:expr)
			s+=e+" ";
		s+=")";
		return s;
		}
	

	/**
	 * Apply an argument - return a clone (can we clone even better?)
	 */
	public ExpApp apply(Exp e)
		{
		ExpApp app=new ExpApp();
		for(int i=0;i<expr.size();i++)
			app.expr.add(expr.get(i));
		app.expr.add(e);
		
		//System.out.println("new applied: "+app);
		
		return app;
		}
	}
