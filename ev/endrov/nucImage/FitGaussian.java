package endrov.nucImage;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

//import org.apache.commons.math.estimation.LevenbergMarquardtEstimator;

public class FitGaussian
	{

	
	

	public double[] fitGaussian2D()
		{
		//sigma00, sigma01, sigma11, c 

		double[] output=new double[4];
		double[] weights=new double[]{1};
		double[] startPoint=new double[]{1}; //TODO
		
		LevenbergMarquardtOptimizer opt=new LevenbergMarquardtOptimizer();
		try
			{
			opt.optimize(new DifferentiableMultivariateVectorialFunction(){

				public MultivariateMatrixFunction jacobian()
					{
					// TODO Auto-generated method stub
					return null;
					}

				public double[] value(double[] arg) throws FunctionEvaluationException, IllegalArgumentException
					{
					return new double[]{0}; //TODO
					}
			
			}, output, weights, startPoint);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		return output;
		}
		
	}
