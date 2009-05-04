package endrov.recording;

import endrov.hardware.Device;

public interface HWSerial extends Device
	{

	/**
	 * Read whatever input is available at the moment
	 */
	public String nonblockingRead();
	
	/**
	 * Read until string occurs. Returns up to and including this string
	 */
	public String readUntilTerminal(String term);
	
	/**
	 * Write string to serial port
	 */
	public void writePort(String s);
	
	


	
	}
