/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

import java.util.*;

import javax.vecmath.Vector2d;


public class EvMathUtil
	{
	/*
	public static double sumPrecise(List<Double> xs)
		{
		double sum=0;
		TreeSet<Double> newXs=new TreeSet<Double>(xs);
		
		}*/
	
	/**
	 * Fit y=kx+m with normal linear least squares
	 * @return (k,m)
	 */
	public static Tuple<Double,Double> fitLinear1D(List<Double> ys, List<Double> xs)
		{
		int n=ys.size();
		if(n!=xs.size())
			throw new RuntimeException("lists not of the same size");
		else if(n<2)
			return null;
		
		double sumx=0;
		double sumx2=0;
		double sumxy=0;
		double sumy=0;
		Iterator<Double> itx=xs.iterator();
		Iterator<Double> ity=ys.iterator();
		while(itx.hasNext())
			{
			double x=itx.next();
			double y=ity.next();
			sumx +=x;
			sumx2+=x*x;
			sumxy+=x*y;
			sumy +=y;
			}
		//http://en.wikipedia.org/wiki/Linear_regression
		double common=n*sumx2 - sumx*sumx;
		
		double k=(n*sumxy - sumx*sumy)/common;
		double m=(sumx2*sumy - sumx*sumxy)/common;
		
		/*
		System.out.println(n);
		System.out.println(sumx);
		System.out.println(sumx2);
		System.out.println(sumxy);
		System.out.println(sumy);
		System.out.println(common);*/
		return new Tuple<Double, Double>(k,m);
		}
	
	
	/**
	 * Fit y=kx+m with weights
	 * @return (k,m)
	 */
	public static Tuple<Double,Double> fitWeightedLinear1D(List<Double> ys, List<Double> xs, List<Double> ws)
		{
		int n=ys.size();
		if(n!=xs.size())
			throw new RuntimeException("lists not of the same size");
		else if(n<2)
			return null;
		
		double sumw=0;
		double sumx=0;
		double sumx2=0;
		double sumxy=0;
		double sumy=0;
		Iterator<Double> itx=xs.iterator();
		Iterator<Double> ity=ys.iterator();
		Iterator<Double> itw=ws.iterator();
		while(itx.hasNext())
			{
			double x=itx.next();
			double y=ity.next();
			double w=itw.next();
			sumx +=x*w;
			sumx2+=x*x*w;
			sumxy+=x*y*w;
			sumy +=y*w;
			sumw +=w;
			}
		//http://en.wikipedia.org/wiki/Linear_regression
		double common=sumw*sumx2 - sumx*sumx;
		
		double k=(sumw*sumxy - sumx*sumy)/common;
		double m=(sumx2*sumy - sumx*sumxy)/common;
		
		/*
		System.out.println(n);
		System.out.println(sumx);
		System.out.println(sumx2);
		System.out.println(sumxy);
		System.out.println(sumy);
		System.out.println(common);*/
		return new Tuple<Double, Double>(k,m);
		}
	
	
	/**
	 * Solve x^2+bx+c=0
	 * 
	 * @return x=fst() +- snd() 
	 */
	public static Tuple<Double,Double> solveQuadratic(double b, double c)
		{
		double out=b/2;
		return Tuple.make(-out,Math.sqrt(out*out-c));
		}
	
	
	/**
	 * Calculate unbiased sample variance
	 * @param sum		Sum X
	 * @param sum2	Sum X^2
	 * @param n			Number of samples
	 */
	public static double unbiasedVariance(double sum, double sum2, int n)
		{
		int n1=n-1;
		return sum2/n1 - sum*sum/(n1*n);
		}

	/**
	 * Biased sample variance i.e. the plug-in estimate. Equals 0 for a single sample
	 * @param sumx	Sum X
	 * @param sumxx	Sum X^2
	 * @param n			Number of samples
	 */
	public static double biasedVariance(double sumx, double sumxx, double n)
		{
		return (sumxx - sumx*sumx/n)/n;
//		return sum2/n - sum*sum/(n*n);
		}

	/**
	 * Biased sample covariance i.e. the plug-in estimate. Equals 0 for a single sample.
	 * @param sumx  sum X
	 * @param sumy  sum Y
	 * @param sumxy sum XY
	 * @param n     Number of samples
	 */
	public static double biasedCovariance(double sumx, double sumy, double sumxy, double n)
		{
//		return (sumxy - 2*sumx*sumy/n + sumx*sumy/n)/n;
		return (sumxy - sumx*sumy/n)/n;
		}
	
	/**
	 * Turn boolean into 1 or 0
	 */
	public static int toInt(boolean b)
		{
		if(b)
			return 1;
		else
			return 0;
		}
	
	
	/**
	 * Given function x->y, find the x that maximizes y. The function is cubic interpolated
	 */
	public static double findXforMaxY(SortedMap<Double,Double> func)
		{
		Double xmid=EvListUtil.getKeyOfMax(func);
		SortedMap<Double,Double> head=func.headMap(xmid);
		if(head.isEmpty())
			return xmid;
		SortedMap<Double,Double> tail=func.tailMap(xmid);
		Iterator<Double> tit=tail.keySet().iterator();
		tit.next();
		if(!tit.hasNext())
			return xmid;
		
		double xbefore=head.lastKey();
		double xafter=tit.next();
		double ybefore=func.get(xbefore);
		double ymid=func.get(xmid);
		double yafter=func.get(xafter);
		System.out.println("xs: "+xbefore+"   "+xmid+"   "+xafter);
		
		
		double x0=xmid-xbefore;
		double x1=xafter-xbefore;
		double y0=ymid-ybefore;
		double y1=yafter-ybefore;
		
		Matrix2d m=new Matrix2d(
				x0*x0,x0,
				x1*x1,x1);
		Vector2d v=new Vector2d(y0,y1);
		m.invert();
		m.transform(v);
		double optimalx=xbefore-v.y/(2*v.x);
		if(optimalx<xbefore)
			return xbefore;
		else if(optimalx>xafter)
			return xafter;
		else
			{
			//This will always be a curve like /\ because y is smaller to the left and right.
			//Hence this extreme point will always be better
			return optimalx;
			}
		}
	
	/**
	 * Return largest value of a/b and b/a, which has to be >=1 
	 */
	public static double ratioAbove1(double a, double b)
		{
		if(a<b)
			return b/a;
		else
			return a/b;
		}

	
	public static void main(String[] args)
		{
		LinkedList<Double> ys=new LinkedList<Double>();
		LinkedList<Double> xs=new LinkedList<Double>();
		
		xs.add(5.0);		ys.add(5.0*2+3);
		xs.add(7.0);		ys.add(7.0*2+3);
		
		System.out.println(fitLinear1D(ys, xs));
		
		}

	

	/**
	 * Clamp value within limits i.e. return value, changed to closest limit if outside
	 */
	public static double clamp(double percentile, double min, double max)
		{
		if(percentile<min)
			return min;
		else if(percentile>max)
			return max;
		else
			return percentile;
		}
	
	
	
	
	

	/**
	 * Interpolate x given x->yTuple<EvDecimal,EvDecimal>. Returns null if x outside.
	 * 
	 * NEEDS TESTING
	 * 
	 */
	public EvDecimal interpolate(SortedMap<EvDecimal, EvDecimal> map, EvDecimal x)
		{
		EvDecimal preciseY=map.get(x);
		if(preciseY!=null)
			return preciseY;
		
		if(map.size()>2)
			{
			SortedMap<EvDecimal, EvDecimal> hmap=map.headMap(x);
			SortedMap<EvDecimal, EvDecimal> tmap=map.tailMap(x);
			
			if(hmap.isEmpty() || tmap.isEmpty())
				return null;
			else
				{
				EvDecimal lastX=hmap.lastKey();
				EvDecimal nextX=tmap.firstKey();
				EvDecimal lastY=hmap.get(lastX);
				EvDecimal nextY=tmap.get(nextX);
				return linInterpolate(lastX, nextX, lastY, nextY, x);
				}
			}
		else
			return null;
		}
	
	/**
	 * Linear interpolation
	 */
	private EvDecimal linInterpolate(EvDecimal lastX,EvDecimal nextX, EvDecimal lastY, EvDecimal nextY, EvDecimal x)
		{
		EvDecimal frac=x.subtract(lastX).divide(nextX.subtract(lastX));
		EvDecimal frac1=EvDecimal.ONE.subtract(frac);
		return frac1.multiply(lastY).add(
				frac.multiply(nextY));
		}
	
	
	}
