package endrov.util;

/**
 * 3D vector, integer
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
	
	
	
	}