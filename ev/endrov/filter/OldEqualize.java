package endrov.filter;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.ShortLookupTable;

//used to be in movie encoder

public class OldEqualize
	{
	
	/**
	 * Equalize gray scale image
	 */
	public BufferedImage equalize(BufferedImage source)
		{
		int[] pcount=new int[256];
		Raster raster=source.getRaster();
		int[] pixels=new int[raster.getWidth()];
		int max=1;
		for(int y=0;y<raster.getHeight();y++)
			{
			raster.getSamples(0, y, raster.getWidth(), 1, 0, pixels);
			for(int p:pixels)
				pcount[p]++;
			}

		int nump=(int)(raster.getWidth()*raster.getHeight()*0.01);
		for(max=255;max>=1 & nump>0;max--)
			nump-=pcount[max];
		max++;
		
		short[] table = new short[256];
		for (int i = 0; i < 256; i++)
			{
			table[i] = (short) (i*255/max);
			if(table[i]>255)
				table[i]=255;
			}
		BufferedImage dest = new BufferedImage(source.getWidth(),source.getHeight(),source.getType());
		LookupTable look = new ShortLookupTable(0,table);
		BufferedImageOp buff = new LookupOp(look,null);
		buff.filter(source, dest);
		return dest;
		}
	}
