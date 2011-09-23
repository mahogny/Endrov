package endrov.colorspace;


/**
 * sRGB color space conversion
 * 
 * http://en.wikipedia.org/wiki/SRGB
 * @author mahogny
 *
 */
public class SRGB
	{
	
	/**
	 * Color triplet, scaled 0-1
	 * @author mahogny
	 *
	 */
	public static class Color
		{
		public double r,g,b;
		public Color(double r, double g, double b)
			{
			this.r = r;
			this.g = g;
			this.b = b;
			}
		}
	
	/**
	 * Input is assumed to be scaled 0-1
	 */
	public Color toSRGB(Color c)
		{
		double r=toSRGB( 3.2406*c.r + -1.5372*c.g + -0.4986*c.b);
		double g=toSRGB(-0.9689*c.r +  1.8758*c.g +  0.0415*c.b);
		double b=toSRGB( 0.0557*c.r + -0.2040*c.g +  1.0570*c.b);
		return new Color(r,g,b);
		}

	public Color toXYZ(Color c)
		{
		double r=toXYZ(0.4124*c.r + 0.3576*c.g + 0.1805*c.b);
		double g=toXYZ(0.2126*c.r + 0.7152*c.g + 0.0722*c.b);
		double b=toXYZ(0.0193*c.r + 0.1192*c.g + 0.9505*c.b);
		return new Color(r,g,b);
		}
	
	/**
	 * Input is assumed to be scaled 0-1
	 */
	public double toSRGB(double v)
		{
		double a=0.055;
		if(v<0.0031308)
			return 12.92*v;
		else
			return (1+a)*Math.pow(v,1/2.4)-a;
		}
	
	
	/**
	 * Input is assumed to be scaled 0-1
	 */
	public double toXYZ(double v)
		{
		double a=0.055;
		if(v<0.04045)
			return v/12.92;
		else
			return Math.pow((v+a)/a,2.4);
		}
	
	

	}
