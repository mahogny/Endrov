package endrov.driverMicromanager;

import java.io.File;
import java.util.*;

import mmcorej.CMMCore;
import mmcorej.DeviceType;

import org.jdom.Element;

import endrov.ev.EV;
import endrov.hardware.*;

/**
 * Micromanager hardware interface
 * @author Johan Henriksson
 *
 */
public class MicroManager extends HardwareProvider implements Hardware
	{
	public static void initPlugin() {}
	static
		{
		HardwareManager.root.hw.put("um", new MicroManager());
//		HardwareManager.registerHardwareProvider(new MicroManager());
		}
	
	
	CMMCore core;

	public MicroManager()
		{
		try
			{
			core=new CMMCore();
			
			File fMMconfig1=new File(EV.getGlobalConfigEndrovDir(),"MMConfig.cfg");
			File fMMconfig=fMMconfig1;
			fMMconfig.getParentFile().mkdirs();
			if(!fMMconfig.exists())
				fMMconfig=new File("MMConfig.cfg");
			if(!fMMconfig.exists())
				{
				System.out.println("No config file found ("+fMMconfig1+" nor "+fMMconfig+")");
				return;
				}
			System.out.println("Loading "+fMMconfig.getAbsolutePath());
			
			core.loadSystemConfiguration(fMMconfig.getPath());
			
	/*
			// add devices
			core.loadDevice("Camera", "DemoCamera", "DCam");
			core.loadDevice("Emission", "DemoCamera", "DWheel");
			core.loadDevice("Excitation", "DemoCamera", "DWheel");
			core.loadDevice("Dichroic", "DemoCamera", "DWheel");
			core.loadDevice("Objective", "DemoCamera", "DObjective");
			core.loadDevice("X", "DemoCamera", "DStage");
			core.loadDevice("Y", "DemoCamera", "DStage");
			core.loadDevice("Z", "DemoCamera", "DStage");
	
			core.initializeAllDevices();
	
			// Set labels for state devices
			//
			// emission filter
			core.defineStateLabel("Emission", 0, "Chroma-D460");
			core.defineStateLabel("Emission", 1, "Chroma-HQ620");
			core.defineStateLabel("Emission", 2, "Chroma-HQ535");
			core.defineStateLabel("Emission", 3, "Chroma-HQ700");
	
			// excitation filter
			core.defineStateLabel("Excitation", 2, "Chroma-D360");
			core.defineStateLabel("Excitation", 3, "Chroma-HQ480");
			core.defineStateLabel("Excitation", 4, "Chroma-HQ570");
			core.defineStateLabel("Excitation", 5, "Chroma-HQ620");
	
			// excitation dichroic
			core.defineStateLabel("Dichroic", 0, "400DCLP");
			core.defineStateLabel("Dichroic", 1, "Q505LP");
			core.defineStateLabel("Dichroic", 2, "Q585LP");
	
			// objective
			core.defineStateLabel("Objective", 1, "Objective-1");
			core.defineStateLabel("Objective", 3, "Nikon 20X Plan Fluor ELWD");
			core.defineStateLabel("Objective", 5, "Zeiss 4X Plan Apo");
	
			// set initial imaging mode
			core.setProperty("Camera", "Exposure", "55");
//			core.setProperty("Objective", "Label", "Nikon 10X S Fluor"); //Need to use state otherwise!
			core.setProperty("Objective", "Label", "Objective-1"); //overwritten by defineStateLabel
			//statelabel extends list!
			
	*/
			// list devices
			
			System.out.println("Device status:");
			for (String device:MMutil.getLoadedDevices(core))
				{
				System.out.println("device: "+device);
				for(Map.Entry<String, String> prop:MMutil.getPropMap(core,device).entrySet())
					{
					System.out.print(" " + prop.getKey() + " = " + prop.getValue());
					System.out.println("  "+MMutil.convVector(core.getAllowedPropertyValues(device, prop.getKey())));
					}
				}

			//Micro-manager has a defunct getDeviceType(), this is a work-around
			//or is it? I think they have a different notion of filter
			Collection<String> isMagnifier=MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.MagnifierDevice));
			Collection<String> isXY=MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.XYStageDevice));
			Collection<String> isStage=MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.StageDevice));
			Collection<String> isShutter=MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.ShutterDevice));
			Collection<String> isSerial=MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.SerialDevice));
			Collection<String> isAutoFocus=MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.AutoFocusDevice));
			Collection<String> isCamera=MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.CameraDevice));
			Collection<String> isState=MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.StateDevice));
			
			//Register all devices
			for(String devName:MMutil.convVector(core.getLoadedDevices()))
				{
				//Device fundamentals
				MMDeviceAdapter adp;
				if(isCamera.contains(devName))
					adp=new MMCamera(this,devName);
				else if(isMagnifier.contains(devName))
					adp=new MMMagnifier(this,devName);
				else if(isXY.contains(devName))
					adp=new MMStage(this,devName,true);
				else if(isStage.contains(devName))
					adp=new MMStage(this,devName,false);
				else if(isShutter.contains(devName))
					adp=new MMShutter(this,devName);
				else if(isAutoFocus.contains(devName))
					adp=new MMAutoFocus(this,devName);
				else if(isState.contains(devName))
					adp=new MMState(this,devName);
				else if(isSerial.contains(devName))
					adp=new MMSerial(this,devName);
				else
					adp=new MMDeviceAdapter(this,devName);
				System.out.println(devName+"---"+adp+" "+adp.getDescName()+" ???? "+core.getDeviceType(devName));
				
				hw.put(devName,adp);
				}
			}
		catch (Exception e) 
			{
			e.printStackTrace();
			System.out.println("err:"+e.getMessage());
			}
		
		
		
		
		
		}
	
	

	
	
	public Set<Hardware> autodetect()
		{
		return null;
		}

	public void getConfig(Element root)
		{
		}

	public void setConfig(Element root)
		{
		}
	
	public List<String> provides()
		{
		List<String> list=new LinkedList<String>();

		
		
		return list;
		}
	public Hardware newProvided(String s)
		{
		return null;
		}


	public String getDescName()
		{
		return "Micro-manager";
		}


	public SortedMap<String, String> getPropertyMap()
		{
		return null;
		}


	public SortedMap<String, PropertyType> getPropertyTypes()
		{
		return null;
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
