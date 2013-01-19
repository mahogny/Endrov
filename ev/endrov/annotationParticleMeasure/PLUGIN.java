/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.annotationParticleMeasure;
import endrov.annotationParticleMeasure.flow.FlowUnitIdentifyParticles3D;
import endrov.annotationParticleMeasure.flow.FlowUnitMeasureParticle;
import endrov.annotationParticleMeasure.flow.FlowUnitMeasureToFile;
import endrov.annotationParticleMeasure.flow.FlowUnitMeasureToSQL;
import endrov.annotationParticleMeasure.flow.FlowUnitShowMeasure;
import endrov.core.EvPluginDefinition;

public class PLUGIN extends EvPluginDefinition
	{
	public String getPluginName()
		{
		return "Flows: Measure";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return true;
		}
	
	public String cite()
		{
		return "";
		}
	
	public String[] requires()
		{
		return new String[]{};
		}
	
	public Class<?>[] getInitClasses()
		{
		
		
		return new Class[]{
				ParticleMeasure.class,
				
				FlowUnitIdentifyParticles3D.class,
				FlowUnitMeasureParticle.class,
				FlowUnitShowMeasure.class,
				
				FlowUnitMeasureToFile.class,
				FlowUnitMeasureToSQL.class,
				
		};
		
		
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
