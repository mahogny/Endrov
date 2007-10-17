package evplugin.roi;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import evplugin.imageWindow.*;

/**
 * Make nuclei by dragging an area. Also move nuclei.
 *
 * @author Johan Henriksson
 */
public class ImageToolROI implements ImageWindowTool
	{
	private boolean active=false;
	private ROI currentROI=null;
	private String currentHandle=null;

	private final ImageWindow w;
	private final ImageRendererROI r;
	
	
	public ImageToolROI(ImageWindow w, ImageRendererROI r)
		{
		this.w=w;
		this.r=r;
		}
	
	public boolean isToggleable()
		{
		return true;
		}
	public String toolCaption()
		{
		return "ROI/Define";
		}
	public boolean enabled()
		{
		return true;
		}

	
	
	public void mouseClicked(MouseEvent e)
		{
		/*
		NucLineage lin=r.getLineage();
		if(SwingUtilities.isLeftMouseButton(e) && lin!=null)
			NucLineage.mouseSelectNuc(NucLineage.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
			*/
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(active)
			{
			for(ROI.Handle h:currentROI.getHandles())
				if(h.getID().equals(currentHandle))
					{
					h.setX(h.getX()+dx);
					h.setY(h.getY()+dy);
					w.updateImagePanel();
					return;
					}
//			x2=w.s2wx(e.getX());
//			y2=w.s2wy(e.getY());
			}
		}
	
	private boolean mouseOverHandle(MouseEvent e, int x, int y)
		{
		return Math.abs(e.getX()-x)<ImageRendererROI.HANDLESIZE && Math.abs(e.getY()-y)<ImageRendererROI.HANDLESIZE;
		}
	
	public void mousePressed(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			//Which handle?
			for(Map.Entry<ROI, TreeMap<String,ROI.Handle>> re:r.handleList.entrySet())
				for(Map.Entry<String, ROI.Handle> rh:re.getValue().entrySet())
					{
					int x=(int)rh.getValue().getX();
					int y=(int)rh.getValue().getY();
					if(mouseOverHandle(e, x, y))
						{
						active=true;
						currentHandle=rh.getKey();
						currentROI=re.getKey();
						return;
						}
					}
			}
		}

	
	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && active)
			{
			/*
			//Make a nucleus if mouse has been dragged
			NucLineage lin=getLineage();
			if(x1!=x2 && y1!=y2 && lin!=null && r.modifyingNucName==null)
				{
				//New name for this nucleus => null
				String nucName=lin.getUniqueNucName();
				NucLineage.Nuc n=lin.getNucCreate(nucName);
				NucLineage.NucPos pos=n.getPosCreate((int)w.frameControl.getFrame());
				pos.x=(x1+x2)/2;
				pos.y=(y1+y2)/2;
				pos.z=w.s2wz(w.frameControl.getZ());
				pos.r=Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))/2;
				
				if(Math.abs(w.w2sx(pos.r)-w.w2sx(0))>8)
					{
					NucLineage.selectedNuclei.clear();
					NucLineage.selectedNuclei.add(new NucPair(lin,nucName));
					BasicWindow.updateWindows();
					}
				}
				*/
			active=false;
			w.updateImagePanel();
			}
		}

	public void mouseMoved(MouseEvent e, int dx, int dy)
		{			
		}

	
	/*
	 * (non-Javadoc)
	 * @see client.ImageWindow.Tool#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
		{
		}

	
	public void paintComponent(Graphics g)
		{
		}

	
	public void keyReleased(KeyEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		active=false;
		}

	
	}
