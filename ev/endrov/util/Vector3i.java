/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

/**
 * Mutable 3D integer vector
 * @author Johan Henriksson
 */
public class Vector3i
	{
	public int x,y,z;
	
	public Vector3i(int x, int y, int z)
		{
		this.x = x;
		this.y = y;
		this.z = z;
		}
	public String toString()
		{
		return "("+x+","+y+","+z+")";
		}
	
	
	public boolean equals(Object obj)
		{
		if(obj instanceof Vector3i)
			{
			Vector3i a=(Vector3i)obj;
			return x==a.x && y==a.y && z==a.z;
			}
		else
			return false;
		}
	
	public int hashCode()
		{
		return x^y^z;
		}
	
	public void subtract(Vector3i v)
		{
		v.x-=v.x;
		v.y-=v.y;
		v.z-=v.z;
		}
	
	
	
	}
