package endrov.nucAutoJH;

import cern.colt.matrix.tdouble.algo.decomposition.DoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.EvListUtil;
import endrov.util.EvMathUtil;

public class LocalMomentum
	{

	

	/**
	 * Do the PCA of pixel intensities weighted as a gaussian at a certain point 
	 */
	public static DoubleEigenvalueDecomposition applyGauss(
			EvPixels p, double sigmaX, double sigmaY, double midx, double midy)
		{
		p=p.getReadOnly(EvPixelsType.DOUBLE);
		double[] arr=p.getArrayDouble();
		int w=p.getWidth();
		int h=p.getHeight();

		double sum=0;		
		double sumx=0;
		double sumy=0;
		double sumxx=0;
		double sumyy=0;
		double sumxy=0;

		
		int extentX=(int)Math.round(3*sigmaX);
		extentX=Math.max(extentX, 2);
		int extentY=(int)Math.round(3*sigmaY);
		extentY=Math.max(extentY, 2);
		
		//For gauss
		double mul2x=-1/(2*sigmaX*sigmaX);
		double mul2y=-1/(2*sigmaY*sigmaY);
		
		int sx=Math.max(0, (int)(midx-extentX));
		int ex=Math.min(w,(int)(midx+extentX+1)); //+1 to the right?
		int sy=Math.max(0, (int)(midy-extentY));
		int ey=Math.min(h,(int)(midy+extentY+1));
		
		
		double minIntensity=Double.MAX_VALUE;
		for(int y=sy;y<ey;y++)
			{
			int base=y*w;
			double dy2=y-midy;
			dy2=dy2*dy2;
			for(int x=sx;x<ex;x++)
				{
				double dx2=x-midx;
				dx2=dx2*dx2;
				double t=arr[base+x];
				if(dx2+dy2<=extentX*extentX)
					if(t<minIntensity)
						minIntensity=t;
				}
			}
		
		for(int y=sy;y<ey;y++)
			{
			int base=y*w;
			double dy2=y-midy;
			dy2=dy2*dy2*mul2y;  //For gauss
			for(int x=sx;x<ex;x++)
				{
				double thisIntensity=arr[base+x];
				thisIntensity-=minIntensity;
				double dx2=x-midx;
				double t;
				
				//For gauss
				dx2=dx2*dx2*mul2x;
				double gauss=Math.exp(dx2+dy2);
				t=gauss*thisIntensity;
				
				sum  +=t;
				sumx +=t*x;
				sumy +=t*y;
				sumxx+=t*x*x;
				sumxy+=t*x*y;
				sumyy+=t*y*y;
				}
			}
		
		double cross=EvMathUtil.biasedCovariance(sumx, sumy, sumxy, sum);
		//;(sumxy - 2*sumx*sumy/sum + sumx*sumy/sum)/sum;
		double[][] arrS=new double[][]{
					{EvMathUtil.biasedVariance(sumx, sumxx, sum),cross},
					{cross, EvMathUtil.biasedVariance(sumy, sumyy, sum)}};
		DenseDoubleMatrix2D matS=new DenseDoubleMatrix2D(arrS);
		/*
		System.out.println("-->");
		System.out.println(matS.toString());*/
		DoubleEigenvalueDecomposition de=new DoubleEigenvalueDecomposition(matS);
		
		return de;
		}
	
	
	/**
	 * Do the PCA of pixel intensities weighted as a gaussian at a certain point 
	 */
	public static DoubleEigenvalueDecomposition applyCircle(
			EvPixels p, double radius, double midx, double midy)
		{
		p=p.getReadOnly(EvPixelsType.DOUBLE);
		double[] arr=p.getArrayDouble();
		int w=p.getWidth();
		int h=p.getHeight();

		double sum=0;		
		double sumx=0;
		double sumy=0;
		double sumxx=0;
		double sumyy=0;
		double sumxy=0;

		
		int extent=(int)Math.round(radius);
		extent=Math.max(extent, 4);
		//System.out.println("extent for circle "+extent);
		
		int sx=Math.max(0, (int)(midx-extent));
		int ex=Math.min(w,(int)(midx+extent+1)); //+1 to the right?
		int sy=Math.max(0, (int)(midy-extent));
		int ey=Math.min(h,(int)(midy+extent+1));
		
		
		double minIntensity=Double.MAX_VALUE;
		for(int y=sy;y<ey;y++)
			{
			int base=y*w;
			double dy2=y-midy;
			dy2=dy2*dy2;
			for(int x=sx;x<ex;x++)
				{
				double dx2=x-midx;
				dx2=dx2*dx2;
				double t=arr[base+x];
				if(dx2+dy2<=extent*extent)
					if(t<minIntensity)
						minIntensity=t;
				}
			}
		
		for(int y=sy;y<ey;y++)
			{
			int base=y*w;
			double dy2=y-midy;
			dy2=dy2*dy2;
			for(int x=sx;x<ex;x++)
				{
				double thisIntensity=arr[base+x];
				thisIntensity-=minIntensity;  //try again
				double dx2=x-midx;
				double t;
				
				//For unweighted circle
				dx2=dx2*dx2;
				if(dx2+dy2<=extent*extent)
					t=thisIntensity;
				else
					t=0;
				
				sum  +=t;
				sumx +=t*x;
				sumy +=t*y;
				sumxx+=t*x*x;
				sumxy+=t*x*y;
				sumyy+=t*y*y;
				}
			}
		
		double cross=(sumxy - 2*sumx*sumy/sum + sumx*sumy/sum)/sum;
		double[][] arrS=new double[][]{
					{EvMathUtil.biasedVariance(sumx, sumxx, sum),cross},
					{cross, EvMathUtil.biasedVariance(sumy, sumyy, sum)}};
		DenseDoubleMatrix2D matS=new DenseDoubleMatrix2D(arrS);
		/*
		System.out.println("-->");
		System.out.println(matS.toString());
		*/
		try
			{
			DoubleEigenvalueDecomposition de=new DoubleEigenvalueDecomposition(matS);
			
			return de;
			}
		catch (Exception e)
			{
			e.printStackTrace();
			System.out.println("-->");
			System.out.println(matS.toString());
			return null;
			}
		}
	
	
	/**
	 * Do the PCA of pixel intensities weighted as a gaussian at a certain point 
	 */
	public static DoubleEigenvalueDecomposition applyCirclePercentile(
			EvPixels p, double radius, double midx, double midy)
		{
		p=p.getReadOnly(EvPixelsType.DOUBLE);
		double[] arr=p.getArrayDouble();
		int w=p.getWidth();
		int h=p.getHeight();

		double sum=0;		
		double sumx=0;
		double sumy=0;
		double sumxx=0;
		double sumyy=0;
		double sumxy=0;

		
		int extent=(int)Math.round(radius);
		extent=Math.max(extent, 1);
		System.out.println("extent for circle "+extent);
		
		int sx=Math.max(0, (int)(midx-extent));
		int ex=Math.min(w,(int)(midx+extent+1)); //+1 to the right?
		int sy=Math.max(0, (int)(midy-extent));
		int ey=Math.min(h,(int)(midy+extent+1));
		
		
		double[] vals=new double[(ey-sy)*(ex-sx)];
		int curpos=0;
		double minIntensity=Double.MAX_VALUE;
		for(int y=sy;y<ey;y++)
			{
			int base=y*w;
			double dy2=y-midy;
			dy2=dy2*dy2;
			for(int x=sx;x<ex;x++)
				{
				double dx2=x-midx;
				dx2=dx2*dx2;
				double t=arr[base+x];
				if(dx2+dy2<=extent*extent)
					if(t<minIntensity)
						minIntensity=t;
				vals[curpos]=t;
				}
			curpos++;
			}
		
		double cutoff=EvListUtil.findPercentileDouble(vals, 0.5);
		
		
		for(int y=sy;y<ey;y++)
			{
			int base=y*w;
			double dy2=y-midy;
			dy2=dy2*dy2;
			for(int x=sx;x<ex;x++)
				{
				double thisIntensity=arr[base+x];
				double dx2=x-midx;
				double t;
				
				//For unweighted circle
				dx2=dx2*dx2;
				if(dx2+dy2<=extent*extent)
					{
					if(thisIntensity>cutoff)
						t=1;
					else
						t=0;
					}
				else
					t=0;
				
				
				sum  +=t;
				sumx +=t*x;
				sumy +=t*y;
				sumxx+=t*x*x;
				sumxy+=t*x*y;
				sumyy+=t*y*y;
				}
			}
		
		double cross=(sumxy - 2*sumx*sumy/sum + sumx*sumy/sum)/sum;
		double[][] arrS=new double[][]{
					{EvMathUtil.biasedVariance(sumx, sumxx, sum),cross},
					{cross, EvMathUtil.biasedVariance(sumy, sumyy, sum)}};
		DenseDoubleMatrix2D matS=new DenseDoubleMatrix2D(arrS);
		/*
		System.out.println("-->");
		System.out.println(matS.toString());*/
		DoubleEigenvalueDecomposition de=new DoubleEigenvalueDecomposition(matS);
		
		return de;
		}
	
	
	
	
	
	
	
	}
