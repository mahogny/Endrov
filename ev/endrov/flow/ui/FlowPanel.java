package endrov.flow.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.vecmath.Vector2d;

import endrov.flow.*;
import endrov.util.Tuple;

/**
 * Panel showing flow
 * @author Johan Henriksson
 *
 */
public class FlowPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener//, KeyListener
	{
	static final long serialVersionUID=0;

	public int cameraX, cameraY;
	public Flow flow=new Flow();

	private Map<Tuple<FlowUnit, String>, ConnPoint> connPoint=new HashMap<Tuple<FlowUnit,String>, ConnPoint>();
	private int mouseLastX, mouseLastY;
	private int mouseLastDragX=0, mouseLastDragY=0;
	private Set<FlowUnit> movingUnits=new HashSet<FlowUnit>();
	private DrawingConn drawingConn=null;

	public FlowUnit placingUnit=null;
	public FlowConn stickyConn=null;
	public Rectangle2D selectRect=null;
	
	public Set<FlowUnit> selectedUnits=new HashSet<FlowUnit>();
	
	/**
	 * Constructor
	 */
	public FlowPanel()
		{
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
/*		setEnabled(true);
		setFocusable(true);
		addKeyListener(this);*/
		
	
		
		}

	
	


	
	
	
	
	protected void paintComponent(Graphics g)
		{
		g.setColor(Color.WHITE);
		g.fillRect(0,0,getWidth(),getHeight());
		
		
		
		Graphics2D g2=(Graphics2D)g;
		g2.translate(-cameraX, -cameraY);
		
		
//		g.drawRect(x-panel.cameraX,y-panel.cameraY,30,30);
		
		//Draw all units
		for(FlowUnit u:flow.units)
			u.paint(g2, this);
		
		//All connection points should now be in the list
		//Draw connection arrows
		for(FlowConn conn:flow.conns)
			{
			Vector2d vFrom=connPoint.get(new Tuple<FlowUnit, String>(conn.fromUnit, conn.fromArg)).pos;
			Vector2d vTo=connPoint.get(new Tuple<FlowUnit, String>(conn.toUnit, conn.toArg)).pos;
			drawConnLine(g,vFrom,vTo,conn);
			}
		
		if(drawingConn!=null)
			{
			ConnPoint p=connPoint.get(new Tuple<FlowUnit, String>(drawingConn.t.fst(), drawingConn.t.snd()));
			Vector2d vFrom=p.pos;
			Vector2d vTo=drawingConn.toPoint;
			if(!p.isFrom)
				{
				Vector2d v=vFrom;
				vFrom=vTo;
				vTo=v;
				}
			drawConnLine(g,vFrom,vTo,null);
			}
		
		if(placingUnit!=null)
			{
			placingUnit.paint(g2,this);
			}
		
		if(selectRect!=null)
			{
			g.setColor(Color.MAGENTA);
			g.drawRect((int)selectRect.getX(), (int)selectRect.getY(), 
					(int)selectRect.getWidth(), (int)selectRect.getHeight());
			}
		
		
		g2.translate(cameraX, cameraY);
		
		}
	
	
	


	public void mouseWheelMoved(MouseWheelEvent e){}

	public void mouseDragged(MouseEvent e)
		{
		int dx=(e.getX()-mouseLastDragX);
		int dy=(e.getY()-mouseLastDragY);
		mouseLastX=e.getX();
		mouseLastY=e.getY();
		
		//Update shape of selection rectangle
		if(selectRect!=null)
			{
			int x=(int)selectRect.getX();
			int y=(int)selectRect.getY();
			selectRect=new Rectangle(x,y,e.getX()+cameraX-x,e.getY()+cameraY-y);
			repaint();
			}
		
		//Pan
		if(SwingUtilities.isRightMouseButton(e))
			{
			cameraX-=dx;
			cameraY-=dy;
			repaint();
			}

		//Move held unit
		if(!movingUnits.isEmpty() && SwingUtilities.isLeftMouseButton(e))
			{
			Set<FlowUnit> tomove=new HashSet<FlowUnit>();
			for(FlowUnit u:movingUnits)
				tomove.addAll(u.getSubUnits(flow));

			for(FlowUnit u:tomove)
//			for(FlowUnit u:holdingUnit.getSubUnits(flow))
				{
				u.x+=dx;
				u.y+=dy;
				}
			repaint();
			}
		
		
		
		//Update shape of connection currently drawed
		if(drawingConn!=null)
			{
			int mx=e.getX()+cameraX;
			int my=e.getY()+cameraY;
			drawingConn.toPoint=new Vector2d(mx,my);
			repaint();
			}
		
		
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		}


	public void mouseMoved(MouseEvent e)
		{
		mouseLastX=e.getX();
		mouseLastY=e.getY();
		
		//Update current position of the unit to be placed
		if(placingUnit!=null)
			{
			Tuple<Vector2d, FlowConn> hit=getHoverSegment();
			if(hit!=null)
				{
				placingUnit.x=(int)hit.fst().x+cameraX;
				placingUnit.y=(int)hit.fst().y+cameraY;
				stickyConn=hit.snd();
				}
			else
				{
				placingUnit.x=mouseLastX+cameraX;
				placingUnit.y=mouseLastY+cameraY;
				}
			Dimension dim=placingUnit.getBoundingBox();
			placingUnit.x-=dim.width/2;
			placingUnit.y-=dim.height/2;
			repaint();
			}
		}

/*
	public void keyPressed(KeyEvent e){}


	public void keyReleased(KeyEvent e){}


	public void keyTyped(KeyEvent e){}
*/

	public void mouseClicked(MouseEvent e)
		{
		int mx=e.getX()+cameraX;
		int my=e.getY()+cameraY;
		if(placingUnit!=null)
			{
			if(SwingUtilities.isLeftMouseButton(e))
				{
				flow.units.add(placingUnit);
				if(stickyConn!=null && !placingUnit.getTypesIn().isEmpty() && !placingUnit.getTypesOut().isEmpty())
					{
					String argin=placingUnit.getTypesIn().keySet().iterator().next();
					String argout=placingUnit.getTypesOut().keySet().iterator().next();
					
					flow.conns.remove(stickyConn);
					flow.conns.add(new FlowConn(stickyConn.fromUnit,stickyConn.fromArg,placingUnit,argin));
					flow.conns.add(new FlowConn(placingUnit,argout,stickyConn.toUnit,stickyConn.toArg));
					}
				
				
				}
			placingUnit=null;
			repaint();
			}
		else
			{
			boolean hitAnything=false;
			for(final FlowUnit u:flow.units)
				if(u.mouseHoverMoveRegion(mx,my))
					{
					if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==1)
						{
						if(!selectedUnits.contains(u))
							{
							selectedUnits.clear();
							selectedUnits.add(u);
							repaint();
							}
						hitAnything=true;
						}
					else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2)
						{
						u.editDialog();
						repaint();
						hitAnything=true;
						}
					else if(SwingUtilities.isRightMouseButton(e))
						{
						JPopupMenu popup = new JPopupMenu();
						
						JMenuItem itEval=new JMenuItem("Evaluate");
						itEval.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{
								try
									{
	//								u.evaluate(flow);
	
									u.updateTopBottom(flow);
	
									System.out.println(u.lastOutput);
									}
								catch (Exception e1)
									{
									e1.printStackTrace();
									}
								
								
								}
						});
	
						//TODO potential space leaks?
						JMenuItem itRemove=new JMenuItem("Remove unit");
						itRemove.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{
								flow.removeUnit(u);
								repaint();
								}
						});
	
						
						popup.add(itEval);
						popup.add(itRemove);
						popup.show(this,e.getX(),e.getY());
						hitAnything=true;
						}
					}
			
			if(!hitAnything && SwingUtilities.isRightMouseButton(e))
				{
				final Tuple<Vector2d,FlowConn> seg=getHoverSegment();
				
				JPopupMenu popup = new JPopupMenu();
				
				JMenuItem itRemove=new JMenuItem("Remove connection");
				itRemove.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e)
						{
						flow.conns.remove(seg.snd());
						repaint();
						}
				});
				
				popup.add(itRemove);
				popup.show(this,e.getX(),e.getY());
				
				}
			}
		
		
		
		
		}


	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}

	

	
	public void mousePressed(MouseEvent e)
		{
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		int mx=e.getX()+cameraX;
		int my=e.getY()+cameraY;

		if(placingUnit!=null)
			{
			
			
			}
		else
			{
			boolean found=false;
			
			//Find connection point
			if(!found && SwingUtilities.isLeftMouseButton(e))
				{
				Tuple<FlowUnit,String> t=findHoverConnPoint(mx, my);
				if(t!=null)
					{
					drawingConn=new DrawingConn();
					drawingConn.t=t;
					drawingConn.toPoint=new Vector2d(mx,my);
					found=true;
					}
				}
			
			//Find component. TODO: containers?
			if(!found && SwingUtilities.isLeftMouseButton(e))
				for(FlowUnit u:flow.units)
					if(u.mouseHoverMoveRegion(mx,my))
						{
						if(selectedUnits.contains(u))
							movingUnits.addAll(selectedUnits);
						else
							movingUnits.add(u);
						/*
						if(!selectedUnits.contains(u))
							{
							selectedUnits.clear();
							selectedUnits.add(u);
							}
							*/
						
						found=true;
						break;
						}
			
			if(!found && SwingUtilities.isLeftMouseButton(e))
				{
				selectRect=new Rectangle(e.getX()+cameraX,e.getY()+cameraY,0,0);
				repaint();
				}
			
			}
				
		
		
		
		}


	public void mouseReleased(MouseEvent e)
		{
		movingUnits.clear();
		if(selectRect!=null && SwingUtilities.isLeftMouseButton(e))
			{
			selectedUnits.clear();
			for(FlowUnit u:flow.units)
				{
				Point p=u.getMidPos();
				if(p.x>selectRect.getX() && p.y>selectRect.getY() && p.x<selectRect.getMaxX() && p.y<selectRect.getMaxY())
					selectedUnits.add(u);
				}
			selectRect=null;
			repaint();
			}
		else if(drawingConn!=null && SwingUtilities.isLeftMouseButton(e))
			{
			int mx=e.getX()+cameraX;
			int my=e.getY()+cameraY;
			Tuple<FlowUnit,String> t=findHoverConnPoint(mx, my);
			Tuple<FlowUnit,String> v=drawingConn.t;
			
			if(t!=null)
				{
				if(connPoint.get(v).isFrom != connPoint.get(t).isFrom)
					{
					//Correct order
					if(!connPoint.get(v).isFrom)
						{
						Tuple<FlowUnit,String> temp=t;
						t=v;
						v=temp;
						}

					//Add if it doesn't exist, otherwise remove
					FlowConn nc=new FlowConn(v.fst(),v.snd(), t.fst(), t.snd());
					boolean exists=false;
					for(FlowConn c:flow.conns)
						if(c.equals(nc))
							{
							exists=true;
							flow.conns.remove(c);
							break;
							}
					if(!exists)
						flow.conns.add(nc);
					}
				}
			drawingConn=null;
			repaint();
			}
		
		}
	
	
	
	/******************************************************************************************************
	 *                               Operations on flow                                                   *
	 *****************************************************************************************************/

	
	private static class SortUnitY implements Comparable<SortUnitY>
		{
		public FlowUnit u;
		public SortUnitY(FlowUnit u){this.u=u;}
		public int compareTo(SortUnitY o){return new Integer(u.y).compareTo(new Integer(o.u.y));}
		}
	
	
	public void alignVert(Set<FlowUnit> sel)
		{
		List<SortUnitY> order=new LinkedList<SortUnitY>();
		Integer maxh=null;
		for(FlowUnit u:sel)
			{
			order.add(new SortUnitY(u));
			if(maxh==null || u.getBoundingBox().height>maxh)
				maxh=u.getBoundingBox().height;
			}
		Collections.sort(order);
		int starty=order.iterator().next().u.getMidPos().y;
		for(int i=0;i<order.size();i++)
			{
			int cy=order.get(i).u.getMidPos().y;
			int ny=starty+i*(int)(maxh*1.3);
			order.get(i).u.y+=ny-cy;
			System.out.println(order.get(i).u.y);
			}
		repaint();
		}
	
	public void alignRight(Set<FlowUnit> sel)
		{
		Map<FlowUnit,Double> xmap=new HashMap<FlowUnit, Double>();
		Double totmax=null;
		
		for(Map.Entry<Tuple<FlowUnit, String>, ConnPoint> e:connPoint.entrySet())
			if(selectedUnits.contains(e.getKey().fst()))
				{
				FlowUnit u=e.getKey().fst();
				String arg=e.getKey().snd();
				if(u.getTypesOut().keySet().contains(arg))
					{
					ConnPoint p=e.getValue();
					Double maxx=xmap.get(u);
					if(maxx==null || p.pos.x>maxx)
						{
						maxx=p.pos.x;
						xmap.put(u,maxx);
						if(totmax==null || totmax<p.pos.x)
							totmax=p.pos.x;
						}
					}
				}
		
		for(FlowUnit u:sel)
			{
			int diff=(int)(xmap.get(u)-totmax);
			u.x-=diff;
			}
		repaint();
		}

	
	
	/******************************************************************************************************
	 *                               Connecting points                                                    *
	 *****************************************************************************************************/

	private static class DrawingConn
		{
		Tuple<FlowUnit,String> t;
		Vector2d toPoint;
		}

	private static class ConnPoint
		{
		Vector2d pos;
		boolean isFrom;
		}
	
	public void drawConnPointLeft(Graphics g,FlowUnit unit, String arg, int x, int y)
		{
		g.setColor(Color.BLACK);
		g.fillRect(x-5, y-2, 5, 5);
		ConnPoint p=new ConnPoint();
		p.pos=new Vector2d(x-2,y);
		p.isFrom=false;
		connPoint.put(new Tuple<FlowUnit, String>(unit,arg), p);
		}
	
	public void drawConnPointRight(Graphics g,FlowUnit unit, String arg, int x, int y)
		{
		g.setColor(Color.BLACK);
		g.fillRect(x, y-2, 5, 5);
		ConnPoint p=new ConnPoint();
		p.pos=new Vector2d(x+2,y);
		p.isFrom=true;
		connPoint.put(new Tuple<FlowUnit, String>(unit,arg), p);
		}

	/**
	 * Find connection point near given coordinate
	 */
	private Tuple<FlowUnit,String> findHoverConnPoint(int mx, int my)
		{
		for(Map.Entry<Tuple<FlowUnit, String>, ConnPoint> entry:connPoint.entrySet())
			{
			Vector2d diff=new Vector2d(mx,my);
			diff.sub(entry.getValue().pos);
			if(diff.lengthSquared()<4*4)
				return entry.getKey();
			}
		return null;
		}
	

	/******************************************************************************************************
	 *                               Connecting lines                                                     *
	 *****************************************************************************************************/
	private List<ConnLineSegment> connSegments=new LinkedList<ConnLineSegment>();
	private abstract class ConnLineSegment
		{
		public FlowConn c;
		public abstract Tuple<Vector2d,Integer> hitLine(int x, int y);
		}
	private class ConnLineSegmentH extends ConnLineSegment
		{
		public int x1,x2,y;
		public ConnLineSegmentH(Graphics g, int x1, int x2, int y, FlowConn c)
			{
			if(x1>x2)
				{
				this.x1=x2;
				this.x2=x1;
				}
			else
				{
				this.x1=x1;
				this.x2=x2;
				}
			this.y=y;
			this.c=c;
			g.drawLine(x1,y,x2,y);
			if(c!=null)
				connSegments.add(this);
			}
		public Tuple<Vector2d,Integer> hitLine(int x, int y)
			{
			int dist=Math.abs(y-this.y);
			if(x>x1 && x<x2)
				return Tuple.make(new Vector2d(x,this.y), dist);
			else
				return null;
			}
		}
	private class ConnLineSegmentV extends ConnLineSegment
		{
		public int x,y1,y2;
		public ConnLineSegmentV(Graphics g, int x, int y1, int y2, FlowConn c)
			{
			if(y1>y2)
				{
				this.y1=y2;
				this.y2=y1;
				}
			else
				{
				this.y1=y1;
				this.y2=y2;
				}
			this.x=x;
			this.c=c;
			g.drawLine(x,y1,x,y2);
			if(c!=null)
				connSegments.add(this);
			}		//g.drawLine(x2, (int)vTo.y,x2, midy);
		

		public Tuple<Vector2d,Integer> hitLine(int x, int y)
			{
			int dist=Math.abs(x-this.x);
			if(y>y1 && y<y2)
				return Tuple.make(new Vector2d(this.x,y), dist);
			else
				return null;
			}
		}

	
	private static final int minLineHitDist=10;
	private Tuple<Vector2d,FlowConn> getHoverSegment()
		{
		Integer closestDist=null;
		ConnLineSegment closestSeg=null;
		Vector2d closestProj=null;
		for(ConnLineSegment seg:connSegments)
			{
			Tuple<Vector2d,Integer> hit=seg.hitLine(mouseLastX, mouseLastY);
			if(hit!=null && (closestDist==null || hit.snd()<closestDist))
				{
				closestDist=hit.snd();
				closestSeg=seg;
				closestProj=hit.fst();
				}
			}
		if(closestDist!=null && closestDist<minLineHitDist)
			return Tuple.make(closestProj,closestSeg.c);
		else
			return null;
		}
	
	/**
	 * Draw connecting line between two points
	 */
	public void drawConnLine(Graphics g,Vector2d vFrom, Vector2d vTo, FlowConn c)
		{
		int spacing=15;
		//g.drawLine(x2, (int)vTo.y,x2, midy);
		

		int midy=(int)(vFrom.y+vTo.y)/2;
		int x1=(int)(vFrom.x+vTo.x)/2;
		int x2=x1;
		if(vFrom.x>vTo.x)
			{
			if(x1<vFrom.x+spacing) x1=(int)vFrom.x+spacing;
			if(x2>vTo.x-spacing) x2=(int)vTo.x-spacing;
			}
		
		new ConnLineSegmentH(g, (int)vFrom.x, x1, (int)vFrom.y,c);
		new ConnLineSegmentV(g, x1, (int)vFrom.y, midy,c);
		new ConnLineSegmentH(g,x1,x2,midy,c);
		new ConnLineSegmentV(g,x2,(int)vTo.y,midy,c);
		new ConnLineSegmentH(g,(int)vTo.x,x2,(int)vTo.y,c);
		}
	}
