/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeasure;

import java.util.*;

import endrov.flow.EvOpGeneral;
import endrov.flow.EvOpStack1;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.Vector3i;

/**
 * Analyze blobs in each stack over time.
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
	
	
	public ParticleMeasure exec(EvChannel regions, EvChannel image)
		{
		ParticleMeasure data=new ParticleMeasure();
		
		return data;
		}
	
	
	}
