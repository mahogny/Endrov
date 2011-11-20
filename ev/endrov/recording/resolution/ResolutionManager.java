package endrov.recording.resolution;

import java.util.HashMap;
import java.util.Map;

import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.recording.HWCamera;

public class ResolutionManager
	{

	
	public static class Resolution
		{
		public Map<EvDevicePath, String> properties=new HashMap<EvDevicePath, String>();
		public Map<EvDevicePath, String> values=new HashMap<EvDevicePath, String>();
		public EvDevicePath camera;
		
		//TODO: which units does mm use?
		public double resX; 
		public double resY;
		}

	
	public Map<String,Resolution> resolutions=new HashMap<String,Resolution>();
	
	
	/**
	 * Get current resolution
	 */
	public Resolution getCurrentResolution(EvDevicePath camera)
		{
		//Check all resolutions for this camera
		nextRes: for(Resolution r:resolutions.values())
			if(r.camera.equals(camera))
				{
				//Check if all property values are correct
				for(Map.Entry<EvDevicePath, String> e:r.properties.entrySet())
					{
					EvDevice device=e.getKey().getDevice();
					String curPropVal=device.getPropertyValue(e.getValue());
					if(!curPropVal.equals(r.values.get(e.getKey())))
						continue nextRes;
					}
				return r;
				}
		return null;
		}
	
	
	/**
	 * Get the total magnification for the entire light path, coming into a given camera
	 * [um/px]
	 */
	public static double getCurrentTotalMagnification(HWCamera cam)
		{
		return 1; //TODO
		}

	



	}
