/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.math;


/**
 * Immutable 3D double vector
 * 
 * @author Johan Henriksson
 */
public class ImVector3d
	{
	/**
	 * Coordinate
	 */
	public double x, y, z;
	
	/**
	 * Make vector from eulerian coordinates
	 * @param x
	 * @param y
	 */
	public ImVector3d(double x, double y, double z)
		{
		this.x=x;
		this.y=y;
		this.z=z;
		}
	
	/**
	 * Rotate by angle
	 * @param angle Angle in radians
	 * @return Rotated vector
	 */
	/*
	public Vector3D rotate(double angle)
		{
		double nx= x*Math.cos(angle) + y*Math.sin(angle);
		double ny=-x*Math.sin(angle) + y*Math.cos(angle);
		return new Vector3D(nx,ny);
		}
		*/
	
	/**
	 * Vector addition
	 * @param v
	 * @return this+v
	 */
	public ImVector3d add(ImVector3d v)
		{
		return new ImVector3d(x+v.x, y+v.y, z+v.z);
		}

	/**
	 * Vector subtraction
	 * @param v
	 * @return this-v
	 */
	public ImVector3d sub(ImVector3d v)
		{
		return new ImVector3d(x-v.x, y-v.y, z-v.z);
		}
	
	/**
	 * Multiplication by scalar
	 * @param a
	 * @return this*a
	 */
	public ImVector3d mul(double a)
		{
		return new ImVector3d(x*a,y*a,z*a);
		}
	
	/**
	 * Length of vector
	 * @return Length of vector
	 */
	public double length()
		{
		return Math.sqrt(length2());
		}

	
	/**
	 * Length of vector
	 * @return Length of vector
	 */
	public double length2()
		{
		return x*x+y*y+z*z;
		}

	/**
	 * Normalize vector
	 * @return Normalized vector
	 */
	public ImVector3d normalize()
		{
		double len=length();
		double ilen=1.0/len;
		return new ImVector3d(x*ilen,y*ilen,z*ilen);
		}

	/**
	 * Dot product
	 * @param v Other vector
	 * @return this.v
	 */
	public double dot(ImVector3d v)
		{
		return x*v.x + y*v.y + z*v.z;
		}

	/**
	 * Cross product
	 * @return this x v
	 */
	public ImVector3d cross(ImVector3d v)
		{
		return new ImVector3d(
				y*v.z - z*v.y,
				z*v.x - x*v.z,
				x*v.y - y*v.x);
		}
	
	/**
	 * Create vector from polar coordinates
	 * @param len Length
	 * @param a Angle
	 * @return New vector
	 */
	/*
	public static Vector3D polar(double len, double a)
		{
		return new Vector3D(len*Math.cos(a), len*Math.sin(a));
		}
	*/
	}
