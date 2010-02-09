/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverMicromanager;

import java.util.Map;

import mmcorej.*;

public class TestUMorig
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
			core.defineConfig("Channel", "FITC", "Emission", "State", "2");
			core.defineConfig("Channel", "FITC", "Excitation", "State", "3");
			core.defineConfig("Channel", "FITC", "Dichroic", "State", "1");
			core.defineConfig("Channel", "DAPI", "Emission", "State", "1");
			core.defineConfig("Channel", "DAPI", "Excitation", "State", "2");
			core.defineConfig("Channel", "DAPI", "Dichroic", "State", "0");
			core.defineConfig("Channel", "Rhodamine", "Emission", "State", "3");
			core.defineConfig("Channel", "Rhodamine", "Excitation", "State", "4");
			core.defineConfig("Channel", "Rhodamine", "Dichroic", "State", "2");
	
			// set initial imaging mode
			core.setProperty("Camera", "Exposure", "55");
			core.setProperty("Objective", "Label", "Nikon 10X S Fluor");
			core.setConfig("Channel", "DAPI");
	
			// list devices
			System.out.println("Device status:");
			for (String device:MMutil.getLoadedDevices(core))
				{
				System.out.println("device: "+device);
//				System.out.println("  " + MMutil.getPropMap(core, device));
				for(Map.Entry<String, String> prop:MMutil.getPropMap(core,device).entrySet())
					{
					System.out.print(" " + prop.getKey() + " = " + prop.getValue());
					System.out.println("  "+MMutil.convVector(core.getAllowedPropertyValues(device, prop.getKey())));
					}
				}
			
			// list configurations
			for (String group:MMutil.convVector(core.getAvailableConfigGroups()))
				{
				StrVector configs = core.getAvailableConfigs(group);
				System.out.println("Group " + group);
				for (int j=0; j<configs.size(); j++)
					{
					Configuration cdata = core.getConfigData(group, configs.get(j));
					System.out.println("   Configuration " + configs.get(j));
					for (int k=0; k<cdata.size(); k++) 
						{
						PropertySetting s = cdata.getSetting(k);
						System.out.println("      " + s.getDeviceLabel() + ", " +	s.getPropertyName() + ", " + s.getPropertyValue());
						}
					}
				}
	
			
		
		   // set some properties
//      core.setProperty("Camera", "Binning", "2");
  //    core.setProperty("Camera", "PixelType", "8bit");

			
       core.setExposure(50);
       core.snapImage();

       if (core.getBytesPerPixel() == 1) 
      	 {
      	 // 8-bit grayscale pixels
      	 byte[] img = (byte[])core.getImage();
      	 System.out.println("Image snapped, " + img.length + " pixels total, 8 bits each.");
      	 System.out.println("Pixel [0,0] value = " + img[0]);
      	 } 
       else if (core.getBytesPerPixel() == 2)
      	 {
      	 // 16-bit grayscale pixels
      	 short[] img = (short[])core.getImage();
      	 System.out.println("Image snapped, " + img.length + " pixels total, 16 bits each.");
      	 System.out.println("Pixel [0,0] value = " + img[0]);
      	 } 
       else
      	 {
      	 System.out.println("Dont' know how to handle images with " +
      			 core.getBytesPerPixel() + " byte pixels.");
      	 }

			
			
			}
		catch (Exception e) 
			{
			System.out.println("err:"+e.getMessage());
			System.exit(1);
			}
		}
	
	
	}
