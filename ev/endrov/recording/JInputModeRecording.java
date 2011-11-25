/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import java.util.HashMap;
import java.util.Map;

import endrov.basicWindow.BasicWindow;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.keyBinding.JInputManager;
import endrov.keyBinding.JInputMode;
import endrov.recording.device.HWStage;
import endrov.util.Tuple;


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
		gpMap.put("x", Tuple.make(new EvDevicePath("ev/demo/stage"),"x"));
		gpMap.put("y", Tuple.make(new EvDevicePath("ev/demo/stage"),"y"));
		gpMap.put("rz", Tuple.make(new EvDevicePath("ev/demo/stage"),"z"));
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
					BasicWindow.updateWindows();
					
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
