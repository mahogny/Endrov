/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowColocalization;

import org.jdom.DataConversionException;
import org.jdom.Element;


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
	public double sumXminusY2;
	public double sumXcoloc, sumYcoloc;
	public int n;
	
	
	/**
	 * Compare several pixels
	 */
	public void add(double[] arrX, double[] arrY)
		{
		if(arrX.length!=arrY.length)
			throw new RuntimeException("coloc arrays have different length");
		for(int i=0;i<arrX.length;i++)
			{
			double x=arrX[i];
			double y=arrY[i];
			add(x,y);
			}
		}
	
	/**
	 * Compare one pixel
	 */
	public void add(double x, double y)
		{
		sumX+=x;
		sumY+=y;
		sumXX+=x*x;
		sumXY+=x*y;
		sumYY+=y*y;
		double dx=x-y;
		sumXminusY2+=dx*dx;
		
		if(!isBackground(y))
			sumXcoloc+=x;
		if(!isBackground(x))
			sumYcoloc+=y;
		
		n++;
		}
	
	private boolean isBackground(double v)
		{
		return v<=0;
		}

	
	
	/**
	 * Variance of X
	 */
	public double getVarX()
		{
		return (sumXX - sumX*sumX/n)/n;
		}

	/**
	 * Variance of Y
	 */
	public double getVarY()
		{
		return (sumYY - sumY*sumY/n)/n;
		}

	/**
	 * Covariance(X,Y)
	 */
	public double getCovXY()
		{
		return (sumXY - sumX*sumY/n)/n;
		}
	
	/**
	 * Pearsons coefficient
	 */
	public double getPearson()
		{
		return getCovXY()/(Math.sqrt(getVarX()*getVarY()));
		}
	
	/**
	 * Pearsons coefficient, assume mean=0
	 */
	public double getPearsonMean0()
		{
		return sumXY/(Math.sqrt(sumXX*sumYY));
		}

	/**
	 * kX=k1
	 */
	public double getKX()
		{
		return sumXY/sumXX;
		}
	
	/**
	 * kY=k2
	 */
	public double getKY()
		{
		return sumXY/sumYY;
		}
	
	/**
	 * Mander coefficient X (=index 1)
	 */
	public double getMandersX()
		{
		return sumXcoloc/sumX;
		}
	
	
	/**
	 * Mander coefficient Y (=index 2)
	 */
	public double getMandersY()
		{
		return sumYcoloc/sumY;
		}
	
	public double getL2()
		{
		return sumXminusY2/n;
		}
	
	
	
	public static void main(String[] args)
		{
		ColocCoefficients c=new ColocCoefficients();
		
		c.add(
				new double[]{1,1,1,1,1,1,1,1,1,1,1,20}, 
				new double[]{2,2,2,2,2,2,2,2,2,2,2,21});
		
		System.out.println("variance X: "+c.getVarX());
		System.out.println("variance Y: "+c.getVarY());
		System.out.println("covariance: "+c.getCovXY());
		System.out.println("Pearson: "+c.getPearson());
		
		System.out.println(c.sumX+"  "+c.sumXX+"  "+c.sumXY+"   "+c.n);
		
		}

	
	public void toXML(Element e)
		{
		e.setAttribute("sumX", ""+sumX);
		e.setAttribute("sumY", ""+sumY);
		e.setAttribute("sumXX", ""+sumXX);
		e.setAttribute("sumXY", ""+sumXY);
		e.setAttribute("sumYY", ""+sumYY);
		e.setAttribute("sumXcoloc", ""+sumXcoloc);
		e.setAttribute("sumYcoloc", ""+sumYcoloc);
		e.setAttribute("sumXminusY2", ""+sumXminusY2);
		e.setAttribute("n", ""+n);
		}
	
	public void fromXML(Element e) throws DataConversionException
		{
		sumX=e.getAttribute("sumX").getDoubleValue();
		sumY=e.getAttribute("sumY").getDoubleValue();
		sumXX=e.getAttribute("sumXX").getDoubleValue();
		sumXY=e.getAttribute("sumXY").getDoubleValue();
		sumYY=e.getAttribute("sumYY").getDoubleValue();
		sumXcoloc=e.getAttribute("sumXcoloc").getDoubleValue();
		sumYcoloc=e.getAttribute("sumYcoloc").getDoubleValue();
		sumXminusY2=e.getAttribute("sumXminusY2").getDoubleValue();
		n=e.getAttribute("n").getIntValue();
		}
	
	
	}
