/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.overviewWindow;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.vecmath.Vector2d;

import endrov.hardware.EvDevicePath;
import endrov.imageWindow.GeneralTool;
import endrov.imageWindow.ImageWindowRenderer;
import endrov.imageset.EvPixels;
import endrov.recording.RecordingResource;
import endrov.recording.ResolutionManager;
import endrov.recording.positionsWindow.AxisInfo;
import endrov.recording.positionsWindow.Position;
import endrov.util.Vector2i;

/**
 * @author Kim Nordlöf, Erik Vernersson
 */
public abstract class OverviewWindowImageView extends JPanel implements
		MouseListener, MouseMotionListener, MouseWheelListener
	{
	static final long serialVersionUID = 0;

	private Vector2i lastMousePosition = new Vector2i();
	public GeneralTool currentTool = null;
	public final Vector<ImageWindowRenderer> imageWindowRenderers = new Vector<ImageWindowRenderer>();

	private Vector2d cameraPos = new Vector2d();


	public EvPixels overviewImage = null;

	private double scale;

	public Vector2d getCameraPos()
		{
		return cameraPos;
		}

	public void resetCameraPos()
		{
		cameraPos = new Vector2d();
		}

	public double getScale()
		{
		return scale;
		}

	public EvPixels[] getImage()
		{
		return new EvPixels[]
			{ overviewImage };
		}

	public abstract Vector2d getOffset();

	public abstract int getLower();

	public abstract int getUpper();

	public abstract EvDevicePath getCameraPath();

	public abstract long getCameraWidth();

	public abstract long getCameraHeight();

	public abstract ResolutionManager.Resolution getResolution();

	public JToggleButton[] toolButtons;

	
	public OverviewWindowImageView()
		{
		scale = 0.5;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		}

	public void setToolButtons(JToggleButton[] toolButtons)
		{
		this.toolButtons = toolButtons;
		}

	protected void paintComponent(Graphics g2)
		{

		Graphics2D g = (Graphics2D) g2;

		g.setColor(new Color(0, 0, 0));
		g.fillRect(0, 0, getWidth(), getHeight());

		Vector2d offset = cameraPos;
		g.translate((int) offset.x, (int) offset.y);
		g.scale(scale, scale);

		// Convert pixels into the right range. Mark under- and overflow
		EvPixels[] pq = getImage();
		if (overviewImage!=null)
			{
			if (pq!=null)
				{
				if (pq.length==1)
					{
					// Grayscale

					EvPixels p = pq[0];

					int lower = getLower();
					int upper = getUpper();
					int diff = upper-lower;
					if (diff==0)
						diff = 1; // Just to avoid divison by zero errors
					int[] parr = p.convertToInt(true).getArrayInt();
					int w = p.getWidth();
					int h = p.getHeight();
					BufferedImage toDraw = new BufferedImage(w, h,
							BufferedImage.TYPE_3BYTE_BGR);
					int[] arrR = new int[parr.length];
					int[] arrG = new int[parr.length];
					int[] arrB = new int[parr.length];
					for (int i = 0; i<parr.length; i++)
						{
						int v = parr[i];
						int out = (v-lower)*255/diff;
						if (out<0)
							{
							arrR[i] = 0;
							arrG[i] = 0;
							arrB[i] = 255;
							}
						else if (out>255)
							{
							arrR[i] = 255;
							arrG[i] = 0;
							arrB[i] = 0;
							}
						else
							{
							arrR[i] = out;
							arrG[i] = out;
							arrB[i] = out;
							}
						}
					WritableRaster raster = toDraw.getRaster();
					raster.setSamples(0, 0, w, h, 0, arrR);
					raster.setSamples(0, 0, w, h, 1, arrG);
					raster.setSamples(0, 0, w, h, 2, arrB);

					g.drawImage(toDraw, 0, 0, null);

					}
				else
					{
					// RGB

					int lower = getLower();
					int upper = getUpper();
					int diff = upper-lower;
					if (diff==0)
						diff = 1; // Just to avoid divison by zero errors

					int[] parrR = pq[0].convertToInt(true).getArrayInt();
					int[] parrG = pq[1].convertToInt(true).getArrayInt();
					int[] parrB = pq[2].convertToInt(true).getArrayInt();
					int w = pq[0].getWidth();
					int h = pq[0].getHeight();
					BufferedImage toDraw = new BufferedImage(w, h,
							BufferedImage.TYPE_3BYTE_BGR);
					int[] arrR = new int[parrR.length];
					int[] arrG = new int[parrG.length];
					int[] arrB = new int[parrB.length];
					for (int i = 0; i<parrR.length; i++)
						{
						int vR = parrR[i];
						int outR = (vR-lower)*255/diff;
						int vG = parrG[i];
						int outG = (vG-lower)*255/diff;
						int vB = parrB[i];
						int outB = (vB-lower)*255/diff;

						if (vR<0||vG<0||vB<0||vR>255||vG>255||vB>255)
							{
							arrR[i] = 255;
							arrG[i] = 0;
							arrB[i] = 0;
							}
						else
							{
							arrR[i] = outR;
							arrG[i] = outG;
							arrB[i] = outB;
							}

						}
					WritableRaster raster = toDraw.getRaster();
					raster.setSamples(0, 0, w, h, 0, arrR);
					raster.setSamples(0, 0, w, h, 1, arrG);
					raster.setSamples(0, 0, w, h, 2, arrB);

					g.drawImage(toDraw, 0, 0, null);

					}

				for (Position pos : RecordingResource.posList)
					{

					g.setColor(pos.getColor().getAWTColor());

					double xPos = 0;
					double yPos = 0;

					for (AxisInfo info : pos.getAxisInfo())
						{
						if (info.getDevice().getAxisName()[info.getAxis()].contains("X"))
							{
							xPos = info.getValue();
							}
						else if (info.getDevice().getAxisName()[info.getAxis()]
								.contains("Y"))
							{
							yPos = info.getValue();
							}
						}
					

					ResolutionManager.Resolution res = getResolution();

					g.fillOval((int) (xPos/res.x+getCameraWidth()/2+getOffset().x),
							(int) (yPos/res.y+getCameraHeight()/2+getOffset().y), 10, 10);
					g.setFont(new Font("Arial", Font.PLAIN, 12));
					g.drawString(pos.toString(),
							(int) (xPos/res.x+getCameraWidth()/2+getOffset().x), (int) (yPos
									/res.y+getCameraHeight()/2+getOffset().y));
					}

				g.translate(-offset.x, -offset.y);
				}
			}

		for (ImageWindowRenderer r : imageWindowRenderers)
			r.draw(g);
		}

	public void mouseClicked(MouseEvent e)
		{
		lastMousePosition = new Vector2i(e.getX(), e.getY());
		EvDevicePath campath = getCameraPath();

		if (campath!=null)
			{
			ResolutionManager.Resolution res = ResolutionManager
					.getCurrentResolutionNotNull(campath);

			Map<String, Double> diff = new HashMap<String, Double>();

			// diff.put("X",(lastMousePosition.x/scale-672-cameraPos.x/scale-getOffset().x)*res.x);
			// diff.put("Y",(lastMousePosition.y/scale-512-cameraPos.y/scale-getOffset().y)*res.y);
			diff.put("X", (lastMousePosition.x/scale-getCameraWidth()/2-cameraPos.x
					/scale-getOffset().x)
					*res.x);
			diff.put("Y", (lastMousePosition.y/scale-getCameraHeight()/2-cameraPos.y
					/scale-getOffset().y)
					*res.y);

			RecordingResource.setStagePos(diff);
			repaint();

			}
		else
			System.out.println("No camera to move");

		if (currentTool!=null)
			currentTool.mouseClicked(e, this);
		}

	public void mouseEntered(MouseEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		if (currentTool!=null)
			currentTool.mouseExited(e);
		}

	public void mousePressed(MouseEvent e)
		{
		lastMousePosition = new Vector2i(e.getX(), e.getY());
		if (currentTool!=null)
			currentTool.mousePressed(e);
		}

	public void mouseReleased(MouseEvent e)
		{
		if (currentTool!=null)
			currentTool.mouseReleased(e);
		}

	public void mouseDragged(MouseEvent e)
		{
		int dx = e.getX()-lastMousePosition.x;
		int dy = e.getY()-lastMousePosition.y;
		lastMousePosition = new Vector2i(e.getX(), e.getY());

		if (SwingUtilities.isLeftMouseButton(e))
			{
			// Left mouse button is for the tool
			if (currentTool!=null)
				currentTool.mouseDragged(e, dx, dy);

			}
		else if (SwingUtilities.isRightMouseButton(e))
			{

			cameraPos.x += dx;
			cameraPos.y += dy;

			repaint();

			}
		}

	public void mouseMoved(MouseEvent e)
		{
		int dx = e.getX()-lastMousePosition.x;
		int dy = e.getY()-lastMousePosition.y;
		lastMousePosition = new Vector2i(e.getX(), e.getY());

		if (currentTool!=null)
			currentTool.mouseMoved(e, dx, dy);

		}

	public void mouseWheelMoved(MouseWheelEvent e)
		{

		Vector2i mousePos = new Vector2i(e.getX(), e.getY());
		Vector2d cameraOffset = new Vector2d();

		double dz = e.getWheelRotation();

		if (dz>0)
			{
			scale /= 1.5;
			cameraOffset = new Vector2d(
					(-mousePos.x+cameraPos.x)/3,
					(-mousePos.y+cameraPos.y)/3);
			}
		else if (dz<0)
			{
			scale *= 1.5;
			cameraOffset = new Vector2d(
					(mousePos.x-cameraPos.x)*0.5,
					(mousePos.y-cameraPos.y)*0.5);

			}
		cameraPos = new Vector2d(cameraPos.x-cameraOffset.x, cameraPos.y-cameraOffset.y);

		repaint();
		}

	}
