package endrov.nucAutoJH;

import javax.vecmath.Vector2d;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateVectorialFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.decomposition.DoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Matrix2d;

//import org.apache.commons.math.estimation.LevenbergMarquardtEstimator;

public class FitGaussian
	{

	public static class Result
		{
		//double sigma00, sigma01, sigma11;
		DenseDoubleMatrix2D sigma;
		double mu0, mu1;
		double C;
		double D;
		}
	
	public static Result fitGaussian2D(EvPixels p, double sigmaInit, double midxInit,  double midyInit)
		{
		double[] out=fitGaussian2D_(p, sigmaInit, midxInit, midyInit);
		//System.out.println("#out "+out.length);
		Result r=new Result();
		r.sigma=new DenseDoubleMatrix2D(new double[][]{{out[0],out[1]},{out[1],out[2]}});
		r.mu0=out[3];
		r.mu1=out[4];
		r.C=out[5];
		r.D=out[6];
		return r;
		}

	private static double[] fitGaussian2D_(EvPixels p, double sigmaInit, final double midxInit, final double midyInit)
		{
		//sigma00, sigma01, sigma11, mu_x, mu_y, c 

		
		p=p.getReadOnly(EvPixelsType.DOUBLE);
		final double[] arrPixels=p.getArrayDouble();
		final int w=p.getWidth();
		final int h=p.getHeight();
		
		int extent=(int)Math.round(3*sigmaInit);
		extent=Math.max(extent, 2);
		
		final int sx=Math.max(0, (int)(midxInit-extent));
		final int ex=Math.min(w,(int)(midxInit+extent+1)); //+1 to the right?
		final int sy=Math.max(0, (int)(midyInit-extent));
		final int ey=Math.min(h,(int)(midyInit+extent+1));
		
		double minIntensity=Double.MAX_VALUE;
		double maxIntensity=Double.MIN_VALUE;
		for(int y=sy;y<ey;y++)
			{
			int base=y*w;
			double dy2=y-midyInit;
			dy2=dy2*dy2;
			for(int x=sx;x<ex;x++)
				{
				double dx2=x-midxInit;
				dx2=dx2*dx2;
				double t=arrPixels[base+x];
				//if(dx2+dy2<=extent*extent)
					{
					if(t<minIntensity)
						minIntensity=t;
					if(t>maxIntensity)
						maxIntensity=t;
					}
				}
			}
		
		
		
		//double[] weights=new double[]{1};
		double[] startPoint=new double[]{
				sigmaInit,0,sigmaInit,
				midxInit,midyInit,minIntensity,maxIntensity-minIntensity};
		//double[] output=new double[startPoint.length];
		
		
		try
			{
			MultivariateRealFunction func=new MultivariateRealFunction(){
			//		opt.optimize(

			public double value(double[] arg) throws FunctionEvaluationException, IllegalArgumentException
			{
			double sigma00=arg[0];
			double sigma01=arg[1];
			double sigma11=arg[2];
			double mu0=arg[3];
			double mu1=arg[4];
			double C=arg[5];
			double D=arg[6];


			double sumError=0;

			Matrix2d sigma=new Matrix2d(sigma00,sigma01,sigma01,sigma11);
			Matrix2d sigmaInv=new Matrix2d();
			sigma.invert(sigmaInv);
			double sigmaDet=sigma.determinant();
			double front=1.0/(2*Math.PI*Math.sqrt(sigmaDet));
			//System.out.println("front: "+front);
			//System.out.println("sigma inv "+sigmaInv);

			if(mu0<sx || mu0>ex)	sumError+=1000000;
			if(mu1<sy || mu1>ey)	sumError+=1000000;
			if(sigma00<1)					sumError+=1000000;
			//if(sigma01<0)					sumError+=1000000;
			if(sigma11<1)					sumError+=1000000;
			if(D<=0)					sumError+=1000000;


			for(int y=sy;y<ey;y++)
				{
				int base=y*w;
				double dy2=y-midyInit;
				dy2=dy2*dy2;
				for(int x=sx;x<ex;x++)
					{
					double dx2=x-midxInit;
					dx2=dx2*dx2;
					double thisReal=arrPixels[base+x];
					//						if(dx2+dy2<=extent*extent)
						{
						//					DoubleMatrix2D sigma=new DenseDoubleMatrix2D(new double[][]{{sigma00,sigma01},{sigma01,sigma11}});
						//double sigmaDet=sigma00*sigma11-sigma01*sigma01;

						double dx0=x-mu0;
						double dx1=y-mu1;

						//http://en.wikipedia.org/wiki/Multivariate_normal_distribution

						Vector2d vX=new Vector2d(dx0,dx1);
						Vector2d op=new Vector2d(vX);
						sigmaInv.transform(op);
						double upper=-0.5*op.dot(vX);
						double exp=Math.exp(upper);

						//System.out.println("front "+front+" "+exp+" C "+C+" thisreal"+thisReal+" upper "+upper);

						
						if(upper>-0.4)
							exp=1;
						else 
							exp=Math.max(0,1+upper+0.4);
							
						/*
						if(exp<0.7)
							exp=0;
						else
							exp=1;
						*/
						
						double thisExpected=D*front*exp+C;
						double diff=thisExpected-thisReal;
						sumError+=diff*diff;

						}
					}
				}

			//System.out.println(sigma00+"\t"+sigma01+"\t"+sigma11+"\tC"+C+"\tmu "+mu0+","+mu1+"\terr "+sumError);
			return sumError;
			//				return new double[]{sumError};
			}

			};

			
			
			NelderMead opt=new NelderMead();
			//LevenbergMarquardtOptimizer opt=new LevenbergMarquardtOptimizer();
			opt.setMaxIterations(10000);
			RealPointValuePair pair=opt.optimize(func, GoalType.MINIMIZE, startPoint);
			
			int numit=opt.getIterations();
			System.out.println("#it "+numit);
			System.out.println("err "+func.value(pair.getPointRef()));
			return pair.getPointRef();
			
//			for(int i=0;i<startPoint.length;i++)
	//			System.out.println("i: "+i+"  "+output[i]);
			//, output, weights, startPoint);
			}
		/*
		catch (MaxIterationsExceededException e)
			{
			System.out.println("max it reached");
			
			}*/
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		//Maybe this is a bad point?
		System.out.println("max it reached");
		return startPoint;
//		return output;
		}
		
	}
