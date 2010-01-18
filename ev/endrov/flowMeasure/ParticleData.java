package endrov.flowMeasure;

import java.util.Vector;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.flow.Flow;
import endrov.flow.FlowUnitDeclaration;

/**
 * 
 * 
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class ParticleData extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static final String metaType="TODO";
	
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,ParticleData.class);
		
		MeasureParticle.registerMeasure("max value", new MeasureMaxIntensity3d());
		MeasureParticle.registerMeasure("sum value", new MeasureSumIntensity3d());
		}

	
	
	
	
	
	
	
	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/

	@Override
	public void loadMetadata(Element e)
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public String saveMetadata(Element e)
		{
		// TODO Auto-generated method stub
		return metaType;
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	@Override
	public void buildMetamenu(JMenu menu)
		{
		}

	@Override
	public String getMetaTypeDesc()
		{
		return "Measured properties";
		}


	
	
	
	

	}
