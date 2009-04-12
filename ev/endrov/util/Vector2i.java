package endrov.util;

/**
 * 2D integer vector
 * @author Johan Henriksson
 *
 */
public class Vector2i
	{
	public int x,y;
	
	public Vector2i(int x, int y)
		{
		this.x=x;
		this.y=y;
		}
	
	public boolean equals(Object obj)
		{
		if(obj instanceof Vector2i)
			{
			Vector3i a=(Vector3i)obj;
			return x==a.x && y==a.y;
			}
		else
			return false;
		}
	
	public int hashCode()
		{
		return x^y;
		}
	
	
	
	}
