package evplugin.modelWindow;

import evplugin.ev.*;

public class Matrix3
	{
	public final double mat[][]=new double[3][3];

	public static Matrix3 rotation100(double a)
		{
		double cosa=Math.cos(a);
		double sina=Math.sin(a);
		Matrix3 m=new Matrix3();
		m.mat[0][0]=1;
		m.mat[1][1]= cosa;	m.mat[1][2]=-sina;
		m.mat[2][1]= sina;	m.mat[2][2]= cosa;
		return m;
		}

	public static Matrix3 rotation010(double a)
		{
		double cosa=Math.cos(a);
		double sina=Math.sin(a);
		Matrix3 m=new Matrix3();
		m.mat[0][0]= cosa;	m.mat[0][2]= sina;
		m.mat[1][1]=1;
		m.mat[2][0]=-sina;	m.mat[2][2]= cosa;
		return m;
		}
	
	public static Matrix3 rotation001(double a)
		{
		double cosa=Math.cos(a);
		double sina=Math.sin(a);
		Matrix3 m=new Matrix3();
		m.mat[0][0]= cosa;	m.mat[0][1]=-sina;
		m.mat[1][0]= sina;	m.mat[1][1]= cosa;
		m.mat[2][2]=1;
		return m;
		}
	
	
	public Matrix3 mul(Matrix3 m)
		{
		Matrix3 n=new Matrix3();
		for(int i=0;i<3;i++)
			for(int j=0;j<3;j++)
				for(int k=0;k<3;k++)
					n.mat[i][j]+=mat[i][k] * m.mat[j][k];
		return n;
		}

	public Vector3D mul(Vector3D v)
		{
		double u[]=new double[3];
		double w[]=new double[]{v.x,v.y,v.z};
		for(int i=0;i<3;i++)
			for(int j=0;j<3;j++)
				u[i]+=mat[i][j]*w[j];
		return new Vector3D(u[0],u[1],u[2]);
		}
	
	
	public Matrix3 invert()
		{
		Matrix3 n=new Matrix3();
		double d=determinant();
		n.mat[0][0]=mat[0][0]/d;
		
		/*
		m11*m22 - m12*m21	m02*m21 - m01*m22	m01*m12 - m02*m11
		m12*m20 - m10*m22	m00*m22 - m02*m20	m02*m10 - m00*m12
		m10*m21 - m11*m20	m01*m20 - m00*m21	m00*m11 - m01*m10
		*/
		//TODO
		return n;
		}

	
	public double determinant()
		{
		return 
		+mat[0][0]*mat[1][1]*mat[2][2] 
		+mat[0][1]*mat[1][2]*mat[2][0]
		+mat[0][2]*mat[1][0]*mat[2][1]
		-mat[0][0]*mat[1][2]*mat[2][1]
		-mat[0][1]*mat[1][0]*mat[2][2]
		-mat[0][2]*mat[1][1]*mat[2][0];
		}
	
	public void print()
		{
		for(int i=0;i<3;i++)
			{
			for(int j=0;j<3;j++)
				System.out.print("\t"+mat[i][j]);
			System.out.println("");
			}
		
		}
	
	/*
	public static Matrix3 rotation(double a, Vector3D axis)
		{
		Matrix3 S=new Matrix3();
		axis=axis.normalize();
		
		S.mat[0][1]=-axis.z;		S.mat[0][2]= axis.y;
		S.mat[1][0]= axis.z;		S.mat[1][2]=-axis.x;
		S.mat[2][0]=-axis.y;		S.mat[2][1]= axis.x;

		double ulen=axis.length2();
		
		
		
		return m;
		}
	
	public static Matrix3 transpose(Matrix3 m)
		{
		Matrix3 n=new Matrix3();
		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++)
				n.mat[i][j]=m.mat[j][i];
		return n;
		}

	
	public Matrix3 mul(double s)
		{
		Matrix3 n=new Matrix3();
		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++)
				n.mat[i][j]=mat[i][j]*s;
		return n;
		}
	*/
	}
