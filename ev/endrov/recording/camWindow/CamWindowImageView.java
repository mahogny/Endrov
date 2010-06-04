/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.camWindow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;
import endrov.imageWindow.GeneralTool;
import endrov.imageWindow.ImageWindowRenderer;
import endrov.imageset.EvPixels;
import endrov.recording.HWStage;
import endrov.recording.HWCamera;
import endrov.recording.RecordingResource;
import endrov.util.Vector2i;

/**
 * Image area. Adjusts visible range to screen. Shows under and over exposed regions.
 * 
 * @author Johan Henriksson
 *
 */
public abstract class CamWindowImageView extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener
	{
	static final long serialVersionUID=0;
	
	private Vector2i lastMousePosition=new Vector2i();
	public GeneralTool currentTool=null;
	public final Vector<ImageWindowRenderer> imageWindowRenderers=new Vector<ImageWindowRenderer>();

	
	public abstract EvPixels[] getImage();
	public abstract Vector2i getOffset();
	public abstract int getLower();
	public abstract int getUpper();
		
	
	public JToggleButton[] toolButtons;
	
	/*
	public CamWindowImageView(JToggleButton[] toolButtons){
		this();
		this.toolButtons = toolButtons;
	}
*/	
	public CamWindowImageView()
		{
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		}
	
	
	public void setToolButtons(JToggleButton[] toolButtons)
		{
		this.toolButtons = toolButtons;
		}
	
	protected void paintComponent(Graphics g)
		{
		Vector2i offset=getOffset();
		
		//Make sure background is filled with something
		g.setColor(new Color(0.3f, 0.1f, 0.3f));
		g.fillRect(0, 0, getWidth(), getHeight());

		g.translate(offset.x, offset.y);

		//Convert pixels into the right range. Mark under- and overflow
		EvPixels[] pq=getImage();
		if(pq!=null)
			{
			if(pq.length==1)
				{
				//Grayscale
			
				EvPixels p=pq[0];
				
				int lower=getLower();
				int upper=getUpper();
				int diff=upper-lower;
				if(diff==0)
					diff=1; //Just to avoid divison by zero errors
				int[] parr=p.convertToInt(true).getArrayInt();
				int w=p.getWidth();
				int h=p.getHeight();
				BufferedImage toDraw=new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
				int[] arrR=new int[parr.length];
				int[] arrG=new int[parr.length];
				int[] arrB=new int[parr.length];
				for(int i=0;i<parr.length;i++)
					{
					int v=parr[i];
					int out=(v-lower)*255/diff;
					if(out<0)
						{
						arrR[i]=0;
						arrG[i]=0;
						arrB[i]=255;
						}
					else if(out>255)
						{
						arrR[i]=255;
						arrG[i]=0;
						arrB[i]=0;
						}
					else
						{
						arrR[i]=out;
						arrG[i]=out;
						arrB[i]=out;
						}
					}
				WritableRaster raster=toDraw.getRaster();
				raster.setSamples(0, 0, w, h, 0, arrR);
				raster.setSamples(0, 0, w, h, 1, arrG);
				raster.setSamples(0, 0, w, h, 2, arrB);
				
				g.drawImage(toDraw, 0, 0, null);
				
				}
			else
				{
				//RGB
				
				int lower=getLower();
				int upper=getUpper();
				int diff=upper-lower;
				if(diff==0)
					diff=1; //Just to avoid divison by zero errors
				
				int[] parrR=pq[0].convertToInt(true).getArrayInt();
				int[] parrG=pq[1].convertToInt(true).getArrayInt();
				int[] parrB=pq[2].convertToInt(true).getArrayInt();
				int w=pq[0].getWidth();
				int h=pq[0].getHeight();
				BufferedImage toDraw=new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
				int[] arrR=new int[parrR.length];
				int[] arrG=new int[parrG.length];
				int[] arrB=new int[parrB.length];
				for(int i=0;i<parrR.length;i++)
					{
					int vR=parrR[i];
					int outR=(vR-lower)*255/diff;
					int vG=parrG[i];
					int outG=(vG-lower)*255/diff;
					int vB=parrB[i];
					int outB=(vB-lower)*255/diff;

					if(vR<0 || vG<0 || vB<0 || vR>255 || vG>255 || vB>255)
						{
						arrR[i]=255;
						arrG[i]=0;
						arrB[i]=0;
						}
					else
						{
						arrR[i]=outR;
						arrG[i]=outG;
						arrB[i]=outB;
						}
					
					}
				WritableRaster raster=toDraw.getRaster();
				raster.setSamples(0, 0, w, h, 0, arrR);
				raster.setSamples(0, 0, w, h, 1, arrG);
				raster.setSamples(0, 0, w, h, 2, arrB);
				
				g.drawImage(toDraw, 0, 0, null);
				
				}

			}
		g.translate(-offset.x, -offset.y);
		
		
		for(ImageWindowRenderer r:imageWindowRenderers)
			r.draw(g);
		}
	
	
	
	public void mouseClicked(MouseEvent e)
		{
		if(currentTool!=null)
			currentTool.mouseClicked(e, this);
		}
	public void mouseEntered(MouseEvent e)
		{
		}
	public void mouseExited(MouseEvent e)
		{
		if(currentTool!=null)
			currentTool.mouseExited(e);
		}
	public void mousePressed(MouseEvent e)
		{
		lastMousePosition=new Vector2i(e.getX(),e.getY());
		if(currentTool!=null)
			currentTool.mousePressed(e);
		}
	public void mouseReleased(MouseEvent e)
		{
		if(currentTool!=null)
			currentTool.mouseReleased(e);
		}
	public void mouseDragged(MouseEvent e)
		{
		boolean move = true;
//		for(JToggleButton b:toolButtons)
//			if(b.isSelected()) move = false;
		int dx=e.getX()-lastMousePosition.x;
		int dy=e.getY()-lastMousePosition.y;
		lastMousePosition=new Vector2i(e.getX(),e.getY());
	
		if(SwingUtilities.isLeftMouseButton(e))
			{
			//Left mouse button is for the tool
			if(currentTool!=null)
				currentTool.mouseDragged(e, dx, dy);
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			double resMagX = 1; //[um/px]
			double resMagY = 1;
			boolean foundCamera=false;
			for(Map.Entry<EvDevicePath,HWCamera> cams:EvHardware.getDeviceMapCast(HWCamera.class).entrySet())
				{
				HWCamera camera = cams.getValue();
				resMagX=resMagY=RecordingResource.getCurrentTotalMagnification(camera);
				foundCamera=true;
				break;
				}
			if(!foundCamera)
				System.out.println("no camera to move");
			
			//TODO update manual view
			if(move)
				{
				moveAxis("x", dx*resMagX);
				moveAxis("y", dy*resMagY);
				repaint();
				}
			}
		}
	public void mouseMoved(MouseEvent e)
		{
		int dx=e.getX()-lastMousePosition.x;
		int dy=e.getY()-lastMousePosition.y;
		lastMousePosition=new Vector2i(e.getX(),e.getY());
		
		if(currentTool!=null)
			currentTool.mouseMoved(e, dx, dy);
		}
	public void mouseWheelMoved(MouseWheelEvent e)
		{
		//TODO magnification
		int dz=e.getWheelRotation();
		moveAxis("z", dz);
		}
	
	public static void moveAxis(String s, double dx)
		{
		for(Map.Entry<EvDevicePath,HWStage> e:EvHardware.getDeviceMapCast(HWStage.class).entrySet())
			{
			HWStage stage=e.getValue();
			for(int i=0;i<stage.getNumAxis();i++)
				{
				if(stage.getAxisName()[i].equals(s))
					{
					//TODO should if possible set both axis' at the same time. might make it faster
					double[] da=new double[stage.getNumAxis()];
					da[i]=dx;
					stage.setRelStagePos(da);
					return;
					}
				}
			}
		System.out.println("Did not find axis to move "+s);
		}
	
	
	
	}
