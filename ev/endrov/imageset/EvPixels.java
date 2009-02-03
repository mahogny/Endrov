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
	 * All converters would be best off in C++ with templates or mixed with opencl or C with macros
	 * 
	 * ranges: it's best to *always* convert between ranges. this guarantees that after a few conversions
	 * and back to the original, it's still within range.
	 * 
	 */
	
	/**
	 * Type of data, any of TYPE_*
	 */
	private int type;
	
	public int getType()
		{
		return type;
		}
	
	
	/** Width */
	private int arrayW;
	/** Height */ 
	private int arrayH;
	
	public int getWidth()
		{
		return arrayW;
		}

	public int getHeight()
		{
		return arrayH;
		}
	
	
	/////// Data containers ///////////
	private byte  arrayB[];
	private short arrayS[];
	private int arrayI[];
	private float arrayF[];
	private double arrayD[];
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
			if(type==TYPE_AWT)
				{
				helperConvertFromAwt(newType);
				}
			else
				{
				helperConvertToInt();
				helperConvertFromInt(newType);
				}
			}
		}
	
	/**
	 * Convert from AWT into any other format
	 */
	private void helperConvertFromAwt(int newType)
		{
		if(newType!=TYPE_AWT)
			{
			WritableRaster r=awt.getRaster();
			arrayW=awt.getWidth();
			arrayH=awt.getHeight();
			awt=null;
			switch(newType)
				{
				case TYPE_BYTE:
				case TYPE_UBYTE:
					helperConvertFromAwt(TYPE_INT);
					helperConvertFromInt(newType);
					break;
				
				case TYPE_SHORT:
				case TYPE_USHORT:
					helperConvertFromAwt(TYPE_INT);
					helperConvertFromInt(newType);
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
					arrayD=new double[r.getWidth()*r.getHeight()];
					r.getSamples(0, 0, r.getWidth(), r.getHeight(), 0, arrayD); 
					break;
				}
			type=newType;
			}
		}
	
	/**
	 * Convert from Int to any other format
	 */
	private void helperConvertFromInt(int newType)
		{
		if(newType!=TYPE_INT && newType!=TYPE_UINT)
			;
		else
			{
			int[] larr=arrayI;
			if(type==TYPE_BYTE || type==TYPE_UBYTE)
				{
				byte[] narr=new byte[larr.length];
				for(int i=0;i<larr.length;i++)
					narr[i]=(byte)larr[i];
				}
			else if(type==TYPE_SHORT || type==TYPE_USHORT)
				{
				byte[] narr=new byte[larr.length];
				for(int i=0;i<larr.length;i++)
					narr[i]=(byte)larr[i];
				}
			else if(newType!=TYPE_INT && newType!=TYPE_UINT)
				;
			else if(newType==TYPE_FLOAT)
				{
				float[] narr=new float[larr.length];
				for(int i=0;i<larr.length;i++)
					narr[i]=(float)larr[i];
				}
			else if(newType==TYPE_DOUBLE)
				{
				double[] narr=new double[larr.length];
				for(int i=0;i<larr.length;i++)
					narr[i]=(double)larr[i];
				}
			else if(newType==TYPE_AWT)
				{
				//Can be made faster
				BufferedImage im=new BufferedImage(arrayW,larr.length/arrayW,BufferedImage.TYPE_BYTE_GRAY);
				im.getRaster().setPixels(0, 0, arrayW, larr.length/arrayW, arrayI);
				}
			arrayI=null;
			type=newType;
			}
		}
	
	/**
	 * Convert from int to any type
	 */
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
			float[] larr=arrayF;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(int)larr[i];
			arrayF=null;
			}
		else if(type==TYPE_DOUBLE)
			{
			double[] larr=arrayD;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(int)larr[i];
			arrayD=null;
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
	
	/**
	 * Clear the content. Remove the type
	 */
	private void unallocate()
		{
		if(type!=0)
			{
			arrayF=null;
			arrayD=null;
			arrayB=null;
			arrayS=null;
			arrayI=null;
			awt=null;
			type=0;
			}
		}
	
	/**
	 * Set all pixels to new image
	 */
	public void setPixels(BufferedImage im)
		{
		unallocate();
		type=TYPE_AWT;
		arrayW=im.getWidth();
		arrayH=im.getHeight();
		awt=im;
		}
	
	/**
	 * Set type and allocate space for image. All pixels will be set to 0
	 */
	public void allocate(int type, int w, int h)
		{
		int s=w*h;
		arrayW=w;
		arrayH=h;
		this.type=type;
		switch(type)
			{
			case TYPE_BYTE:
			case TYPE_UBYTE:
				arrayB=new byte[s];
				break;
			case TYPE_SHORT:
			case TYPE_USHORT:
				arrayS=new short[s];
				break;
			case TYPE_INT:
			case TYPE_UINT:
				arrayI=new int[s];
				break;
			case TYPE_FLOAT:
				arrayF=new float[s];
				break;
			case TYPE_DOUBLE:
				arrayD=new double[s];
				break;
			case TYPE_AWT:
				awt=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
				break;
			}
		}
	
	
	
	public static void main(String[] arg)
		{
		EvPixels p=new EvPixels();
		p.allocate(EvPixels.TYPE_INT, 200, 100);
		int[] arr=p.getArrayI();
		arr[5]=1;
		p.convertTo(TYPE_AWT, false);
		
		//but what if I want a copy of a different type? why the need to duplicate *and* convert?
		
		/*
		 * this is what I really want
		 * 
		 * EvPixels n=p.getReadOnly();
		 * EvPixels n=p.getReadOnlyFormats(TYPE_INT,....);
		 * EvPixels n=p.getReadOnly
		 * 
		 * somehow also keep track of it was copied. set a flag internally?
		 * 
		 * EvPixels n=p.getWritableCopy()
		 * 
		 * And in very few cases, p.convert();  but this can be done with the above functions
		 * 
		 * 
		 */
		
		
		}
	}
