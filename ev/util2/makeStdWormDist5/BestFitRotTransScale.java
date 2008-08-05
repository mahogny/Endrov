package util2.makeStdWormDist5;

import java.util.*;
import javax.vecmath.*;

import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import static java.lang.Math.*;


/**
 * Multiple fitting of several lineages
 * @author Johan Henriksson
 *
 */
public class BestFitRotTransScale
	{
	public Map<NucLineage,LinInfo> lininfo=new HashMap<NucLineage, LinInfo>();
	public List<LinPair> pair=new LinkedList<LinPair>();
	public NucLineage refLin;
	
	
	
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Info about one lineage
	 */
	public static class LinInfo
		{
		public double[] x=new double[]{0,0,0, 0,0,0, 1};
		public double[] dx=new double[]{0,0,0, 0,0,0, 0};
		public Map<String,Vector3d[]> dtransform=new HashMap<String, Vector3d[]>();
		public Map<String,Vector3d> transformed=new HashMap<String, Vector3d>();
		public Map<String,Vector3d> untransformed=new HashMap<String, Vector3d>();
		public Map<String,Double> untransformedR=new HashMap<String, Double>();
		
		
		/**
		 * Transform all new points
		 */
		public Vector<Vector3d> getTransformed(Vector<Vector3d> points, double x[])
			{
			Vector<Vector3d> transformed=new Vector<Vector3d>();
			Matrix3d rotx=rotx(x[4-1]);
			Matrix3d roty=roty(x[5-1]);
			Matrix3d rotz=rotz(x[6-1]);
			Matrix3d rotxp=rotxp(x[4-1]);
			Matrix3d rotyp=rotyp(x[5-1]);
			Matrix3d rotzp=rotzp(x[6-1]);
			Vector3d trans=new Vector3d(x[1-1],x[2-1],x[3-1]);
			double scale=x[7-1];
			for(int i=0;i<points.size();i++)
				{
				Vector3d v=points.get(i);
				Vector3d tv=BestFitRotTransScale.transform(rotx, roty, rotz, rotxp, rotyp, rotzp, trans, scale, v, -1);
				transformed.add(tv);
				}
			return transformed;
			}

		/**
		 * Transform one point
		 */
		public Vector3d transform(Vector3d v, double x[])
			{
			Vector<Vector3d> vv=new Vector<Vector3d>();
			vv.add(v);
			vv=getTransformed(vv,x);
			return vv.get(0);
			}
		
		/**
		 * Update transforms
		 *
		 */
		public void update()
			{
			transformed.clear();
			dtransform.clear();
			dx=new double[]{0,0,0, 0,0,0, 0};
			
			
			Matrix3d rotx=rotx(x[4-1]);
			Matrix3d roty=roty(x[5-1]);
			Matrix3d rotz=rotz(x[6-1]);
			Matrix3d rotxp=rotxp(x[4-1]);
			Matrix3d rotyp=rotyp(x[5-1]);
			Matrix3d rotzp=rotzp(x[6-1]);
			Vector3d trans=new Vector3d(x[1-1],x[2-1],x[3-1]);
			double scale=x[7-1];
			
			for(Map.Entry<String, Vector3d> entry:untransformed.entrySet())
				{
				Vector3d v=entry.getValue();
				Vector3d tv=BestFitRotTransScale.transform(rotx, roty, rotz, rotxp, rotyp, rotzp, trans, scale, v, -1);
				transformed.put(entry.getKey(), tv);
				
				Vector3d[] diff=new Vector3d[7];
				for(int diffi=1;diffi<=7;diffi++)
					diff[diffi-1]=BestFitRotTransScale.transform(rotx, roty, rotz, rotxp, rotyp, rotzp, trans, scale, v, diffi);
				dtransform.put(entry.getKey(),diff);
				}
			}
		
		}

	
	
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * One pair of lineages
	 */
	public static class LinPair
		{
		public Set<String> commonNuc=new HashSet<String>();		
		public NucLineage goalLin, newLin;
		
		
		/**
		 * Sum up dx for one pair
		 */
		public void oneIteration(BestFitRotTransScale parent)
			{
			LinInfo newInfo=parent.lininfo.get(newLin);
			LinInfo goalInfo=parent.lininfo.get(goalLin);
			for(String nucname:commonNuc)
				{
				Vector3d tv=newInfo.transformed.get(nucname);
				Vector3d gv=goalInfo.transformed.get(nucname);
//				System.out.println(nucname+" "+tv+" "+gv);
				Vector3d diff=new Vector3d(tv);
				diff.sub(gv);
				parent.incNumPointEps(diff.lengthSquared());
				for(int j=1;j<=7;j++)
					{
//					Vector3d dtrans=transform(rotx, roty, rotz, rotxp, rotyp, rotzp, trans, scale, v, j);
					Vector3d dtrans=newInfo.dtransform.get(nucname)[j-1];
					newInfo.dx[j-1]+=2*dtrans.dot(diff);

					//Difference inverted, changes sign
					Vector3d dtrans2=goalInfo.dtransform.get(nucname)[j-1];
					goalInfo.dx[j-1]-=2*dtrans2.dot(diff);
					}
				}
			}
		
		
		}
	
	

	
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////

	
	private double eps;
	private int numpoint;
	
	private void incNumPointEps(double eps)
		{
		numpoint++;
		this.eps+=eps;
		}

	
	public BestFitRotTransScale()
		{
		}
	public BestFitRotTransScale(BestFitRotTransScale bf)
		{
		//Shallow copy
		lininfo.putAll(bf.lininfo);
		pair.addAll(bf.pair);
		}
	
	
	
	/**
	 * Add one lineage to fit
	 */
	public void addLineage(NucLineage lin)
		{
		if(!lininfo.containsKey(lin))
			{
			for(NucLineage lin2:lininfo.keySet())
				{
				LinPair p=new LinPair();
				p.goalLin=lin;
				p.newLin=lin2;
				pair.add(p);
				}
			
			lininfo.put(lin,new LinInfo());
			}
		}
	
	public void findCommonNuc()
		{
		for(LinPair p:pair)
			{
			p.commonNuc.clear();
			p.commonNuc.addAll(lininfo.get(p.newLin).untransformed.keySet());
			p.commonNuc.retainAll(lininfo.get(p.goalLin).untransformed.keySet());
			}
		}
	
	
	/**
	 * Iterate until done
	 */
	public void iterate(int minit, int maxit, double okEps)
		{
		for(int i=0;i<maxit;i++)
			{
			doOneIteration();
			
//			System.exit(0);
			
			if(i%40==0)
				System.out.println("eps "+eps);
//			System.out.println("eps "+eps);
			if((i>minit && eps<okEps*numpoint) || numpoint==0)
				{
				System.out.println("ok");
				break;
				}
			}
//		System.exit(0);
		}
	
	/**
	 * Do one single iteration
	 */
	public void doOneIteration()
		{
		//Transform all points
		numpoint=0;
		eps=0;
		for(LinInfo info:lininfo.values())
			info.update();
		
		//For every pair, calculate how to move
//		System.out.println("#pair "+pair.size());
		for(LinPair p:pair)
			p.oneIteration(this);
//		System.out.println("#point "+numpoint);
		
		if(numpoint>0)
			{
		
			//Apply move
			double lambdaT=0.1;
			double lambdaR=0.0001;
			double lambdaS=0.0001;
	
			for(LinInfo info:lininfo.values())
				{
				//Weigh
				for(int i=0;i<3;i++)
					info.dx[i]*=lambdaT/numpoint;
				for(int i=3;i<6;i++)
					info.dx[i]*=lambdaR/numpoint;
				for(int i=6;i<7;i++)
					info.dx[i]*=lambdaS/numpoint;
				
				//Move
				for(int i=0;i<7;i++)
					info.x[i]-=info.dx[i];
	
				//Restrict values
				if(info.x[7-1]<0.01)
					info.x[7-1]=0.01;
				if(info.x[7-1]>100)
					info.x[7-1]=100;
				info.x[7-1]=1; //fix scale
				}
			
			//Restore reference rotation
			lininfo.get(refLin).x=new double[]{0,0,0, 0,0,0, 1};
		
			}
		
//		System.out.println("eps: "+eps+"  scale: "+x[7-1]+"    rot   "+x[4-1]+" "+x[5-1]+" "+x[6-1]+"    trans "+x[1-1]+" "+x[2-1]+" "+x[3-1]);
//		System.out.println("diff: scale: "+dx[7-1]+"    rot   "+dx[4-1]+" "+dx[5-1]+" "+dx[6-1]+"    trans "+dx[1-1]+" "+dx[2-1]+" "+dx[3-1]);
		}
	
	
	
	
	
	
	
	
	
	
	
	
	

	

	///////////////////////// general transformation ///////////////////////////////////
	
	public static Vector3d transform(
			Matrix3d rotx, Matrix3d roty, Matrix3d rotz,
			Matrix3d rotxp, Matrix3d rotyp, Matrix3d rotzp,
			Vector3d trans,	double scale,
			Vector3d u, int diffi)
		{
		Vector3d v=new Vector3d(u);
		if(diffi==7) ; else v.scale(scale);
		if(diffi==6) rotzp.transform(v); else rotz.transform(v);
		if(diffi==5) rotyp.transform(v); else roty.transform(v);
		if(diffi==4) rotxp.transform(v); else rotx.transform(v);
		if(diffi==1 || diffi==2 || diffi==3)
			v=new Vector3d(0,0,0);
		v.add(getTrans(trans,diffi));
		return v;
		}
	public static Vector3d getTrans(Vector3d trans, int diffi)
		{
		if(diffi==-1)
			return trans;
		else if(diffi==1)
			return new Vector3d(1,0,0);
		else if(diffi==2)
			return new Vector3d(0,1,0);
		else if(diffi==3)
			return new Vector3d(0,0,1);
		else
			return new Vector3d(0,0,0);
		}
	
	/////////////////////////////////////// rotation ////////////////////////////////////
	private static Matrix3d rotx(double a)
		{
		Matrix3d m=new Matrix3d();
		m.m11=cos(a); m.m12=-sin(a);
		m.m21=sin(a); m.m22=cos(a);
		m.m00=1;
		return m;
		}
	private static Matrix3d roty(double a)
		{
		Matrix3d m=new Matrix3d();
		m.m00=cos(a); m.m02=-sin(a);
		m.m20=sin(a); m.m22=cos(a);
		m.m11=1;
		return m;
		}
	private static Matrix3d rotz(double a)
		{
		Matrix3d m=new Matrix3d();
		m.m00=cos(a); m.m01=-sin(a);
		m.m10=sin(a); m.m11=cos(a);
		m.m22=1;
		return m;
		}
	
	
	/////////////////////////////////////// rotation prim ////////////////////////////////////
	private static Matrix3d rotxp(double a)
		{
		Matrix3d m=new Matrix3d();
		m.m11=-sin(a); m.m12=-cos(a);
		m.m21=cos(a);  m.m22=-sin(a);
		return m;
		}
	private static Matrix3d rotyp(double a)
		{
		Matrix3d m=new Matrix3d();
		m.m00=-sin(a); m.m02=-cos(a);
		m.m20=cos(a);  m.m22=-sin(a);
		return m;
		}
	private static Matrix3d rotzp(double a)
		{
		Matrix3d m=new Matrix3d();
		m.m00=-sin(a); m.m01=-cos(a);
		m.m10=cos(a);  m.m11=-sin(a);
		return m;
		}
	
	
	/*
	public static void main(String[] arg)
		{
		BestFit bf=new BestFit();
		
		bf.goalpoint.add(new Vector3d(0,0,0));
		bf.goalpoint.add(new Vector3d(1,0,0));

//		bf.newpoint.add(new Vector3d(0,0,0));
//		bf.newpoint.add(new Vector3d(0,1,0));
		
		bf.newpoint.add(new Vector3d(1+0,0,0));
		bf.newpoint.add(new Vector3d(1+sqrt(0.5),sqrt(0.5),0));
	
//		bf.newpoint.add(new Vector3d(0,0,0));
//		bf.newpoint.add(new Vector3d(2,0,0));

//		bf.newpoint.add(new Vector3d(1,0,0));
//		bf.newpoint.add(new Vector3d(2,0,0));
		
		bf.iterate(100);
		
		}
	*/
	
	
	}
