package evplugin.lineageWindow;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.nuc.*;

//TODO: kill internal which are not in use, especially roots
//can use weak references! make a WeakTreeMap and stop using strings?

/**
 * The lineage view is so specific to the view that there is no point in separating it.
 * @author Johan Henriksson
 */
public class LineageView extends JPanel 
	{
	static final long serialVersionUID=0;
	
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Settings ////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	private static final Color frameLineColor=new Color(220,220,220);
	private static final Color curFrameLineColor=new Color(150,150,150);
	private static final Color frameStringColor=new Color(100,100,100);
	
	/** Size of expander icon */
	private static final int expanderSize=4;
	/** Size of key frame icon */
	private static final int keyFrameSize=2;
	
	
	

	////////////////////////////////////////////////////////////////
	
	public int camVY, camVX;
	private int frameDist=5;
	public double currentFrame=0;
	public boolean displayHorizontalTree=true;
	public NucLineage currentLin=null;
	public boolean showFrameLines=true;
	public boolean showKeyFrames=true;
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Cached tree /////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	private WeakHashMap<NucLineage, DrawCache> drawCache=new WeakHashMap<NucLineage, DrawCache>();
	
	/** Cached information about nuclei */
	private class DrawCache
		{
		TreeMap<String, Internal> nucInternal=new TreeMap<String, Internal>();
		}

	/** Get draw cache for currently selected lineage */
	private DrawCache getDrawCache()
		{
		return getDrawCache(currentLin);
		}
	
	/** Get draw cache for a lineage */
	private DrawCache getDrawCache(NucLineage lin)
		{
		DrawCache dc=drawCache.get(lin);
		if(dc==null)
			drawCache.put(lin, dc=new DrawCache());
		return dc;
		}
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Cached keyframes ////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	private LinkedList<KeyFramePos> drawnKeyFrames=new LinkedList<KeyFramePos>();

	public static class KeyFramePos
		{
		public int x, y, frame;
		public String nuc;
		}
	
	

	/**
	 * Draw key frame icon
	 */
	private void drawKeyFrame(Graphics g, int x, int y, String nuc, int frame)
		{
		g.drawOval(x-keyFrameSize, y-keyFrameSize, 2*keyFrameSize, 2*keyFrameSize);
		KeyFramePos f=new KeyFramePos();
		f.frame=frame;
		f.x=x;
		f.y=y;
		f.nuc=nuc;
		drawnKeyFrames.add(f);
		}
	
	/**
	 * Get key frame closest to given position
	 */
	public KeyFramePos getKeyFrame(int x, int y)
		{
		KeyFramePos nearest=null;
		int nearestDist2=0;
		for(KeyFramePos f:drawnKeyFrames)
			{
			int dx=f.x-x, dy=f.y-y;
			int dist2=dx*dx+dy*dy;
			if(nearest==null || dist2<nearestDist2)
				{
				nearestDist2=dist2;
				nearest=f;
				}
			}
		if(nearestDist2<10*10)
			return nearest;
		else
			return null;
		}
	
		
	
	
	
  /////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// The rest ////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	
	
	private int getVirtualWidth()
		{
		return displayHorizontalTree ? getWidth() : getHeight();
		}
	private int getVirtualHeight()
		{
		return displayHorizontalTree ? getHeight() : getWidth();
		}
	
	
	/**
	 * Go to the first root
	 */
	public void goRoot()
		{	
		Integer allMinFrame=null;
		Set<String> roots=getRootNuc();
		String rootName=null;
		for(String nucName:roots)
			{
			NucLineage.Nuc nuc=currentLin.nuc.get(nucName);
			if(allMinFrame==null || nuc.pos.firstKey()<allMinFrame)
				{
				allMinFrame=nuc.pos.firstKey();
				rootName=nucName;
				}
			}
		if(allMinFrame!=null)
			goInternalNuc(getNucinfo(rootName));
		}

	/**
	 * Go to one selected nucleus
	 */
	public void goSelected()
		{
		if(!NucLineage.selectedNuclei.isEmpty())
			{
			String nucName=NucLineage.selectedNuclei.iterator().next().getRight();
			Internal internal=getNucinfo(nucName);
			goInternalNuc(internal);
			}
		}
	private void goInternalNuc(Internal internal)
		{
		camVY+=internal.lastB-getVirtualHeight()/2;
		camVX+=internal.lastC-getVirtualWidth()/2;
		repaint();
		}
	
	/**
	 * Move according to mouse movement
	 */
	public void pan(int dx, int dy)
		{
		if(displayHorizontalTree)
			{
			camVX-=dx;
			camVY-=dy;
			}
		else
			{
			camVX-=dy;
			camVY+=dx;
			}
		}
	
	/**
	 * Get frame from screen x,y coordinate
	 */
	public int getFrameFromCursor(int x, int y)
		{
		if(displayHorizontalTree)
			return c2f(x);
		else
			return c2f(y);
		}
	
	
	/**
	 * Change the frame distance but keep the camera reasonably fixed
	 * @param s New frame distance, >=1
	 */
	public void setFrameDist(int s)
		{
		if(s<1)	s=1; //Not allowed to happen, quick fix
		int h=getVirtualWidth()/2;
		double curmid=(camVX+h)/frameDist;
		frameDist=s;
		camVX=(int)(curmid*frameDist-h);
		}
	
	
	/**
	 * Move camera to show some frame
	 */
	public void setFrame(int frame)
		{
		camVX=frame*frameDist-getVirtualWidth()/2;
		repaint();
		}
	
	public int getFrame()
		{
		return (camVX+getVirtualWidth()/2)/frameDist;
		}
	
	
	/**
	 * Get all root nuclei
	 */
	private SortedSet<String> getRootNuc()
		{
		SortedSet<String> list=new TreeSet<String>();
		if(currentLin!=null)
			for(String nucName:currentLin.nuc.keySet())
				if(currentLin.nuc.get(nucName).parent==null)
					list.add(nucName);
		return list;
		}
	
	
	public void foldAll()
		{
		Set<String> roots=getRootNuc();
		for(String nucName:roots)
			recursiveExpand(nucName, false);
		repaint();
		}

	public void unfoldAll()
		{
		Set<String> roots=getRootNuc();
		for(String nucName:roots)
			recursiveExpand(nucName, true);
		repaint();
		}

	
	/**
	 * Draw the component
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g)
		{
		//Rotate image, part 1
		Graphics h;
		BufferedImage bim=null;
		if(displayHorizontalTree)
			h=g;
		else
			{
			bim=new BufferedImage(getVirtualWidth(),getVirtualHeight(),BufferedImage.TYPE_3BYTE_BGR);
			h=bim.getGraphics();
			}
		
		//Redo list of clickable regions
		regionClickList.clear();
		drawnKeyFrames.clear();
		
		//Fill background
		h.setColor(Color.WHITE);
		h.fillRect(0, 0, getVirtualWidth(), getVirtualHeight());

		//Draw frame lines
		drawFrameLines(h);

		//Clean up
		removeUnusedInternal();

		//Update tree structure
		Set<String> roots=getRootNuc();
		for(String nucName:roots)
			updateTreeFormat(h,nucName);
		
		//Draw all trees
		int displacement=0;
		boolean first=true;
		
		
		
		for(String nucName:roots)
			{
			Internal nuc=getNucinfo(nucName);
			if(first)	first=false; else displacement+=nuc.sizer/2; //I don't like this layout really
			drawTree(h, nucName,displacement+getVirtualHeight()/2-camVY); 
			displacement+=nuc.sizer/2; //maybe really half this and half next?
			}

		//Rotate image part 2
		if(bim!=null)
			{
			AffineTransform af = new AffineTransform(); 
			af.translate(getVirtualHeight(), 0);
			af.rotate(Math.PI/2.0);
			((Graphics2D)g).drawImage(bim, af, null);
			}
		}
	
	/**
	 * Remove unused Internal nodes
	 */
	public void removeUnusedInternal()
		{
		//TODO. clean-up not really needed.
		//Could use weak references, clean-up automatic
		}
	
	/**
	 * Draw the frame lines in the background
	 */
	public void drawFrameLines(Graphics g)
		{
		if(showFrameLines)
			{
			int frameLineSkip=20/frameDist;
			if(frameLineSkip<1)
				frameLineSkip=1;
			int starti=camVX/frameDist;
			while(starti%frameLineSkip!=0)
				starti--;
			
			Graphics2D g2=(Graphics2D)g;
			for(int i=starti;i<getVirtualWidth()/frameDist+1+frameLineSkip+camVX/frameDist;i+=frameLineSkip)
				{
				int x=i*frameDist-camVX;
				g.setColor(frameLineColor);
				g.drawLine(x, 0, x, getVirtualHeight());
				g.setColor(frameStringColor);
				int y=getVirtualHeight()-5;//5
				g2.translate(x, y);
				g2.rotate(-Math.PI/2);
				g.drawString(""+i, 0, 0);
				g2.rotate(Math.PI/2);
				g2.translate(-x, -y);
				}
			g.setColor(curFrameLineColor);
			g.drawLine(getVirtualWidth()/2, 0, getVirtualWidth()/2, getVirtualHeight());
			}
		}
	
	
	
	
	/**
	 * Recursive function to draw a tree
	 * @param internal Which node to recurse from
	 */
	private void drawTree(Graphics g, String nucName, int midr)
		{
		NucLineage.Nuc nuc=currentLin.nuc.get(nucName);
		if(nuc==null)
			{
			//This is a hack in my opinion. Better if it can be found during tree structure planner
			//but this is more flexible right now. will give some artifacts
			System.out.println(nucName+" not found while drawing. bug!!!?");
			getDrawCache().nucInternal.remove(nucName);
			return;
			}
		
		if(nuc.pos.isEmpty())
			{
			System.out.println("Error: no positions for "+nucName);
			return;
			}
		Internal internal=getNucinfo(nucName);
		int firstFrame=nuc.pos.firstKey();
		int startc=f2c(firstFrame);
		int endc=f2c(nuc.lastFrame());
		
		//Draw line spanning frames
		g.setColor(Color.BLACK);
		g.drawLine(startc, midr, endc, midr);
		if(nuc.end!=null && nuc.child.size()==0)
			drawNucEnd(g, f2c(nuc.end), midr);
		internal.lastC=startc;
		internal.lastB=midr;
		
		//Draw keyframes
		if(showKeyFrames && midr>-keyFrameSize && midr<getVirtualHeight()+keyFrameSize)
			{
			g.setColor(Color.RED);
			for(int frame:nuc.pos.keySet())
				drawKeyFrame(g,f2c(frame), midr, nucName, frame);
			}
		
		//Draw children
		if(internal.expanded)
			{
			for(String cName:nuc.child)
				{
				NucLineage.Nuc c=currentLin.nuc.get(cName);
				if(!c.pos.isEmpty())
					{
					Internal cInternal=getDrawCache().nucInternal.get(cName);
					//Draw connecting line
					g.setColor(Color.BLACK);
					g.drawLine(endc,midr,f2c(c.pos.firstKey()),midr+cInternal.centerDisplacement);
					//Recurse down
					drawTree(g,cName, midr+cInternal.centerDisplacement);
					}
				}
			}
		
		//Draw expander
		if(nuc.child.size()>0)
			drawExpanderSymbol(g,nucName, endc,midr,internal.expanded);

		//Draw name of nucleus. Warn if something is wrong
		if(nuc.end!=null && nuc.child.size()>0)
			drawNucName(g, "!!! ", new NucPair(currentLin, nucName), midr, endc);
		else
			drawNucName(g, "", new NucPair(currentLin, nucName), midr, endc);
		}

	
	
	

	
	
	
	/**
	 * Draw arrow pointing out that the nucleus continue existing
	 */
	private void drawNucEnd(Graphics g, int endc, int midr)
		{
		int size=10;
		g.setColor(Color.BLUE);
		g.drawLine(endc, midr-size, endc, midr+size);
		}
	
	
	/**
	 * Convert frame position to coordinate
	 */
	private int f2c(int f)
		{
		return f*frameDist-camVX;
		}
	
	/**
	 * Convert coordinate to frame position
	 */
	private int c2f(int c)
		{
		return (c+camVX)/frameDist;
		}
	
	
	/**
	 * Draw text name
	 */
	private void drawNucName(Graphics g, String prefix, NucPair nucPair, int midr, int endc)
		{
		String nucName=nucPair.getRight();
		int fontHeight=g.getFontMetrics().getHeight();
		int fontWidth=g.getFontMetrics().stringWidth(prefix+nucName);
		int textc=endc+5;
		Graphics2D g2=(Graphics2D)g;
		if(NucLineage.selectedNuclei.contains(nucPair))
			g2.setColor(Color.RED);
		else
			g2.setColor(Color.BLUE);
		
		//Graphics
		int textr=midr+fontHeight/4;
		g2.translate(textc, textr);
		g2.drawString(nucName, 0, 0);
		g2.translate(-textc, -textr);
		//Make it clickable
		regionClickList.add(new ClickRegionName(prefix+nucName, textc, textr-3*fontHeight/4, fontWidth,fontHeight));
		}
	
	/**
	 * Draw the [+] and [-] symbol
	 * @param g Graphics context
	 * @param nucname Name of corresponding nucleus
	 * @param x Mid x coordinate
	 * @param y Mid y coordinate
	 * @param expanded If a + or - should be shown
	 */
	private void drawExpanderSymbol(Graphics g, String nucname, int x, int y, boolean expanded)
		{
		//Do graphics
		g.setColor(Color.WHITE);
		g.fillRect(x-expanderSize, y-expanderSize, 2*expanderSize, 2*expanderSize);
		g.setColor(Color.BLACK);
		g.drawRect(x-expanderSize, y-expanderSize, 2*expanderSize, 2*expanderSize);		
		g.drawLine(x-expanderSize, y, x+expanderSize,y);
		if(expanded)
			g.drawLine(x, y+expanderSize, x,y-expanderSize);
		//Make it clickable
		regionClickList.add(new ClickRegionExpander(nucname, x-expanderSize, y-expanderSize, 2*expanderSize,2*expanderSize));
		}
	

	
	/**
	 * Prepare rendering sizes
	 */
	private void updateTreeFormat(Graphics g, String nucName)
		{		
		Internal internal=getNucinfo(nucName);
		NucLineage.Nuc nuc=currentLin.nuc.get(nucName);

		//Total width of children. 0 if none expanded
		int totw=0;
		
		//Only recurse if children are visible
		if(internal.expanded)
			{
			//Sum up total width for children
			for(String cName:nuc.child)
				{
				Internal cInternal=getNucinfo(cName);
				updateTreeFormat(g,cName);
				totw+=cInternal.sizer;
				}
			//Set displacements
			if(nuc.child.size()==1)
				{
				Internal cInternal=getNucinfo(nuc.child.first());
				cInternal.centerDisplacement=10;
				}
			else if(nuc.child.size()==2 && false) //why did I set this to false?
				{
				//Divide evenly. this does not work properly with multitrees
				Internal cInternal1=getNucinfo(nuc.child.first());
				Internal cInternal2=getNucinfo(nuc.child.last());
				cInternal1.centerDisplacement=-totw/2;
				cInternal2.centerDisplacement=+totw/2;
				}
			else
				{
				int fromleft=0;
				for(String cName:nuc.child)
					{
					Internal cInternal=getDrawCache().nucInternal.get(cName);
					cInternal.centerDisplacement=fromleft+cInternal.sizer/2-totw/2;
					fromleft+=cInternal.sizer;
					}
				}
			}
		
		//Compute width for this node
		internal.sizer=totw;
		int fontHeight=g.getFontMetrics().getHeight()*2;
		if(internal.sizer<fontHeight)
			internal.sizer=fontHeight;
		}
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Tree pos ////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * Cached information about nuclei
	 */
	public static class Internal
		{
		public boolean expanded=true;
		public int sizer=0;
		public int centerDisplacement=0;
		public int lastB, lastC;
		}

	
	/**
	 * Get information structure about nucleus
	 * @param nuc Name of nucleus
	 * @return Existing structure or a new one
	 */
	public Internal getNucinfo(String nuc)
		{
		Internal i=getDrawCache().nucInternal.get(nuc);
		if(i==null)
			{
			i=new Internal();
			getDrawCache().nucInternal.put(nuc, i);
			}
		return i;
		}
	
	
	/** List of all mouse click handlers */
	LinkedList<ClickRegion> regionClickList=new LinkedList<ClickRegion>();
	
	/**
	 * Handle mouse click in view
	 */
	public void clickRegion(MouseEvent e)
		{
		int mousex,mousey;
		if(displayHorizontalTree)
			{
			mousex=e.getX();
			mousey=e.getY();
			}
		else
			{
			mousex=e.getY();
			mousey=e.getX();
			}
		for(ClickRegion r:regionClickList)
			if(mousex>=r.x && mousey>=r.y && mousex<=r.x+r.w && mousey<=r.y+r.h)
				{
				r.clickRegion(e);
				return;
				}
		if(SwingUtilities.isLeftMouseButton(e))
			NucLineage.selectedNuclei.clear();
		BasicWindow.updateWindows();
		}

	/**
	 * Mouse click handler
	 */
	private abstract class ClickRegion
		{
		public int x=0,y=0,w=0,h=0;
		public abstract void clickRegion(MouseEvent e);
		}

	/**
	 * Mouse click handler: on a name panel
	 */
	private class ClickRegionName extends ClickRegion
		{
		String nucname;
		public ClickRegionName(String nucname, int x, int y, int w, int h)
			{this.nucname=nucname; this.x=x; this.y=y; this.w=w; this.h=h;}
		public void clickRegion(MouseEvent e)
			{
			if(currentLin!=null && SwingUtilities.isLeftMouseButton(e))
				NucLineage.mouseSelectNuc(new NucPair(currentLin, nucname), (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
			BasicWindow.updateWindows();
			}
		}
	
	
	/**
	 * Mouse click handler: on an expander
	 */
	private class ClickRegionExpander extends ClickRegion
		{
		String nucname;
		public ClickRegionExpander(String nucname, int x, int y, int w, int h)
			{this.nucname=nucname; this.x=x; this.y=y; this.w=w; this.h=h;}
		public void clickRegion(MouseEvent e)
			{
			Internal internal=getNucinfo(nucname);
			if(SwingUtilities.isLeftMouseButton(e))
				internal.expanded=!internal.expanded;
			else if(SwingUtilities.isRightMouseButton(e))
				recursiveExpand(nucname, !internal.expanded);
			repaint();
			}
		}

	/**
	 * Recursively expand/un-expand
	 */
	public void recursiveExpand(String nucName, boolean expand)
		{
		if(currentLin!=null)
			{
			Internal internal=getNucinfo(nucName);
			internal.expanded=expand;
			NucLineage.Nuc nuc=currentLin.nuc.get(nucName);
			for(String childName:nuc.child)
				recursiveExpand(childName, expand);
			}
		}
	
	
	}
