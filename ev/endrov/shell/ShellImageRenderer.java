package endrov.shell;

import java.util.List;
import java.awt.*;
import java.awt.geom.*;
import javax.vecmath.*;

import endrov.imageWindow.*;

/**
 * Render shells in image window
 * @author Johan Henriksson
 */
public class ShellImageRenderer implements ImageWindowRenderer
	{
	private ImageWindow w;
	
	public Shell currentShell=null;
	
	public ShellImageRenderer(ImageWindow w)
		{
		this.w=w;
		}
	
	public void draw(Graphics g)
		{
		for(Shell shell:getShells())
			drawShell(g, shell);
		}
	
	public void dataChangedEvent()
		{
		}

	
	public List<Shell> getShells()
		{
		return w.getImageset().getObjects(Shell.class);
		}
	

	
	/**
	 * Draw the egg shell
	 */
	private void drawShell(Graphics g, Shell s)
		{
		Graphics2D g2=(Graphics2D)g;
		g.setColor(Color.GREEN);

		double plongaxis=projectSphere(s.major, s.midz);
		double pshortaxis=projectSphere(s.minor, s.midz);

		//Coordinate transformation
		Vector2d so=w.transformW2S(new Vector2d(s.midx,s.midy)); //TODO: ellipse fails rotation

		double polarrad=10;
		double angle=s.angle+w.getRotation();

		g2.rotate(angle,so.x,so.y);
		g2.draw(new Ellipse2D.Double((double)(so.x-plongaxis),(double)(so.y-pshortaxis),(double)(2*plongaxis),(double)(2*pshortaxis)));
		g2.draw(new Ellipse2D.Double((double)(so.x+plongaxis-polarrad),(double)(so.y-polarrad),polarrad*2, polarrad*2));
		g2.rotate(-angle,so.x,so.y);
		}
	
	
	
	/**
	 * Project sphere onto plane
	 * @param r Radius
	 * @param z Relative z
	 * @return Projected radius in pixels
	 */
	private double projectSphere(double r, double z)
		{
		//Currently assumes resx=resy. Maybe this should be specified harder?
		double wz=w.frameControl.getModelZ().doubleValue();
//		w.s2wz(w.frameControl.getZ());
		double tf=r*r-(z-wz)*(z-wz);
		if(tf>0)
			{
			double wpr=Math.sqrt(tf);
			return w.scaleW2s(wpr);	
			}
		else
			return -1;
		}
	}
