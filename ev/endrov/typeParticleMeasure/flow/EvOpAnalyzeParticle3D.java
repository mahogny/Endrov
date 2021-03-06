/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure.flow;

import java.util.*;

import endrov.typeImageset.EvChannel;
import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.typeParticleMeasure.ParticleMeasureEval;
import endrov.util.ProgressHandle;

/**
 * Analyze blobs in each stack over time.
 * 
 * evop vs the real class - are both needed?
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpAnalyzeParticle3D 
	{
	private HashSet<String> enabled=new HashSet<String>();
	
	
	public void enable(String prop)
		{
		enabled.add(prop);
		}
	
	
	public void disable(String prop)
		{
		enabled.remove(prop);
		}
	
	
	public ParticleMeasure exec(ProgressHandle progh, EvChannel regions, EvChannel image)
		{
		ParticleMeasure data=ParticleMeasureEval.prepareEvaluate(progh, "", image, regions, new LinkedList<String>(enabled));
		return data;
		}
	
	
	}
