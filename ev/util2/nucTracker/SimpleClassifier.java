/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.nucTracker;

import java.util.Arrays;
import java.util.List;


/**
 * Simple classifier based on two integrals
 */
public class SimpleClassifier
	{
	
	
	/**
	 * One rectangle, to integrate over image
	 */
	public static class FeatureRect
		{
		double x1,y1,x2,y2;
		
		public FeatureRect()
			{
			}
		public FeatureRect(double x1,double y1,double x2,double y2)
			{
			this.x1=x1;this.y1=y1;this.x2=x2;this.y2=y2;
			}
		
		public int integrate(TImage tim, int scale, int x, int y)
			{
			return tim.getSum(x+(int)(x1*scale),y+(int)(y1*scale),x+(int)(x2*scale), y+(int)(y2*scale));
			}
		public int area(int scale)
			{
			int x1p=(int)(x1*scale);
			int y1p=(int)(y1*scale);
			int x2p=(int)(x2*scale);
			int y2p=(int)(y2*scale);
			return (x2p-x1p)*(y2p-y1p);
			}
		public String toString()
			{
			return ""+x1+" "+y1+" "+x2+" "+y2;
			}
		}
	
	
	/**
	 * Data storage for finding optimal c in simple classifier
	 */
	private static class FeatureOpt implements Comparable<FeatureOpt>
		{
		public double c;
		public TImage tim;
		public int compareTo(FeatureOpt o)
			{
			if(o.c>c)	return -1;
			else if(o.c<c) return 1;
			else return 0;
			}
		}
	
	
	
	FeatureRect r1=new FeatureRect(), r2=new FeatureRect();
	double c,s;
	double optEps=0;
	
	public SimpleClassifier()
		{
		}

	public SimpleClassifier(double c, double s, FeatureRect r1, FeatureRect r2)
		{
		this.c=c;
		this.s=s;
		this.r1=r1;
		this.r2=r2;
		}

	public void optimize(List<TImage> images, int standardSize)
		{
		int numim=images.size();
		
	
		//Integrate images
		FeatureOpt fo[]=new FeatureOpt[numim];
		double aArea=r1.area(standardSize);
		double bArea=r2.area(standardSize)-aArea;
		for(int i=0;i<numim;i++)
			{
			FeatureOpt f=new FeatureOpt();
			f.tim=images.get(i);
			double ta=r1.integrate(f.tim, standardSize,0,0);
			double tb=r2.integrate(f.tim, standardSize,0,0)-ta;
			ta/=aArea;
			tb/=bArea;
			f.c=tb/ta;
			fo[i]=f;
			}

		//Must sort images by c
		//s*(a*c-b)>0
		//c increasing order
		Arrays.sort(fo);

		//Integrate error
		double dlistMinus[]=new double[numim];
		double dlistPlus[]=new double[numim];
		for(int i=0;i<numim;i++)
			{
			if(i!=0)
				{
				dlistMinus[i]=dlistMinus[i-1];
				dlistPlus[i]=dlistPlus[i-1];
				}
			if(fo[i].tim.valueY==1) //TODO CHECK
				dlistMinus[i]+=fo[i].tim.weightD;
			else
				dlistPlus[i]+=fo[i].tim.weightD;
			}



		boolean first=true;
		double minusTot=dlistMinus[numim-1];
		double plusTot=dlistPlus[numim-1];
		for(int i=0;i<numim;i++)
			{
			// landscape: \___/
			// one should maybe penalize false positives in that case

			double thisEpsPlus=minusTot-dlistMinus[i]+dlistPlus[i];
			if(first || thisEpsPlus<optEps)
				{
				first=false;
				optEps=thisEpsPlus;
				s=1;
				c=fo[i].c;
				}
//		System.out.println("eps "+i+" "+thisEps);

			double thisEpsMinus=plusTot-dlistPlus[i]+dlistMinus[i];
			if(thisEpsMinus<optEps)
				{
				first=false;
				optEps=thisEpsMinus;
				s=-1;
				c=fo[i].c;
				}
			
			}
		}

	//eval
	//set size once and for all?
	public double eval(TImage tim, int size, int x, int y)
		{
		double aArea=r1.area(size);
		double bArea=r2.area(size)-aArea;
		double ta=r1.integrate(tim, (int)size,x,y);
		double tb=r2.integrate(tim, (int)size,x,y)-ta;
		ta/=aArea;
		tb/=bArea;
//		System.out.println(""+aArea+" "+bArea+" "+(s*(ta*c-tb)));
		if(s*(ta*c-tb)>0)
			return 1;
		else
			return -1;
		}
	
	public String toString()
		{
		return ""+c+" "+s+"\n"+r1.toString()+"\n"+r2.toString();
		}
	}
