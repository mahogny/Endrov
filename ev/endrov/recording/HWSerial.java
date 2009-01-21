package endrov.recording;

import endrov.hardware.Hardware;

public interface HWSerial extends Hardware
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
