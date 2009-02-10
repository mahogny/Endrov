package util2.integrateExpression;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import endrov.imageset.Imageset;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;

/**
 * Util functions to mess around with expression levels
 */
public class ExpUtil
	{
	private static int min2(int a, int b)
		{
		return a<b? a:b;
		}

	private static int max2(int a, int b)
		{
		return a>b? a:b;
		}
	
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
	 * Integrate [f(x)-c*g(x)]^2
	 */
	public static double integrateFunctionFminusG2(TreeMap<Double, Double> funcF, TreeMap<Double, Double> funcG, double c)
		{
		if(funcF.size()<2 || funcG.size()<2)
			return 0;

		double minX=max2(funcF.firstKey(), funcG.firstKey());
		double maxX=min2(funcF.lastKey(),  funcG.lastKey());
		
		funcF=cutFunc(funcF, minX, maxX);
		funcF=cutFunc(funcG, minX, maxX);
		
		TreeSet<Double> xs=new TreeSet<Double>();
		xs.addAll(funcF.keySet());
		xs.addAll(funcG.keySet());
		
		
		return 0;
		/*
		FuncArr arr[]=new FuncArr[]{new FuncArr(funcF,minX,maxX),new FuncArr(funcG,minX,maxX)};
		int indexF=0;
		int indexG=0;
		
		//a*a*delta+2*a*b*delta*delta/2+b*b*delta*delta*delta/3.0
		
		
		for(int i=0;indexF<arr[0].x.length && indexG<arr[1].x.length;i++)
			{//+1?
			if(arr[0].x[i+1]<arr[1].x[i+1])
				{
				//0 ends first
				
				
				}
			else
				{
				//1 ends first
				
				}
			
			
			}
			
		
		
		*/
		
		
		}
	
	/**
	 * Cut off function to be within interval
	 */
	public static TreeMap<Double, Double> cutFunc(TreeMap<Double, Double> func, double min, double max)
		{
		if(func.firstKey()>=min && func.lastKey()<=max)
			return func;
		else
			{
			TreeMap<Double, Double> funcNew=new TreeMap<Double, Double>();
			Iterator<Map.Entry<Double, Double>> itval=func.entrySet().iterator();
			Map.Entry<Double, Double> lastPoint=itval.next();
			Map.Entry<Double, Double> nextPoint=null;
			while(itval.hasNext())
				{
				nextPoint=itval.next();
				if(nextPoint.getKey()>min)
					{
					if(lastPoint.getKey()<min)
						{
						//Add last point, interpolated to be inside
						double slope=(nextPoint.getValue()-lastPoint.getValue())/(nextPoint.getKey()-lastPoint.getKey());
						funcNew.put(min, lastPoint.getValue()+(min-lastPoint.getKey())*slope);
						}
					else
						funcNew.put(lastPoint.getKey(),lastPoint.getValue());
					if(nextPoint.getKey()>max)
						{
						//Add next point, interpolated to be inside, and signal to not add it again
						double slope=(nextPoint.getValue()-lastPoint.getValue())/(nextPoint.getKey()-lastPoint.getKey());
						funcNew.put(min, nextPoint.getValue()-(nextPoint.getKey()-max)*slope);
						nextPoint=null;
						break;
						}
					}
				}
			if(nextPoint!=null)
				funcNew.put(nextPoint.getKey(),nextPoint.getValue());
			return funcNew;
			}
		}
	
	private static class FuncArr
		{
		double[] x;
		double[] y;
		
		/**
		 * Convert whole
		 */
		public FuncArr(TreeMap<Double, Double> func)
			{
			set(func);
			}

		public void set(TreeMap<Double, Double> func)
			{
			x=new double[func.size()];
			y=new double[func.size()];
			Iterator<Map.Entry<Double, Double>> itval=func.entrySet().iterator();
			int i=0;
			while(itval.hasNext())
				{
				Map.Entry<Double, Double> nextPoint=itval.next();
				x[i]=nextPoint.getKey();
				y[i]=nextPoint.getValue();
				i++;
				}
			}

		/**
		 * Convert & cut function to be with interval
		 */
		/*
		public FuncArr(TreeMap<Double, Double> func, double min, double max)
			{
			if(func.isEmpty())
				{
				x=new double[0];
				y=new double[0];
				}
			else if(func.firstKey()>=min && func.lastKey()<=max)
				set(func);
			else
				{
				
				set(funcNew);
				}
			}
		*/
		
		}
	
	
	}
