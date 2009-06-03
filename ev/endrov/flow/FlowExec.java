package endrov.flow;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;

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
	private WeakReference<EvContainer> parent=new WeakReference<EvContainer>(null);
	//It can be discussed if parent pointers should not be in the objects
	
	private EvPath currentPath;
	
	
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


	/**
	 * Set Data pointer
	 */
	public void setData(EvData data)
		{
		this.data=new WeakReference<EvData>(data);
		}
	
	/**
	 * Set parent pointer
	 */
	public void setParent(EvContainer con)
		{
		this.parent=new WeakReference<EvContainer>(con);
		}
	
	
	public EvData getData()
		{
		return data.get();
		}
	
	
	public EvContainer getParent()
		{
		return parent.get();
		}
	
	
	public EvPath getCurrentPath()
		{
		return currentPath;
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
	
	
	}