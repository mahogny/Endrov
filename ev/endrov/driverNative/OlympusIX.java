/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverNative;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Element;

import endrov.hardware.Device;
import endrov.hardware.DeviceProvider;
import endrov.hardware.PropertyType;
import endrov.recording.HWSerial;
import endrov.recording.HWShutter;
import endrov.recording.RecordingResource;
import endrov.recording.VirtualSerial;


/**
 * I/O multiplexer for Olympus IX
 * @author Johan Henriksson
 *
 */
public class OlympusIX extends DeviceProvider implements Device
	{
	public final static String newLine="\r\n";

	public static class VirtualSerialIX extends VirtualSerial
		{
		public VirtualSerialIX()
			{
			super("IX");
			autoresponse.put("1SHUT0?"+newLine, "1SHUT0 IN"+newLine);
			autoresponse.put("1SHUT1?"+newLine, "1SHUT1 IN"+newLine);
			autoresponse.put("1SHUT2?"+newLine, "1SHUT2 IN"+newLine);
			autoresponse.put("1MU?"+newLine, "1MU 3"+newLine);
			autoresponse.put("1OB?"+newLine, "1OB 2"+newLine);
			autoresponse.put("1CD?"+newLine, "1CD 5"+newLine);
			autoresponse.put("1LMPSEL?"+newLine, "1LMPSEL EPI"+newLine);
			autoresponse.put("1LMP?"+newLine, "1LMP 100"+newLine);
			autoresponse.put("1VER?"+newLine, "test??"+newLine);
			}
		public String response(String s)
			{
			return null;
			}
		}
	
	public HWSerial serial=null;
	
	
	
	
	public OlympusIX()
		{
		hw.put("shutter1", new DevShutter(1));
		hw.put("shutter2", new DevShutter(2));
		hw.put("MU", new DevMirrorUnit());
		hw.put("Objective", new DevObjective());
		hw.put("Condenser", new DevCondenser());
		hw.put("LampSource", new DevLampSource());
		hw.put("LampIntensity", new DevLampIntensity());
		}
	
	public synchronized void sendCommand(String cmd)
		{
		if(serial!=null)
			serial.writePort(cmd+newLine);
		}
	
	public synchronized String queryCommand(String cmd)
		{
		serial.writePort(cmd+newLine);
		String s=serial==null ? "\r\n" : serial.readUntilTerminal("\r\n");
		s=s.substring(cmd.length());
		s=s.substring(0,s.length()-2);
		System.out.println("#"+s+"#");
		return s; //which?
		//return "123";
//		return s;
		}
	
	/** Shutter */
	public class DevShutter extends BasicNativeCachingStateDevice implements HWShutter
		{
		final int shutterNum;
		public DevShutter(int shutterNum)
			{
			this.shutterNum=shutterNum;
			System.out.println("---------------create shutter "+this.shutterNum);
			}
		public DevShutter()
			{
			this(1);
			}
		public String getDescName(){return "IX shutter";}
		public int getCurrentStateHW()
			{
			System.out.println("shutternum "+shutterNum);
			return queryCommand("1SHUT"+shutterNum+"?").equals("IN") ? 1 : 0;
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1SHUT"+shutterNum+" "+(state!=0?"IN":"OUT"));
			//
			}
		public double getResMagX(){return 1;}
		public double getResMagY(){return 1;}
		public boolean hasConfigureDialog(){return false;}
		public void openConfigureDialog(){}
		}

	/** Prism */
	public class DevPrism extends BasicNativeCachingStateDevice 
		{
		public DevPrism(){super(1,2);}
		public String getDescName(){return "IX prism";}
		public int getCurrentStateHW()
			{
			return Integer.parseInt(queryCommand("1PRISM?"));
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1PRISM "+state);
			//
			}
		public double getResMagX(){return 1;}
		public double getResMagY(){return 1;}
		public boolean hasConfigureDialog(){return false;}
		public void openConfigureDialog(){}
		}
	
	
	/** Mirror unit */
	public class DevMirrorUnit extends BasicNativeCachingStateDevice 
		{
		public DevMirrorUnit(){super(1,5);}
		public String getDescName(){return "IX mirror unit";}
		public int getCurrentStateHW()
			{
			return Integer.parseInt(queryCommand("1MU?"));
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1MU "+state);
			//
			}
		public double getResMagX(){return 1;}
		public double getResMagY(){return 1;}
		public boolean hasConfigureDialog(){return false;}
		public void openConfigureDialog(){}
		}
	
	/** Objective */
	public class DevObjective extends BasicNativeCachingStateDevice 
		{
		public DevObjective(){super(1,5);}
		public String getDescName(){return "IX objective";}
		public int getCurrentStateHW()
			{
			return Integer.parseInt(queryCommand("1OB?"));
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1OB "+state);
			//
			}
		public double getResMagX(){return RecordingResource.magFromLabel(getCurrentStateLabel());}
		public double getResMagY(){return RecordingResource.magFromLabel(getCurrentStateLabel());}
		public boolean hasConfigureDialog(){return false;}
		public void openConfigureDialog(){}
		}
	
	/** Condenser */
	public class DevCondenser extends BasicNativeCachingStateDevice 
		{
		public DevCondenser(){super(1,5);}
		public String getDescName(){return "IX condenser";}
		public int getCurrentStateHW()
			{
			return Integer.parseInt(queryCommand("1CD?"));
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1CD "+state);
			//
			}
		public double getResMagX(){return 1;}
		public double getResMagY(){return 1;}
		public boolean hasConfigureDialog(){return false;}
		public void openConfigureDialog(){}
		}
	
	
	/** Lamp source */
	public class DevLampSource extends BasicNativeCachingStateDevice 
		{
		public DevLampSource(){super(new int[]{0,1},new String[]{"DIA","EPI"});}
		public String getDescName(){return "IX lamp source";}
		public int getCurrentStateHW()
			{
			return queryCommand("1LMPSEL?").equals("DIA")?0:1;
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1LMPSEL "+state);
			//
			}
		public double getResMagX(){return 1;}
		public double getResMagY(){return 1;}
		public boolean hasConfigureDialog(){return false;}
		public void openConfigureDialog(){}
		}
	
	/** Lamp intensity */
	public class DevLampIntensity extends BasicNativeCachingStateDevice 
		{
		public DevLampIntensity()
			{
			//TODO no idea about range
			super(0,10);
			}
		public String getDescName(){return "IX lamp source";}
		public int getCurrentStateHW()
			{
			return Integer.parseInt(queryCommand("1LMP?"));
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1LMP "+state);
			//
			}
		public double getResMagX(){return 1;}
		public double getResMagY(){return 1;}
		public boolean hasConfigureDialog(){return false;}
		public void openConfigureDialog(){}
		}
	
	
	

	public Set<Device> autodetect()
		{
		return null;
		}

	public void getConfig(Element root)
		{
		}

	public List<String> provides()
		{
		return null;
		}
	public Device newProvided(String s)
		{
		return null; //TODO
		}

	public void setConfig(Element root)
		{
		}

	public String getDescName()
		{
		return "Olympus IX";
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
	
	
	public boolean hasConfigureDialog(){return false;}
	public void openConfigureDialog(){}

	
	
	
	}
