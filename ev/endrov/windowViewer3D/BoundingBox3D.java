package endrov.windowViewer3D;

/**
 * 3D bounding box
 * 
 * @author Johan Henriksson
 *
 */
public class BoundingBox3D
	{
	public double xmin, xmax;
	public double ymin, ymax;
	public double zmin, zmax;

	public BoundingBox3D()
		{
		xmin=ymin=zmin=Double.MAX_VALUE;
		xmax=ymax=zmax=Double.MIN_VALUE;
		}
	
	public BoundingBox3D(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax)
		{
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.zmin = zmin;
		this.zmax = zmax;
		}


	public void addPointX(double p)
		{
		if(p<xmin)
			xmin=p;
		if(p>xmax)
			xmax=p;
		}

	public void addPointY(double p)
		{
		if(p<ymin)
			ymin=p;
		if(p>ymax)
			ymax=p;
		}

	public void addPointZ(double p)
		{
		if(p<zmin)
			zmin=p;
		if(p>zmax)
			zmax=p;
		}

	public void addPoint(double x, double y, double z)
		{
		addPointX(x);
		addPointY(y);
		addPointZ(z);
		}
	
	

	public void addBoundingBox(BoundingBox3D bb)
		{
		if(bb.xmax>xmax)
			xmax=bb.xmax;
		if(bb.xmin<xmin)
			xmax=bb.xmin;
		
		if(bb.ymax>ymax)
			ymax=bb.ymax;
		if(bb.ymin<ymin)
			ymin=bb.ymin;

		if(bb.zmax>zmax)
			zmax=bb.zmax;
		if(bb.zmin<zmin)
			zmin=bb.zmin;
		}
	
	
	}