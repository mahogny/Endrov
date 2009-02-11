package util2.integrateExpression;

import java.util.*;

import endrov.imageset.Imageset;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * Util functions to mess around with expression levels
 */
public class ExpUtil
	{
	/*
	private static int min2(int a, int b)
		{
		return a<b? a:b;
		}

	private static int max2(int a, int b)
		{
		return a>b? a:b;
		}
	*/
	private static double min2(double a, double b)
		{
		return a<b? a:b;
		}
	private static double max2(double a, double b)
		{
		return a>b? a:b;
		}
	
	/**
	 * Correct for background etc. Signal has already been divided by exposure time. 
	 * Try to keep bg signal constant? **this does not work well**. in fact, background is almost constant 
	 */
	public static void correctBG2(NucLineage lin, String expName, SortedSet<EvDecimal> frames, SortedMap<EvDecimal, Double> bgsignal)
		{
		
		double lastBg=bgsignal.get(bgsignal.firstKey());
		for(EvDecimal frame:frames)
			{
			double nextBg=bgsignal.get(frame);
			double diff=nextBg-lastBg;
			
			for(NucLineage.Nuc nuc:lin.nuc.values())
				{
				NucExp nexp=nuc.exp.get(expName);
				if(nexp!=null)
					{
					Double level=nexp.level.get(frame);
					if(level!=null)
						nexp.level.put(frame,level-diff);
					}
				}
			
			lastBg=nextBg;
			}
		
		}

	
	
	/**
	 * Correct for background etc. Signal has already been divided by exposure time.
	 * In addition, make it fit together by forcing average of last frame to be the average of this frame.
	 */
	public static void correctExposureChange(Imageset imset, NucLineage lin, String expName, SortedSet<EvDecimal> frames)
		{
		//Initital correction=none
		double correction=0;
		
		//For all frames
		Double lastExposure=null;
		EvDecimal lastFrame=null;
		for(EvDecimal frame:frames)
			{
			String sExpTime=imset.metaFrame.get(frame).get("exposuretime");
			double expTime=1;
			if(sExpTime!=null)
				expTime=Double.parseDouble(sExpTime);
			
			//Trigger correction
			if(lastExposure!=null && !lastExposure.equals(expTime))
				{
				Double lastAv=helperGetAverageForFrame(lin, expName, lastFrame);
				Double curAv=helperGetAverageForFrame(lin, expName, frame);
				//Note that the last frame has been corrected so to make = we restore the last average
				if(lastAv!=null && curAv!=null)
					{
					correction=curAv-(lastAv+correction);
					System.out.println("------------Frame "+frame+"    Correction "+correction+"     "+lastAv+" "+curAv);
					}
				else
					System.out.println("------------Frame "+frame+"    skipping correction, no samples");
				}
			//else
				//System.out.println("No corr change "+frame);
			lastFrame=frame;
			lastExposure=expTime;
			
			//Correct this frame. Also get 
			for(NucLineage.Nuc nuc:lin.nuc.values())
				{
				NucExp nexp=nuc.exp.get(expName);
				if(nexp!=null)
					{
					Double level=nexp.level.get(frame);
					if(level!=null)
						nexp.level.put(frame,level-correction);
					}
				}
			}
		}
	private static Double helperGetAverageForFrame(NucLineage lin, String expName, EvDecimal frame)
		{
		int count=0;
		double lastAverage=0;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				{
				Double level=nexp.level.get(frame);
				if(level!=null)
					{
					lastAverage+=level;
					count++;
					}
				}
			}
		if(count!=0)
			return lastAverage/count;
		else
			return null;
		}
	
	
	
	
	
	public static void clearExp(NucLineage lin, String expName)
		{
		for(NucLineage.Nuc nuc:lin.nuc.values())
			nuc.exp.remove(expName);
		}
		
	/**
	 * Normalize expression such that maximum is 1.0
	 */
	public static void normalizeSignal(NucLineage lin, String expName)
		{
		Double max=null;
		double normalizedVal=100;
		
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				for(double level:nexp.level.values())
					if(max==null || level>max)
						max=level;
			}

		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				{
				nexp.unit=null;
				HashMap<EvDecimal, Double> newlevel=new HashMap<EvDecimal, Double>();
				for(Map.Entry<EvDecimal, Double> e:nexp.level.entrySet())
					newlevel.put(e.getKey(),normalizedVal*e.getValue()/max);
				nexp.level.clear();
				nexp.level.putAll(newlevel);
				}
			}

		}
	
	/**
	 * Compress signal destructively by only keeping levels where the change is great
	 * 
	 * Integral should be kept the same: 
	 * base functions phi_i(x), node values u_i, coordinates(frames) x_i, expression f(x)
	 * Condition: int_{x_i}^{x_{i+1}} dx sum u_i phi_i(x) = int_{x_i}^{x_{i+1}} dx f(x) 
	 * ...=(u_{i+1}+u_i)(x_{i+1}-x_i)/2
	 * 
	 * Num unknowns=num frames
	 * Num equations=num frames-1
	 * Add one constraint: starting value has to be the safe, u_0=f(x_0)
	 * 
	 * Matrix best solved directly without forming it.
	 * algorithm:
	 * set u_0
	 * until have all u_i, i>=0:
	 *   Solve for u_{i+1}: (u_{i+1}+u_i)(x_{i+1}-x_i)/2 = int_{x_i}^{x_{i+1}} dx f(x)
	 *     => u_{i+1} = 2 int_{x_i}^{x_{i+1}} dx f(x)/(x_{i+1}-x_i) - u_i
	 * 
	 */
	public static void compressSignal(NucLineage lin, String expName)
		{
		
		}
	/*
	public static void compressSignal(NucLineage lin, String expName)
		{
		
		}*/
	
	
	
	public static LinkedList<Tuple<Double, Double>> pointMap2List(TreeMap<Double, Double> map)
		{
		LinkedList<Tuple<Double, Double>> list=new LinkedList<Tuple<Double,Double>>();
		for(Map.Entry<Double, Double> e:map.entrySet())
			list.add(new Tuple<Double, Double>(e.getKey(),e.getValue()));
		return list;
		}
		
	/**
	 * Integrate a piece-wise linear function
	 */
	public static double integratePLFunction(TreeMap<Double, Double> func)
		{
		if(func.isEmpty())
			return 0;
		else
			{
			double sum=0;
			Iterator<Map.Entry<Double, Double>> itval=func.entrySet().iterator();
			Map.Entry<Double, Double> lastPoint=itval.next();
			while(itval.hasNext())
				{
				Map.Entry<Double, Double> nextPoint=itval.next();
				sum+=(nextPoint.getKey()-lastPoint.getKey())*(lastPoint.getValue()+nextPoint.getValue())/2.0;
				lastPoint=nextPoint;
				}
			return sum;
			}
		}

	
	
	
	/**
	 * Integrate a piece-wise linear function
	 * List x->y, sorted by x
	 */
	public static double integratePLFunction(List<Tuple<Double, Double>> func)
		{
		if(func.isEmpty())
			return 0;
		else
			{
			double sum=0;
			ListIterator<Tuple<Double, Double>> itval=func.listIterator();
			Tuple<Double, Double> lastPoint=itval.next();
			while(itval.hasNext())
				{
				Tuple<Double, Double> nextPoint=itval.next();
				sum+=(nextPoint.fst()-lastPoint.fst())*(lastPoint.snd()+nextPoint.snd())/2.0;
				lastPoint=nextPoint;
				}
			return sum;
			}
		}

	/**
	 * Cut off beginning up to x
	 */
	private static void cutBeginning(List<Tuple<Double, Double>> func, double x)
		{
		ListIterator<Tuple<Double, Double>> itval=func.listIterator();
		Tuple<Double, Double> lastval=itval.next();
		Tuple<Double, Double> nextval=itval.next();
		while(nextval.fst()<x)
			{
			itval.previous();
			itval.remove();
			itval.next();
			lastval=nextval;
			nextval=itval.next();
			}
		//Interpolate here if not exactly overlapping
		if(lastval.fst()<x)
			{
			itval.previous();
			itval.previous();
			itval.remove();
			itval.add(interpol(slope(lastval,nextval),lastval,x));
			}
		}
	
	/**
	 * Get last two points
	 */
	private static Tuple<Tuple<Double, Double>,Tuple<Double, Double>> getLastTwo(LinkedList<Tuple<Double, Double>> func)
		{
		Tuple<Double, Double> last=func.removeLast();
		Tuple<Double, Double> secondLast=func.removeLast();
		func.addLast(secondLast);
		func.addLast(last);
		return new Tuple<Tuple<Double,Double>, Tuple<Double,Double>>(secondLast,last);
		}
	

	private static Tuple<Double, Double> interpol(double slope, Tuple<Double, Double> p, double x)
		{
		return new Tuple<Double, Double>(x, p.snd()+(x-p.fst())*slope);
		}
	
	private static double slope(Tuple<Double, Double> p1, Tuple<Double, Double> p2)
		{
		return (p2.snd()-p1.snd())/(p2.fst()-p1.fst());
		}
	
	/**
	 * Cut off end after x
	 */
	private static void cutEnd(LinkedList<Tuple<Double, Double>> func, double x)
		{
		Tuple<Tuple<Double, Double>,Tuple<Double, Double>> s=getLastTwo(func);
		while(s.fst().fst()>x)
			{
			func.removeLast();
			s=getLastTwo(func);
			}
		//Interpolate here if not exactly overlapping
		if(s.snd().fst()>x)
			{
			func.removeLast();
			func.addLast(interpol(slope(s.fst(),s.snd()),s.fst(),x));
			}
		}
	
	
	/**
	 * Reslice to have xlist as x, cut ends
	 */
	public static void reslice(LinkedList<Tuple<Double, Double>> func, LinkedList<Double> xlist)
		{
		if(func.size()<2)
			return;
		cutBeginning(func, xlist.getFirst());
		cutEnd(func, xlist.getLast());
		
		System.out.println(func);
		
		ListIterator<Tuple<Double, Double>> itval=func.listIterator();
		ListIterator<Double> itx=xlist.listIterator();
		Tuple<Double, Double> lastval=itval.next();
		Tuple<Double, Double> nextval=itval.next();
		double slope=slope(lastval,nextval);
		itx.next(); //Skip first point, guaranteed to be there by cut
		while(itx.hasNext())
			{
			double curx=itx.next();
			System.out.println(curx);
			while(nextval.fst()<curx)
				{
				lastval=nextval;
				nextval=itval.next();
				slope=slope(lastval,nextval);
				}
			//Insert point
			if(nextval.fst()!=curx)
				{
				itval.previous();
				Tuple<Double, Double> newpoint=interpol(slope,lastval,curx);
				itval.add(newpoint);
				lastval=newpoint;
				nextval=itval.next();
				}
			}
		}


	
	/**
	 * Integrate [f(x)-c*g(x)]^2
	 */
	public static double integrateFunctionFminusG2(TreeMap<Double,Double> mapF, TreeMap<Double,Double> mapG, double c)
		{
		if(mapF.size()<2 || mapG.size()<2)
			return 0;

		//Figure out which x to use
		double minX=max2(mapF.firstKey(), mapG.firstKey());
		double maxX=min2(mapF.lastKey(),  mapG.lastKey());
		TreeSet<Double> xs=new TreeSet<Double>(mapF.keySet());
		xs.addAll(mapG.keySet());
		HashSet<Double> toRemove=new HashSet<Double>();
		toRemove.addAll(xs.headSet(minX));
		toRemove.addAll(xs.tailSet(maxX));
		toRemove.remove(maxX);
		xs.removeAll(toRemove);
		LinkedList<Double> xslist=new LinkedList<Double>(xs);
		System.out.println(xslist);
		
		
		//Reslice functions
		LinkedList<Tuple<Double, Double>> funcF=pointMap2List(mapF);
		LinkedList<Tuple<Double, Double>> funcG=pointMap2List(mapG);
		reslice(funcF, xslist);
		reslice(funcG, xslist);

		//Now intervals are supposed to be the same. This makes integration a lot simpler, just iterate over
		//intervals of one of the functions
		System.out.println(funcF);
		System.out.println(funcG);
		
		ListIterator<Tuple<Double, Double>> itvalF=funcF.listIterator();
		ListIterator<Tuple<Double, Double>> itvalG=funcG.listIterator();
		
		double sum=0;
		Tuple<Double, Double> lastvalF=itvalF.next();
		Tuple<Double, Double> lastvalG=itvalG.next();
		while(itvalF.hasNext())
			{
			Tuple<Double, Double> nextvalF=itvalF.next();
			Tuple<Double, Double> nextvalG=itvalG.next();
			
			double delta=nextvalF.fst()-lastvalF.fst();
			
			double a=
				lastvalF.snd()-
				lastvalG.snd()*c;
			double b=
				slope(lastvalF,nextvalF)-
				slope(lastvalG,nextvalG)*c;
			System.out.println("ab "+a+" "+b);
			System.out.println(lastvalF+" "+nextvalF+"    "+lastvalG+" "+nextvalG);
			
			double delta2=delta*delta;
			sum += a*a*delta + a*b*delta2 + b*b*delta2*delta/3.0;
			
			lastvalF=nextvalF;
			lastvalG=nextvalG;
			}
		
		return sum;
		}
	
	
	public static void main(String[] args)
		{
		/*
		TreeMap<Double,Double> map=new TreeMap<Double, Double>();
		map.put(1.0,5.0);
		map.put(2.0,6.0);
		map.put(3.0,8.0);
		map.put(4.0,10.0);
		map.put(5.0,12.0);
		
		LinkedList<Tuple<Double, Double>> func=pointMap2List(map);
				
		
		TreeSet<Double> xs2=new TreeSet<Double>(map.keySet());
		xs2.add(1.5);
		xs2.remove(1.0);
		LinkedList<Double> xs=new LinkedList<Double>(xs2);
		
		System.out.println(func);
		reslice(func,xs);
		
		
		System.out.println(func);
		*/

		TreeMap<Double,Double> mapF=new TreeMap<Double, Double>();
		mapF.put(0.0,0.0);
		mapF.put(1.0,0.0);

		TreeMap<Double,Double> mapG=new TreeMap<Double, Double>();
		mapG.put(0.0,0.0);
		mapG.put(2.0,2.0);

		System.out.println(integrateFunctionFminusG2(mapF, mapG, 1));
		}
	
	
	
	
	}
