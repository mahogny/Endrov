package util2.makeStdWormDist2;

import java.util.*;
import javax.vecmath.*;

import util2.makeStdWormDist2.NucStats.NucStatsOne;



public class BestFitLength
	{
	public Map<String,NucStatsOne> nuc;
	public double eps;
	
	
	public static final boolean doprint=false;
	
	public void clear()
		{
		nuc=null;
		}
	
	
	public void init(Map<String,NucStatsOne> nuc)
		{
		this.nuc=nuc;
		
		for(NucStatsOne one:nuc.values())
			one.curpos=null;
		
		}
	
	
	public void iterate(int minit, int maxit, double okEps)
		{
		for(int i=0;i<maxit;i++)
			{
			doOneIteration();
			if(i>minit && eps<okEps)
				break;
			}
		}
	
	public void doOneIteration()
		{
		if(doprint)
			System.out.println();
		if(doprint)
			System.out.println();

		
		//Clear
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			e.getValue().df.set(0,0,0);
		
		//Sum up df and eps
		Map<String,Double> epsForNuc=new HashMap<String, Double>();
		eps=0;
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			{
			NucStatsOne one=e.getValue();
			String iName=e.getKey();
			
			double neps=0;
			for(NucStats.Neigh n:one.neigh)
				{
				double thiseps=total2(iName, iName, n, one.df);
/*				NucStatsOne otherOne=nuc.get(n.name);
				if(otherOne!=null)
					thiseps+=total2(iName, n.name, n, otherOne.df);*/ //symmetry
				eps+=thiseps;
				neps+=thiseps;
				if(doprint)
					System.out.println(e.getKey()+" <-> "+n.name+" "+thiseps);
				}
	
//			System.out.println("df for "+e.getKey()+" "+one.df+" neps "+neps);
			if(doprint)
				System.out.println("pos for "+e.getKey()+" "+one.curpos+ " neps "+neps);
			epsForNuc.put(e.getKey(), neps);
			
			}
		eps/=nuc.size();


		
		//Converge
		/*
		Double Eps=null;
		double thelambda=0;
		for(double lambda=0.01;lambda<0.1;lambda+=0.01)
			{
			double neps=totalEps(lambda);
			if(Eps==null || neps<Eps)
				{
				Eps=neps;
				thelambda=lambda;
				}
			}
		System.out.println("Optimal lambda "+thelambda);
		*/
		
		double lambda=0.01;
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			{
			e.getValue().df.scale(lambda);
//			e.getValue().df.scale(0.001*epsForNuc.get(e.getKey()));
			
			
//			e.getValue().df.normalize();			e.getValue().df.scale(0.0001);
			e.getValue().curpos.sub(e.getValue().df);
			}
		

		
		
		if(doprint)
			System.out.println("eps "+eps);
		
		}

	
	
	/**
	 * get error
	 */
	private double totalEps(double lambda)
		{
		double eps=0;
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			{
			NucStatsOne one=e.getValue();
			String iName=e.getKey();
			
			
			for(NucStats.Neigh n:one.neigh)
				{
				eps+=total2(iName, iName, n, one.df);
				NucStatsOne otherOne=nuc.get(n.name);
				if(otherOne!=null)
					{
					double thiseps=totalEps(iName, n.name, n, lambda);
					eps+=thiseps;
					System.out.println(e.getKey()+" <-> "+n.name+" "+thiseps);
					}
				}
			
			}
		eps/=nuc.size();
		return eps;
		}
	private double totalEps(String iName, String diffName, NucStats.Neigh neigh, double lambda)
		{
		NucStatsOne ni=nuc.get(iName);
		NucStatsOne nj=nuc.get(neigh.name);
		if(nj!=null)
			{
			Vector3d d=new Vector3d(nj.df);
			d.sub(ni.df);
			d.scale(lambda);
			
			Vector3d c=new Vector3d(ni.curpos);
			c.sub(nj.curpos);
			c.sub(d);
			double distxixj=c.length();
			double g=distxixj-neigh.dist;
			return g*g*neigh.weight;
			}
		else
			{
			return 0;
			}
		}
	
	
	
	/**
	 * Set df. Return eps
	 */
	private double total2(String iName, String diffName, NucStats.Neigh neigh, Vector3d df)
		{
		if(diffName.equals(iName) || diffName.equals(neigh.name))
			{
			NucStatsOne ni=nuc.get(iName);
			NucStatsOne nj=nuc.get(neigh.name);
			if(nj!=null)
				{
				
				Vector3d c=new Vector3d(ni.curpos);
				c.sub(nj.curpos);
				double distxixj=c.length();
	
				double g=distxixj-neigh.dist;
				double common=g*neigh.weight*2/distxixj;

//				System.out.println("compare "+iName+ " "+neigh.name+" "+neigh.weight);

				if(diffName.equals(neigh.name))
					common=-common;
	
				c.scale(common);
				df.add(c);
	
				return g*g*neigh.weight;
				}
			else
				{
				df.set(0,0,0);
				return 0;
				}
			}
		else
			{
			df.set(0,0,0);
			return 0;
			}
		}
			
	
	
	
	
	
	}
