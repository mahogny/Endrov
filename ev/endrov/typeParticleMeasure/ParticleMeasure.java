/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure;

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
		public boolean acceptParticle(int id, Particle info);
		}
	
	
	/**
	 * Information about one well
	 */
	public static class Well 
		{
		private TreeMap<EvDecimal,Frame> frameMap=new TreeMap<EvDecimal, ParticleMeasure.Frame>();
		
		
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
								Particle pInfo=oldInfo.getParticle(id);
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
		private HashMap<Integer,Particle> particleMap=new HashMap<Integer, Particle>();
		private List<Runnable> lazyCalc=new LinkedList<Runnable>();


		/**
		 * Calculate values using all lazy evaluations
		 */
		private void runLazyEvaluations()
			{
			if(!lazyCalc.isEmpty())
				{
				List<Runnable> c=new LinkedList<Runnable>(lazyCalc);
				lazyCalc.clear();
				for(Runnable r:c)
					r.run();
				}
			}

		
		
		public Particle getCreateParticle(int id)
			{
			runLazyEvaluations();
			Particle info=particleMap.get(id);
			if(info==null)
				particleMap.put(id,info=new Particle());
			return info;
			}

		public void putParticle(int id, Particle value)
			{
			runLazyEvaluations();
			particleMap.put(id, value);
			}

		public Particle getParticle(int id)
			{
			runLazyEvaluations();
			return particleMap.get(id);
			}

		public Set<Integer> getParticleIDs()
			{
			runLazyEvaluations();
			return particleMap.keySet();
			}

		public Set<Map.Entry<Integer, Particle>> entrySet()
			{
			runLazyEvaluations();
			return particleMap.entrySet();
			}

		public int size()
			{
			runLazyEvaluations();
			return particleMap.size();
			}

		public Collection<Particle> getParticles()
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
	public static class Particle
		{
		private HashMap<String, Object> map=new HashMap<String, Object>();
	
		/**
		 * Get value as double
		 */
		public Double getDouble(String s)
			{
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
	 * All wells (containing the particles) 
	 */
	private TreeMap<String, Well> wellMap=new TreeMap<String, Well>();

	/**
	 * Columns ie properties, for each particle
	 */
	private TreeSet<String> columns=new TreeSet<String>();

	
	

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
	 * Empty measure
	 */
	public ParticleMeasure()
		{
		}

	
	
	
	
	
		
	
	/**
	 * Get which columns exist
	 */
	public SortedSet<String> getColumns()
		{
		return Collections.unmodifiableSortedSet(columns);
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
	

	public void addColumn(String s)
		{
		columns.add(s);
		}

	
	public ParticleMeasure filter(final ParticleFilter filter)
		{
		ParticleMeasure out=new ParticleMeasure();
		
		//Copy all the columns
		out.columns.addAll(columns);
		
		//Copy all wells
		for(Map.Entry<String, Well> f:wellMap.entrySet())
			out.wellMap.put(f.getKey(),f.getValue().filter(filter));
		
		return out;
		}
	

	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/

	@Override
	public void loadMetadata(Element e)
		{
		columns.clear();
		wellMap.clear();
		
		LinkedList<String> columnsList=new LinkedList<String>();
		for(Object o:e.getChildren())
			if(o instanceof Element)
				{
				Element oe=(Element)o;
				//Columns must come first
				if(oe.getName().equals("column"))
					columnsList.add(oe.getAttributeValue("name"));
				else if(oe.getName().equals("well"))
					{
					//For each well
					Well well=new Well();
					String wellName=oe.getAttributeValue("name");
					
					//For each frame
					for(Object o2:oe.getChildren())
						if(o2 instanceof Element)
							{
							Element eFrame=(Element)o2;
							EvDecimal frame=new EvDecimal(eFrame.getAttributeValue("frame"));
							Frame f=new Frame();

							//For each particle
							for(Object o3:eFrame.getChildren())
								if(o3 instanceof Element)
									{
									Element eParticle=(Element)o3;
									int pid=Integer.parseInt(eParticle.getAttributeValue("id"));
									Particle p=new Particle();
									
									//For each column
									int colindex=0;
									for(Object o4:eParticle.getChildren())
										if(o4 instanceof Element)
											{
											Element eElem=(Element)o4;
											
											String val=eElem.getAttributeValue("v");
											if(eElem.getName().equals("d"))
												p.put(columnsList.get(colindex), Double.parseDouble(val));
											else if(eElem.getName().equals("i"))
												p.put(columnsList.get(colindex), Integer.parseInt(val));
											else if(eElem.getName().equals("null"))
												p.put(columnsList.get(colindex), null);
											else if(eElem.getName().equals("s"))
												p.put(columnsList.get(colindex), val);
											else
												throw new RuntimeException("Unknown type "+eElem.getName());
											colindex++;
											}
									if(colindex!=columnsList.size())
										throw new RuntimeException("Too few columns for one particle");
									
									f.putParticle(pid, p);
									}
							well.setFrame(frame, f);
							}
					setWell(wellName, well);
					}
				else
					throw new RuntimeException("Not recognized "+oe.getName());
				}
		columns.addAll(columnsList);
		}

	@Override
	public String saveMetadata(Element e)
		{
		//Write out columns and their types
		for(String col:getColumns())
			{
			Element eCol=new Element("column");
			eCol.setAttribute("name", col);
			e.addContent(eCol);
			}
		
		//For each well
		for(String wellName:getWellNames())
			{
			Well well=getWell(wellName);
			Element eWell=new Element("well");
			eWell.setAttribute("name", wellName);
			e.addContent(eWell);
			
			//For each frame
			for(EvDecimal frame:well.getFrames())
				{
				Frame f=well.getFrame(frame);

				Element eFrame=new Element("frame");
				eFrame.setAttribute("frame", frame.toString());
				eWell.addContent(eFrame);
				
				//For each particle
				for(int pid:f.getParticleIDs())
					{
					Element eParticle=new Element("p");
					eParticle.setAttribute("id",Integer.toString(pid));
					eFrame.addContent(eParticle);
					Particle p=f.getParticle(pid);
					
					//For each column
					for(String col:getColumns())
						{
						Element eElem;
						Object o=p.getObject(col);
						if(o==null)
							{
							eElem=new Element("null");
							}
						else if(o instanceof Double)
							{
							eElem=new Element("d");
							eElem.setAttribute("v", o.toString());
							}
						else if(o instanceof Integer)
							{
							eElem=new Element("i");
							eElem.setAttribute("v", o.toString());
							}
						else //if(o instanceof String)
							{
							//Convert anything else to a string
							eElem=new Element("s");
							eElem.setAttribute("v", o.toString());
							}
						eParticle.addContent(eElem);
						}
					}
				}
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
	
	
	public Set<String> getWellNames()
		{
		return wellMap.keySet();
		}
	
	
	

	}
