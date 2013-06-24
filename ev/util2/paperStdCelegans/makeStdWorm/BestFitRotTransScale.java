/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperStdCelegans.makeStdWorm;

import java.util.*;
import javax.vecmath.*;

import endrov.typeLineage.Lineage;
import static java.lang.Math.*;


/**
 * Overlap several lineage objects
 * 
 * @author Johan Henriksson
 *
 */
public class BestFitRotTransScale
	{
	public Map<Lineage,TransformedLineage> transformedLineage=new HashMap<Lineage, TransformedLineage>();
	public List<LineagePair> pair=new LinkedList<LineagePair>();
	public Lineage refLin;
	
	
	
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * One lineage about to be transformed
	 */
	public static class TransformedLineage
		{
		public double[] x=new double[]{0,0,0, 0,0,0, 1,1,1};
		public double[] dx=new double[]{0,0,0, 0,0,0, 0,0,0};
		public Map<String,Vector3d[]> dtransform=new HashMap<String, Vector3d[]>();
		public Map<String,Vector3d> transformedPoint=new HashMap<String, Vector3d>();
		public Map<String,Vector3d> originalPoint=new HashMap<String, Vector3d>();
		public Map<String,Double> untransformedR=new HashMap<String, Double>();
		public int numpoint;
		
		public synchronized void synchAddDx(double dx[])
			{
			for(int i=0;i<dx.length;i++)
				this.dx[i]+=dx[i];
			}
		
		
		/**
		 * Transform all new points
		 */
		public Vector<Vector3d> transformPoints(Vector<Vector3d> points, double x[])
			{
			Vector<Vector3d> transformed=new Vector<Vector3d>();
			Matrix3d rotx=rotx(x[4-1]);
			Matrix3d roty=roty(x[5-1]);
			Matrix3d rotz=rotz(x[6-1]);
			Matrix3d rotxp=rotxp(x[4-1]);
			Matrix3d rotyp=rotyp(x[5-1]);
			Matrix3d rotzp=rotzp(x[6-1]);
			Vector3d trans=new Vector3d(x[1-1],x[2-1],x[3-1]);
			double scaleX=x[7-1];
			double scaleY=x[8-1];
			double scaleZ=x[9-1];
			for(int i=0;i<points.size();i++)
				{
				Vector3d v=points.get(i);
				Vector3d tv=BestFitRotTransScale.transformPointWithDiff(rotx, roty, rotz, rotxp, rotyp, rotzp, trans, scaleX, scaleY, scaleZ, v, -1);
				transformed.add(tv);
				}
			return transformed;
			}

		/**
		 * Transform one point
		 */
		public Vector3d transformPoint(Vector3d v, double x[])
			{
			Vector<Vector3d> vv=new Vector<Vector3d>();
			vv.add(v);
			vv=transformPoints(vv,x);
			return vv.get(0);
			}
		
		/**
		 * Update transforms
		 *
		 */
		public void update()
			{
			transformedPoint.clear();
			dtransform.clear();
			numpoint=0;
			dx=new double[]{0,0,0, 0,0,0, 0,0,0};
			
			
			Vector3d trans=new Vector3d(x[1-1],x[2-1],x[3-1]);
			Matrix3d rotx=rotx(x[4-1]);
			Matrix3d roty=roty(x[5-1]);
			Matrix3d rotz=rotz(x[6-1]);
			Matrix3d rotxp=rotxp(x[4-1]);
			Matrix3d rotyp=rotyp(x[5-1]);
			Matrix3d rotzp=rotzp(x[6-1]);
			double scaleX=x[7-1];
			double scaleY=x[8-1];
			double scaleZ=x[9-1];
			
			for(Map.Entry<String, Vector3d> entry:originalPoint.entrySet())
				{
				Vector3d v=entry.getValue();
				Vector3d tv=BestFitRotTransScale.transformPointWithDiff(rotx, roty, rotz, rotxp, rotyp, rotzp, trans, 
						scaleX, scaleY, scaleZ, 
						v, -1);
				transformedPoint.put(entry.getKey(), tv);
				
				Vector3d[] diff=new Vector3d[dx.length];
				for(int diffIndex=1;diffIndex<=dx.length;diffIndex++)
					diff[diffIndex-1]=BestFitRotTransScale.transformPointWithDiff(rotx, roty, rotz, rotxp, rotyp, rotzp, trans,
							scaleX, scaleY, scaleZ, 
							v, diffIndex);
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
	public static class LineagePair
		{
		public HashMap<String,Double> commonNuc=new HashMap<String,Double>();		
		public Lineage goalLin;
		public Lineage newLin;
		
		
		private void addCommonNuc(String nuc, double weight)
			{
			commonNuc.put(nuc,weight);
			}
		
		/**
		 * Sum up dx for one pair
		 * 
		 * dx = sum_points  sum_diffindex   2 (d/diffIndex trans) . (point in "new" - point in "goal" ) * weight
		 * 
		 */
		public void sumDxOneIteration(BestFitRotTransScale fitter)
			{
			TransformedLineage newInfo=fitter.transformedLineage.get(newLin);
			TransformedLineage goalInfo=fitter.transformedLineage.get(goalLin);
			double newdx[]=new double[9];
			double goaldx[]=new double[newdx.length];
			for(String nucname:commonNuc.keySet())
				{
				Vector3d tv=newInfo.transformedPoint.get(nucname);
				Vector3d gv=goalInfo.transformedPoint.get(nucname);
				Vector3d diff=new Vector3d(tv);
				diff.sub(gv);
				fitter.incNumPointEps(diff.lengthSquared());
				
				double weight=commonNuc.get(nucname);
				
				
				for(int j=1;j<=newdx.length;j++)
					{
					Vector3d dtrans=newInfo.dtransform.get(nucname)[j-1];
					newdx[j-1]+=2*dtrans.dot(diff)*weight;
					
					//Difference inverted, changes sign
					Vector3d dtrans2=goalInfo.dtransform.get(nucname)[j-1];
					goaldx[j-1]-=2*dtrans2.dot(diff)*weight;
					}
				}
			newInfo.numpoint+=commonNuc.size();
			goalInfo.numpoint+=commonNuc.size();
			newInfo.synchAddDx(newdx);
			goalInfo.synchAddDx(goaldx);
			}
		
		
		}
	
	

	
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////

	
	private double eps;
	
	private void incNumPointEps(double eps)
		{
		this.eps+=eps;
		}

	
	public BestFitRotTransScale()
		{
		}
	public BestFitRotTransScale(BestFitRotTransScale bf)
		{
		//Shallow copy
		transformedLineage.putAll(bf.transformedLineage);
		pair.addAll(bf.pair);
		}
	
	
	
	/**
	 * Add one lineage to fit
	 */
	public void addLineage(Lineage lin)
		{
		if(!transformedLineage.containsKey(lin))
			{
			for(Lineage lin2:transformedLineage.keySet())
				{
				LineagePair p=new LineagePair();
				p.goalLin=lin;
				p.newLin=lin2;
				pair.add(p);
				}
			
			transformedLineage.put(lin,new TransformedLineage());
			}
		}
	
	public void findCommonNuc()
		{
		for(LineagePair p:pair)
			{
			p.commonNuc.clear();
			for(String nucname:transformedLineage.get(p.newLin).originalPoint.keySet())
				p.addCommonNuc(nucname,1);
			p.commonNuc.keySet().retainAll(transformedLineage.get(p.goalLin).originalPoint.keySet());
			}
		}
	
	
	/**
	 * Iterate until done
	 */
	public void iterate(int minit, int maxit, double okEps)
		{
		for(int i=0;i<maxit;i++)
			{
			doOneIteration(true);
			
			if(i%40==0)
				System.out.println("eps "+eps);
			if((i>minit))
				{
				System.out.println("ok");
				break;
				}
			}
		}
	
	/**
	 * Do one single iteration
	 */
	public void doOneIteration(boolean noScaling)
		{
		//Transform all points
		eps=0;
		for(TransformedLineage info:transformedLineage.values())
			info.update();
		
		//For every pair, calculate how to move
		//Turns out to be faster single-threaded
		for(LineagePair p:pair)
			p.sumDxOneIteration(this);

		//Constants, how fast to move toward the derivative
		double lambdaT=0.1;
		double lambdaR=0.0001;
		double lambdaS=0.0001;

		//numpoint for each info?

		//Apply move
		for(TransformedLineage info:transformedLineage.values())
			if(info.numpoint>0)
				{
				//Weigh each dx (speed of convergence)
				for(int i=0;i<3;i++)
					info.dx[i]*=lambdaT/info.numpoint;
				for(int i=3;i<6;i++)
					info.dx[i]*=lambdaR/info.numpoint;
				for(int i=6;i<9;i++)
					info.dx[i]*=lambdaS/info.numpoint;

				//Update transformation:  new x = x - x'
				for(int i=0;i<info.x.length;i++)
					info.x[i]-=info.dx[i];

				//Restrict scaling values
				for(int i=7;i<=9;i++)
					{
					double s=info.x[i-1];
					if(s<0.01)
						s=0.01;
					if(s>100)
						s=100;
					if(noScaling)
						s=1;
					info.x[i-1]=s;
					}
				
				}

		//Restore reference rotation
//		lininfo.get(refLin).x=new double[]{0,0,0, 0,0,0, 1};
//if not activated, we should get some sort of average

		
//		System.out.println("eps: "+eps+"  scale: "+x[7-1]+"    rot   "+x[4-1]+" "+x[5-1]+" "+x[6-1]+"    trans "+x[1-1]+" "+x[2-1]+" "+x[3-1]);
//		System.out.println("diff: scale: "+dx[7-1]+"    rot   "+dx[4-1]+" "+dx[5-1]+" "+dx[6-1]+"    trans "+dx[1-1]+" "+dx[2-1]+" "+dx[3-1]);
		}
	
	
	
	
	
	
	
	
	
	
	
	
	

	

	///////////////////////// general transformation ///////////////////////////////////
	
	public static Vector3d transformPointWithDiff(
			Matrix3d rotx, Matrix3d roty, Matrix3d rotz,
			Matrix3d rotxp, Matrix3d rotyp, Matrix3d rotzp,
			Vector3d trans,	
			double scaleX, double scaleY, double scaleZ,
			Vector3d point, int diffi)
		{
		Vector3d v=new Vector3d(point);
		
		//Scaling
		if(diffi==9) ; else v.z*=scaleZ;
		if(diffi==8) ; else v.y*=scaleY;
		if(diffi==7) ; else v.x*=scaleX;
		
		//Rotation
		if(diffi==6) rotzp.transform(v); else rotz.transform(v);
		if(diffi==5) rotyp.transform(v); else roty.transform(v);
		if(diffi==4) rotxp.transform(v); else rotx.transform(v);
		
		//Translation
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




/*
final BestFitRotTransScale This=this;
EvParallel.map_(pair, new EvParallel.FuncAB<LinPair, Object>(){
	public Object func(LinPair in)
		{
		in.oneIteration(This);
		return null;
		}
});
*/
