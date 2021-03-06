/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareNative;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.jdom.Element;

import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDeviceObserver;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvDeviceProvider;
import endrov.hardware.EvHardware;
import endrov.hardware.HWSerial;
import endrov.recording.device.HWStage;

/**
 * ITK Corvus stage drive + joystick.
 * The language is Venus-1 which is supposed to work with many other ITK devices.
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class ITKCorvus extends EvDeviceProvider implements EvDevice
	{
	private final static String sendNewLine=" "; //\r for terminal mode
	private final static String recvNewLine="\r\n";  
	//TODO
	
//	public HWSerial serial=null;
	
	/**
	 * I/O is blocking. hence the GUI dies upon init (most likely)
	 */
	
	public HWSerial serial=(HWSerial)EvHardware.getDevice(new EvDevicePath("mm.com2"));
	//new VirtualSerialBasic();
	
	
	private int numAxis=3;

	public ITKCorvus()
		{
		hw.put("stage", new CorvusStage());
		}
	
	public synchronized void sendCommand(String cmd)
		{
		if(serial!=null)
			serial.writePort(cmd+sendNewLine);
		}
	
	public synchronized String queryCommand(String cmd)
		{
		if(serial!=null)
			{
			serial.writePort(cmd+sendNewLine);
			String s=serial.readUntilTerminal(recvNewLine);
			s=s.substring(cmd.length());
			s=s.substring(0,s.length()-recvNewLine.length());
			System.out.println("#"+s+"#");
			return s; //which?
			}
		else
			{
			return "";
			}
		//return "123";
//		return s;
		}
	
	
	public void initController()
		{
		//Set device to host mode
		sendCommand("0 mode");
		
		//Use millimeter on all axis
		for(int i=0;i<3;i++)
			sendCommand("1 "+i+" setunit");
		
		
		}
	
	private int getNumAxis()
		{
		return numAxis;
		}
	
	
	/**
	 * Corvus special command: The number of axis can be set. 
	 */
	public void setNumAxis(int num)
		{
		numAxis=num;
		sendCommand(numAxis+" setdim");
		}

	
	/**
	 * Axis 
	 */
	private class CorvusStage implements HWStage
		{
	

		public CorvusStage()
			{
			
			}
		
		public int getNumAxis()
			{
			return ITKCorvus.this.getNumAxis();
			}
		

		public String[] getAxisName()
			{
			/*
			if(getNumAxis()==0)
				return new String[]{};
			else if(getNumAxis()==1)
				return new String[]{"x"};
			else if(getNumAxis()==2)
				return new String[]{"x","y"};
			else //if(getNumAxis()==3)*/
				return new String[]{"x","y","z"};
			}

		/*
		public int getNumAxis()
			{
			return numAxis;
			}*/

		public double[] getStagePos()
			{
			String ret=queryCommand("p");
			double[] dret=new double[3];
			StringTokenizer st=new StringTokenizer(ret);
			for(int i=0;i<getNumAxis();i++)
				dret[i]=Double.parseDouble(st.nextToken());
			return dret;
			}

		public void setRelStagePos(double[] axis)
			{
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<getNumAxis();i++)
				sb.append(axis[i]+" ");
			sb.append("r");
			sendCommand(sb.toString());
			}

		public void setStagePos(double[] axis)
			{
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<getNumAxis();i++)
				sb.append(axis[i]+" ");
			sb.append("m");
			sendCommand(sb.toString());
			}

		public void goHome()
			{
			sendCommand("cal");
			}
		
		
		public String getDescName()
			{
			return "ITK Corvus stage";
			}
	
		public SortedMap<String, String> getPropertyMap(){return new TreeMap<String, String>();}
		public SortedMap<String, DevicePropertyType> getPropertyTypes(){return new TreeMap<String, DevicePropertyType>();}
		public String getPropertyValue(String prop){return null;}
		public Boolean getPropertyValueBoolean(String prop){return null;}
		public void setPropertyValue(String prop, boolean value){}
		public void setPropertyValue(String prop, String value){}
		
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
		
		public boolean hasSampleLoadPosition(){return false;}
		public void setSampleLoadPosition(boolean b){}
		public boolean getSampleLoadPosition(){return false;}

		public void stop()
			{
			}

		}
	
	
	
	

	public Set<EvDevice> autodetect()
		{
		/**
		 * The command "identify" will return something like "Corvus ...".
		 * How does this work together with host mode? 
		 * 
		 * 
		 */
		
		
		return null;
		}

	public void getConfig(Element root)
		{
		}

	public List<String> provides()
		{
		return null;
		}
	public EvDevice newProvided(String s)
		{
		return null; //TODO
		}

	public void setConfig(Element root)
		{
		}

	public String getDescName()
		{
		return "ITK Corvus";
		}

	public SortedMap<String, String> getPropertyMap()
		{
		return new TreeMap<String, String>();
		}

	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		return new TreeMap<String, DevicePropertyType>();
		}

	public String getPropertyValue(String prop)
		{
		return null;
		}

	public Boolean getPropertyValueBoolean(String prop)
		{
		return null;
		}

	public void setPropertyValue(String prop, boolean value)
		{
		}

	public void setPropertyValue(String prop, String value)
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


	public boolean hasSampleLoadPosition(){return false;}
	public void setSampleLoadPosition(boolean b){}
	public boolean gsetSampleLoadPosition(){return false;}
	
	public void stop()
		{
		}

	}
