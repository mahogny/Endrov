/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverMicromanager;

import java.io.File;
import java.util.*;

import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.PropertyBlock;
import mmcorej.PropertyPair;

import org.jdom.Element;

import endrov.driverMicromanager.conf.ConfiguratorDlg;
import endrov.ev.EV;
import endrov.hardware.*;

/**
 * Micromanager hardware interface
 * @author Johan Henriksson
 *
 */
public class MicroManager extends EvDeviceProvider implements EvDevice
	{
	
	
	CMMCore core;

	
	File configFile;
	
	public MicroManager()
		{
		try
			{
			core=new CMMCore();
			
			//core.enableStderrLog(true);
			core.enableDebugLog(false);
			
			File fMMconfig1=new File(EV.getGlobalConfigEndrovDir(),"MMConfig.cfg");
			File fMMconfig=fMMconfig1;
			fMMconfig.getParentFile().mkdirs();
			if(!fMMconfig.exists())
				fMMconfig=new File("MMConfig.cfg");
			if(!fMMconfig.exists())
				{
				System.out.println("No config file found ("+fMMconfig1+" nor "+fMMconfig+")");
				configFile=new File(EV.getGlobalConfigEndrovDir(),"MMConfig.cfg");
				configFile.createNewFile();
				return;
				}
			System.out.println("Micro-manager version "+core.getAPIVersionInfo()+" loading config "+fMMconfig.getAbsolutePath());
			
			configFile=fMMconfig;
			core.loadSystemConfiguration(fMMconfig.getPath());

			
			populateFromCore();
			

			}
		catch (Exception e) 
			{
			e.printStackTrace();
			System.out.println("err:"+e.getMessage());
			}
		
		
		
		
		}
	
	
	private void populateFromCore()
		{
		// list devices
		
		try
			{
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
			//Collection<String> isMagnifier=MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.MagnifierDevice));
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
				//else if(isMagnifier.contains(devName))
					//adp=new MMMagnifier(this,devName);
				else if(isXY.contains(devName))
					adp=new MMStage(this,devName,true);
				else if(isStage.contains(devName))
					adp=new MMStage(this,devName,false);
				else if(isShutter.contains(devName))
					adp=new MMShutter(this,devName);
				else if(isAutoFocus.contains(devName))
					adp=new MMAutoFocus(this,devName);
				else if(isSerial.contains(devName))
					adp=new MMSerial(this,devName);
				else if(isState.contains(devName))
					adp=new MMState(this,devName);
				else
					adp=new MMDeviceAdapter(this,devName);
				//System.out.println(devName+"---"+adp+" "+adp.getDescName()+" ???? "+core.getDeviceType(devName));
				
				hw.put(devName,adp);
				}
			
			
			
			/**
			 * Read property blocks
			 */
			for(String blockName:MMutil.convVector(core.getAvailablePropertyBlocks()))
				{
				PropertyBlock b=core.getPropertyBlockData(blockName);
				HashMap<String, String> prop=new HashMap<String, String>(); 
				try
					{
					for(int i=0;i<b.size();i++)
						{
						PropertyPair pair=b.getPair(i);
						prop.put(pair.getPropertyName(), pair.getPropertyValue());
						}
					}
				catch (Exception e)
					{
					e.printStackTrace();
					System.out.println("This should never happen");
					}

				if(hw.containsKey(blockName))
					{
					/**
					 * Associate with device, somehow
					 */
					
					}
				
				}
			}
		catch (Exception e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
	
	
	public Set<EvDevice> autodetect()
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
	public EvDevice newProvided(String s)
		{
		return null;
		}


	public String getDescName()
		{
		return "Micro-manager";
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
	
	
	public boolean hasConfigureDialog(){return true;}
	public void openConfigureDialog()
		{
		ConfiguratorDlg dlg=new ConfiguratorDlg(core,configFile.getAbsolutePath());
		dlg.setVisible(true);
		populateFromCore();
		}

	
	
	public EvDeviceObserver event=new EvDeviceObserver();
	
	public void addListener(EvDeviceObserver.Listener listener)
		{
		event.addWeakListener(listener);
		}
	
	
	public void removeListener(EvDeviceObserver.Listener listener)
		{
		event.remove(listener);
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvHardware.root.hw.put("um", new MicroManager());
//		HardwareManager.registerHardwareProvider(new MicroManager());
		}

	}
