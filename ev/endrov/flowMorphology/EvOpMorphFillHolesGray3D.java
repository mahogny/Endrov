/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import endrov.flow.EvOpStack1;
import endrov.typeImageset.EvPixelsType;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Fill holes in gray image. O(w h d)
 * 
 * <br/>
 * P.Soille, C.Gratin - "An efficient algorithm for drainage network extraction on DEMs"
 * @author Johan Henriksson
 */
public class EvOpMorphFillHolesGray3D extends EvOpStack1
	{
	@Override
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return apply(ph, p[0]);
		}

	private static class QE //implements Comparable<QE>
		{
		public int x,y,z;
		/*
		public double intensity;
		public int compareTo(QE o)
			{
			if(intensity<o.intensity)
				return -1;
			else if(intensity>o.intensity)
				return 1;
			else
				return 0;
			}*/
		public QE(int x, int y, int z/*, double intensity*/)
			{
			this.x = x;
			this.y = y;
			this.z = z;
//			this.intensity = intensity;
			}
		}
	
	/**
	 * Get queue for intensity. TreeMap gives fast access to lowest intensity level.
	 * HashMap gives fast access to queue.
	 */
	private static LinkedList<QE> getCreate(
			TreeMap<Double,LinkedList<QE>> queues, 
			HashMap<Double,LinkedList<QE>> imaps, double intensity)
		{
		LinkedList<QE> v=imaps.get(intensity);
		if(v==null)
			{
			v=new LinkedList<QE>();
			imaps.put(intensity, v);
			queues.put(intensity, v);
			}
		return v;
		}
	
	public static EvStack apply(ProgressHandle progh, EvStack in)
		{
		int w=in.getWidth();
		int h=in.getHeight();
		int d=in.getDepth();

		EvStack out=new EvStack();
		out.copyMetaFrom(in);
		out.allocate(w, h, d, EvPixelsType.DOUBLE, in);
		
		double[][] inarr=in.getArraysDoubleReadOnly(progh);
		double[][] outarr=out.getArraysDoubleReadOnly(progh);
		
		//Have all pixels unmarked
		for(double[] plane:outarr)
			for(int i=0;i<plane.length;i++)
				plane[i]=Double.MAX_VALUE;

		TreeMap<Double,LinkedList<QE>> queues=new TreeMap<Double, LinkedList<QE>>();
		HashMap<Double,LinkedList<QE>> imaps=new HashMap<Double, LinkedList<QE>>();

		//Border as starting point
//		PriorityQueue<QE> q=new PriorityQueue<QE>();
		for(int ax=0;ax<w;ax++)
			for(int ay=0;ay<h;ay++)
				{
				int az=0;
				getCreate(queues, imaps, inarr[az][ay*w+ax]).add(new QE(ax,ay,az));
				//q.add(new QE(ax,ay,az,inarr[az][ay*w+ax]));
				outarr[az][ay*w+ax]=inarr[az][ay*w+ax];
				az=d-1;
				getCreate(queues, imaps, inarr[az][ay*w+ax]).add(new QE(ax,ay,az));
				//q.add(new QE(ax,ay,az,inarr[az][ay*w+ax]));
				outarr[az][ay*w+ax]=inarr[az][ay*w+ax];
				}

		for(int ax=0;ax<w;ax++)
			for(int az=0;az<d;az++)
				{
				int ay=0;
				getCreate(queues, imaps, inarr[az][ay*w+ax]).add(new QE(ax,ay,az));
				//q.add(new QE(ax,ay,az,inarr[az][ay*w+ax]));
				outarr[az][ay*w+ax]=inarr[az][ay*w+ax];
				ay=h-1;
				getCreate(queues, imaps, inarr[az][ay*w+ax]).add(new QE(ax,ay,az));
				//q.add(new QE(ax,ay,az,inarr[az][ay*w+ax]));
				outarr[az][ay*w+ax]=inarr[az][ay*w+ax];
				}

		for(int ay=0;ay<h;ay++)
			for(int az=0;az<d;az++)
				{
				int ax=0;
				getCreate(queues, imaps, inarr[az][ay*w+ax]).add(new QE(ax,ay,az));
				//q.add(new QE(ax,ay,az,inarr[az][ay*w+ax]));
				outarr[az][ay*w+ax]=inarr[az][ay*w+ax];
				ax=w-1;
				getCreate(queues, imaps, inarr[az][ay*w+ax]).add(new QE(ax,ay,az));
				//q.add(new QE(ax,ay,az,inarr[az][ay*w+ax]));
				outarr[az][ay*w+ax]=inarr[az][ay*w+ax];
				}
		
		while(!queues.isEmpty())
			{
			double fromintensity=queues.firstKey();
			LinkedList<QE> thisq=queues.get(fromintensity);
			
			while(!thisq.isEmpty())
				{
				QE v=thisq.poll();
				int x=v.x;
				int y=v.y;
				int z=v.z;

				//Evaluate neighbours
				if(x>0)
					doNeigh(queues, imaps, x-1, y, z, w, fromintensity, inarr, outarr);
				if(x<w-1)
					doNeigh(queues, imaps, x+1, y, z, w, fromintensity, inarr, outarr);
				if(y>0)
					doNeigh(queues, imaps, x, y-1, z, w, fromintensity, inarr, outarr);
				if(y<h-1)
					doNeigh(queues, imaps, x, y+1, z, w, fromintensity, inarr, outarr);
				if(z>0)
					doNeigh(queues, imaps, x, y, z-1, w, fromintensity, inarr, outarr);
				if(z<d-1)
					doNeigh(queues, imaps, x, y, z+1, w, fromintensity, inarr, outarr);
				}

			//Lower levels no longer processed
			queues.remove(fromintensity);
			imaps.remove(fromintensity);
			}
				
		return out;
		}

	private static void doNeigh(TreeMap<Double,LinkedList<QE>> queues, 
			HashMap<Double,LinkedList<QE>> imaps,
			int x, int y, int z, int w, double fromintensity, 
			double[][] inarr, double[][] markarr)
		{
		int index=y*w+x;
		if(markarr[z][index]==Double.MAX_VALUE)
			{
			double newintensity=Math.max(fromintensity, inarr[z][index]);
			markarr[z][index]=newintensity;
			getCreate(queues, imaps, newintensity).addLast(new QE(x,y,z)); //last or first?
			}
		}
	
	
	}
