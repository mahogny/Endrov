/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure.flow;

import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.util.math.EvDecimal;

/**
 * Filter particle measure based on one double parameter
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpFilterParticleMeasureDouble 
	{
	private Double lower;
	private Double upper;
	private String prop;
	
	public EvOpFilterParticleMeasureDouble(Double lower, Double upper, String prop)
		{
		this.lower = lower;
		this.upper = upper;
		this.prop = prop;
		}

	/**
	 * Apply filter
	 */
	public ParticleMeasure exec(ParticleMeasure pm)
		{
		return pm.filter(new ParticleMeasure.ParticleFilter()
			{
				public boolean acceptParticle(int id, ParticleMeasure.Particle info)
					{
					double value=info.getDouble(prop);
					
					if(lower!=null)
						if(value<lower)
							return false;
					
					if(upper!=null)
						if(value>upper)
							return false;

					return true;
					}
				
				public boolean acceptFrame(EvDecimal frame)
					{
					return true;
					}
			});
		}
	
	
	}
