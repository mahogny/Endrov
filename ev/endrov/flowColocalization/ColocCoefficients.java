package endrov.flowColocalization;


/**
 * Colocalization calculation. Assumes two images X and Y. Add all pixels,
 * then retrieve the statistics.
 * <p/>
 * Pixels<=0 are considered background when calculating Manders coefficient
 * <p/>
 * Definitions: http://support.svi.nl/wiki/ColocalizationTheory
 * 
 * 
 * Interesting pseuodo-code for improved numerical instability exists at:
 * http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient
 * 
 * @author Johan Henriksson
 *
 */
public class ColocCoefficients
	{

	public double sumX, sumXX, sumY, sumYY, sumXY;
	public int n;

	public double sumXcoloc, sumYcoloc;
	
	
	/**
	 * Add pixels from arrays
	 */
	public void add(double[] arrX, double[] arrY)
		{
		for(int i=0;i<arrX.length;i++)
			{
			double x=arrX[i];
			double y=arrY[i];
			
			sumX+=x;
			sumY+=y;
			sumXX+=x*x;
			sumXY+=y*y;
			sumYY+=y*y;
			
			if(!isBackground(y))
				sumXcoloc+=x;
			if(!isBackground(x))
				sumYcoloc+=y;
			}
		n+=arrX.length;
		}
	
	
	private boolean isBackground(double v)
		{
		return v<=0;
		}

	
	/**
	 * Variance of X
	 */
	public double varX()
		{
		return (sumXX - sumX*sumX/n)/n;
		}

	/**
	 * Variance of Y
	 */
	public double varY()
		{
		return (sumYY - sumY*sumY/n)/n;
		}

	/**
	 * Covariance(X,Y)
	 */
	public double covXY()
		{
		return (sumXY - sumX*sumY/n)/n;
		}
	
	/**
	 * Pearsons coefficient
	 */
	public double pearson()
		{
		return covXY()/(Math.sqrt(varX()*varY()));
		}
	
	/**
	 * Pearsons coefficient, assume mean=0
	 */
	public double pearsonMean0()
		{
		return sumXY/(Math.sqrt(sumXX*sumYY));
		}

	/**
	 * kX=k1
	 */
	public double kX()
		{
		return sumXY/sumXX;
		}
	
	/**
	 * kY=k2
	 */
	public double kY()
		{
		return sumXY/sumYY;
		}
	
	/**
	 * Mander coefficient X or 1
	 */
	public double mandersX()
		{
		return sumXcoloc/sumX;
		}
	
	
	/**
	 * Mander coefficient Y or 2
	 */
	public double mandersY()
		{
		return sumYcoloc/sumY;
		}
	
	
	
	}
