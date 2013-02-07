package endrov.windowPlateAnalysis;

import java.util.Collection;
import java.util.Iterator;

import endrov.util.math.EvMathUtil;

/**
 * Aggregation math functions
 * 
 * @author Johan Henriksson
 *
 */
public class CalcAggregation
	{
	
	
	public static abstract class AggregationMethod
		{
		private String name;
		public AggregationMethod(String name)
			{
			this.name=name;
			}
		public abstract Double calc(Collection<Double> listA, Collection<Double> listB);
		public String toString()
			{
			return name;
			}
		}


	public static final AggregationMethod aggrMean=new AggregationMethod("Mean")
		{
		public Double calc(Collection<Double> listA, Collection<Double> listB)
			{
			if(listA.isEmpty())
				return null;
			double sum=0;
			for(double d:listA)
				sum+=d;
			sum/=listA.size();
			return sum;
			}
		};

	public static final AggregationMethod aggrSum=new AggregationMethod("Sum")
		{
		public Double calc(Collection<Double> listA, Collection<Double> listB)
			{
			double sum=0;
			for(double d:listA)
				sum+=d;
			return sum;
			}
		};

	public static final AggregationMethod aggrMax=new AggregationMethod("Max")
		{
		public Double calc(Collection<Double> listA, Collection<Double> listB)
			{
			return EvMathUtil.maxAllDouble(listA);
			}
		};

	public static final AggregationMethod aggrMin=new AggregationMethod("Min")
		{
		public Double calc(Collection<Double> listA, Collection<Double> listB)
			{
			return EvMathUtil.minAllDouble(listA);
			}
		};

	public static final AggregationMethod aggrCount=new AggregationMethod("Count")
		{
		public Double calc(Collection<Double> listA, Collection<Double> listB)
			{
			return listA.size();
			}
		};

		
	public static final AggregationMethod aggrStdDev=new AggregationMethod("Std.deviation")
		{
		public Double calc(Collection<Double> listA, Collection<Double> listB)
			{
			if(listA.size()<2)
				return null;
			double sumx=0;
			double sumxx=0;
			for(double d:listA)
				{
				sumx+=d;
				sumxx+=d*d;
				}
			return Math.sqrt(EvMathUtil.unbiasedVariance(sumx, sumxx, listA.size()));
			}
		};

	public static final AggregationMethod aggrPearson=new AggregationMethod("Pearson corr")
		{
		public Double calc(Collection<Double> listA, Collection<Double> listB)
			{
			if(listA.isEmpty() || listA.size()!=listB.size())
				return null;
			
			int n=listA.size();
			
			double sumA=0, sumAA=0;
			double sumB=0, sumBB=0;
			double sumAB=0;
			for(double d:listA)
				{
				sumA+=d;
				sumAA+=d*d;
				}
			for(double d:listB)
				{
				sumB+=d;
				sumBB+=d*d;
				}
			Iterator<Double> itA=listA.iterator();
			Iterator<Double> itB=listB.iterator();
			while(itA.hasNext())
				sumAB+=itA.next()*itB.next();
			
			double varA=(sumAA - sumA*sumA/n)/n;
			double varB=(sumBB - sumB*sumB/n)/n;
			double covAB=(sumAB - sumA*sumB/n)/n;
			return covAB/(Math.sqrt(varA*varB));
			}
		};

		/*
	public static final AggregationMethod aggrSpearman=new AggregationMethod("Spearman corr")
		{
		public Double calc(Collection<Double> listA, Collection<Double> listB)
			{
			//Calculate rank within each list
			
			
			
			
			
			//Final eq!
			
			
			if(listA.isEmpty() || listA.size()!=listB.size())
				return null;
	
			}
		};
	
*/		
		
	public static final AggregationMethod aggrSkew=new AggregationMethod("Skew")
		{
		public Double calc(Collection<Double> listA, Collection<Double> listB)
			{
			if(listA.size()<2 || listA.size()!=listB.size())
				return null;
	
			double sigma=aggrStdDev.calc(listA, null);
			double mu=aggrMean.calc(listA, null);
			
			double x3mean=0;
			for(double x:listA)
				x3mean+=x*x*x;
			x3mean/=listA.size();
			
			return (x3mean - 3*mu*sigma*sigma - mu*mu*mu)/(sigma*sigma*sigma);
			}
		};

	
	public static AggregationMethod[] getAggregationMethods()
		{
		return new AggregationMethod[]{
				//TODO spearman!
				
				aggrCount, aggrMean, aggrSum, aggrMin, aggrMax, aggrStdDev, aggrPearson, aggrSkew
		};
		}

	
	}
