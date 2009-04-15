package endrov.unsortedImageFilters;

import endrov.imageset.EvPixels;

public class Convolve
	{

	/**
	 * Convolution
	 * 
	 * Complexity O(w*h*kw*kh)
	 * 
	 */
	/*
	public static EvPixels convolve(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		//List<Vector2i> kpos=kernelPos(kernel, kcx, kcy);
		
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				int sum=0;
				
				
				
				
				boolean found=true;
				find: for(Vector2i v:kpos)
					{
					int kx=v.x+ax;
					int ky=v.y+ay;
					if(kx>=0 && kx<w && ky>=0 && ky<h)
						{
						if(inPixels[in.getPixelIndex(kx, ky)]==0)
							{
							found=false;
							break find;
							}
						}
					else
						{
						found=false;
						break find;
						}
					}
				int i=out.getPixelIndex(ax, ay);
				if(found)
					outPixels[i]=1;
				else
					outPixels[i]=0;
				}
		
		return out;
		
		
		}*/
	
	}
