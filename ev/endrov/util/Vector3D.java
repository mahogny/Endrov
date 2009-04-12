package endrov.util;


/**
 * Immutable 3D double vector
 * 
 * @author Johan Henriksson
 */
public class Vector3D
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
	public Vector3D(double x, double y, double z)
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
	public Vector3D add(Vector3D v)
		{
		return new Vector3D(x+v.x, y+v.y, z+v.z);
		}

	/**
	 * Vector subtraction
	 * @param v
	 * @return this-v
	 */
	public Vector3D sub(Vector3D v)
		{
		return new Vector3D(x-v.x, y-v.y, z-v.z);
		}
	
	/**
	 * Multiplication by scalar
	 * @param a
	 * @return this*a
	 */
	public Vector3D mul(double a)
		{
		return new Vector3D(x*a,y*a,z*a);
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
	public Vector3D normalize()
		{
		double len=length();
		double ilen=1.0/len;
		return new Vector3D(x*ilen,y*ilen,z*ilen);
		}

	/**
	 * Dot product
	 * @param v Other vector
	 * @return this.v
	 */
	public double dot(Vector3D v)
		{
		return x*v.x + y*v.y + z*v.z;
		}

	/**
	 * Cross product
	 * @return this x v
	 */
	public Vector3D cross(Vector3D v)
		{
		return new Vector3D(
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
