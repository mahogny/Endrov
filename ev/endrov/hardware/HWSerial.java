/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;


public interface HWSerial extends EvDevice
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
