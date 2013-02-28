/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure.flow;

import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.util.math.EvDecimal;
import endrov.util.mathExpr.MathExpr;
import endrov.util.mathExpr.MathExprEnvironment;
import endrov.util.mathExpr.MathExprStdEnvironment;
import endrov.util.mathExpr.MathExpr.EvalException;

/**
 * Filter particle measure based on one double parameter
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpFilterParticleMeasure 
	{
	private MathExpr expr;
	
	public EvOpFilterParticleMeasure(MathExpr expr)
		{
		this.expr=expr;
		}

	
	

	/**
	 * Apply filter
	 */
	public ParticleMeasure exec(ParticleMeasure pm)
		{
		if(expr==null)
			return pm;
		else
			return pm.filter(new ParticleMeasure.ParticleFilter()
				{
					public boolean acceptParticle(int id, final ParticleMeasure.ColumnSet info)
						{
						//This object will be created many many times. Would be better if the filter got an entire frame to take care of
						final MathExprEnvironment env=new MathExprStdEnvironment()
							{
							public Object getSymbolValue(String name) throws EvalException
								{
								try
									{
									return info.getDouble(name);
									}
								catch (Exception e)
									{
									throw new EvalException("Could not get value for "+name);
									}
								}
							};
						
						try
							{
							return (Boolean)expr.evalExpr(env);
							}
						catch (EvalException e)
							{
							throw new RuntimeException(e.getMessage());
							}
						}
					
					public boolean acceptFrame(EvDecimal frame)
						{
						return true;
						}
				});
		}
	
	
	}
