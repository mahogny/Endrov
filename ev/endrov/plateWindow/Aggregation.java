package endrov.plateWindow;

import java.util.Collection;
import java.util.Iterator;

import endrov.util.EvMathUtil;

public class Aggregation
	{
	
	//TODO correlation as well
	
	
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
				sumx+=d*d;
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
	
			double meanA=aggrMean.calc(listA, null);
			double meanB=aggrMean.calc(listB, null);
			
			double sum=0;
			Iterator<Double> itA=listA.iterator();
			Iterator<Double> itB=listB.iterator();
			while(itA.hasNext())
				sum += (itA.next()-meanA)*(itB.next()-meanB);
			sum/=listA.size();
			return sum;
			}
		};
/*
	public static final AggregationMethod aggrSkew=new AggregationMethod("Skew")
		{
		public Double calc(Collection<Double> listA, Collection<Double> listB)
			{
			}
		};
*/

	


		public static AggregationMethod[] getAggrModes()
			{
			return new AggregationMethod[]{
					//TODO spearman!
					
					aggrMean, aggrSum, aggrMin, aggrMax, aggrStdDev, aggrPearson
			};
			}

	
	}
