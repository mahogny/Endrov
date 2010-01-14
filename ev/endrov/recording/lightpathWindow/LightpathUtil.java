/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.lightpathWindow;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;



/**
 * 
 * @author Johan Henriksson
 *
 */
public class LightpathUtil
	{
	private static final int beamR=6;
	//private static final int beamSigma=3;
	

	
	public static final ImageIcon iconObjective=new ImageIcon(LightpathUtil.class.getResource("jhObjective2.png"));
	public static final ImageIcon iconToEye=new ImageIcon(LightpathUtil.class.getResource("jhEye.png"));
	public static final ImageIcon iconToCam=new ImageIcon(LightpathUtil.class.getResource("jhCamera.png"));
	public static final ImageIcon iconLightSource=new ImageIcon(LightpathUtil.class.getResource("jhLightSource.png"));
	public static final ImageIcon iconLenseVertical=new ImageIcon(LightpathUtil.class.getResource("wikiLenseVertical.png"));

	

	public static void drawObjective(Graphics g, int x, int y)
		{
		g.drawImage(iconObjective.getImage(), x-iconObjective.getIconWidth()/2, y, null);
		}
	

	public static void drawToEye(Graphics g, int x, int y)
		{
		g.drawImage(iconToEye.getImage(), x-iconToEye.getIconWidth()/2, y-iconToEye.getIconHeight()/2, null);
		}
	
	public static void drawToCam(Graphics g, int x, int y)
		{
		g.drawImage(iconToCam.getImage(), x-iconToCam.getIconWidth()/2, y-iconToCam.getIconHeight()/2, null);
		}
	
	
	public static void drawLenseVertical(Graphics g, int x, int y)
		{
		g.drawImage(iconLenseVertical.getImage(), x-iconLenseVertical.getIconWidth()/2, y-iconLenseVertical.getIconHeight()/2, null);
		}
	
	public static void drawLightSource(Graphics g, int x, int y)
		{
		g.drawImage(iconLightSource.getImage(), x-iconLightSource.getIconWidth()/2, y-iconLightSource.getIconHeight()/2, null);
		}

	
	private static float fmin1(double x)
		{
		return Math.max(Math.min(0.9f,(float)x),0f);
		}
	
	public static void drawLightBeamHorizonal(Graphics g, Color c, int x1, int x2, int midy)
		{
		float[] carr=new float[3];
		c.getRGBColorComponents(carr);
		//System.out.println(carr[2]);
		for(int i=-beamR;i<beamR;i++)
			{
			float weight=fmin1((beamR-Math.abs(i))/(double)beamR);
			
			g.setColor(new Color(carr[0],carr[1],carr[2],weight));
			g.drawLine(x1, midy+i, x2, midy+i);
			}
		}

	public static void drawLightBeamVertical(Graphics g, Color c,  int midx, int y1, int y2)
		{
		float[] carr=new float[3];
		c.getRGBColorComponents(carr);
		for(int i=-beamR;i<beamR;i++)
			{
			float weight=fmin1((beamR-Math.abs(i))/(double)beamR);
			g.setColor(new Color(carr[0],carr[1],carr[2],weight));
			g.drawLine(midx+i, y1, midx+i,y2);
			}
		}

	/**
	 * Draw stage with sample
	 */
	public static void drawStage(Graphics g, int x, int y)
		{
		int dy=6;
		int[] xPoints=new int[]{x-20, x+20, x+25, x-25};
		int[] yPoints=new int[]{y-dy, y-dy, y+dy, y+dy};
		g.setColor(Color.GRAY);
		g.fillPolygon(xPoints, yPoints, 4);
		g.setColor(Color.BLACK);
		g.drawPolygon(xPoints, yPoints, 4);
		}
	

	

	public static void drawBeamsplit(Graphics g, int midx, int midy, Color cup, Color cdown, Color cright, boolean showMirror, boolean isDichroic)
		{
		int boxR=15;
		
		//Box
		g.setColor(Color.lightGray);
		g.fillRect(midx-boxR, midy-boxR, boxR*2, boxR*2);
		//g.drawRect(midx-boxR, midy-boxR, boxR*2, boxR*2);
		
		//Beams
		float[] carr=new float[3];
		if(cright!=null)
			{
			cright.getColorComponents(carr);
			for(int i=-beamR;i<beamR;i++)
				{
				float weight=fmin1((beamR-Math.abs(i))/(double)beamR);
				g.setColor(new Color(carr[0],carr[1],carr[2],weight));
				g.drawLine(midx-i, midy+i, midx+boxR, midy+i);
				}
			}
		if(cdown!=null)
			{
			cdown.getColorComponents(carr);
			for(int i=-beamR;i<beamR;i++)
				{
				float weight=fmin1((beamR-Math.abs(i))/(double)beamR);
				g.setColor(new Color(carr[0],carr[1],carr[2],weight));
				g.drawLine(midx+i, midy-i, midx+i,midy+boxR);
				}
			}
		if(cup!=null)
			{
			cup.getColorComponents(carr);
			for(int i=-beamR;i<beamR;i++)
				{
				float weight=fmin1((beamR-Math.abs(i))/(double)beamR);
				g.setColor(new Color(carr[0],carr[1],carr[2],weight));
				g.drawLine(midx+i, midy-boxR, midx+i,midy-i);
				}
			}
		
		//Mirror
		if(showMirror)
			{
			if(isDichroic)
				{
				float [] Dashes = {3.0F, 3.0F};
				g=g.create();
				((Graphics2D)g).setStroke(new BasicStroke (1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, Dashes, 0.F));
				}
			g.setColor(Color.BLACK);
			g.drawLine(midx-boxR, midy+boxR, midx+boxR, midy-boxR);
			}
		}
	

	public static void drawBeamWaistUpper(Graphics g, Color c, int midx, int y1, int y2)
		{
		drawBeamVariableRadiusVertical(g, c, midx, y1, y2, beamR, 1);
		}
	
	
	public static void drawBeamWaistLower(Graphics g, Color c, int midx, int y1, int y2)
		{
		drawBeamVariableRadiusVertical(g, c, midx, y1, y2, 1, beamR);
		}
	
	public static void drawBeamVariableRadiusVertical(Graphics g, Color c, int midx, int y1, int y2, int r1, int r2)
		{
		float[] carr=c.getRGBColorComponents(null);
		for(int y=y1;y<y2;y++)
			{
			double fact=(y-y1)/(double)(y2-y1);
			int hereBeamR=(int)((1-fact)*r1+fact*r2);
			
			for(int i=-hereBeamR;i<hereBeamR;i++)
				{
				double weight=1-Math.abs(i)/(double)hereBeamR;
				g.setColor(new Color(carr[0],carr[1],carr[2],(float)weight));
				g.drawLine(midx+i, y, midx+i,y);
				}
			}
		}
	
	/**
	 * Draw a filter in horizontal direction
	 * @param c Color, can be transparent. If null, no color
	 * @param transparency Modulate color 0..1
	 */
	public static void drawFilterHorizontal(Graphics g, Color c, double transparency, int midx, int midy)
		{
		drawFilter(g, c, transparency, midx, midy, 10, 15);
		}
	
	
	/**
	 * Draw a filter in vertical direction
	 * @param c Color, can be transparent. If null, no color
	 * @param transparency Modulate color 0..1
	 */
	public static void drawFilterVertical(Graphics g, Color c, double transparency, int midx, int midy)
		{
		drawFilter(g, c, transparency, midx, midy, 15, 10);
		}
	
	/**
	 * Draw a filter with specified dimensions
	 * @param c Color, can be transparent. If null, no color
	 * @param transparency Modulate color 0..1
	 */
	public static void drawFilter(Graphics g, Color c, double transparency, int midx, int midy, int rx, int ry)
		{
		g.setColor(Color.BLACK);
		g.drawOval(midx-rx, midy-ry, 2*rx, 2*ry);
		if(c!=null)
			{
			float[] carr=c.getRGBComponents(null);
			carr[3]*=transparency;
			g.setColor(new Color(carr[0],carr[1],carr[2],carr[3]));
			g.fillOval(midx-rx, midy-ry, 2*rx, 2*ry);
			}
		}
	
	}
