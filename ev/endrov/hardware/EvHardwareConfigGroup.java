package endrov.hardware;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom.Element;

import endrov.core.EndrovCore;
import endrov.core.PersonalConfig;
import endrov.core.log.EvLog;
import endrov.core.observer.GeneralObserver;
import endrov.gui.window.BasicWindow;


/**
 * Config group for hardware aka a meta state. Should maybe even be made a new type of device?
 * 
 * @author Johan Henriksson
 *
 */
public class EvHardwareConfigGroup
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/

	
	/**
	 * Config groups
	 */
	private static Map<String,EvHardwareConfigGroup> groups=Collections.synchronizedMap(new TreeMap<String, EvHardwareConfigGroup>());
	
	
	public static void putConfigGroup(String name, EvHardwareConfigGroup hwg)
		{
		groups.put(name, hwg);
		updateAllListeners();
		}

	public static void removeConfigGroup(String name)
		{
		groups.remove(name);
		updateAllListeners();
		}
	
	public static Map<String,EvHardwareConfigGroup> getConfigGroups()
		{
		return groups;
		}

	public static EvHardwareConfigGroup getConfigGroup(String name)
		{
		return groups.get(name);
		}
	
	private static void updateAllListeners()
		{
		for(GroupsChangedListener l:groupsChangedListeners.getListeners())
			l.hardwareGroupsChanged();
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	/**
	 * Which properties should be included in this group. Only used as an aid to create new states, since
	 * a group have no states when created and the list must be stored somewhere
	 */
	public Set<EvDevicePropPath> propsToInclude=new TreeSet<EvDevicePropPath>();
	
	/**
	 * The states
	 */
	private Map<String, State> states=new TreeMap<String, State>();
	
	/**
	 * Get the name of all states
	 * @return
	 */
	public Set<String> getStateNames()
		{
		return new TreeSet<String>(states.keySet());
		}
	
	
	
	
	/**
	 * One state
	 * @author Johan Henriksson
	 *
	 */
	public static class State
		{
		public Map<EvDevicePropPath, String> propMap=new HashMap<EvDevicePropPath, String>();

		/**
		 * Make a string representation of the state 
		 */
		public String toString()
			{
			StringBuilder sb=new StringBuilder();
			boolean first=true;
			for(Map.Entry<EvDevicePropPath, String> e:propMap.entrySet())
				{
				if(!first)
					sb.append(", ");
				sb.append(e.getKey());
				sb.append("=");
				sb.append(e.getValue());
				first=false;
				}
			return sb.toString();
			}
		
		/**
		 * Set state - change all other devices. Synchronization?
		 */
		public void activate()
			{
			System.out.println("Activate");
			for(Map.Entry<EvDevicePropPath, String> e:propMap.entrySet())
				{
				EvDevice dev=e.getKey().getDevice();
				String prop=e.getKey().getProperty();
				System.out.println(e.getKey()+" => "+e.getValue());
				if(dev!=null)
					dev.setPropertyValue(prop, e.getValue());
				else
					EvLog.printError("No such device, "+e.getKey(), null);
				}
			}

		/**
		 * Check if this is the current state
		 */
		public boolean isCurrent()
			{
			//Check if all property values are correct
			for(Map.Entry<EvDevicePropPath, String> e:propMap.entrySet())
				{
				EvDevice device=e.getKey().getDevice();
				String propName=e.getKey().getProperty();
				String curPropVal=device.getPropertyValue(propName);

				if(!curPropVal.equals(e.getValue()))
					return false;
				}
			return true;
			}
			
		
		public static State recordCurrent(Collection<EvDevicePropPath> propsToInclude)
			{
			State state=new State();
			for(EvDevicePropPath p:propsToInclude)
				state.propMap.put(p, p.getDevice().getPropertyValue(p.getProperty()));
			return state;
			}
		
		}

	/**
	 * Get one state by name
	 */
	public State getState(String name)
		{
		return states.get(name);
		}
	
	/**
	 * Remove one state
	 */
	public void removeState(String name)
		{
		states.remove(name);
		updateAllListeners();
		}
	
	/**
	 * Store current state as a new state
	 */
	public void captureCurrentStateAsNew(String name)
		{
		State st=new State();
		for(EvDevicePropPath p:propsToInclude)
			st.propMap.put(p, p.getDevice().getPropertyValue(p.getProperty()));
		states.put(name,st);
		updateAllListeners();
		}
	
	public void putState(String name, State state)
		{
		states.put(name,state);
		updateAllListeners();
		}

	
	
	public final static GeneralObserver<GroupsChangedListener> groupsChangedListeners=new GeneralObserver<GroupsChangedListener>(); 
	public interface GroupsChangedListener
		{
		public void hardwareGroupsChanged();
		}
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		
		
		EndrovCore.personalConfigLoaders.put("configgroups",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				for(Object o:e.getChildren())
					{
					Element eGroup=(Element)o;

					EvHardwareConfigGroup group=new EvHardwareConfigGroup();
					String groupName=eGroup.getAttributeValue("name");
					groups.put(groupName, group);

					for(Object oo:eGroup.getChildren())
						if(((Element)oo).getName().equals("state"))
							{
							//One state
							Element eState=(Element)oo;
							State state=new State();
							
							for(Object ooo:eState.getChildren())
								{
								Element eSetting=(Element)ooo;
								EvDevicePropPath dev=new EvDevicePropPath(
										new EvDevicePath(eSetting.getAttributeValue("device")),
										eSetting.getAttributeValue("property"));
								state.propMap.put(dev, 
										eSetting.getAttributeValue("value"));
								}
							group.states.put(eState.getAttributeValue("name"), state);
							}
						else if(((Element)oo).getName().equals("include"))
							{
							//Properties to include for new states
							Element eState=(Element)oo;
							for(Object ooo:eState.getChildren())
								{
								Element eSetting=(Element)ooo;
								EvDevicePropPath dev=new EvDevicePropPath(
										new EvDevicePath(eSetting.getAttributeValue("device")),
										eSetting.getAttributeValue("property"));
								group.propsToInclude.add(dev);
								}
							}
					}
				//May need to update GUI if windows already loaded
				BasicWindow.updateWindows();
				}
			
			public void savePersonalConfig(Element e)
				{
				Element eConfig=new Element("configgroups");
				
				for(Map.Entry<String,EvHardwareConfigGroup> ge:groups.entrySet())
					{
					Element eGroup=new Element("group");
					eGroup.setAttribute("name", ge.getKey());
					EvHardwareConfigGroup group=ge.getValue();
					
					//Properties to include
					Element eInc=new Element("include");
					for(EvDevicePropPath ve:group.propsToInclude)
						{
						Element eSetting=new Element("setting");
						eSetting.setAttribute("device", ve.getDeviceName());
						eSetting.setAttribute("property", ve.getProperty());
						eInc.addContent(eSetting);
						}
					eGroup.addContent(eInc);

					//Config groups
					for(Map.Entry<String, State> se:group.states.entrySet())
						{
						Element eState=new Element("state");
						eState.setAttribute("name", se.getKey());

						State state=se.getValue();
						for(Map.Entry<EvDevicePropPath,String> ve:state.propMap.entrySet())
							{
							Element eSetting=new Element("setting");
							eSetting.setAttribute("device", ve.getKey().getDeviceName());
							eSetting.setAttribute("property", ve.getKey().getProperty());
							eSetting.setAttribute("value", ve.getValue());
							eState.addContent(eSetting);
							}
						eGroup.addContent(eState);
						}
					eConfig.addContent(eGroup);
					}
				
				e.addContent(eConfig);
				}
			});
			
		}
	
	}
