package endrov.util;

/**
 * Mutable 2D integer vector
 * @author Johan Henriksson
 *
 */
public class Vector2i
	{
	public int x,y;
	
	public Vector2i()
		{
		this.x=0;
		this.y=0;
		}
	
	public Vector2i(int x, int y)
		{
		this.x=x;
		this.y=y;
		}
	
	public boolean equals(Object obj)
		{
		if(obj instanceof Vector2i)
			{
			Vector2i a=(Vector2i)obj;
			return x==a.x && y==a.y;
			}
		else
			return false;
		}
	
	public int hashCode()
		{
		return x^y;
		}
	
	@Override
	public String toString()
		{
		return "{"+x+","+y+"}";
		}
	
	
	}
