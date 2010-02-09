/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

/**
 * Virtual serial device: No automatic replies or anything. Override VirtualSerial to create a proper testing environment
 * @author Johan Henriksson
 */
public class VirtualSerialBasic extends VirtualSerial
	{
	public VirtualSerialBasic(String title)
		{
		super(title);
		}
	public VirtualSerialBasic()
		{
		super("-");
		}
	public String response(String s)
		{
		return null;
		}
	}
