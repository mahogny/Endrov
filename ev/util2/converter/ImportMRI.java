/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.converter;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;

import javax.imageio.ImageIO;

/**
 * Some code I used to import the MRI example set on www.endrov.net.
 * Does not depend on EV
 * @author Johan Henriksson
 */
public class ImportMRI
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		int w=256, h=256, d=109;
		
		File outdir=new File("/home/mahogny/_imagedata/mrbrain/mrbrain-foo/00000000/");
		outdir.mkdirs();
		
		try
			{
			DataInputStream di=new DataInputStream(new FileInputStream(new File("/home/mahogny/Desktop/gfx articles/data/MRbrain")));
			int[] pix=new int[w];
			
			for(int z=0;z<d;z++)
				{
				BufferedImage bim=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
				WritableRaster r=bim.getRaster();
				
				for(int y=0;y<h;y++)
					{
					for(int x=0;x<w;x++)
						{
						//int s=(di.readShort()&0xFF)<<2;
						int s=di.read();
						int t=di.read();
						s=t<<8 | s;
						pix[x]=s>>4;
						//System.out.println(""+s);
						}
					r.setPixel(0, y, pix);
					r.setSamples(0, y, w, 1, 0, pix);
					}
				System.out.println("z:"+z);
				
				String zs=""+z;
				while(zs.length()<8)
					zs="0"+zs;
				zs+=".png";
				
				ImageIO.write(bim, "png", new File(outdir,zs));
				}
			
			
			
			di.close();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		
		
		}

	}
