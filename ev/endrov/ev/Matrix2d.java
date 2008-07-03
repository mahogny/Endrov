package endrov.ev;

import javax.vecmath.*;

public final class Matrix2d
	{
	double m00, m01, m10, m11;
	
	public Matrix2d()
		{
		}

	public void mul(double scalar, Matrix2d m)
		{
		m00=m.m00*scalar;	m01=m.m01*scalar;
		m10=m.m10*scalar;	m11=m.m11*scalar;
		}

	public void mul(double scalar)
		{
		mul(scalar, this);
		}
	
	public void negate()
		{
		mul(-1);
		}
	
	public void rot(double angle)
		{
		double c=Math.cos(angle);
		double s=Math.sin(angle);
		m00=c;	m01=-s;
		m10=s;	m11= c;
		}
	
	public void transform(Tuple2d t, Tuple2d result)
		{
		double x,y;
		x=m00*t.x + m01*t.y;
		y=m10*t.x + m11*t.y;
		result.x=x;
		result.y=y;
		}
	
	public void transform(Tuple2d t)
		{
		transform(t, t);
		}
	
	public double determinant()
		{
		return m00*m11-m10*m01;
		}
	
	}
