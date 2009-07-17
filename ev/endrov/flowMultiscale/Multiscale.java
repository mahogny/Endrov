package endrov.flowMultiscale;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.EvListUtil;

/**
 * Tools for working with MRA/MSA
 * 
 * @author Johan Henriksson
 *
 */
public class Multiscale
	{

	/**
	 * Find scale of feature by maximizing RickerWavelet(sigma) at a given point.
	 * Uses DoG approximation.<br/><br/>
	 * 
	 * Uses several 2^-series around the guessed sigma, supposedly very fast 
	 */
	public static double findFeatureScale(EvPixels p, double sigmaGuess, int x, int y)
		{
		p=p.getReadOnly(EvPixelsType.DOUBLE);
		SortedMap<Double,Double> dog=new TreeMap<Double, Double>(); //sigma -> dog

		/*
		findFeatureScale(p, sigmaGuess, x, y, dog);
		findFeatureScale(p, sigmaGuess*0.75, x, y, dog);
		findFeatureScale(p, sigmaGuess*(1+0.25*0.25), x, y, dog);
		findFeatureScale(p, sigmaGuess*(1+0.25*0.25*0.25), x, y, dog);
		findFeatureScale(p, sigmaGuess*(1+0.25*0.25*0.25*0.25), x, y, dog);
		*/
		for(double mul=0.8;mul<1.2;mul+=0.025)
			findFeatureScale(p, sigmaGuess*mul, x, y, dog);
		//An iterative method might be better
		//interval 8-halving?
		
		
		/*
		findFeatureScale(p, sigmaGuess*0.85, x, y, dog);
		findFeatureScale(p, sigmaGuess*0.95, x, y, dog); //
		findFeatureScale(p, sigmaGuess*1.00, x, y, dog);
		findFeatureScale(p, sigmaGuess*1.05, x, y, dog); //
		findFeatureScale(p, sigmaGuess*1.10, x, y, dog);
		findFeatureScale(p, sigmaGuess*1.15, x, y, dog); //
		findFeatureScale(p, sigmaGuess*1.20, x, y, dog);
		findFeatureScale(p, sigmaGuess*1.25, x, y, dog); //
		findFeatureScale(p, sigmaGuess*1.30, x, y, dog);
		findFeatureScale(p, sigmaGuess*1.37, x, y, dog); //
		findFeatureScale(p, sigmaGuess*1.45, x, y, dog);
*/
		
		//System.out.println("map: "+dog);
		return EvListUtil.getKeyOfMax(dog);
//		return EvMathUtil.findXforMaxY(dog);
		
		/*
		double[] arrsigma=new double[4];
		for(int i=-2;i<2;i++)
			arrsigma[i+2]=Math.pow(2,i)*sigmaGuess;
		
		double[] conv=new double[arrsigma.length];
		for(int i=0;i<arrsigma.length;i++)
			conv[i]=convolveGaussPoint2D(p, arrsigma[i], arrsigma[i],x,y);
		double[] dog=new double[arrsigma.length-1];
		for(int i=0;i<arrsigma.length-1;i++)
			{
			dog[i]=conv[i]-conv[i+1];
			dog2.put(conv[i],dog[i]);
			
			System.out.println("sigma "+arrsigma[i]+" => "+dog[i]);
			
			}
		

		//Can interpolate cubic to find better value
		return arrsigma[EvListUtil.getIndexOfMax(dog)];
		*/
		
		
		}
	public static void findFeatureScale(EvPixels p, double sigmaGuess, int x, int y, Map<Double,Double> dog)
		{
		double[] arrsigma=new double[4];
		for(int i=-2;i<2;i++)
			arrsigma[i+2]=Math.pow(2,i)*sigmaGuess;
		
		double[] conv=new double[arrsigma.length];
		for(int i=0;i<arrsigma.length;i++)
			conv[i]=convolveGaussPoint2D(p, arrsigma[i], arrsigma[i],x,y);
		for(int i=0;i<arrsigma.length-1;i++)
			dog.put(arrsigma[i],conv[i]-conv[i+1]);
		}
	
	

	
	/**
	 * Find scale of feature by maximizing RickerWavelet(sigma) at a given point.
	 * Uses DoG approximation.<br/><br/>
	 * 
	 * Tries several sigma (sigmaDiv) within a range, then recursively looks within subintervals, numIt times.
	 */
	public static double findFeatureScale2(EvPixels p, int x, int y, double minSigma, double maxSigma, int sigmaDiv, int numIt)
		{
		double[] sigmaArr=new double[sigmaDiv];
		double[] dogArr=new double[sigmaDiv];

		int maxIndex=0;
		for(int i=0;i<sigmaDiv;i++)
			{
			double sigma=(maxSigma-minSigma)*i/(sigmaDiv-1.0) + minSigma;
			sigmaArr[i]=sigma;
			dogArr[i]=convolveGaussPoint2D(p, sigma, sigma,x,y) - convolveGaussPoint2D(p, sigma*2, sigma*2,x,y);
			if(i==0 || dogArr[maxIndex]<dogArr[i])
				maxIndex=i;
			}
		if(numIt>0)
			{
			//Recurse
			int fromi=Math.max(0, maxIndex-1);
			int toi=Math.min(sigmaDiv-1, maxIndex+1);
			return findFeatureScale2(p, x, y, sigmaArr[fromi], sigmaArr[toi], sigmaDiv, numIt-1);
			}
		else
			{
			//Done
			return sigmaArr[maxIndex];
			}
		
		}
	
	/**
	 * Convolve with a gaussian at a single point
	 */
	public static double convolveGaussPoint2D(EvPixels p, double sigmaX, double sigmaY, double midx, double midy)
		{
		p=p.getReadOnly(EvPixelsType.DOUBLE);
		double[] arr=p.getArrayDouble();
		int w=p.getWidth();
		int h=p.getHeight();

		double sum=0;
		double conv=0;
		
		int extentX=(int)Math.round(3*sigmaX);
		extentX=Math.max(extentX, 2);
		int extentY=(int)Math.round(3*sigmaY);
		extentY=Math.max(extentY, 2);
		
		double mul2x=-1/(2*sigmaX*sigmaX);
		double mul2y=-1/(2*sigmaY*sigmaY);

		
		int sx=Math.max(0, (int)(midx-extentX));
		int ex=Math.min(w,(int)(midx+extentX+1)); //+1 to the right?
		int sy=Math.max(0, (int)(midy-extentY));
		int ey=Math.min(h,(int)(midy+extentY+1));
		
		for(int y=sy;y<ey;y++)
			{
			int base=y*w;
			double dy2=y-midy;
			dy2=dy2*dy2*mul2y;
			for(int x=sx;x<ex;x++)
				{
				double dx2=x-midx;
				dx2=dx2*dx2*mul2x;
				double t=Math.exp(dx2+dy2);
				sum+=t;
				conv+=arr[base+x]*t;
				}
			}
		return conv/sum;
		}
	
	
	}
