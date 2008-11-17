package endrov.recording;

import endrov.hardware.Hardware;

public interface HWSerial extends Hardware
	{

	
	public String nonblockingRead();
	public String readUntilTerminal(String term);
	public void writePort(String s);
	
	


	
	}
