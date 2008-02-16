package util2.makeStdWorm;

import java.util.*;
import javax.vecmath.*;
import static java.lang.Math.*;



public class BestFit
	{
	double x[]=new double[]{0,0,0, 0,0,0, 1}; //tx ty tz   rx ry rz  s
	
	public Vector<Vector3d> goalpoint=new Vector<Vector3d>();
	public Vector<Vector3d> newpoint=new Vector<Vector3d>();
	public double eps;

	public double getTx(){return x[1-1];}
	public double getTy(){return x[2-1];}
	public double getTz(){return x[3-1];}
	public double getRx(){return x[4-1];}
	public double getRy(){return x[5-1];}
	public double getRz(){return x[6-1];}
	public double getScale(){return x[7-1];}

	
	public void clear()
		{
		goalpoint.clear();
		newpoint.clear();
		}
	
	public Vector<Vector3d> getTransformed()
		{
		Vector<Vector3d> transformed=new Vector<Vector3d>();
		Matrix3d rotx=rotx(getRx());
		Matrix3d roty=roty(getRy());
		Matrix3d rotz=rotz(getRz());
		Matrix3d rotxp=rotxp(getRx());
		Matrix3d rotyp=rotyp(getRy());
		Matrix3d rotzp=rotzp(getRz());
		Vector3d trans=new Vector3d(getTx(),getTy(),getTz());
		double scale=x[7-1];
		for(int i=0;i<newpoint.size();i++)
			{
			Vector3d v=newpoint.get(i);
			Vector3d tv=transform(rotx, roty, rotz, rotxp, rotyp, rotzp, trans, scale, v, -1);
			transformed.add(tv);
			}
		return transformed;
		}
	
	
	public Vector3d transform(Vector3d v)
		{
		Matrix3d rotx=rotx(getRx());
		Matrix3d roty=roty(getRy());
		Matrix3d rotz=rotz(getRz());
		Matrix3d rotxp=rotxp(getRx());
		Matrix3d rotyp=rotyp(getRy());
		Matrix3d rotzp=rotzp(getRz());
		Vector3d trans=new Vector3d(getTx(),getTy(),getTz());
		double scale=getScale();
		return transform(rotx, roty, rotz, rotxp, rotyp, rotzp, trans, scale, v, -1);
		}
	
	
	public void iterate(int minit, int maxit, double okEps)
		{
		for(int i=0;i<maxit;i++)
			{
			doOneIteration();
			if(i>minit && eps<okEps)
				break;
			}
//		System.exit(0);
		}
	
	public void doOneIteration()
		{
		Matrix3d rotx=rotx(getRx());
		Matrix3d roty=roty(getRy());
		Matrix3d rotz=rotz(getRz());
		Matrix3d rotxp=rotxp(getRx());
		Matrix3d rotyp=rotyp(getRy());
		Matrix3d rotzp=rotzp(getRz());
		Vector3d trans=new Vector3d(getTx(),getTy(),getTz());
		double scale=x[7-1];
		double dx[]=new double[7];		
		eps=0;
		
		for(int i=0;i<newpoint.size();i++)
			{
			Vector3d v=newpoint.get(i);
			Vector3d gv=goalpoint.get(i);
			Vector3d tv=transform(rotx, roty, rotz, rotxp, rotyp, rotzp, trans, scale, v, -1);
			
			Vector3d diff=new Vector3d(tv);
			diff.sub(gv);
			eps+=diff.lengthSquared();
			
			for(int j=1;j<=7;j++)
				{
				Vector3d dtrans=transform(rotx, roty, rotz, rotxp, rotyp, rotzp, trans, scale, v, j);
				dx[j-1]+=2*dtrans.dot(diff);
				}
			}
		eps/=newpoint.size();

		double lambdaT=0.1;
		double lambdaR=0.0001;
		double lambdaS=0.0001;
		for(int i=0;i<3;i++)
			dx[i]*=lambdaT/newpoint.size();
		for(int i=3;i<6;i++)
			dx[i]*=lambdaR/newpoint.size();
		for(int i=6;i<7;i++)
			dx[i]*=lambdaS/newpoint.size();
		
		for(int i=0;i<7;i++)
			x[i]-=dx[i];

		//Restrict values
		if(x[7-1]<0.01)
			x[7-1]=0.01;
		if(x[7-1]>100)
			x[7-1]=100;
		x[7-1]=1;
		
//		System.out.println("eps: "+eps+"  scale: "+x[7-1]+"    rot   "+x[4-1]+" "+x[5-1]+" "+x[6-1]+"    trans "+x[1-1]+" "+x[2-1]+" "+x[3-1]);
//		System.out.println("diff: scale: "+dx[7-1]+"    rot   "+dx[4-1]+" "+dx[5-1]+" "+dx[6-1]+"    trans "+dx[1-1]+" "+dx[2-1]+" "+dx[3-1]);
		}
	
	
	
	private Vector3d transform(
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


	private Vector3d getTrans(Vector3d trans, int diffi)
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
