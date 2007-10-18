package evplugin.shell;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

import evplugin.data.*;
import evplugin.imageWindow.*;

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

	
	public Vector<Shell> getShells()
		{
		Vector<Shell> list=new Vector<Shell>();
		for(EvObject ob:w.getImageset().metaObject.values())
			if(ob instanceof Shell)
				list.add((Shell)ob);
		return list;
		}
	

	
	/**
	 * Draw the egg shell
	 */
	private void drawShell(Graphics g, Shell s)
		{
//		if(s.exists())
			{
			Graphics2D g2=(Graphics2D)g;
			g.setColor(Color.GREEN);
			
			double plongaxis=projectSphere(s.major, s.midz);
			double pshortaxis=projectSphere(s.minor, s.midz);
			
			//Coordinate transformation
			double sox=w.w2sx(s.midx);
			double soy=w.w2sy(s.midy);
			
			double polarrad=10;
			
			g2.rotate(s.angle,sox,soy);
			g2.draw(new Ellipse2D.Double((double)(sox-plongaxis),(double)(soy-pshortaxis),(double)(2*plongaxis),(double)(2*pshortaxis)));
			g2.draw(new Ellipse2D.Double((double)(sox+plongaxis-polarrad),(double)(soy-polarrad),polarrad*2, polarrad*2));
			g2.rotate(-s.angle,sox,soy);
			
		//	System.out.println("# "+sox+" "+soy);
			
			}
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
		double wz=w.s2wz(w.frameControl.getZ());
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
