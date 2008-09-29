package endrov.recording;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Image from camera
 * 
 * should later be changed to an EV image
 * 
 * @author Johan Henriksson
 *
 */
public class CameraImage
	{
	public int w,h;
	public int bytesPerPixel;
	public Object pixels; //byte[] or short[]
	
	public String toString()
		{
		return ""+w+" x "+h;
		}
	
	/**
	 * Make AWT image out of input
	 */
	public BufferedImage getAWT()
		{
		//MM DOES NOT SUPPORT COLOR!!!
		
		BufferedImage im=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r=im.getRaster();
		for(int y=0;y<h;y++)
			{
			//I'm sure this can be made faster, low-level if not otherwise
			int p[]=new int[w];
			if(bytesPerPixel==1)
				{
				byte[] in=(byte[])pixels;
				for(int x=0;x<w;x++)
					p[x]=in[x];
				}
			else if(bytesPerPixel==2)
				{
				short[] in=(short[])pixels;
				for(int x=0;x<w;x++)
					p[x]=in[x];
				}
			r.setPixels(0, y, w, 1, p);
			}
		
		
		return im;
		}
	
	}
