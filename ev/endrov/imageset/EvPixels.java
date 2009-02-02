package endrov.imageset;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

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
	private float arrayF[];
	private double arrayD[];
	
	private byte  arrayB[];
	private short arrayS[];
	private int arrayI[];
	
	private BufferedImage awt;

	/////// Access to arrays. This way the pointer cannot be changed externally
	public float[] getArrayF(){return arrayF;}
	public double[] getArrayD(){return arrayD;}
	public byte[] getArrayB(){return arrayB;}
	public short[] getArrayS(){return arrayS;}
	public int[] getArrayI(){return arrayI;}
	public BufferedImage getAWT(){return awt;}
	
	
	
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
			//Conversion is coded to be slow but short at the moment. Metaprogramming would help!
			if(isIntegral() || type==TYPE_AWT)
				{
				helperConvertToInt();
				
				
				
				}
			
			
			
			
			if(newType==TYPE_AWT)
				{
				
				}
			
			
			//TODO
			
			}
		}
	
	private void helperConvertFromAwt(int newType)
		{
		WritableRaster r=awt.getRaster();
		arrayW=awt.getWidth();
		awt=null;
		switch(newType)
			{
			case TYPE_BYTE:
			case TYPE_UBYTE:
				helperConvertFromAwt(TYPE_INT);
				break;
			
			case TYPE_SHORT:
			case TYPE_USHORT:
				helperConvertFromAwt(TYPE_INT);
				break;
			
			case TYPE_INT:
			case TYPE_UINT:
				arrayI=new int[r.getWidth()*r.getHeight()];
				r.getSamples(0, 0, r.getWidth(), r.getHeight(), 0, arrayI); 
				break;
				
			case TYPE_FLOAT:
				arrayF=new float[r.getWidth()*r.getHeight()];
				r.getSamples(0, 0, r.getWidth(), r.getHeight(), 0, arrayF); 
				break;
				
			case TYPE_DOUBLE:
				arrayF=new float[r.getWidth()*r.getHeight()];
				r.getSamples(0, 0, r.getWidth(), r.getHeight(), 0, arrayF); 
				break;
			}
		type=newType;
			
		
		}
	
	private void helperConvertToInt()
		{
		//There are some local variable optimizations(?).
		int[] narr=null;
		if(type==TYPE_BYTE || type==TYPE_UBYTE)
			{
			byte[] larr=arrayB;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=larr[i];
			arrayB=null;
			}
		else if(type==TYPE_SHORT || type==TYPE_USHORT)
			{
			short[] larr=arrayS;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=larr[i];
			arrayS=null;
			}
		else if(type==TYPE_INT || type==TYPE_UINT)
			{
			narr=this.arrayI;
			}
		else if(type==TYPE_FLOAT)
			{
			short[] larr=arrayS;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=larr[i];
			arrayS=null;
			}
		else if(type==TYPE_DOUBLE)
			{
			
			}
		else if(type==TYPE_AWT)
			{
			WritableRaster r=awt.getRaster();
			narr=new int[awt.getWidth()*awt.getHeight()];
			r.getSamples(0, 0, awt.getWidth(), awt.getHeight(), 0, narr); //exist for more types
			arrayW=awt.getWidth();
			awt=null;
			}
		this.arrayI=narr;
		type=TYPE_INT;
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
