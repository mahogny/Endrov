package endrov.util;


/**
 * Immutable 2D double vector
 * 
 * @author Johan Henriksson
 */
public class Vector2D
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
	public Vector2D(double x, double y)
		{
		this.x=x;
		this.y=y;
		}
	
	/**
	 * Rotate by angle
	 * @param angle Angle in radians
	 * @return Rotated vector
	 */
	public Vector2D rotate(double angle)
		{
		double nx= x*Math.cos(angle) + y*Math.sin(angle);
		double ny=-x*Math.sin(angle) + y*Math.cos(angle);
		return new Vector2D(nx,ny);
		}
	
	/**
	 * Vector addition
	 * @param v
	 * @return this+v
	 */
	public Vector2D add(Vector2D v)
		{
		return new Vector2D(x+v.x, y+v.y);
		}

	/**
	 * Vector subtraction
	 * @param v
	 * @return this-v
	 */
	public Vector2D sub(Vector2D v)
		{
		return new Vector2D(x-v.x, y-v.y);
		}
	
	/**
	 * Multiplication by scalar
	 * @param a
	 * @return this*a
	 */
	public Vector2D mul(double a)
		{
		return new Vector2D(x*a,y*a);
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
	public Vector2D normalize()
		{
		double len=length();
		return new Vector2D(x/len,y/len);
		}

	/**
	 * Dot product
	 * @param v Other vector
	 * @return this.v
	 */
	public double dot(Vector2D v)
		{
		return x*v.x + y*v.y;
		}

	/**
	 * Create vector from polar coordinates
	 * @param len Length
	 * @param a Angle
	 * @return New vector
	 */
	public static Vector2D polar(double len, double a)
		{
		return new Vector2D(len*Math.cos(a), len*Math.sin(a));
		}
	
	}
