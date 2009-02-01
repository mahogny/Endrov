package endrov.imageset;

import java.awt.image.BufferedImage;

public class EvPixels
	{
	/*
	 * +- are the same for signed/unsigned. / is by all means not. * might give sensible results, one claims it won't, one claims it does.
	 * 
	 * 
	 * Auto-generation of code: access a virtual type, Ptype. parse and replace all occurances. messy since java has no infix operators.
	 * Later opencl-stuff will need more elaborate solutions anyway. use byte as the basic edition? byte is never used otherwise, good candidate.
	 * 
	 */
	
	/**
	 * Type of data, any of TYPE_*
	 */
	int type;
	
	
	/**
	 * Width of one line of pixels, if it is of array type
	 */
	public int arrayW;
	
	/////// Data containers ///////////
	public float arrayF[];
	public double arrayD[];
	
	public byte  arrayB[];
	public short arrayS[];
	public int arrayI[];
	
	public BufferedImage awt;

	/////// Types ////////////////
	public static final int TYPE_BYTE   =1<<0;
	public static final int TYPE_UBYTE  =1<<1;
	public static final int TYPE_SHORT  =1<<2;
	public static final int TYPE_USHORT =1<<3;
	public static final int TYPE_INT    =1<<4;
	public static final int TYPE_UINT   =1<<5;

	public static final int TYPE_FLOAT  =1<<6;
	public static final int TYPE_DOUBLE =1<<7;

	public static final int TYPE_AWT    =1<<8;

	/**
	 * Is some sort of integer
	 */
	public boolean isIntegral()
		{
		return (type & 0x3F) != 0;
		}
	
	/**
	 * Is signed
	 */
	public boolean isSigned()
		{
		return (type & (TYPE_BYTE | TYPE_SHORT | TYPE_INT | TYPE_FLOAT | TYPE_DOUBLE)) != 0;
		}
	
	/**
	 * Is some sort of floating point
	 */
	public boolean isFP()
		{
		return (type & (TYPE_FLOAT | TYPE_DOUBLE)) != 0;
		}
	
	public static boolean typeIsAnyByte(int t) {return (t&(TYPE_BYTE  | TYPE_UBYTE))!=0;}
	public static boolean typeIsAnyShort(int t){return (t&(TYPE_SHORT | TYPE_USHORT))!=0;}
	public static boolean typeIsAnyInt(int t)  {return (t&(TYPE_INT   | TYPE_UINT))!=0;}
	
	/**
	 * Convert to a different type. 
	 * TODO scale range by shifting integer types and dividing for float? what are the ranges?
	 */
	public void convertTo(int newType, boolean scaleRange)
		{
		//Only convert if needed
		if(type!=newType)
			{
			//TODO
			
			}
		}
	
	/**
	 * Convert if needed: Ensure it is a signed format
	 */
	public void ensureSigned(boolean scaleRange)
		{
		if(!isSigned())
			{
			int ntype=0;
			if(isIntegral())
				{
				if(type==TYPE_UINT)
					ntype=TYPE_INT;
				else if(type==TYPE_USHORT)
					ntype=TYPE_SHORT;
				else if(type==TYPE_UBYTE)
					ntype=TYPE_BYTE;
				}
			else
				ntype=TYPE_INT;
			convertTo(ntype, scaleRange);
			}
		}
	
	
	}
