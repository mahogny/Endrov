/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.core.log.EvLog;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.flow.FlowType;
import endrov.typeParticleMeasure.calc.MeasureProperty;
import endrov.typeParticleMeasure.calc.ParticleMeasureCenterOfMass;
import endrov.typeParticleMeasure.calc.ParticleMeasureCentroid;
import endrov.typeParticleMeasure.calc.ParticleMeasureGeometricPCA;
import endrov.typeParticleMeasure.calc.ParticleMeasureMaxIntensity;
import endrov.typeParticleMeasure.calc.ParticleMeasureMeanIntensity;
import endrov.typeParticleMeasure.calc.ParticleMeasureMedianIntensity;
import endrov.typeParticleMeasure.calc.ParticleMeasureModalIntensity;
import endrov.typeParticleMeasure.calc.ParticleMeasurePerimeter;
import endrov.typeParticleMeasure.calc.ParticleMeasureSumIntensity;
import endrov.typeParticleMeasure.calc.ParticleMeasureSurfaceArea;
import endrov.typeParticleMeasure.calc.ParticleMeasureVolume;
import endrov.util.math.EvDecimal;

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
	public static final String metaType="ParticleMeasure";
	

	public static final FlowType FLOWTYPE=new FlowType(ParticleMeasure.class);

	

	

	/**
	 * Filter of particle measure data
	 * 
	 * TODO can use the same structure also for adding data!
	 */
	public static interface ParticleFilter
		{
		/**
		 * Accept a frame? if false then all particles will be discarded
		 */
		public boolean acceptFrame(EvDecimal frame);
		
		/**
		 * Accept a particle?
		 */
		public boolean acceptParticle(int id, ColumnSet info);
		}
	
	
	/**
	 * Information about one well
	 */
	public static class Well 
		{
		private TreeMap<EvDecimal,Frame> frameMap=new TreeMap<EvDecimal, ParticleMeasure.Frame>();
		
		private ColumnSet columns=new ColumnSet();

		public ColumnSet getWellColumns()
			{
			return columns;
			}
		
		/**
		 * Get data for one frame. Evaluate if necessary
		 */
		public Frame getFrame(EvDecimal frame)
			{
			Frame info=frameMap.get(frame);
			if(info!=null)
				return info;
			else
				return null;
			}

		public void setFrame(EvDecimal frame, Frame info)
			{
			frameMap.put(frame, info);
			}

		/**
		 * Get which frames exist
		 */
		public SortedSet<EvDecimal> getFrames()
			{
			return Collections.unmodifiableSortedSet((SortedSet<EvDecimal>)frameMap.keySet());
			}

		
		
		/**
		 * Get a new particle measure where particles and frames have been filtered.
		 * Will execute lazily.
		 */
		public Well filter(final ParticleFilter filter)
			{
			Well out=new Well();
			
			//Copy all frames
			for(Map.Entry<EvDecimal, Frame> f:frameMap.entrySet())
				if(filter.acceptFrame(f.getKey()))
					{
					//Create place-holder for frame
					final Frame oldInfo=f.getValue();
					final Frame newInfo=new Frame();
					out.frameMap.put(f.getKey(), newInfo);

					//Filter can execute lazily as well
					newInfo.registerLazyCalculation(
					new Runnable()
						{
						public void run()
							{
							//Filter particles
							for(int id:oldInfo.getParticleIDs())
								{
								ColumnSet pInfo=oldInfo.getParticle(id);
								if(filter.acceptParticle(id, pInfo))
									newInfo.putParticle(id,pInfo);
								}
							}
						});
					}
			
			return out;
			}

		}

	
	/**
	 * Information about one frame - Just a list of particles, with a lazy evaluator
	 */
	public static class Frame 
		{
		private HashMap<Integer,ColumnSet> particleMap=new HashMap<Integer, ColumnSet>();
		private ColumnSet columns=new ColumnSet();
		
		private List<Runnable> lazyCalc=new LinkedList<Runnable>();

		/**
		 * Calculate values using all lazy evaluations
		 */
		private void runLazyEvaluations()
			{
			if(!lazyCalc.isEmpty())
				{
				System.out.println("running pm lazy calc");
				List<Runnable> c=new LinkedList<Runnable>(lazyCalc);
				lazyCalc.clear();
				for(Runnable r:c)
					r.run();
				}
			}

		
		public ColumnSet getFrameColumns()
			{
			runLazyEvaluations();
			return columns;
			}
		
		public ColumnSet getCreateParticle(int id)
			{
			runLazyEvaluations();
			ColumnSet info=particleMap.get(id);
			if(info==null)
				particleMap.put(id,info=new ColumnSet());
			return info;
			}

		public void putParticle(int id, ColumnSet value)
			{
			runLazyEvaluations();
			particleMap.put(id, value);
			}

		public ColumnSet getParticle(int id)
			{
			runLazyEvaluations();
			return particleMap.get(id);
			}

		public Set<Integer> getParticleIDs()
			{
			runLazyEvaluations();
			return particleMap.keySet();
			}

		public Set<Map.Entry<Integer, ColumnSet>> entrySet()
			{
			runLazyEvaluations();
			return particleMap.entrySet();
			}

		public int size()
			{
			runLazyEvaluations();
			return particleMap.size();
			}

		public Collection<ColumnSet> getParticles()
			{
			runLazyEvaluations();
			return particleMap.values();
			}

		public void registerLazyCalculation(Runnable runnable)
			{
			lazyCalc.add(runnable);
			}
		}
	
	

	/**
	 * Data for one particle. It's in the form Key -> Value. Value can be of any type, but functions exist for automatic data conversions
	 */
	public static class ColumnSet
		{
		private HashMap<String, Object> map=new HashMap<String, Object>();
	
		/**
		 * Get value as double
		 */
		public Double getDouble(String s)
			{
			if(!map.containsKey(s))
				throw new RuntimeException("Unknown type "+s);
			Object o=map.get(s);
			if(o instanceof String)
				return Double.parseDouble((String)o);
			else if(o instanceof Number)
				return ((Number)o).doubleValue();
			else if(o==null)
				return null;
			else
				throw new RuntimeException("Bad type: "+o.getClass());
			}
	
		/**
		 * Get value as integer
		 */
		public Integer getInt(String s)
			{
			if(!map.containsKey(s))
				throw new RuntimeException("Unknown type "+s);
			Object o=map.get(s);
			if(o instanceof String)
				return Integer.parseInt((String)o);
			else if(o instanceof Number)
				return ((Number)o).intValue();
			else if(o==null)
				return null;
			else
				throw new RuntimeException("Bad type: "+o.getClass());
			}
	
		/**
		 * Get value as string
		 */
		public String getString(String s)
			{
			if(!map.containsKey(s))
				throw new RuntimeException("Unknown type "+s);
			Object o=map.get(s);
			if(o==null)
				return null;
			else
				return o.toString();
			}
	
		/**
		 * Get raw object
		 */
		public Object getObject(String s)
			{
			if(!map.containsKey(s))
				throw new RuntimeException("Unknown type "+s);
			return map.get(s);
			}

		public void put(String key, Object value)
			{
			map.put(key, value);
			}
		}
	


	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/


	/**
	 * Empty measure
	 */
	public ParticleMeasure()
		{
		}

	/**
	 * All wells (containing the particles) 
	 */
	private TreeMap<String, Well> wellMap=new TreeMap<String, Well>();

	/**
	 * Columns ie properties, for each particle
	 */
	private TreeSet<String> particleColumns=new TreeSet<String>();

	/**
	 * Columns ie properties, for each well/frame
	 */
	private TreeSet<String> frameColumns=new TreeSet<String>();
	
	
	/**
	 * Columns ie properties, for each well
	 */
	private TreeSet<String> wellColumns=new TreeSet<String>();


	/**
	 * Get a well
	 */
	public Well getWell(String wellName)
		{
		return wellMap.get(wellName);
		}

	/**
	 * Set a well
	 */
	public void setWell(String wellName, Well well)
		{
		wellMap.put(wellName, well);
		}

		
	
	/**
	 * Get which columns exist for each particle
	 */
	public SortedSet<String> getParticleColumns()
		{
		return Collections.unmodifiableSortedSet(particleColumns);
		}

	/**
	 * Get which columns exist for each well/frame
	 */
	public SortedSet<String> getFrameColumns()
		{
		return Collections.unmodifiableSortedSet(frameColumns);
		}

	/**
	 * Get which columns exist for each well
	 */
	public SortedSet<String> getWellColumns()
		{
		return Collections.unmodifiableSortedSet(wellColumns);
		}

	
	@Override
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}

	@Override
	public String getMetaTypeDesc()
		{
		return "Measured properties";
		}


	
	
	
	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}
	

	public void addFrameColumn(String s)
		{
		frameColumns.add(s);
		}

	public void addWellColumn(String s)
		{
		wellColumns.add(s);
		}

	public void addParticleColumn(String s)
		{
		particleColumns.add(s);
		}

	
	public ParticleMeasure filter(final ParticleFilter filter)
		{
		ParticleMeasure out=new ParticleMeasure();
		
		//Copy all the columns
		out.particleColumns.addAll(particleColumns);
		
		//Copy all wells
		for(Map.Entry<String, Well> f:wellMap.entrySet())
			out.wellMap.put(f.getKey(),f.getValue().filter(filter));
		
		return out;
		}
	

	public Set<String> getWellNames()
		{
		return wellMap.keySet();
		}
	
	public void clearData()
		{
		wellMap.clear();
		particleColumns.clear();
		}

	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/

	@Override
	public void loadMetadata(Element e)
		{
		Element ePerParticle=e.getChild("perparticle");
		Element ePerFrame=e.getChild("perframe");
		Element ePerWell=e.getChild("perwell");
		try
			{
			ParticleMeasureIO.readCSV(this, new StringReader(ePerParticle.getText()), '\t');
			ParticleMeasureIO.readCSV(this, new StringReader(ePerFrame.getText()), '\t');
			ParticleMeasureIO.readCSV(this, new StringReader(ePerWell.getText()), '\t');
			}
		catch (IOException e1)
			{
			EvLog.printError(e1);
			}
		
		}

	@Override
	public String saveMetadata(Element e)
		{
		try
			{
			StringWriter swParticle=new StringWriter();
			ParticleMeasureIO.writeCSVperparticle(this, swParticle, true, "\t", true);
			swParticle.flush();

			StringWriter swFrame=new StringWriter();
			ParticleMeasureIO.writeCSVperframe(this, swFrame, true, "\t", true);
			swFrame.flush();

			StringWriter swWell=new StringWriter();
			ParticleMeasureIO.writeCSVperwell(this, swWell, true, "\t", true);
			swWell.flush();

			Element ePerParticle=new Element("perparticle");
			ePerParticle.setText(swParticle.toString());
			e.addContent(ePerParticle);
			
			Element ePerFrame=new Element("perframe");
			ePerFrame.setText(swParticle.toString());
			e.addContent(ePerFrame);
			
			Element ePerWell=new Element("perwell");
			ePerWell.setText(swParticle.toString());
			e.addContent(ePerWell);
			}
		catch (IOException e1)
			{
			EvLog.printError(e1);
			}
		
		return metaType;
		}
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,ParticleMeasure.class);
		
		MeasureProperty.registerMeasure("max value", new ParticleMeasureMaxIntensity());
		MeasureProperty.registerMeasure("sum value", new ParticleMeasureSumIntensity());
		MeasureProperty.registerMeasure("mean value", new ParticleMeasureMeanIntensity());
		MeasureProperty.registerMeasure("modal value", new ParticleMeasureModalIntensity());
		MeasureProperty.registerMeasure("median value", new ParticleMeasureMedianIntensity());
		
		MeasureProperty.registerMeasure("volume", new ParticleMeasureVolume());
		MeasureProperty.registerMeasure("center of mass", new ParticleMeasureCenterOfMass());
		MeasureProperty.registerMeasure("centroid", new ParticleMeasureCentroid());
		MeasureProperty.registerMeasure("surface area", new ParticleMeasureSurfaceArea());
		MeasureProperty.registerMeasure("perimeter", new ParticleMeasurePerimeter());
		MeasureProperty.registerMeasure("Geometric PCA", new ParticleMeasureGeometricPCA());
		}
	

	}
