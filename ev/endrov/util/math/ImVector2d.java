/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.math;


/**
 * Immutable 2D double vector
 * 
 * @author Johan Henriksson
 */
public class ImVector2d
	{
	/**
	 * Coordinate
	 */
	public double x, y;
	
	/**
	 * Make vector from eulerian coordinates
	 * @param x
	 * @param y
	 */
	public ImVector2d(double x, double y)
		{
		this.x=x;
		this.y=y;
		}
	
	/**
	 * Rotate by angle
	 * @param angle Angle in radians
	 * @return Rotated vector
	 */
	public ImVector2d rotate(double angle)
		{
		double nx= x*Math.cos(angle) + y*Math.sin(angle);
		double ny=-x*Math.sin(angle) + y*Math.cos(angle);
		return new ImVector2d(nx,ny);
		}
	
	/**
	 * Vector addition
	 * @param v
	 * @return this+v
	 */
	public ImVector2d add(ImVector2d v)
		{
		return new ImVector2d(x+v.x, y+v.y);
		}

	/**
	 * Vector subtraction
	 * @param v
	 * @return this-v
	 */
	public ImVector2d sub(ImVector2d v)
		{
		return new ImVector2d(x-v.x, y-v.y);
		}
	
	/**
	 * Multiplication by scalar
	 * @param a
	 * @return this*a
	 */
	public ImVector2d mul(double a)
		{
		return new ImVector2d(x*a,y*a);
		}
	
	/**
	 * Length of vector
	 * @return Length of vector
	 */
	public double length()
		{
		return Math.sqrt(x*x+y*y);
		}
	
	/**
	 * Normalize vector
	 * @return Normalized vector
	 */
	public ImVector2d normalize()
		{
		double len=length();
		return new ImVector2d(x/len,y/len);
		}

	/**
	 * Dot product
	 * @param v Other vector
	 * @return this.v
	 */
	public double dot(ImVector2d v)
		{
		return x*v.x + y*v.y;
		}

	/**
	 * Create vector from polar coordinates
	 * @param len Length
	 * @param a Angle
	 * @return New vector
	 */
	public static ImVector2d polar(double len, double a)
		{
		return new ImVector2d(len*Math.cos(a), len*Math.sin(a));
		}
	
	}
