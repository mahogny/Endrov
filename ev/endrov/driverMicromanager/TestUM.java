/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverMicromanager;

import java.util.Map;

import mmcorej.*;

public class TestUM
	{
	
	public static void main(String[] args)
		{
	
		// create core object
		CMMCore core = new CMMCore();
	
		try 
			{
	
			// load devices
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
			core.defineStateLabel("Objective", 1, "Nikon 10X S Fluor");
			core.defineStateLabel("Objective", 3, "Nikon 20X Plan Fluor ELWD");
			core.defineStateLabel("Objective", 5, "Zeiss 4X Plan Apo");
	
			// define configurations
/*			core.defineConfig("Channel", "FITC", "Emission", "State", "2");
			core.defineConfig("Channel", "FITC", "Excitation", "State", "3");
			core.defineConfig("Channel", "FITC", "Dichroic", "State", "1");
			core.defineConfig("Channel", "DAPI", "Emission", "State", "1");
			core.defineConfig("Channel", "DAPI", "Excitation", "State", "2");
			core.defineConfig("Channel", "DAPI", "Dichroic", "State", "0");
			core.defineConfig("Channel", "Rhodamine", "Emission", "State", "3");
			core.defineConfig("Channel", "Rhodamine", "Excitation", "State", "4");
			core.defineConfig("Channel", "Rhodamine", "Dichroic", "State", "2");*/
	
			// set initial imaging mode
			core.setProperty("Camera", "Exposure", "55");
			core.setProperty("Objective", "Label", "Nikon 10X S Fluor");
//			core.setConfig("Channel", "DAPI");
	
			// list devices
			System.out.println("Device status:");
			for (String device:MMutil.getLoadedDevices(core))
				{
				System.out.println("device: "+device);
//				System.out.println("  " + MMutil.getPropMap(core, device));
				for(Map.Entry<String, String> prop:MMutil.getPropMap(core,device).entrySet())
					{
					System.out.print(" " + prop.getKey() + " = " + prop.getValue());
					if(core.isPropertyReadOnly(device, prop.getKey()))
						System.out.print(" (ro) ");
					if(core.hasPropertyLimits(device, prop.getKey()))
						System.out.print(" <"+core.getPropertyLowerLimit(device, prop.getKey())+" -- "+core.getPropertyLowerLimit(device, prop.getKey())+"> ");
					System.out.println("  "+MMutil.convVector(core.getAllowedPropertyValues(device, prop.getKey())));
					}
				}
			
			
		
		   // set some properties
      core.setProperty("Camera", "Binning", "2");
      core.setProperty("Camera", "PixelType", "8bit");
			core.setProperty("Camera", "Exposure", "50");
			//CCDTemperature
//       core.setExposure(50);
			core.setProperty("Core", "AutoShutter", "1");
			
			
			
//       core.setAutoShutter(true);
//       core.setCameraDevice(arg0);
       
       core.snapImage();

//       CameraImage im=MMutil.snap(core);
  //     System.out.println(im);
      
			
       
       /*
       core.loadDevice("Port", "SerialManager", "COM1");
       core.setProperty("Port", "StopBits", "2");
       core.setProperty("Port", "Parity", "None");
       core.initializeDevice("Port");

       core.setSerialPortCommand("Port", "MOVE X=300", "\r");
       String answer = core.getSerialPortAnswer("Port", "\r");
       */
       
       /*
        * // The following devices must stop moving before the image is acquired
core.assignImageSynchro("X");
core.assignImageSynchro("Y");
core.assignImageSynchro("Z");
core.assignImageSynchro("Emission");
        */
       //alternate way
      // core.waitForDevice("Emission"); // until it stops moving
       
       //core.loadSystemConfiguration("MMConfig.cfg");

       
       /*
    // take image with manual shutter
       core.setAutoShutter(false); // disable auto shutter
       core.setProperty("Shutter", "State", "1"); // open
       core.waitForDevice("Shutter");
       core.snapImage();
       core.setProperty("Shutter", "State", "0"); // close
       */
       /*
       //check Z stage status
       boolean ZStageBusy = core.deviceBusy("Z");

       //check if any of the devices in the systema are busy
       boolean systemBusy = core.systemBusy();
       */
			}
		catch (Exception e) 
			{
			System.out.println("err:"+e.getMessage());
			System.exit(1);
			}
		}
	
	
	}
