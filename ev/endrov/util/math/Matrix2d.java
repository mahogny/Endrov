/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.math;

import javax.vecmath.*;

/**
 * 2D matrix, missing in Java3D. Follows the same style
 * @author Johan Henriksson
 *
 */
public final class Matrix2d
	{
	double m00, m01, m10, m11;
	
	public Matrix2d()
		{
		}

	public Matrix2d(double m00, double m01, double m10, double m11)
		{
		this.m00=m00;
		this.m01=m01;
		this.m10=m10;
		this.m11=m11;
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
		double x=m00*t.x + m01*t.y;
		double y=m10*t.x + m11*t.y;
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

	public void invert(Matrix2d result)
		{
		double det=determinant();
		double temp=m00;
		result.m00=m11/det;
		result.m11=temp/det;
		double bar=m01;
		result.m01=-m10/det;
		result.m10=-bar/det;
		}

	public void invert()
		{
		invert(this);
		}
	
	
	public String toString()
		{
		return "["+m00+" "+m01+"; "+m10+" "+m11+"]";
		}
	}
