/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
	private ImageWindowInterface w;
	
	public Shell currentShell=null;
	
	public ShellImageRenderer(ImageWindowInterface w)
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
		return w.getRootObject().getObjects(Shell.class);
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
		Vector2d so=w.transformPointW2S(new Vector2d(s.midx,s.midy)); 
		//Note that ellipse fails rotation

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
		//double wz=w.frameControl.getModelZ().doubleValue();
		double wz=w.getZ().doubleValue();
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
