package endrov.unsortedImageFilters;

import endrov.flow.std.math.OpImageMulImage;
import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.imageMath.ImageMath;

/**
 * 
 * Geometric momentum, defined as m_pq=sum_ij x^p y^q f(x,y), can be used for shape recognition and localization.
 * 
 * Central moment is calculated as: mu_pq=sum_ij (x-x_0)^p (y-y_0)^q f(x,y)   with (x_0,y_0)=(m_10,m_01)/m_00, that is, the geometric center.
 * 
 * See "Moment functions in image analysis", R Makundan, K R Ramakrishnan
 * 
 * Examples:
 * 
 * m_pq/(m_00^((p+q+2)/2)) is scale invariant
 * mu_pq/(mu_00^((p+q+2)/2)) is scale and translation invariant
 * 
 * rotation invariants exists.
 * 
 * @author Johan Henriksson
 *
 */
public class GeometricMomentum
	{
	/**
	 * Calculate momentum: m_pq=sum_ij x^p y^q f(x,y)
	 * 
	 * TODO special faster versions can be deployed if image is binary
	 * 
	 * Complexity O(w*h*(p+q))
	 */
	public static double momentum(EvPixels in, int p, int q)
		{
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels pq=GenerateSpecialImage.genXpYp(w, h, p, q);
		
		return ImageMath.sum(new OpImageMulImage().exec(pq, in));
		
		/*
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		int[] inPixels=in.getArrayInt();
		
		int sum=0;
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int xp;
				int yp;
				
				sum+=xp*yp*inPixels[in.getPixelIndex(ax, ay)];
				
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				int area=(tox-fromx)*(toy-fromy);
				outPixels[out.getPixelIndex(ax, ay)]=CumSum.integralFromCumSum(cumsum, fromx, tox, fromy, toy)/(int)area;
				}
			}
		return out;*/
		}
	
	
	/*
	 * Other types of momentum:
	 * ComplexMoment, LegendreMoment, ZernikeMoment
	 */
	
	}
