package endrov.driverNative;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.jdom.Element;

import endrov.hardware.Hardware;
import endrov.hardware.HardwareProvider;
import endrov.hardware.PropertyType;
import endrov.recording.HWSerial;
import endrov.recording.HWStage;
import endrov.recording.VirtualSerialBasic;

/**
 * ITK Corvus stage drive + joystick.
 * The language is Venus-1 which is supposed to work with many other ITK devices.
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class ITKCorvus extends HardwareProvider implements Hardware
	{
	private final static String sendNewLine="\r"; 
	private final static String recvNewLine="\r\n";  
	//TODO
	
//	public HWSerial serial=null;
	
	public HWSerial serial=new VirtualSerialBasic();
	
	
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
		serial.writePort(cmd+sendNewLine);
		String s=serial==null ? "\r\n" : serial.readUntilTerminal("\r\n");
		s=s.substring(cmd.length());
		s=s.substring(0,s.length()-2);
		System.out.println("#"+s+"#");
		return s; //which?
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
		
		public int getCurrentStateHW()
			{
			return 0;
			}

		public void setCurrentStateHW(int state)
			{
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

		public String getDescName()
			{
			return "ITK Corvus stage";
			}
	
		public SortedMap<String, String> getPropertyMap(){return new TreeMap<String, String>();}
		public SortedMap<String, PropertyType> getPropertyTypes(){return new TreeMap<String, PropertyType>();}
		public String getPropertyValue(String prop){return null;}
		public Boolean getPropertyValueBoolean(String prop){return null;}
		public void setPropertyValue(String prop, boolean value){}
		public void setPropertyValue(String prop, String value){}
	}
	
	
	
	

	public Set<Hardware> autodetect()
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
	public Hardware newProvided(String s)
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

	public SortedMap<String, PropertyType> getPropertyTypes()
		{
		return new TreeMap<String, PropertyType>();
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
	
	}
