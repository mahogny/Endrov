package endrov.windowPlateAnalysis.scene;

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

import endrov.core.log.EvLog;
import endrov.gui.EvColor;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;

/**
 * Scene element: One raster image
 */
public class Scene2DImage implements Scene2DElement
	{
	public int x, y;
	public double resX=1;
	public double resY=1;
//	CoordinateSystem cs=new CoordinateSystem();
	public EvPixels pixels=null;
	public double contrast=1;
	public double brightness=0;
	private BufferedImage bufi=null;
	public EvColor color=EvColor.white;
	
	public EvColor borderColor;
	
	
	
	public BufferedImage applyIntensityTransform(EvPixels p)
		{
		double contrastR=contrast*color.getRedDouble();
		double contrastG=contrast*color.getGreenDouble();
		double contrastB=contrast*color.getBlueDouble();
		
		int w=p.getWidth();
		int h=p.getHeight();
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
			}

		buf.getRaster().setDataElements(0, 0, w, h, outarr);
		return buf;
		}
	
	
	
	public void update()
		{
		bufi=null;
		}

	/**
	 * Load image into memory
	 */
	public void prepareImage()
		{
		try
			{
			//Load image if this has not already been done
			if(pixels==null)
				bufi=null;
			else
				{
				if(bufi==null)
					{
					bufi=applyIntensityTransform(pixels);
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
		prepareImage(); 


		g2.translate(x,y);
		g2.scale(resX, resY);  


		if(bufi!=null)
			{
			g2.drawImage(bufi, null, 0, 0);
			}

		if(borderColor!=null)
			{
			int w=pixels.getWidth();
			int h=pixels.getHeight();

			//Reference area. This is what the transform decides on; the image above should be in it
			g2.setColor(borderColor.getAWTColor()); //No displacement
			g2.drawRect(0, 0, w, h);
			}



		g2.scale(1.0/resX, 1.0/resY);  
		g2.translate(-x,-y);

		}


	public Vector2d transformImageWorld(Vector2d u)
		{
		return new Vector2d(u.x*resX+x, u.y*resY+y);
		}

	public Vector2d transformWorldImage(Vector2d u)
		{
		return new Vector2d((u.x-x)/resX, (u.y-y)/resY);
		}

	/** 
	 * Convert image coordinate to screen coordinate (image scaled by binning) 
	 */
	public Vector2d transformI2S(Scene2DView p, Vector2d u)
		{
		return p.transformPointW2S(transformImageWorld(new Vector2d(u.x,u.y)));
		}
	
	
	
	/** 
	 * Convert screen coordinate to image coordinate (image scaled by binning) 
	 */
	public Vector2d transformS2I(Scene2DView p, Vector2d u)
		{
		return transformWorldImage(p.transformPointS2W(u));
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
		if(pixels!=null)
			{
			int w=pixels.getWidth();
			int h=pixels.getHeight();
				
			Vector2d v1=transformImageWorld(new Vector2d(0,0));
			Vector2d v2=transformImageWorld(new Vector2d(w,h));
			
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
	 *
	 */
	/*
	public void zoomToFit(Scene2DView p)
		{
		int w=pixels.getWidth();
		int h=pixels.getHeight();
					
		//Adjust zoom
		double zoom1=p.getWidth()/(double)(w*resX);
		double zoom2=p.getHeight()/(double)(h*resY);
		p.zoom=Math.min(zoom1,zoom2);

		//Place camera in the middle
		Vector2d mid=transformI2S(p,new Vector2d(w/2,h/2));
		mid.sub(new Vector2d(p.getWidth()/2,p.getHeight()/2));
		p.transX-=(int)mid.x/p.zoom;
		p.transY-=(int)mid.y/p.zoom;
		}

*/

	private void invalidate()
		{
		bufi=null;
		}



	public void setContrastBrightness(double contrast, double brightness)
		{
		this.contrast=contrast;
		this.brightness=brightness;
		invalidate();
		}



	
	}