package endrov.recording;

import java.util.*;

import endrov.hardware.Hardware;

public interface HWState extends Hardware
	{

	public List<String> getStateNames();

	public int getCurrentState();
	public String getCurrentStateLabel();
	
	public void setCurrentState(int state);
	public void setCurrentStateLabel(String label);

	}
