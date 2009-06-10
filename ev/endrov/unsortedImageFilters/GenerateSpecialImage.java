package endrov.unsortedImageFilters;

import endrov.flow.std.math.OpImageMulImage;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;

/**
 * Generate special images useful for calculations
 * 
 * @author Johan Henriksson
 *
 */
public class GenerateSpecialImage
	{

	/**
	 * Return an image of given size: im(x,y)=x^p*y^q.
	 * Requires p,q>=0
	 */
	public static EvPixels genXpYp(int w, int h, int p, int q)
		{
		EvPixels out=GenerateSpecialImage.genConstant(w, h, 1);
		
		if(p>0)
			{
			EvPixels mul=GenerateSpecialImage.genIncX(w, h);
			for(int i=0;i<p;i++)
				out=new OpImageMulImage().exec1(out, mul);
			}
		if(q>0)
			{
			EvPixels mul=GenerateSpecialImage.genIncY(w, h);
			for(int i=0;i<q;i++)
				out=new OpImageMulImage().exec1(out, mul);
			}
		return out;
		}

	/**
	 * Return an image of given size: im(x,y)=c
	 * c c c
	 * c c c
	 * c c c
	 */
	public static EvPixels genConstant(int w, int h, int c)
		{
		EvPixels p=new EvPixels(EvPixels.TYPE_INT,w,h);
		int[] aPixels=p.getArrayInt();
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			for(int x=0;x<w;x++)
				aPixels[base+x]=c;
			}
		return p;
		}

	/**
	 * Return an image of given size: im(x,y)=y
	 * 0 0 0
	 * 1 1 1
	 * 2 2 2
	 */
	public static EvPixels genIncY(int w, int h)
		{
		EvPixels p=new EvPixels(EvPixels.TYPE_INT,w,h);
		int[] aPixels=p.getArrayInt();
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			for(int x=0;x<w;x++)
				aPixels[base+x]=y;
			}
		return p;
		}

	/**
	 * Return an image of given size: im(x,y)=x
	 * 0 1 2
	 * 0 1 2
	 * 0 1 2
	 */
	public static EvPixels genIncX(int w, int h)
		{
		EvPixels p=new EvPixels(EvPixels.TYPE_INT,w,h);
		int[] aPixels=p.getArrayInt();
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			for(int x=0;x<w;x++)
				aPixels[base+x]=x;
			}
		return p;
		}
	
	
	
	/**
	 * Common kernels? gaussian, laplace etc
	 */

	
	
	/**
	 * Copy an image over several focal planes. Adapts size according to a template stack, likely
	 * the stack the new stack will be combined with 
	 */
	public static EvStack repeatImageZ(EvImage im, EvStack template)
		{
		EvStack s=new EvStack();
		s.getMetaFrom(template);
		for(EvDecimal d:template.keySet())
			s.put(d, im.makeShadowCopy());
		return s;
		}
	
	
	/**
	 * Copy an image over several focal planes. Adapts size according to a template stack, likely
	 * the stack the new stack will be combined with 
	 */
	public static EvStack repeatImageZ(EvPixels p, EvStack template)
		{
		EvImage im=new EvImage();
		im.setPixelsReference(p);
		return repeatImageZ(im, template);
		}
	
	
	
	}
