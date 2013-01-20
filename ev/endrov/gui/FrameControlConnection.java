package endrov.gui;

import endrov.util.math.EvDecimal;

public interface FrameControlConnection
	{
	public EvDecimal firstFrame();
	public EvDecimal lastFrame();
	
	public void frameControlUpdated();
	
	}
