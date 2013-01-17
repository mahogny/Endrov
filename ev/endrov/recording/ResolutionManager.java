package endrov.recording;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardwareConfigGroup;

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

	
	
	
	
	
	/**
	 * Map of all resolutions. CameraPath -> NameOfState -> State
	 * 
	 * Here the camera is the index, which makes sense from an endrov point of view. micromanager would rather have a state for current camera
	 * but this would reduce the multiplexing capability 
	 */
	public static Map<EvDevicePath,Map<String,ResolutionState>> resolutions=new HashMap<EvDevicePath, Map<String,ResolutionState>>();
	//public static Map<HWCamera,Map<String,ResolutionState>> resolutions=new HashMap<HWCamera, Map<String,ResolutionState>>();
	
	
	public static Map<String,ResolutionState> getCreateResolutionStatesMap(EvDevicePath camera)
		{
		Map<String,ResolutionState> map=resolutions.get(camera.getDevice());
		if(map==null)
			resolutions.put(camera, map=new HashMap<String, ResolutionState>());
		return map;
		}
	
	
	/**
	 * Get current resolution
	 */
	public static ResolutionState getCurrentResolutionState(EvDevicePath camera)
		{
		if(camera==null)
			return null;
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
		if(camera==null)
			return null;
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
	
	
	
	/**
	 * Find an unused name of a resolution
	 */
	public static String getUnusedResName(EvDevicePath campath)
		{
		// Find all names in use
		Set<String> usedNames = new HashSet<String>();
		for (EvDevicePath p : ResolutionManager.resolutions.keySet())
			usedNames.addAll(ResolutionManager.resolutions.get(p).keySet());

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

