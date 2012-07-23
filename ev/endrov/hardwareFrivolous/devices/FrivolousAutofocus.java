package endrov.hardwareFrivolous.devices;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDeviceObserver;
import endrov.recording.device.HWAutoFocus;



/**
 * Frivolous autofocus
 * 
 * @author Johan Henriksson, David Johansson, Arvid Johansson
 *
 */
class FrivolousAutofocus implements HWAutoFocus
	{

	private FrivolousStage stage;
	public EvDeviceObserver event=new EvDeviceObserver();
	public boolean contAutoFocus=false;
	public boolean contFocusLock=false;
	public double offset=0;

	
	public FrivolousAutofocus(FrivolousStage stage)
		{
		this.stage=stage;
		}
	
	public String getDescName()
		{
		return "Frivolous autofocus";
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

	public boolean hasConfigureDialog()
		{
		return false;
		}

	public void openConfigureDialog()
		{
		}

	
	
	public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.addWeakListener(listener);
		}
	public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.remove(listener);
		}

	
	public void enableContinuousFocus(boolean enable)
		{
		contAutoFocus=enable;
		}

	public void fullFocus() throws IOException
		{
		double[] pos=stage.getStagePos();
		pos[2]=offset;
		stage.setStagePos(pos);
		}

	public double getAutoFocusOffset()
		{
		return offset;
		}

	public double getCurrentFocusScore()
		{
		return 0;
		}

	public double getLastFocusScore()
		{
		return 0;
		}

	public void incrementalFocus() throws IOException
		{
		double[] pos=stage.getStagePos();
		pos[2]=0;
		stage.setStagePos(pos);
		}

	public boolean isContinuousFocusEnabled()
		{
		return contAutoFocus;
		}

	public boolean isContinuousFocusLocked()
		{
		return contFocusLock;
		}

	public void setAutoFocusOffset(double offset)
		{
		this.offset=offset;
		}

	}
