package endrov.nucAutoJH;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.decomposition.DoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.EvMathUtil;

public class LocalMomentum
	{

	

	/**
	 * Convolve with a gaussian at a single point
	 */
	public static DoubleEigenvalueDecomposition apply(EvPixels p, double sigmaX, double sigmaY, double midx, double midy)
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
				double t=Math.exp(dx2+dy2)*arr[base+x];
				
				sum+=t;
				sumx=t*x;
				sumy=t*x;
				sumxx=t*x*x;
				sumxy=t*x*y;
				sumyy=t*y*y;
				}
			}
		
		double cross=(sumxy-2*sumx*sumy+sumx*sumy/sum)/sum;
		double[][] arrS=new double[][]{
					{EvMathUtil.biasedVariance(sumx, sumxx, sum),cross},
					{cross, EvMathUtil.biasedVariance(sumy, sumyy, sum)}};
		DenseDoubleMatrix2D matS=new DenseDoubleMatrix2D(arrS);
		DoubleEigenvalueDecomposition de=new DoubleEigenvalueDecomposition(matS);
		return de;
		}
	
	
	
	}
