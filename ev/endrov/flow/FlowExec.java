/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.util.ProgressHandle;

/**
 * To allow multiple instances of a running Flow, the state has to be separated from the flow declaration
 * 
 * @author Johan Henriksson
 *
 */
public class FlowExec
	{
	private WeakHashMap<FlowUnit, UnitState> unitStates=new WeakHashMap<FlowUnit, UnitState>();
	private WeakReference<EvData> data=new WeakReference<EvData>(null);
	/*
	private WeakReference<EvContainer> parent=new WeakReference<EvContainer>(null);   //It can be discussed if parent pointers should not be in the objects
	private WeakReference<Flow> flow=new WeakReference<Flow>(null);*/
	private EvPath currentPath;
	
	public ProgressHandle ph=new ProgressHandle(); //TODO connect it
	
	/**
	 * Connection to outside world
	 */
	public FlowExecListener listener=new IgnorantListener();

	/**
	 * A connection to the outside using input/output objects
	 */
	public interface FlowExecListener
		{
		public Object getInputObject(String name);
		public void setOutputObject(String name, Object ob);
		}

	/**
	 * Listener that does nothing i.e. no connection to outside
	 */
	public static class IgnorantListener implements FlowExecListener
		{
		public Object getInputObject(String name)
			{
			return null;
			}
		public void setOutputObject(String name, Object ob)
			{
			System.out.println("output: "+name+"\t======>\t"+ob);
			}		
		}

	
	public FlowExec(EvData data2, EvContainer parent2, EvPath path,	Flow flow)
		{
		data=new WeakReference<EvData>(data2);
		//parent=new WeakReference<EvContainer>(parent2);
		currentPath=path;
		//this.flow=new WeakReference<Flow>(flow);
		
		System.out.println("the path is "+currentPath);
		
		}

	public EvData getData()
		{
		return data.get();
		}
	
	public EvContainer getParent()
		{
		return currentPath.getParent().getObject(data.get());
		
		//return parent.get();
		}
	
	public EvPath getPath()
		{
		return currentPath;
		}
	
	public Flow getFlow()
		{
		return (Flow)currentPath.getObject(data.get());
		}
	
	
	/**
	 * State of a unit
	 */
	private static class UnitState
		{
		Map<String,Object> lastOutputMap=new HashMap<String, Object>();
		Object state;
		}
	
	/**
	 * If a unit requires internal state then it has to provide this method to create it.
	 * This is because of all initialization that is expected to take place.
	 */
	public interface StateCreator<E>
		{
		public E createState();
		}
	
	/**
	 * Get unitstate or create if needed
	 */
	private UnitState getCreateUnit(FlowUnit unit)
		{
		UnitState s=unitStates.get(unit);
		if(s==null)
			unitStates.put(unit, s=new UnitState());
		return s;
		}
	
	/**
	 * Get the entire output map of a unit
	 */
	public Map<String,Object> getLastOutput(FlowUnit unit)
		{
		return getCreateUnit(unit).lastOutputMap;
		}

	/**
	 * Get the output map of the unit, clearing it to prepare for new output
	 */
	public Map<String,Object> getLastOutputCleared(FlowUnit unit)
		{
		Map<String,Object> map=getCreateUnit(unit).lastOutputMap;
		map.clear();
		return map;
		}
	
	
	/**
	 * Get state for a unit. Hides dirty details and provides type matching
	 */
	@SuppressWarnings("unchecked")
	public <E> E getState(FlowUnit unit, StateCreator<E> sc)
		{
		UnitState s=getCreateUnit(unit);
		if(s.state==null)
			s.state=sc.createState();
		return (E)s.state;
		}
	
	
	
	/**
	 * Get the last units without further output
	 */
	private Collection<FlowUnit> getLeafNodes()
		{
		Flow f=getFlow();
		Set<FlowUnit> units=new HashSet<FlowUnit>(f.units);
		for(FlowConn c:f.conns)
			if(units.remove(c.fromUnit));
		return units;
		}
		
	
	
	/**
	 * Evaluate all units
	 */
	public void evaluateAll() throws Exception
		{
		Flow f=getFlow();
		for(FlowUnit u:getLeafNodes())
			updateTopBottom(f,u);
		
		//TODO this is BAD! it will force reevaluation of all units and will not work with loops. rewrite from scratch 
		}


	/**
	 * Evaluate flow top-bottom with this component as the top
	 */
	public void updateTopBottom(FlowUnit u) throws Exception
		{
		updateTopBottom(getFlow(), u);
		}
	
	/**
	 * Evaluate flow top-bottom with this component as the top
	 */
	private void updateTopBottom(Flow flow, FlowUnit u) throws Exception
		{
		// TODO cache. how to say if a component is done?
		Set<FlowUnit> toUpdate = new HashSet<FlowUnit>();
		for (String arg : u.getTypesIn(flow).keySet())
			toUpdate.add(flow.getInputUnit(u, arg));
		for (FlowUnit uu : toUpdate)
			if (uu!=null)
				updateTopBottom(flow, uu);
		u.evaluate(flow, this);
		}
	
	
	/**
	 * Get execution state as string
	 */
	public String toString()
		{
		StringBuffer sb=new StringBuffer();
		for(Map.Entry<FlowUnit, UnitState> e:unitStates.entrySet())
			{
			sb.append(" "+e.getKey()+"\n");
			for(Map.Entry<String, Object> bind:e.getValue().lastOutputMap.entrySet())
				sb.append("    "+bind.getKey()+"\t=>\t"+bind.getValue()+"\n");
			}
		return sb.toString();
		}
	

	
	}