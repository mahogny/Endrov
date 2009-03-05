package endrov.imageset;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;


/**
 * One pixel plane. The Endrov philosophy is that there are no ranges, pixels are raw sensor readings. Only upon display need the range be adjusted
 * and is done so by various filters in real-time. This means that converting data e.g. from int to double will not rescale it. It is up to the user
 * to ensure the data range fits during unsafe conversions.
 * 
 * Emphasis is on the need for filters. Filters cannot cope with all types so conversion will be needed. The getReadOnly() and getWritable()
 * will give images in formats according to bitflags of valid formats. getReadOnly() is designed to have zero overhead if no conversion is needed.
 * 
 * Safe conversions are
 * AWT -> --------------->
 *        unsigned byte -> short -> int -> float -> double 
 * and these will be performed automatically if needed. When an unsafe conversion is enforced the least bad choice will be taken. The AWT format
 * can be of several bit depths and is given special treatment. 
 * 
 * This class is designed for performance. Depending on the type, different arrays will be used e.g. use getArrayInt when the data is of type int.
 * Data is stored in a 1D array as Java does not have truly 2D arrays/matrices. The index in the array is width*x+y. It can be calculated with
 * convenience functions but this will be slower than doing it manually.
 * 
 *  
 * 
 * @author Johan Henriksson
 *
 */
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
	 * 
	 * uint8 -> int16 -> int32 -> float -> double
	 * 
	 */
	
	/** Type of data, any of TYPE_* */
	private int type;
		/** Width */
	private int w;
	/** Height */ 
	private int h;
	
	/**
	 * Get type of pixel format
	 */
	public int getType()
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
	public float[] getArrayFloat(){return arrayF;}
	public double[] getArrayDouble(){return arrayD;}
	public byte[] getArrayUnsignedByte(){return arrayB;}
	public short[] getArrayShort(){return arrayS;}
	public int[] getArrayInt(){return arrayI;}
	public BufferedImage getAWT(){return awt;}
	
	
	
	/////// Types ////////////////
	public static final int TYPE_UBYTE  =1<<1;
	public static final int TYPE_SHORT  =1<<2;
	public static final int TYPE_INT    =1<<4;

	public static final int TYPE_FLOAT  =1<<6;
	public static final int TYPE_DOUBLE =1<<7;

	public static final int TYPE_AWT    =1<<8;

	/////// Compound types ========
	public static final int TYPES_SIGNED = TYPE_SHORT | TYPE_INT | TYPE_FLOAT | TYPE_DOUBLE;
	public static final int TYPES_INTEGRAL = TYPE_UBYTE | TYPE_SHORT | TYPE_INT;
	public static final int TYPES_FP = TYPE_FLOAT | TYPE_DOUBLE;
	public static final int TYPES_ANY = TYPE_UBYTE | TYPE_SHORT | TYPE_INT | TYPE_FLOAT | TYPE_DOUBLE | TYPE_AWT;
	public static final int TYPES_ANYBUTAWT = TYPE_UBYTE | TYPE_SHORT | TYPE_INT | TYPE_FLOAT | TYPE_DOUBLE;
	
	/**
	 * Constructing an empty pixelset, shouldn't really be possible outside
	 */
	private EvPixels()
		{
		}

	/**
	 * Deep copy of image
	 */
	public EvPixels(EvPixels p)
		{
		setPixels(p);
		
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
		setPixels(awt);
		}
	
	
	/**
	 * Allocate a new pixel plane
	 */
	public EvPixels(int type, int w, int h)
		{
		allocate(type,w,h);
		}
	
	/**
	 * Is some sort of integer
	 */
	public boolean isIntegral()
		{
		return (type & TYPES_INTEGRAL) != 0;
		}
	
	/**
	 * Is signed
	 */
	public boolean isSigned()
		{
		return (type & TYPES_SIGNED) != 0;
		}
	
	/**
	 * Is some sort of floating point
	 */
	public boolean isFP()
		{
		return (type & TYPES_FP) != 0;
		}
	
	/**
	 * Convert to a different type. 
	 */
	public EvPixels convertTo(int newType, boolean readOnly)
		{
		//Only convert if needed
		if(type!=newType)
			{
			//Conversion is coded to be slow but short at the moment. Metaprogramming would help!
			if(type==TYPE_AWT)
				return helperConvertFromAwt(newType);
			else
				{
				EvPixels p;
				if(type==TYPE_INT)
					p=this;
				else
					p=helperConvertToInt();
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
	
	
	
	/**
	 * Get pixels in valid format. For performance the data will not be copied and converted unless needed. Use bit operations to put together valid types. 
	 */
	public EvPixels getReadOnly(int validTypes)
		{
		return get(validTypes, true);
		}
	
	/**
	 * Get a copy of the image in a valid format. Writing on copy will not modify the original. Use bit operations to put together valid types
	 */
	public EvPixels getWritable(int validTypes)
		{
		return get(validTypes, false);
		}

	public static String type2string(int type)
		{
		if(type==0)
			return "no type";
		else if(type==TYPE_AWT)
			return "AWT";
		else if(type==TYPE_UBYTE)
			return "ubyte";
		else if(type==TYPE_SHORT)
			return "short";
		else if(type==TYPE_INT)
			return "int";
		else if(type==TYPE_FLOAT)
			return "float";
		else if(type==TYPE_DOUBLE)
			return "double";
		else
			return "<???>";
		}
	
	/**
	 * Get pixels in suitable format
	 */
	public EvPixels get(int validTypes, boolean readOnly)
		{
		//Quick check: can current type be kept?
		//Needed to keep AWT in particular
		if((validTypes & type)!=0)
			{
			System.out.println("keep type"+type2string(type));
			if(readOnly)
				return this;
			else
				return new EvPixels(this);
			}
		
		int[] typeOrder=new int[]{TYPE_AWT,TYPE_UBYTE,TYPE_SHORT,TYPE_INT,TYPE_FLOAT,TYPE_DOUBLE};

		//Current type in list
		int curtypei=0;
		while(type!=typeOrder[curtypei])
			curtypei++;
		
		//AWT has to be treated specially
		if(type==TYPE_AWT)
			curtypei=2;
		
		//Try to upconvert to satisfy
		for(int i=curtypei;i<typeOrder.length;i++)
			if((typeOrder[i] & validTypes)!=0)
				{
				//System.out.println("Upconvert "+type2string(typeOrder[i]));
				return convertTo(typeOrder[i], readOnly);
				}
		
		//Try to downconvert. Best-effort, use the least destructible option
		for(int i=curtypei;i>=0;i--)
			if((typeOrder[i] & validTypes)!=0)
				{
				//System.out.println("Downconvert "+type2string(typeOrder[i]));
				return convertTo(typeOrder[i], readOnly);
				}

		//Sane options exhausted
		throw new RuntimeException("No types at all would fit conversion! Was the acceptable-list 0?");
		}
	
	
	
	/**
	 * Convert from AWT into any other format.
	 * Assumes it is not AWT so conversion will always be needed
	 */
	private EvPixels helperConvertFromAwt(int newType)
		{
		EvPixels p=new EvPixels();
		WritableRaster r=awt.getRaster();
		p.w=awt.getWidth();
		p.h=awt.getHeight();
		p.type=newType;
		
		EvPixels q;
		switch(newType)
			{
			case TYPE_UBYTE:
				q=helperConvertFromAwt(TYPE_INT);
				return q.helperConvertFromInt(newType);
			
			case TYPE_SHORT:
				q=helperConvertFromAwt(TYPE_INT);
				return q.helperConvertFromInt(newType);
			
			case TYPE_INT:
				p.arrayI=new int[r.getWidth()*r.getHeight()];
				r.getSamples(0, 0, r.getWidth(), r.getHeight(), 0, p.arrayI);
				return p;
				
			case TYPE_FLOAT:
				p.arrayF=new float[r.getWidth()*r.getHeight()];
				r.getSamples(0, 0, r.getWidth(), r.getHeight(), 0, p.arrayF);
				return p;
				
			case TYPE_DOUBLE:
				p.arrayD=new double[r.getWidth()*r.getHeight()];
				r.getSamples(0, 0, r.getWidth(), r.getHeight(), 0, p.arrayD);
				return p;
			
			default:
				System.out.println("Conversion error in fromawt");
			}
		throw new RuntimeException("convert from awt to "+type2string(newType)+" not supported by this function");
		}
	
	/**
	 * Convert from Int to any other format. 
	 * Assumes it is not int so conversion will always be needed
	 */
	private EvPixels helperConvertFromInt(int newType)
		{
		EvPixels p=new EvPixels();
		p.w=w;
		p.h=h;
		p.type=newType;
		System.out.println("conv int->"+type2string(newType));
		
		int[] larr=arrayI;
		if(newType==TYPE_UBYTE)
			{
			byte[] narr=new byte[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(byte)larr[i];
			p.arrayB=narr;
			return p;
			}
		else if(newType==TYPE_SHORT)
			{
			short[] narr=new short[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(short)larr[i];
			p.arrayS=narr;
			return p;
			}
		else if(newType==TYPE_FLOAT)
			{
			float[] narr=new float[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(float)larr[i];
			p.arrayF=narr;
			return p;
			}
		else if(newType==TYPE_DOUBLE)
			{
			double[] narr=new double[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(double)larr[i];
			p.arrayD=narr;
			return p;
			}
		else if(newType==TYPE_AWT)
			{
			//Can be made faster
			p.awt=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
			p.awt.getRaster().setPixels(0, 0, w, h, arrayI);
			return p;
			}
		throw new RuntimeException("convert from int to "+type2string(newType)+" not supported by this function");
		}
	
	/**
	 * Convert from int to any type.
	 * Assumes it is not int so conversion will always be needed
	 */
	private EvPixels helperConvertToInt()
		{
		//There are some local variable optimizations(?).
		int[] narr=null;
		if(type==TYPE_UBYTE)
			{
			byte[] larr=arrayB;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(int)(larr[i] & 0xff); //Kill off sign
			}
		else if(type==TYPE_SHORT)
			{
			short[] larr=arrayS;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=larr[i];
			}
		else if(type==TYPE_FLOAT)
			{
			float[] larr=arrayF;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(int)larr[i];
			}
		else if(type==TYPE_DOUBLE)
			{
			double[] larr=arrayD;
			narr=new int[larr.length];
			for(int i=0;i<larr.length;i++)
				narr[i]=(int)larr[i];
			}
		else if(type==TYPE_AWT)
			{
			WritableRaster r=awt.getRaster();
			narr=new int[awt.getWidth()*awt.getHeight()];
			r.getSamples(0, 0, awt.getWidth(), awt.getHeight(), 0, narr); //exist for more types
			}
		
		
		if(narr!=null)
			{
			EvPixels p=new EvPixels();
			p.arrayI=narr;
			p.type=TYPE_INT;
			p.w=w;
			p.h=h;
			return p;
			}
		else
			throw new RuntimeException("convert to int from "+type2string(type)+" not supported by this function");
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

		if(arrayB!=null)
			{
			arrayB=new byte[im.arrayB.length];
			for(int i=0;i<arrayB.length;i++)
				arrayB[i]=im.arrayB[i];
			}

		if(arrayS!=null)
			{
			arrayS=new short[im.arrayS.length];
			for(int i=0;i<arrayS.length;i++)
				arrayS[i]=im.arrayS[i];
			}

		if(arrayI!=null)
			{
			arrayI=new int[im.arrayI.length];
			for(int i=0;i<arrayI.length;i++)
				arrayI[i]=im.arrayI[i];
			}

		if(arrayF!=null)
			{
			arrayF=new float[im.arrayF.length];
			for(int i=0;i<arrayF.length;i++)
				arrayF[i]=im.arrayF[i];
			}

		if(arrayD!=null)
			{
			arrayD=new double[im.arrayD.length];
			for(int i=0;i<arrayD.length;i++)
				arrayD[i]=im.arrayD[i];
			}
		
		if(awt!=null)
			{
			BufferedImage bim=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
			bim.getGraphics().drawImage(awt, 0, 0, null);
			awt=bim;
			}
		
		}

	
	/**
	 * Set type and allocate space for image. All pixels will be set to 0
	 */
	public void allocate(int type, int w, int h)
		{
		int s=w*h;
		this.w=w;
		this.h=h;
		this.type=type;
		switch(type)
			{
			case TYPE_UBYTE:
				arrayB=new byte[s];
				break;
			case TYPE_SHORT:
				arrayS=new short[s];
				break;
			case TYPE_INT:
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
		return convertTo(EvPixels.TYPE_AWT, true).getAWT();
		}

	public String asciiImage()
		{
		StringBuffer sb=new StringBuffer();
		if(type==TYPE_INT)
			{
			for(int i=0;i<getHeight();i++)
				{
				for(int j=0;j<getWidth();j++)
					sb.append(arrayI[i*getWidth()+j]+"\t");
				sb.append("\n");
				}
			}
		else if(type==TYPE_DOUBLE)
			{
			for(int i=0;i<getHeight();i++)
				{
				for(int j=0;j<getWidth();j++)
					sb.append(arrayD[i*getWidth()+j]+"\t");
				sb.append("\n");
				}
			}
		return sb.toString();
		}
	
	/*
	public static void main(String[] arg)
		{
		EvPixels p=new EvPixels(EvPixels.TYPE_INT, 200, 100);
		int[] arr=p.getArrayInt();
		arr[5]=1;
		EvPixels q=p.getReadOnly(TYPE_AWT);
		
		//but what if I want a copy of a different type? why the need to duplicate *and* convert?
		
		}
	*/
	}
