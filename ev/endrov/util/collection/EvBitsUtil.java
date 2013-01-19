/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.collection;

public class EvBitsUtil
	{
	public static final byte[] intToByteArray(int value) 
		{
		return new byte[] {
				(byte)(value >>> 24),
				(byte)(value >>> 16),
				(byte)(value >>> 8),
				(byte)value};
		}


	public static final int byteArrayToInt(byte b0, byte b1, byte b2, byte b3) 
		{
		return (b0 << 24)
		+ ((b1 & 0xFF) << 16)
		+ ((b2 & 0xFF) << 8)
		+ (b3 & 0xFF);
		}


	public static final byte[] shortToByteArray(short value) 
		{
		return new byte[] {
				(byte)(value >>> 8),
				(byte)value};
		}


	public static final int byteArrayToShort(byte b0,byte b1) 
		{
		return ((b0 & 0xFF) << 8)	+ (b1 & 0xFF);
		}

	
	
	}
