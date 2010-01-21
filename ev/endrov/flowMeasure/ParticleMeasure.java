package endrov.flowMeasure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;
import endrov.util.Memoize;

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
		public Set<String> getColumns();
		public void analyze(EvStack stack, FrameInfo info);
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

	private Set<String> useMeasures=new HashSet<String>();
	
	public ParticleMeasure(EvStack stack, List<String> use)
		{
		useMeasures.addAll(use);
		EvChannel ch=new EvChannel();
		ch.imageLoader.put(EvDecimal.ZERO, stack);
		setChannel(ch);
		}
	
	
	public ParticleMeasure(EvChannel ch, List<String> use)
		{
		useMeasures.addAll(use);
		setChannel(ch);
		}
	
	private void setChannel(EvChannel ch)
		{
		//Figure out columns
		for(String s:useMeasures)
			columns.addAll(measures.get(s).getColumns());

		//Lazily evaluate stacks
		for(Map.Entry<EvDecimal, EvStack> e:ch.imageLoader.entrySet())
			{
			final EvStack stack=e.getValue();
			final FrameInfo info=new FrameInfo();
			
			info.calcInfo=new CalcInfo(){
				public void calc()
					{
					for(String s:useMeasures)
						measures.get(s).analyze(stack, info);
					}
				};
			
			}
		
		
		}
	
	
	
	
	private interface CalcInfo
		{
		public void calc();
		}
		
	/**
	 * Data for one frame
	 */
	public static class FrameInfo
		{
		private CalcInfo calcInfo;
		//TODO something to fill in values
		
		public TreeMap<String, Object> map=new TreeMap<String, Object>();
		
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
	
	private TreeMap<EvDecimal, FrameInfo> foo=new TreeMap<EvDecimal, FrameInfo>();
	private TreeSet<String> columns=new TreeSet<String>();
	
	/**
	 * Get data for one frame
	 */
	public FrameInfo getFrame(EvDecimal frame)
		{
		FrameInfo info=foo.get(frame);
		if(info==null)
			{
			info=new FrameInfo();
			foo.put(frame, info);
			
			for(MeasurePropertyType p:measures.values())
				p.analyze(new EvStack(), info);
			//TODO need to plug stack lazily - hence build for all frames
			//
			
			}
		return info;
		}
	
	
	
	/**
	 * Get the columns
	 */
	public Set<String> getColumns()
		{
		//Cache columns
		if(columns==null)
			{
			columns=new TreeSet<String>();
			for(MeasurePropertyType p:measures.values())
				columns.addAll(p.getColumns());
			}
		return columns;
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
