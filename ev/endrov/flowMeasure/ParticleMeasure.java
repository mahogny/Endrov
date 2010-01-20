package endrov.flowMeasure;

import java.util.HashMap;
import java.util.Vector;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.flow.Flow;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.EvStack;

/**
 * 
 * 
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class ParticleMeasure extends EvObject
	{
	

	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static final String metaType="TODO";
	
	/**
	 * Property types
	 */
	protected static HashMap<String, ParticleMeasure.MeasurePropertyType> measures=new HashMap<String, ParticleMeasure.MeasurePropertyType>();
	
	
	/**
	 * One property to measure
	 */
	public interface MeasurePropertyType
		{
		public String getDesc();
		
		
		
		public void analyze(EvStack stack);
		}
	
	

	/**
	 * Register one property that can be measured
	 */
	public static void registerMeasure(String name, ParticleMeasure.MeasurePropertyType t)
		{
		synchronized (measures)
			{
			measures.put(name, t);
			}
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


	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,ParticleMeasure.class);
		
		ParticleMeasure.registerMeasure("max value", new MeasureMaxIntensity3d());
		ParticleMeasure.registerMeasure("sum value", new ParticleMeasureSumIntensity3d());
		}
	

	}
