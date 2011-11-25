/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareNative;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDeviceObserver;
import endrov.hardware.HWSerial;

/**
 * Serial communication over TCP/IP
 * TODO this code has not been tested
 * @author Johan Henriksson
 *
 */
public class EvSerialTCP implements HWSerial
	{
	Socket s;
	InputStream is;
	OutputStream os;
	
	private String leftOver="";
	
	public EvSerialTCP(Socket s) throws IOException
		{
		this.s=s;
		is=s.getInputStream();
		os=s.getOutputStream();
		}
	
	public String nonblockingRead()
		{
		try
			{
			StringBuffer sb=new StringBuffer();
			int num=is.available();
			for(int i=0;i<num;i++)
				sb.append((char)is.read());
			return sb.toString();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			return "";
			}
		}

	public String readUntilTerminal(String term)
		{
		String s=leftOver;
		int i;
		while((i=s.indexOf(term))==-1)
			s=s+nonblockingRead();
		int len=i+term.length();
		leftOver=s.substring(len);
		return s.substring(0,len);
		}

	public void writePort(String s)
		{
		try
			{
			for(byte c:s.getBytes())
				os.write(c);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}

	public String getDescName()
		{
		return "Socket "+s;
		}

	public SortedMap<String, String> getPropertyMap()
		{
		TreeMap<String, String> map=new TreeMap<String, String>();
		return map;
		}

	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		TreeMap<String, DevicePropertyType> map=new TreeMap<String, DevicePropertyType>();
		return map;
		}

	public String getPropertyValue(String prop)
		{
		return null;
		}

	public Boolean getPropertyValueBoolean(String prop)
		{
		return null;
		}

	public void setPropertyValue(String prop, String value)
		{
		}

	public void setPropertyValue(String prop, boolean value)
		{
		}

	
	public boolean hasConfigureDialog(){return false;}
	public void openConfigureDialog(){}

	
	public EvDeviceObserver event=new EvDeviceObserver();
	public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.addWeakListener(listener);
		}
	public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.remove(listener);
		}

	}
