/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.WeakHashMap;


/**
 * <p>
 * One pixel plane. The Endrov philosophy is that there are no ranges, pixels are raw sensor readings. Only upon display need the range be adjusted
 * and is done so by various filters in real-time. This means that converting data e.g. from int to double will not rescale it. It is up to the user
 * to ensure the data range fits during unsafe conversions. 
 * </p>
 * 
 * <p>
 * Emphasis is on the need for filters. Filters cannot cope with all types so conversion will be needed. The getReadOnly() and getWritable()
 * will give images in formats according to bitflags of valid formats. getReadOnly() is designed to have zero overhead if no conversion is needed.
 * </p>
 * 
 * <p>
 * Safe conversions are
 * <pre>
 * AWT -> ---------------> 
 *        unsigned byte -> short -> int -> float -> double 
 * </pre>
 * and these will be performed automatically if needed. When an unsafe conversion is enforced the least bad choice will be taken. The AWT format
 * can be of several bit depths and is given special treatment. 
 * </p>
 * 
 * <p>
 * This class is designed for performance. Depending on the type, different arrays will be used e.g. use getArrayInt when the data is of type int.
 * Data is stored in a 1D array as Java does not have truly 2D arrays/matrices. The index in the array is width*x+y. It can be calculated with
 * convenience functions but this will be slower than doing it manually.
 * </p>
 *  
 * 
 * @author Johan Henriksson
 *
 */
public class EvPixels implements AnyEvImage
	{
	private static WeakHashMap<EvPixels, Object> allEvPixels=new WeakHashMap<EvPixels, Object>();
	
	
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
	 * 
	 * uint8 -> int16 -> int32 -> float -> double
	 * 
	 */
	
	/** Type of data, any of TYPE_* */
	//private int type;
	private EvPixelsType type;
		/** Width */
	private int w;
	/** Height */ 
	private int h;
	
	/**
	 * Get type of pixel format
	 */
	public EvPixelsType getType()
		{
		return type;
		}
	
	/**
	 * Get width of image
	 */
	public int getWidth()
		{
		return w;
		}

	/**
	 * Get height of image
	 */
	public int getHeight()
		{
		return h;
		}
	
	
	/////// Data containers ///////////
	private byte  arrayB[];
	private short arrayS[];
	private int arrayI[];
	private float arrayF[];
	private double arrayD[];
	private BufferedImage awt;

	/////// Access to arrays. This way the pointer cannot be changed externally
	/**
	 * Return pointer to content. Array meant to be modifiable.
	 */
	public float[] getArrayFloat(){return arrayF;}
	/**
	 * Return pointer to content. Array meant to be modifiable.
	 */
	public double[] getArrayDouble(){return arrayD;}
	/**
	 * Return pointer to content. Array meant to be modifiable.
	 */
	public byte[] getArrayUnsignedByte(){return arrayB;}
	/**
	 * Return pointer to content. Array meant to be modifiable.
	 */
	public short[] getArrayShort(){return arrayS;}
	/**
	 * Return pointer to content. Array meant to be modifiable.
	 */
	public int[] getArrayInt(){return arrayI;}
	/**
	 * Return pointer to content. Array meant to be modifiable.
	 */
	public BufferedImage getAWT(){return awt;}
	
	
	
	/////// Types ////////////////
	/** Unsigned byte */
	public static final int TYPE_UBYTE  =1<<1;
	/** Signed short 16-bit */
	public static final int TYPE_SHORT  =1<<2;
	/** Signed integer 32-bit */
	public static final int TYPE_INT    =1<<4;
	/** Floating point (approximate decimals) 32-bit */
	public static final int TYPE_FLOAT  =1<<6;
	/** Floating point (approximate decimals) 64-bit */
	public static final int TYPE_DOUBLE =1<<7;
	/** AWT BufferedImage */
	public static final int TYPE_AWT    =1<<8;

	/////// Compound types ========
	public static final int TYPES_SIGNED = TYPE_SHORT | TYPE_INT | TYPE_FLOAT | TYPE_DOUBLE;
	public static final int TYPES_INTEGRAL = TYPE_UBYTE | TYPE_SHORT | TYPE_INT;
	public static final int TYPES_FP = TYPE_FLOAT | TYPE_DOUBLE;
	public static final int TYPES_ANY = TYPE_UBYTE | TYPE_SHORT | TYPE_INT | TYPE_FLOAT | TYPE_DOUBLE | TYPE_AWT;
	public static final int TYPES_ANYBUTAWT = TYPE_UBYTE | TYPE_SHORT | TYPE_INT | TYPE_FLOAT | TYPE_DOUBLE;
	

	/**
	 * Get the approximate memory usage of this pixel
	 */
	public int getMemoryUsage()
		{
		if(arrayB!=null)
			return arrayB.length;
		else if(arrayS!=null)
			return arrayS.length*2;
		else if(arrayI!=null)
			return arrayI.length*4;
		else if(arrayF!=null)
			return arrayF.length*4;
		else if(arrayD!=null)
			return arrayD.length*8;
		else if(awt!=null)
			return awt.getWidth()*awt.getHeight(); //Approx
		return 0;
		}

	private void createdNew()
		{
		allEvPixels.put(this,null);
		System.out.println("------------- now #evpixel in memory: "+allEvPixels.size());
		}
	
	/**
	 * Constructing an empty pixelset, shouldn't really be possible outside
	 */
	private EvPixels()
		{
		count(1);
		createdNew();
		}

	/**
	 * Deep copy of another image
	 */
	public EvPixels(EvPixels p)
		{
		count(1);
		setPixels(p);
		createdNew();

		/*
		 * 
		 *  TODO does not work on mac, new in 1.6
		if(arrayB!=null)
			p.arrayB=Arrays.copyOf(arrayB, arrayB.length);
		if(arrayS!=null)
			p.arrayS=Arrays.copyOf(arrayS, arrayS.length);
		if(arrayI!=null)
			p.arrayI=Arrays.copyOf(arrayI, arrayI.length);
		if(arrayF!=null)
			p.arrayF=Arrays.copyOf(arrayF, arrayF.length);
		if(arrayD!=null)
			p.arrayD=Arrays.copyOf(arrayD, arrayD.length);
		*/

	
		}
	

	/**
	 * Copy constructor, from AWT image
	 */
	public EvPixels(BufferedImage awt)
		{
		count(1);
		createdNew();
		setPixels(awt);
		}
	
	
	/**
	 * Allocate a new pixel plane
	 */
	public EvPixels(EvPixelsType type, int w, int h)
		{
		count(1);
		createdNew();
		allocate(type,w,h);
		}
	
	public static EvPixels createFromUByte(int w, int h, byte[] a)
		{
		EvPixels p=new EvPixels();
		p.type=EvPixelsType.UBYTE;
		p.w=w;
		p.h=h;
		p.arrayB=a;
		return p;
		}
	
	public static EvPixels createFromShort(int w, int h, short[] a)
		{
		EvPixels p=new EvPixels();
		p.type=EvPixelsType.SHORT;
		p.w=w;
		p.h=h;
		p.arrayS=a;
		return p;
		}

	public static EvPixels createFromInt(int w, int h, int[] a)
		{
		EvPixels p=new EvPixels();
		p.type=EvPixelsType.INT;
		p.w=w;
		p.h=h;
		p.arrayI=a;
		return p;
		}

	public static EvPixels createFromFloat(int w, int h, float[] a)
		{
		EvPixels p=new EvPixels();
		p.type=EvPixelsType.FLOAT;
		p.w=w;
		p.h=h;
		p.arrayF=a;
		return p;
		}

	public static EvPixels createFromDouble(int w, int h, double[] a)
		{
		EvPixels p=new EvPixels();
		p.type=EvPixelsType.DOUBLE;
		p.w=w;
		p.h=h;
		p.arrayD=a;
		return p;
		}
	
	
	
	/**
	 * Convert to a different type. 
	 */
	private EvPixels convertTo(EvPixelsType newType, boolean readOnly)
		{
		//Only convert if needed
		if(type!=newType)
			{
			//Conversion is coded to be slow but short at the moment. Metaprogramming would help!
			if(type==EvPixelsType.AWT)
				return helperConvertFromAwt(newType);
			else
				{
				EvPixels p;
				if(type==EvPixelsType.INT)
					p=this;
				else
					p=helperConvertToInt();
				
				if(newType==EvPixelsType.INT)
					return p;
				else
					return p.helperConvertFromInt(newType);
				}
			}
		else
			{
			if(readOnly)
				return this;
			else
				return new EvPixels(this);
			}
		}
	
	public EvPixels convertToFloat(boolean readOnly)
		{
		return convertTo(EvPixelsType.FLOAT, readOnly);
		}
	
	public EvPixels convertToDouble(boolean readOnly)
		{
		return convertTo(EvPixelsType.DOUBLE, readOnly);
		}

	public EvPixels convertToInt(boolean readOnly)
		{
		return convertTo(EvPixelsType.INT, readOnly);
		}

	public EvPixels convertToShort(boolean readOnly)
		{
		return convertTo(EvPixelsType.SHORT, readOnly);
		}

	/**
	 * Get pixels in valid format. For performance the data will not be copied and converted unless needed. Use bit operations to put together valid types. 
	 */
	public EvPixels getReadOnly(EvPixelsType... validTypes)
		{
		return get(Arrays.asList(validTypes), true);
		}
	
	/**
	 * Get a copy of the image in a valid format. Writing on copy will not modify the original. Use bit operations to put together valid types
	 */
	public EvPixels getWritable(EvPixelsType... validTypes)
		{
		return get(Arrays.asList(validTypes), false);
		}


	
	/**
	 * Get pixels in suitable format
	 */
	public EvPixels get(Collection<EvPixelsType> validTypes, boolean readOnly)
		{
		//System.out.println("Wanting "+validTypes);
		HashSet<EvPixelsType> validT=new HashSet<EvPixelsType>();
		validT.addAll(validTypes);
		
		//Quick check: can current type be kept?
		//Needed to keep AWT in particular
		if(validT.contains(type))
			{
			//System.out.println("keep type"+type);
			if(readOnly)
				return this;
			else
				return new EvPixels(this);
			}
		
		EvPixelsType[] typeOrder=new EvPixelsType[]{EvPixelsType.AWT,EvPixelsType.UBYTE,EvPixelsType.SHORT,EvPixelsType.INT,EvPixelsType.FLOAT,EvPixelsType.DOUBLE};

		//Current type in list
		int curtypei=0;
		while(type!=typeOrder[curtypei])
			curtypei++;
		
		//AWT has to be treated specially
		if(type==EvPixelsType.AWT)
			curtypei=2;
		
		//Try to upconvert to satisfy
		for(int i=curtypei;i<typeOrder.length;i++)
			if(validT.contains(typeOrder[i]))
				{
				//System.out.println("Upconvert "+typeOrder[i]);
				return convertTo(typeOrder[i], readOnly);
				}
		
		//Try to downconvert. Best-effort, use the least destructible option
		for(int i=curtypei;i>=0;i--)
			if(validT.contains(typeOrder[i]))
				{
				//System.out.println("Downconvert "+typeOrder[i]);
				return convertTo(typeOrder[i], readOnly);
				}

		//Sane options exhausted
		throw new RuntimeException("No types at all would fit conversion! Was the acceptable-list 0?");
		}
	
	
	
	/**
	 * Convert from AWT into any other format.
	 * Assumes it is not AWT so conversion will always be needed
	 */
	private EvPixels helperConvertFromAwt(EvPixelsType newType)
		{
		EvPixels p=new EvPixels();
		WritableRaster r=awt.getRaster();
		p.w=awt.getWidth();
		p.h=awt.getHeight();
		p.type=newType;
		
		//System.out.println("convertAWT -> "+newType);
		
		EvPixels q;
		switch(newType)
			{
			case UBYTE:
				q=helperConvertFromAwt(EvPixelsType.INT);
				return q.helperConvertFromInt(newType);
			
			case SHORT:
				q=helperConvertFromAwt(EvPixelsType.INT);
				return q.helperConvertFromInt(newType);
			
			case INT:
				p.arrayI=new int[r.getWidth()*r.getHeight()];
				r.getSamples(0, 0, r.getWidth(), r.getHeight(), 0, p.arrayI);
				return p;
				
			case FLOAT:
				p.arrayF=new float[r.getWidth()*r.getHeight()];
				r.getSamples(0, 0, r.getWidth(), r.getHeight(), 0, p.arrayF);
				return p;
				
			case DOUBLE:
				p.arrayD=new double[r.getWidth()*r.getHeight()];
				r.getSamples(0, 0, r.getWidth(), r.getHeight(), 0, p.arrayD);
				return p;
			
			default:
				throw new RuntimeException("convert from awt to "+newType.getDesc()+" not supported by this function");
			}
		}
	
	/**
	 * Convert from Int to any other format. 
	 * Assumes it is not int so conversion will always be needed
	 */
	private EvPixels helperConvertFromInt(EvPixelsType newType)
		{
		EvPixels p=new EvPixels();
		p.w=w;
		p.h=h;
		p.type=newType;
		
		//System.out.println("convINT -> "+newType);
		
		int[] larr=arrayI;
		if(newType==EvPixelsType.UBYTE)
			{
			byte[] narr=new byte[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(byte)larr[i];
			p.arrayB=narr;
			return p;
			}
		else if(newType==EvPixelsType.SHORT)
			{
			short[] narr=new short[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(short)larr[i];
			p.arrayS=narr;
			return p;
			}
		else if(newType==EvPixelsType.FLOAT)
			{
			float[] narr=new float[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(float)larr[i];
			p.arrayF=narr;
			return p;
			}
		else if(newType==EvPixelsType.DOUBLE)
			{
			double[] narr=new double[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(double)larr[i];
			p.arrayD=narr;
			return p;
			}
		else if(newType==EvPixelsType.AWT)
			{
			/*
			for(int d:arrayI)
				if(d<0 || d>300)
					System.out.print(" "+d);
			System.out.println();
			System.out.println();
			*/
			//Can be made faster
			p.awt=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
			p.awt.getRaster().setPixels(0, 0, w, h, arrayI);
			
			return p;
			}
		throw new RuntimeException("convert from int to "+newType+" not supported by this function");
		}
	
	/**
	 * Convert from int to any type.
	 * Assumes it is not int so conversion will always be needed
	 */
	private EvPixels helperConvertToInt()
		{
		//System.out.println("conv "+type+" -> INT");
		
		//There are some local variable optimizations(?).
		int[] narr=null;
		if(type==EvPixelsType.UBYTE)
			{
			byte[] larr=arrayB;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(int)(larr[i] & 0xff); //Kill off sign
			}
		else if(type==EvPixelsType.SHORT)
			{
			short[] larr=arrayS;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=larr[i];
			}
		else if(type==EvPixelsType.FLOAT)
			{
			float[] larr=arrayF;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(int)larr[i];
			}
		else if(type==EvPixelsType.DOUBLE)
			{
			double[] larr=arrayD;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(int)larr[i];
			}
		else if(type==EvPixelsType.AWT)
			{
			WritableRaster r=awt.getRaster();
			narr=new int[awt.getWidth()*awt.getHeight()];
			r.getSamples(0, 0, awt.getWidth(), awt.getHeight(), 0, narr); //exist for more types
			}
		
		
		if(narr!=null)
			{
			EvPixels p=new EvPixels();
			p.arrayI=narr;
			p.type=EvPixelsType.INT;
			p.w=w;
			p.h=h;
			return p;
			}
		else
			throw new RuntimeException("convert to int from "+type+" not supported by this function");
		}
	
	
	/**
	 * Clear the content. Remove the type
	 */
	private void unallocate()
		{
		if(type!=null)
			{
			arrayF=null;
			arrayD=null;
			arrayB=null;
			arrayS=null;
			arrayI=null;
			awt=null;
			type=null;
			}
		}
	
	/**
	 * Set all pixels to new image
	 */
	public void setPixels(BufferedImage im)
		{
		unallocate();
		type=EvPixelsType.AWT;
		w=im.getWidth();
		h=im.getHeight();
		awt=im;
		}


	/**
	 * Set all pixels to image. Will copy data, not link to it
	 */
	public void setPixels(EvPixels im)
		{
		unallocate();
		
		type=im.type;
		w=im.w;
		h=im.h;

		if(im.arrayB!=null)
			{
			arrayB=new byte[im.arrayB.length];
			for(int i=0;i<arrayB.length;i++)
				arrayB[i]=im.arrayB[i];
			}

		if(im.arrayS!=null)
			{
			arrayS=new short[im.arrayS.length];
			for(int i=0;i<arrayS.length;i++)
				arrayS[i]=im.arrayS[i];
			}

		if(im.arrayI!=null)
			{
			arrayI=new int[im.arrayI.length];
			for(int i=0;i<arrayI.length;i++)
				arrayI[i]=im.arrayI[i];
			}

		if(im.arrayF!=null)
			{
			arrayF=new float[im.arrayF.length];
			for(int i=0;i<arrayF.length;i++)
				arrayF[i]=im.arrayF[i];
			}

		if(im.arrayD!=null)
			{
			arrayD=new double[im.arrayD.length];
			for(int i=0;i<arrayD.length;i++)
				arrayD[i]=im.arrayD[i];
			}
		
		if(im.awt!=null)
			{
			BufferedImage bim=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
			bim.getGraphics().drawImage(im.awt, 0, 0, null);
			awt=bim;
			}
		
		}

	
	/**
	 * Set type and allocate space for image. All pixels will be set to 0
	 */
	public void allocate(EvPixelsType type, int w, int h)
		{
		int s=w*h;
		this.w=w;
		this.h=h;
		this.type=type;
		switch(type)
			{
			case UBYTE:
				arrayB=new byte[s];
				break;
			case SHORT:
				arrayS=new short[s];
				break;
			case INT:
				arrayI=new int[s];
				break;
			case FLOAT:
				arrayF=new float[s];
				break;
			case DOUBLE:
				arrayD=new double[s];
				break;
			case AWT:
				awt=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
				break;
			default:
				throw new RuntimeException("Provided bad type to allocate, "+type);
			}
		}
	
	
	public int getRowIndex(int y)
		{
		return w*y;
		}
	
	public int getPixelIndex(int x, int y)
		{
		return w*y+x;
		}
	
	
	public BufferedImage quickReadOnlyAWT()
		{
		return convertTo(EvPixelsType.AWT, true).getAWT();
		}
	
	public String toString()
		{
		return "EvPixels "+type+" "+w+"x"+h;
		}

	/**
	 * Put image values into string. For debugging
	 */
	public String asciiImage()
		{
		StringBuffer sb=new StringBuffer();
		if(type==EvPixelsType.INT)
			{
			for(int i=0;i<getHeight();i++)
				{
				for(int j=0;j<getWidth();j++)
					sb.append(arrayI[i*getWidth()+j]+"\t");
				sb.append("\n");
				}
			}
		else if(type==EvPixelsType.DOUBLE)
			{
			for(int i=0;i<getHeight();i++)
				{
				for(int j=0;j<getWidth();j++)
					sb.append(arrayD[i*getWidth()+j]+"\t");
				sb.append("\n");
				}
			}
		else
			return convertTo(EvPixelsType.DOUBLE, true).asciiImage();
		return sb.toString();
		}
	
	/**
	 * Put given line values into string. for debugging
	 */
	public String asciiPart(int y, int startX, int endX)
		{
		StringBuffer sb=new StringBuffer();
		if(type==EvPixelsType.UBYTE)
			return convertTo(EvPixelsType.INT, true).asciiPart(y, startX, endX);
		if(type==EvPixelsType.SHORT)
			return convertTo(EvPixelsType.INT, true).asciiPart(y, startX, endX);
		else if(type==EvPixelsType.INT)
			{
			for(int j=startX;j<endX;j++)
				sb.append(arrayI[y*getWidth()+j]+"\t");
			}
		else if(type==EvPixelsType.DOUBLE)
			{
			for(int j=startX;j<endX;j++)
				sb.append(arrayD[y*getWidth()+j]+"\t");
			}
		else
			return convertTo(EvPixelsType.DOUBLE, true).asciiPart(y, startX, endX);
		return sb.toString();
		}
	
	
	/*
	public static void main(String[] arg)
		{
		EvPixels p=new EvPixels(EvPixelsType.TYPE_INT, 200, 100);
		int[] arr=p.getArrayInt();
		arr[5]=1;
		EvPixels q=p.getReadOnly(EvPixelsType.TYPE_AWT);
		
		//but what if I want a copy of a different type? why the need to duplicate *and* convert?
		
		}
	*/
	
	
	
	/**
	 * Get 2D array. This is not fast! Recommended only for use with matlab
	 */
	public double[][] getArrayDouble2D()
		{
		double[][] aim=new double[h][w];

		if(type==EvPixelsType.INT)
			{
			int curi=0;
			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					{
					aim[ay][ax]=arrayI[curi];
					curi++;
					}
			}
		else
			{
			
			
			//TODO support other types
			
			BufferedImage bim=convertTo(EvPixelsType.AWT, true).getAWT();
	
			Raster r=bim.getRaster();
			for(int i=0;i<h;i++)
				r.getSamples(0, i, w, 1, 0, aim[i]);
			}
		return aim;
		}

	/**
	 * ONLY recommended for non-java users as it is not as fast as using getArray
	 * and filling it in yourself, avoiding a copy.
	 */
	public void setArrayDouble2D(double[][] arr)
		{
		type=EvPixelsType.DOUBLE;
		h=arr.length;
		w=arr[0].length;
		arrayD=new double[w*h];
		for(int y=0;y<h;y++)
			for(int x=0;x<w;x++)
				arrayD[x+y*w]=arr[y][x];
		}
	
	
	
	/**
	 * Convert unsigned short to signed integer
	 */
	public static int[] convertUshortToInt(short[] a, int len)
		{
		int[] b=new int[len];
		for(int i=0;i<len;i++)
			{
			int v=a[i];
			if(v<0)
				v+=65536; //TODO or 65535? I think 6
			v/=8; //TODO temp. AWT messes up later in contrast/brightness
			b[i]=v;
			}
		return b;		
		}
	
	/**
	 * Convert unsigned byte to signed short
	 */
	public static short[] convertUbyteToShort(byte[] a, int len)
		{
		short[] b=new short[len];
		for(int i=0;i<len;i++)
			{
			short v=a[i];
			if(v<0)
				v+=256; //TODO or 256?
			b[i]=v;
			}
		return b;		
		}

	@Override
	protected void finalize() throws Throwable
		{
		super.finalize();
		count(-1);
		}
	
	
	private static int livePixels=0;
	
	private synchronized void count(int c)
		{
		livePixels+=c;
		/*
		if(c>0)
			System.out.println("creating pixels, live "+livePixels);
		else
			System.out.println("--##---- finalize evpixel, live: "+livePixels);
			*/
		}
	
	public static int getNumLiveImages()
		{
		return livePixels;
		}
	
	
	/**
	 * Check if the pixel values theoretically could fit in 8 bits
	 */
	public boolean fitsIn8bit()
		{
		return getType()==EvPixelsType.AWT;
		}
	}
