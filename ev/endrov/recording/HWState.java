package endrov.recording;

import java.util.List;

import endrov.hardware.Hardware;

public interface HWState extends Hardware
	{

	public List<String> getStateNames();

	public int getCurrentState();
	
	public void setCurrentState(int state);

	}
