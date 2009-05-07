package endrov.recording;

import java.util.*;

import endrov.hardware.Device;

public interface HWState extends Device, HWMagnifier
	{

	public List<String> getStateNames();

	public int getCurrentState();
	public String getCurrentStateLabel();
	
	public void setCurrentState(int state);
	public void setCurrentStateLabel(String label);

	}
