package endrov.recording;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardwareConfigGroup;
import endrov.recording.device.HWCamera;

/**
 * Resolution handling
 * 
 * @author Johan Henriksson
 * @author Kim Nordlöf, Erik Vernersson
 */
public class ResolutionManager
	{

	/**
	 * State condition for having a resolution on particular device
	 */
	public static class ResolutionState
		{
		public EvHardwareConfigGroup.State state;
		public Resolution cameraRes;
		}
	
	/**
	 * Resolution of capturing device
	 */
	public static class Resolution
		{
		public Resolution(double resX, double resY)
			{
			this.x = resX;
			this.y = resY;
			}
		
		// [um/px] 
		public double x,y;
		}

	
	public static Map<HWCamera,Map<String,ResolutionState>> resolutions=new HashMap<HWCamera, Map<String,ResolutionState>>();
	
	
	
	public static Map<String,ResolutionState> getCreateResolutionStatesMap(EvDevicePath camera)
		{
		Map<String,ResolutionState> map=resolutions.get(camera.getDevice());
		if(map==null)
			resolutions.put((HWCamera) camera.getDevice(), map=new HashMap<String, ResolutionState>());
		return map;
		}
	
	
	/**
	 * Get current resolution
	 */
	public static ResolutionState getCurrentResolutionState(EvDevicePath camera)
		{

		Map<String,ResolutionState> rmap=resolutions.get(camera.getDevice());	
		
		if(rmap==null)
			return null;

		//Check all resolutions for this camera
		for(ResolutionState r:rmap.values())
			if(r.state.isCurrent())
				return r;
		return null;
		}
	
	/**
	 * Get current resolution
	 */
	public static Resolution getCurrentResolution(EvDevicePath camera)
		{
		ResolutionState state=getCurrentResolutionState(camera);
		if(state!=null)
			return state.cameraRes;
		else
			return null;
		}
	
	/**
	 * Get current resolution, default to 1
	 */
	public static Resolution getCurrentResolutionNotNull(EvDevicePath camera)
		{
		Resolution res=getCurrentResolution(camera);
		if(res!=null)
			return res;
		else
			return new Resolution(1.0, 1.0);
		}
	
	
	

	public static String getUnusedResName(EvDevicePath campath)
		{
	// Find all names in use
		Set<String> usedNames = new HashSet<String>();
		for (HWCamera cam2 : ResolutionManager.resolutions.keySet())
			usedNames.addAll(ResolutionManager.resolutions.get(cam2).keySet());

		// Generate an unused name
		String name;
		int resi = 0;
		do
			{
			name = campath.getLeafName()+" "+resi;
			resi++;
			} while (usedNames.contains(name));
		return name;
		}
	
	}

