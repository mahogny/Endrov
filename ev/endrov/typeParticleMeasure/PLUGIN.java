/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure;
import endrov.core.EvPluginDefinition;
import endrov.typeParticleMeasure.flow.FlowUnitIdentifyParticles3D;
import endrov.typeParticleMeasure.flow.FlowUnitMeasureParticle;
import endrov.typeParticleMeasure.flow.FlowUnitMeasureToFile;
import endrov.typeParticleMeasure.flow.FlowUnitMeasureToSQL;
import endrov.typeParticleMeasure.flow.FlowUnitShowMeasure;

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
