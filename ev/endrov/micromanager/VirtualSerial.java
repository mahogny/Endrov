package endrov.micromanager;

import java.util.SortedMap;
import java.util.TreeMap;

import endrov.hardware.PropertyType;
import endrov.recording.HWSerial;

public class VirtualSerial implements HWSerial
	{
	private String fifoIn="";
	private Object lock=new Object();
	
	

	public String nonblockingRead()
		{
		synchronized (lock)
			{
			String s=fifoIn;
			fifoIn="";
			return s;
			}
		}
	public String readUntilTerminal(String term)
		{
		return "";//TODO
		}
	public void writePort(String s)
		{
		
		}
	
	
	public String getDescName()
		{
		return "Virtual serial port";
		}
	public SortedMap<String, String> getPropertyMap()
		{
		//what about speed settings etc?
		TreeMap<String, String> m=new TreeMap<String, String>();
		return m;
		}
	public SortedMap<String, PropertyType> getPropertyTypes()
		{
		TreeMap<String, PropertyType> m=new TreeMap<String, PropertyType>();
		return m;
		}
	public String getPropertyValue(String prop)
		{
		return null;
		}
	public boolean getPropertyValueBoolean(String prop)
		{
		return false;
		}
	public void setPropertyValue(String prop, boolean value)
		{
		}
	public void setPropertyValue(String prop, String value)
		{
		}
	
	
	}
