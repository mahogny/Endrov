package endrov.flowGenerateImage;

import endrov.flow.std.math.EvOpImageMulImage;
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
				out=new EvOpImageMulImage().exec1(out, mul);
			}
		if(q>0)
			{
			EvPixels mul=GenerateSpecialImage.genIncY(w, h);
			for(int i=0;i<q;i++)
				out=new EvOpImageMulImage().exec1(out, mul);
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
	 * Return an image of given size: im(x,y)=c
	 * c c c
	 * c c c
	 * c c c
	 */
	public static EvPixels genConstant(int w, int h, double c)
		{
		EvPixels p=new EvPixels(EvPixels.TYPE_DOUBLE,w,h);
		double[] aPixels=p.getArrayDouble();
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
	 * 2D Gaussian function placed in the middle. Follows the equation with a cut-off at the border so it might
	 * not be 100% normalized but closed to for a large kernel
	 */
	public static EvPixels genGaussian2D(int w, int h, double sigmaX, double sigmaY)
		{
		EvPixels p=new EvPixels(EvPixels.TYPE_DOUBLE,w,h);
		double[] aPixels=p.getArrayDouble();
		
		double mul1=1/(sigmaX*Math.sqrt(2*Math.PI)) * 1/(sigmaY*Math.sqrt(2*Math.PI));
		double mul2x=-1/(2*sigmaX*sigmaX);
		double mul2y=-1/(2*sigmaY*sigmaY);
		
		double midx=w/2;
		double midy=h/2;
		
		//If exp is expensive then it need only be O(max(w,h)) times.
		//This comes at others costs however
		
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			double dy2=y-midy;
			dy2=dy2*dy2*mul2y;
			for(int x=0;x<w;x++)
				{
				double dx2=x-midx;
				dx2=dx2*dx2*mul2x;
				aPixels[base+x]=mul1*Math.exp(dx2+dy2);
				}
			}
		return p;
		}

	
	/**
	 * 3D Gaussian function placed in the middle. Follows the equation with a cut-off at the border so it might
	 * not be 100% normalized but closed to for a large kernel
	 */
	public static EvStack genGaussian3D(double sigmaX, double sigmaY, double sigmaZ, int w, int h, int d)
		{
		EvStack s=new EvStack();
		s.setTrivialResolution();
		
		double mul1=1/(sigmaX*Math.sqrt(2*Math.PI)) * 1/(sigmaY*Math.sqrt(2*Math.PI)) * 1/(sigmaZ*Math.sqrt(2*Math.PI));
		double mul2x=-1/(2*sigmaX*sigmaX);
		double mul2y=-1/(2*sigmaY*sigmaY);
		double mul2z=-1/(2*sigmaZ*sigmaZ);
		
		double midx=EvStack.calcMidWidth(w);
		double midy=EvStack.calcMidWidth(h);
		double midz=EvStack.calcMidWidth(d);
		
		//Could generate a single plane and multiply by Math.exp(mul2 dz2) if it makes any difference
		
		for(int curd=0;curd<d;curd++)
		//int curd=0;
		//for(EvDecimal decd:template.keySet())
			{
			EvPixels p=new EvPixels(EvPixels.TYPE_DOUBLE,w,h);
			double[] aPixels=p.getArrayDouble();
			
			double dz2=curd-midz;
			dz2=dz2*dz2*mul2z;
			
			for(int y=0;y<h;y++)
				{
				int base=y*w;
				double dy2=y-midy;
				dy2=dy2*dy2*mul2y;
				double dyz2=dy2+dz2;
				for(int x=0;x<w;x++)
					{
					double dx2=x-midx;
					dx2=dx2*dx2;
					aPixels[base+x]=mul1*Math.exp(mul2x*dx2+dyz2);
					}
				}
			
			s.put(curd, new EvImage(p));
//			s.put(decd, new EvImage(p));
			//curd++;
			}
		
		return s;
		}
	
	
	/**
	 * Common kernels? laplace etc
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
