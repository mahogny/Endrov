package endrov.recording;

import java.util.HashMap;
import java.util.Map;

import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardwareConfigGroup;

/**
 * Resolution handling
 * 
 * @author Johan Henriksson
 *
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
		
		/** [um/px] */
		public double x;
		/** [um/px] */
		public double y;
		}

	
	public static Map<EvDevicePath,Map<String,ResolutionState>> resolutions=new HashMap<EvDevicePath, Map<String,ResolutionState>>();
	
	
	
	public static Map<String,ResolutionState> getCreateResolutionStatesMap(EvDevicePath camera)
		{
		Map<String,ResolutionState> map=resolutions.get(camera);
		if(map==null)
			resolutions.put(camera, map=new HashMap<String, ResolutionState>());
		return map;
		}
	
	
	/**
	 * Get current resolution
	 */
	public static ResolutionState getCurrentResolutionState(EvDevicePath camera)
		{
		Map<String,ResolutionState> rmap=resolutions.get(camera);
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
			return state.cameraRes;//.get(camera);
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
	
	}
