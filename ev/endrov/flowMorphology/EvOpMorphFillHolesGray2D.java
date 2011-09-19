/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * Fill holes in gray image. O(w h log(w h)), in practice closer to O(w h log(w+h))
 * <br/>
 * P.Soille, C.Gratin - "An efficient algorithm for drainage network extraction on DEMs"
 * @author Johan Henriksson
 */
public class EvOpMorphFillHolesGray2D extends EvOpSlice1
	{
	@Override
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return apply(p[0]);
		}

	private static class QE //implements Comparable<QE>
		{
		public int x,y;
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
			}
			*/
		public QE(int x, int y/*, double intensity*/)
			{
			this.x = x;
			this.y = y;
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

	
	public static EvPixels apply(EvPixels in)
		{
		in=in.convertToDouble(true);
		int w=in.getWidth();
		int h=in.getHeight();

		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		
		double[] inarr=in.getArrayDouble();
		double[] outarr=out.getArrayDouble();
		
		TreeMap<Double,LinkedList<QE>> queues=new TreeMap<Double, LinkedList<QE>>();
		HashMap<Double,LinkedList<QE>> imaps=new HashMap<Double, LinkedList<QE>>();

		//Have all pixels unmarked
		for(int i=0;i<outarr.length;i++)
			outarr[i]=Double.MAX_VALUE;
		
		//Border as starting point
//		PriorityQueue<QE> q=new PriorityQueue<QE>();
		for(int ax=0;ax<w;ax++)
			{
			int ay=0;
			getCreate(queues, imaps, inarr[ay*w+ax]).add(new QE(ax,ay));
			outarr[ay*w+ax]=inarr[ay*w+ax];
			ay=h-1;
			getCreate(queues, imaps, inarr[ay*w+ax]).add(new QE(ax,ay));
			outarr[ay*w+ax]=inarr[ay*w+ax];
			}

		for(int ay=0;ay<h;ay++)
			{
			int ax=0;
			getCreate(queues, imaps, inarr[ay*w+ax]).add(new QE(ax,ay));
			outarr[ay*w+ax]=inarr[ay*w+ax];
			ax=w-1;
			getCreate(queues, imaps, inarr[ay*w+ax]).add(new QE(ax,ay));
			outarr[ay*w+ax]=inarr[ay*w+ax];
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

				//Evaluate neighbours
				if(x>0)
					doNeigh(queues, imaps, x-1, y, w, fromintensity, inarr, outarr);
				if(x<w-1)
					doNeigh(queues, imaps, x+1, y, w, fromintensity, inarr, outarr);
				if(y>0)
					doNeigh(queues, imaps, x, y-1, w, fromintensity, inarr, outarr);
				if(y<h-1)
					doNeigh(queues, imaps, x, y+1, w, fromintensity, inarr, outarr);
				}
			
			//Lower levels no longer processed
			queues.remove(fromintensity);
			imaps.remove(fromintensity);
			}
		
		return out;
		}

	private static void doNeigh(
			TreeMap<Double,LinkedList<QE>> queues, HashMap<Double,LinkedList<QE>> imaps,
			int x, int y, int w, double fromintensity, 
			double[] inarr, double[] markarr)
		{
		int index=y*w+x;
		if(markarr[index]==Double.MAX_VALUE)
			{
			double newintensity=Math.max(fromintensity, inarr[index]);
			markarr[index]=newintensity;
			getCreate(queues, imaps, newintensity).addLast(new QE(x,y)); //last or first?
			}
		}
	
	
	}
