/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import java.util.HashMap;
import java.util.Map;

import endrov.gui.keybinding.JInputManager;
import endrov.gui.keybinding.JInputMode;
import endrov.gui.window.EvBasicWindow;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.recording.device.HWStage;
import endrov.util.collection.Tuple;


/**
 * Use input to control hardware
 * 
 * @author Johan Henriksson
 *
 */
public class JInputModeRecording implements JInputMode
	{
	/**
	 * Axis name TO DevicePath+axis
	 */
	Map<String, Tuple<EvDevicePath,String>> gpMap=new HashMap<String, Tuple<EvDevicePath,String>>();
	
	public JInputModeRecording()
		{
		gpMap.put("x", Tuple.make(new EvDevicePath("ev/demo/stage"),"X"));
		gpMap.put("y", Tuple.make(new EvDevicePath("ev/demo/stage"),"Y"));
		gpMap.put("rz", Tuple.make(new EvDevicePath("ev/demo/stage"),"Z"));
		//rz
		}
	
	
	public void bindAxisPerformed(JInputManager.EvJinputStatus status)
		{
		//System.out.println(status);
		for(Map.Entry<String, Float> e:status.values.entrySet())
			{
			Tuple<EvDevicePath,String> t=gpMap.get(e.getKey());
			if(t!=null)
				{
				EvDevice dev=t.fst().getDevice();
				if(dev instanceof HWStage)
					{
					HWStage stage=(HWStage)dev;
					String axisName[]=stage.getAxisName();
					double axis[]=new double[stage.getNumAxis()];
					for(int i=0;i<axisName.length;i++)
						if(axisName[i].equals(t.snd()))
							axis[i]+=e.getValue();
					stage.setRelStagePos(axis);
					EvBasicWindow.updateWindows();
					
					}
				else
					System.out.println("Not stage");
				
				
				
				}
//			else
//				System.out.println("Device for axis found "+e.getKey());
			}
		
		
		}
	
	
	public void bindKeyPerformed(JInputManager.EvJinputButtonEvent e)
		{
		}

	
	
	}
