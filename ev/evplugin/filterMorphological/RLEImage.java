package evplugin.filterMorphological;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * Binary image in RLE form. Implementation of algorithms described in
 * "Fast Algorithms for Binary Dilation and Erosion Using Run-Length Encoding"
 * - Wook-Joong Kim, Seong-Dae Kim, and Kyuheon Kim
 */
public class RLEImage
	{
	private static class RLEstrip
		{
		int start, stop;
		}

	private HashMap<Integer,List<RLEstrip>> strips=new HashMap<Integer, List<RLEstrip>>();
	
	public RLEImage(){}

	/** Generate from image */
	public RLEImage(BufferedImage im)
		{
		WritableRaster r=im.getRaster();
		int width=r.getWidth();
		int h=r.getHeight();
		int[] pix=new int[width];
		for(int y=0;y<h;y++)
			{
			r.getSamples(0, y, width, 1, 0, pix);
			readStrip(pix,y,width,0);
			}
		}
	
	/** Turn a [] kernel into a [][] kernel directly */
	public RLEImage(int[] kernel, int width, int centerx, int centery)
		{
		this(split(kernel,width), centerx, centery);
		}
	private static int[][] split(int[] kernel, int width)
		{
		int[][] kernel2=new int[kernel.length/width][width];
		for(int y=0;y<kernel.length;y++)
			for(int x=0;x<width;x++)
				kernel2[y][x]=kernel[y*width+x];
		return kernel2;
		}

	/** Build from kernel */
	public RLEImage(int[][] kernel, int centerx, int centery)
		{
		int width=kernel[0].length;
		int h=kernel.length;
		for(int y=0;y<h;y++)
			readStrip(kernel[y],y-centery,width,centerx);
		}
	
	private void readStrip(int[] pix, int y, int width, int centerx)
		{
		List<RLEstrip> line=new LinkedList<RLEstrip>();
		strips.put(y,line);
		for(int x=0;x<width;)
			{
			if(pix[x]>0)
				{
				RLEstrip strip=new RLEstrip();
				strip.start=x-centerx;
				while(x<width && pix[x]>0)
					x++;
				strip.stop=x-1-centerx;
				line.add(strip);
				}
			else
				x++;
			}
		}
	
	/** Dilate this (+) s and return new result */
	public RLEImage dilate(RLEImage s)
		{
		RLEImage tot=new RLEImage();
		for(Map.Entry<Integer, List<RLEstrip>> zstrip:strips.entrySet())
			{
			for(Map.Entry<Integer, List<RLEstrip>> sstrip:s.strips.entrySet())
				{
				int yd=zstrip.getKey()+sstrip.getKey();
				List<RLEstrip> newstrips=tot.strips.get(yd);
				if(newstrips==null)
					tot.strips.put(yd, newstrips=new Vector<RLEstrip>());
				for(RLEstrip zs:zstrip.getValue())
					for(RLEstrip ss:sstrip.getValue())
						{
						RLEstrip newstrip=new RLEstrip();
						newstrip.start=zs.start+ss.start;
						newstrip.stop=zs.stop+ss.stop;
						insert(newstrips,newstrip);
						}
				}
			}
		return tot;
		}
	private void insert(List<RLEstrip> strip, RLEstrip s)
		{
		strip.add(s); //should be optimized, sorted insert & replace. then use linked list.
		}
	
	}
