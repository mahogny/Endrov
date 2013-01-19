/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeImageset;

/**
 * Pixel data type
 * @author Johan Henriksson
 *
 */
public enum EvPixelsType
	{
	UBYTE(1<<1,"Unsigned byte","ubyte"),
	SHORT(1<<2, "Signed short 16-bit","short"),
	INT(1<<4, "Signed integer 32-bit","int"),
	FLOAT(1<<6, "Floating point (approximate decimals) 32-bit","float"),
	DOUBLE(1<<7, "Floating point (approximate decimals) 64-bit","double"),
	AWT(1<<8, "AWT BufferedImage","AWT");
	
	
	private int val;
	private String shortDesc;
	private String longDesc;
	
	private EvPixelsType(int val, String longDesc, String shortDesc)
		{
		this.val = val;
		this.shortDesc = shortDesc;
		this.longDesc = longDesc;
		}
	
	public String getLongDesc()
		{
		return longDesc;
		}

	public String getDesc()
		{
		return shortDesc;
		}

	public int getVal()
		{
		return val;
		}
	
	
	public boolean isIntegral()
		{
		return this==UBYTE || this==SHORT || this==INT;
		}
	
	/**
	 * Is signed
	 */
	public boolean isSigned()
		{
		return this!=UBYTE && this!=AWT;
		}
	
	/**
	 * Is some sort of floating point
	 */
	public boolean isFP()
		{
		return this==FLOAT || this==DOUBLE;
		}
	}
