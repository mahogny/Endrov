package endrov.util;

import java.util.*;


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
	public Tuple<Double,Double> solveQuadratic(double b, double c)
		{
		double out=b/2;
		return Tuple.make(-out,Math.sqrt(out*out-c));
		}
	
	
	public static void main(String[] args)
		{
		LinkedList<Double> ys=new LinkedList<Double>();
		LinkedList<Double> xs=new LinkedList<Double>();
		
		xs.add(5.0);		ys.add(5.0*2+3);
		xs.add(7.0);		ys.add(7.0*2+3);
		
		System.out.println(fitLinear1D(ys, xs));
		
		}
	}
