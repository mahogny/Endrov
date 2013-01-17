package endrov.plateWindow.scene;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.Raster;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import javax.vecmath.Vector2d;

import endrov.basicWindow.EvColor;
import endrov.ev.EvLog;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Scene element: One raster image
 */
public class Scene2DStack implements Scene2DElement
	{
	int z;
	private EvStack stack=null;
	public double contrast=1;
	public double brightness=0;
	private BufferedImage bufi=null;
	public EvColor color=EvColor.white;
	
	public EvColor borderColor;
	
	
	
	public BufferedImage applyIntensityTransform(EvPixels p)
		{
//		System.out.println("here4");
		double contrastR=contrast*color.getRedDouble();
		double contrastG=contrast*color.getGreenDouble();
		double contrastB=contrast*color.getBlueDouble();
		
	//	System.out.println("here5");
		int w=p.getWidth();
		int h=p.getHeight();
	//	System.out.println("---"+w+" "+h);			
		double[] aPixels=p.convertToDouble(true).getArrayDouble();
		BufferedImage buf=new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
		
		byte[] outarr=new byte[w*h*3];
		
		for(int i=0;i<w*h;i++)
			{
			byte b=clampByte((int)(aPixels[i]*contrastB+brightness));
			byte g=clampByte((int)(aPixels[i]*contrastG+brightness));
			byte r=clampByte((int)(aPixels[i]*contrastR+brightness));
			outarr[i*3+0]=r;
			outarr[i*3+1]=g;
			outarr[i*3+2]=b;
//				System.out.println("   "+r+" "+g+" "+b);
			}

		buf.getRaster().setDataElements(0, 0, w, h, outarr);
		return buf;
		}
	
	
	
	
	
	
	
	public void setImage(EvStack stack, int z)//EvImage image)
		{
		this.z=z;
		this.stack=stack;
		}
	
	

	public EvStack getStack()
		{
		return stack;
		}
	
	public void update()
		{
		bufi=null;
		}

	/**
	 * Load image into memory
	 */
	public void loadImage()
		{
		try
			{
			if(stack==null)
				bufi=null;
			else
				{
				//Load image if this has not already been done
				if(bufi==null)
					{
					EvImage image=stack.getInt(z);
		
					//TODO handle progress. Put this in a thread if it takes too long. or, put it there right away, postpone update of image
					if(image!=null)
						{
						EvPixels p=image.getPixels(new ProgressHandle()); /////////////////////////////////////////// HERE FOR LONG EXECUTIONS /////////////
						bufi=applyIntensityTransform(p);
						}
					else
						System.out.println("Null image, z:"+z);
					}
				}
			}
		catch(Exception e)
			{
			EvLog.printError("image panel: image failed to load",e);
			}
		}
	
	
	
	public void paintComponent(Graphics g, Scene2DView p)
		{
		Graphics2D g2 = (Graphics2D)g; 			
		if(bufi!=null)
			{

			g2.scale(stack.resX, stack.resY);  
			g2.drawImage(bufi, null, 0, 0);
			g2.scale(1.0/stack.resX, 1.0/stack.resY);  
			} 
		
		if(borderColor!=null && stack!=null)
			{
			int w=stack.getWidth();
			int h=stack.getHeight();
			
			//Reference area. This is what the transform decides on; the image above should be in it
			g2.setColor(borderColor.getAWTColor()); //No displacement
			//actually already displaced. red is double-displaced
			Vector2d u1=transformI2S(p,stack,new Vector2d(0,0));
			Vector2d u2=transformI2S(p,stack,new Vector2d(w,0));
			Vector2d u3=transformI2S(p,stack,new Vector2d(0,h));
			Vector2d u4=transformI2S(p,stack,new Vector2d(w,h));
			g2.drawLine((int)u1.x, (int)u1.y, (int)u2.x, (int)u2.y);
			g2.drawLine((int)u3.x, (int)u3.y, (int)u4.x, (int)u4.y);
			g2.drawLine((int)u1.x, (int)u1.y, (int)u3.x, (int)u3.y);
			g2.drawLine((int)u2.x, (int)u2.y, (int)u4.x, (int)u4.y);
			}

		
		}
	
	


	/** 
	 * Convert image coordinate to screen coordinate (image scaled by binning) 
	 */
	public static Vector2d transformI2S(Scene2DView p, EvStack stack, Vector2d u)
		{
		return p.transformPointW2S(stack.transformImageWorld(new Vector2d(u.x,u.y)));
		}
	
	
	
	/** 
	 * Convert screen coordinate to image coordinate (image scaled by binning) 
	 */
	public static Vector2d transformS2I(Scene2DView p, EvStack stack, Vector2d u)
		{
		return stack.transformWorldImage(p.transformPointS2W(u));
		}
	
	
	
	
	
	
	static final byte clampByte(int i)
		{
		if(i > 255)
			return -1; //really correct? why -1????
		if(i < 0)
			return 0;
		else
			return (byte)i;
		}
	
	private static final byte clampShort(int i)
		{
		if(i > 65535)
			return -1; //really correct?
		if(i < 0)
			return 0;
		else
			return (byte)i;
		}
	
	static EvPixels applyOrig(EvPixels a, double contrast, double brightness)
		{
		if(a.getType()==EvPixelsType.AWT)
			{
			BufferedImage src=a.getAWT();
			
			int numBits=src.getSampleModel().getSampleSize(0);
			LookupOp f;
			
			if(numBits==8)
				{
				byte[] b=new byte[256];
				for(int i=0;i<256;i++)
					b[i]=clampByte((int)(i*contrast+brightness));     //Centralize contrast* maybe?
				ByteLookupTable table=new ByteLookupTable(0,b);
				f=new LookupOp(table,null);
				}
			else if(numBits==16)
				{
				short[] b=new short[65536];
				for(int i=0;i<65536;i++)
					b[i]=clampShort((int)(i*contrast+brightness));     //Centralize contrast* maybe?
				ShortLookupTable table=new ShortLookupTable(0,b);
				f=new LookupOp(table,null);
				
				}
			else
				f=null;
			
			WritableRaster wr = Raster.createWritableRaster(src.getSampleModel(),new Point(0,0));
			BufferedImage bufo = new BufferedImage(src.getColorModel(),wr,true,new Hashtable<Object,Object>());
			f.filter(src,bufo);
			
			return new EvPixels(bufo);
			}
		else
			{
			a=a.getReadOnly(EvPixelsType.DOUBLE);
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(a.getType(),w,h);
			double[] aPixels=a.getArrayDouble();
			double[] outPixels=out.getArrayDouble();
			
			for(int i=0;i<aPixels.length;i++)
				{
				double c=aPixels[i]*contrast+brightness;
				if(c>255) c=255;
				else if(c<0) c=0;
				outPixels[i]=c;
				}
			
			return out;
			}
		}







	public Rectangle getBoundingBox()
		{
		if(stack!=null)
			{
			int w=stack.getWidth();
			int h=stack.getHeight();
				
			Vector2d v1=stack.transformImageWorld(new Vector2d(0,0));
			Vector2d v2=stack.transformImageWorld(new Vector2d(w,h));
			
			double x1=Math.min(v1.x, v2.x);
			double y1=Math.min(v1.y, v2.y);
			double x2=Math.max(v1.x, v2.x);
			double y2=Math.max(v1.y, v2.y);
			
			Rectangle r=new Rectangle((int)x1,(int)y1, (int)(x2-x1), (int)(y2-y1));
			
			return r;
			}
		else
			return null;
		}


	/**
	 * Zoom image to fit panel
	 */
	/*
	public void zoomToFit(Scene2DView p)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
					
		//Adjust zoom
		double zoom1=p.getWidth()/(double)(w*stack.resX);
		double zoom2=p.getHeight()/(double)(h*stack.resY);
		p.zoom=Math.min(zoom1,zoom2);

		//Place camera in the middle
		Vector2d mid=transformI2S(p,stack,new Vector2d(w/2,h/2));
		mid.sub(new Vector2d(p.getWidth()/2,p.getHeight()/2));
		p.transX-=(int)mid.x/p.zoom;
		p.transY-=(int)mid.y/p.zoom;
		}
	*/
	}