package endrov.flowMeasure;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.flow.FlowType;
import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;

/**
 * Measurements of particles - identified regions in stacks.
 * Designed to handle channels by lazy evaluation of each stack.<br/>
 * 
 * There is by design no connection of the ID of one frame to the next,
 * this interpretation is done outside by the user.<br/>
 * 
 * This object can also be used as a quick property output for other things.
 * In that case the frame and ID should be set to 0 unless there is a good
 * reason for something else.<br/>
 * 
 * Output data can not normally be modified - a special filter could do it.
 * Filters act by lazily rewriting one measure to another.
 * 
 * TODO what about channel comparisons?
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
	 * One property to measure. Columns must be fixed. 
	 * 
	 */
	public interface MeasurePropertyType
		{
		public String getDesc();
		
		/**
		 * Which properties, must be fixed and the same for all particles 
		 */
		public Set<String> getColumns();
		
		/**
		 * Evaluate a stack, store in info. If a particle is not in the list it must be
		 * created. All particles in the map must receive data.
		 */
		public void analyze(EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info);
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

	
	

	/**
	 * Measure lazily
	 */
	private interface CalcInfo
		{
		public void calc();
		}
		
	/**
	 * Information about one frame - Just a list of particles, with a lazy evaluator
	 */
	public static class FrameInfo extends HashMap<Integer,ParticleInfo>
		{
		private static final long serialVersionUID = 1L;
		private CalcInfo calcInfo;
		
		public HashMap<String, Object> getCreate(int id)
			{
			ParticleInfo info=get(id);
			if(info==null)
				put(id,info=new ParticleInfo());
			return info.map;
			}
		
		
		}
	
	
	
	/**
	 * Data for one particle
	 */
	public static class ParticleInfo
		{
		private HashMap<String, Object> map=new HashMap<String, Object>();
		
		public Double getDouble(String s)
			{
			return (Double)map.get(s);
			}

		public Integer getInt(String s)
			{
			return (Integer)map.get(s);
			}

		public String getString(String s)
			{
			return (String)map.get(s);
			}

		public Object getObject(String s)
			{
			return map.get(s);
			}

		
		
		}
	
	
	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/

	//TODO this barely qualifies as an object since it contains no data. is this fine?
	
	@Override
	public void loadMetadata(Element e)
		{
		}

	@Override
	public String saveMetadata(Element e)
		{
		return metaType;
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	/**
	 * 
	 */
	private TreeMap<EvDecimal, FrameInfo> frameInfo=new TreeMap<EvDecimal, FrameInfo>();
	
	/**
	 * Columns ie properties, for each particle
	 */
	private TreeSet<String> columns=new TreeSet<String>();

	/**
	 * Which measures to invoke
	 */
	private Set<String> useMeasures=new HashSet<String>();



	public static final FlowType FLOWTYPE=new FlowType(ParticleMeasure.class);

	/**
	 * Empty measure
	 */
	public ParticleMeasure()
		{
		}

	/**
	 * Measure a stack
	 */
	public ParticleMeasure(EvStack stackValue, EvStack stackMask, List<String> use)
		{
		EvChannel chValue=new EvChannel();
		chValue.imageLoader.put(EvDecimal.ZERO, stackValue);
		EvChannel chMask=new EvChannel();
		chMask.imageLoader.put(EvDecimal.ZERO, stackMask);
		prepareEvaluate(chValue, chMask, use);
		}
	
	/**
	 * Measure one entire channel
	 */
	public ParticleMeasure(EvChannel chValue, EvChannel chMask, List<String> use)
		{
		prepareEvaluate(chValue, chMask, use);
		}
	
	/**
	 * Prepare all lazy evaluations. Measures should have been decided by this point
	 */
	private void prepareEvaluate(EvChannel chValue, final EvChannel chMask, List<String> use)
		{
		//Clear prior data
		useMeasures.clear();
		useMeasures.addAll(use);
		columns.clear();
		frameInfo.clear();
		
		//Figure out columns
		for(String s:useMeasures)
			columns.addAll(measures.get(s).getColumns());

		//Lazily evaluate stacks
		for(Map.Entry<EvDecimal, EvStack> e:chValue.imageLoader.entrySet())
			{
			final EvStack stackValue=e.getValue();
			final FrameInfo info=new FrameInfo();
			final EvDecimal frame=e.getKey();
			
			info.calcInfo=new CalcInfo(){
				public void calc()
					{
					for(String s:useMeasures)
						measures.get(s).analyze(stackValue, chMask.getFrame(frame),info);
					}
				};
			
			}
		}
	
	
	
	/**
	 * Get data for one frame
	 */
	public Map<Integer,ParticleInfo> getFrame(EvDecimal frame)
		{
		FrameInfo info=frameInfo.get(frame);
		if(info!=null)
			{
			if(info.calcInfo!=null)
				{
				info.calcInfo.calc();
				info.calcInfo=null;
				}
			return Collections.unmodifiableMap(info);
			}
		else
			return null;
		}
	
	
	
	/**
	 * Get the columns
	 */
	public SortedSet<String> getColumns()
		{
		return Collections.unmodifiableSortedSet(columns);
		}
	
	
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
