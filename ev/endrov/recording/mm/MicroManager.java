package endrov.recording.mm;

import java.util.*;

import mmcorej.CMMCore;
import mmcorej.DeviceType;

import org.jdom.Element;

import endrov.hardware.*;

/**
 * Micromanager hardware interface
 * @author Johan Henriksson
 *
 */
public class MicroManager extends HardwareProvider
	{
	public static void initPlugin() {}
	static
		{
		HardwareManager.registerHardwareProvider(new MicroManager());
		}
	
	
	CMMCore core=new CMMCore();

	public MicroManager()
		{
		try
			{
			core.loadSystemConfiguration("MMConfig.cfg");
			
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
			/*
			System.out.println("Device status:");
			for (String device:MMutil.getLoadedDevices(core))
				{
				System.out.println("device: "+device);
				for(Map.Entry<String, String> prop:MMutil.getPropMap(core,device).entrySet())
					{
					System.out.print(" " + prop.getKey() + " = " + prop.getValue());
					System.out.println("  "+MMutil.convVector(core.getAllowedPropertyValues(device, prop.getKey())));
					}
				}*/
		
			//Register all devices
			for(String devName:MMutil.convVector(core.getLoadedDevices()))
				{
				//Device fundamentals
				DeviceType type=core.getDeviceType(devName);
				MMDeviceAdapter adp;
				if(type==DeviceType.CameraDevice)
					adp=new MMCamera(this,devName);
				else if((type.swigValue() & DeviceType.MagnifierDevice.swigValue())!=0)
					adp=new MMMagnifier(this,devName);
				else if((type.swigValue() & DeviceType.XYStageDevice.swigValue())!=0)
					adp=new MMStage(this,devName);
				else if((type.swigValue() & DeviceType.StageDevice.swigValue())!=0)
					adp=new MMStage(this,devName);
				else if((type.swigValue() & DeviceType.ShutterDevice.swigValue())!=0)
					adp=new MMShutter(this,devName);
				else
					adp=new MMDeviceAdapter(this,devName);
				System.out.println(devName+"---"+type+"---------------"+type.swigValue());
				
				hw.put(devName,adp);
				}
			}
		catch (Exception e) 
			{
			e.printStackTrace();
			System.out.println("err:"+e.getMessage());
			}
		
		
		
		
		
		}
	
	
	public String getName()
		{
		return "umanager";
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

	}
