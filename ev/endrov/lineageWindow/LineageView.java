package endrov.lineageWindow;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.*;

import endrov.basicWindow.BasicWindow;
import endrov.nuc.*;

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
	/////////////////////////// Hard Settings ///////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	private static final Color frameLineColor=new Color(220,220,220);
	private static final Color curFrameLineColor=new Color(150,150,150);
	private static final Color frameStringColor=new Color(100,100,100);

	/** Size of dot on expression profile */
	private static final int expDotSize=1;
	/** Size of expander icon */
	private static final int expanderSize=4;
	/** Size of key frame icon */
	private static final int keyFrameSize=2;
	

	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// State //((((((((/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	
	public NucLineage currentLin=null;

	public double camVY, camVX;
//	public double currentFrame=0;
	
	private double frameDist=5;
	private double branchScale=1;

	public double expScale=1;
	
	public boolean showHorizontalTree=true;	
	public boolean showFrameLines=true;
	public boolean showKeyFrames=true;	
	public boolean showExpLine=true;
	public boolean showExpSolid=true;
	public boolean showExpDot=true;
	public boolean showTreeLabel=true;
	public boolean showLeafLabel=true;
	
	
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
	
	/** Get width of view as if horizontal */
	private int getVirtualWidth()
		{
		return showHorizontalTree ? getWidth() : getHeight();
		}
	/** Get height of view as if horizontal */
	private int getVirtualHeight()
		{
		return showHorizontalTree ? getHeight() : getWidth();
		}
	/** Get frame from screen x,y coordinate */
	public int getFrameFromCursor(int x, int y)
		{
		return showHorizontalTree ? c2f(x) : c2f(y);
		}
	/** Convert frame position to coordinate */
	private int f2c(int f)
		{
		return (int)(f*frameDist-camVX);
		}	
	/** Convert coordinate to frame position */
	private int c2f(int c)
		{
		return (int)((c+camVX)/frameDist);
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
			if((allMinFrame==null || nuc.pos.firstKey()<allMinFrame) && !nuc.pos.isEmpty())
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
			String nucName=NucLineage.selectedNuclei.iterator().next().snd();
			Internal internal=getNucinfo(nucName);
			goInternalNuc(internal);
			}
		}
	private void goInternalNuc(Internal internal)
		{
		camVY+=internal.lastVY-getVirtualHeight()/2;
		camVX+=internal.lastVXstart-getVirtualWidth()/2;
		repaint();
		}
	
	/**
	 * Move according to mouse movement
	 */
	public void pan(int dx, int dy)
		{
		if(showHorizontalTree)
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
	 * Change the frame distance (*VX) without moving camera
	 */
	public void setFrameDist(double s)
		{
		if(s<0.1)	s=0.1; //Not allowed to happen, quick fix
		double h=getVirtualWidth()/2.0;
		double curmid=(camVX+h)/frameDist;
		frameDist=s;
		camVX=curmid*frameDist-h;
		}
	
	/**
	 * Change branch scale (*VY) without moving camera
	 */
	public void setBranchScale(double s)
		{
		//TODO WRONG FORMULA?
		double h=getVirtualHeight()/2.0;
		double curmid=(camVY+h)/branchScale;
		branchScale=s;
		camVY=curmid*branchScale-h;
		}


	
	/** Move camera to show some frame */
	public void setFrame(double frame)
		{
		camVX=frame*frameDist-getVirtualWidth()/2;
		repaint();
		}
	/** Get frame camera currently looks at */
	public double getFrame()
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
	
	/** Fold all trees */
	public void foldAll()
		{
		for(String nucName:getRootNuc())
			recursiveExpand(nucName, false);
		repaint();
		}
	/** Unfold all trees */
	public void unfoldAll()
		{
		for(String nucName:getRootNuc())
			recursiveExpand(nucName, true);
		repaint();
		}

	
	/**
	 * Draw everything
	 */
	public void paintComponent(Graphics g)
		{
		//Rotate image, part 1
		Graphics h;
		BufferedImage bim=null;
		if(showHorizontalTree)
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

		//Update tree structure
		removeUnusedInternal();
		for(String nucName:getRootNuc())
			updateTreeFormat(h,nucName);
		
		//Draw all trees
		int displacement=0;
		boolean first=true;
		for(String nucName:getRootNuc())
			{
			Internal nuc=getNucinfo(nucName);
			if(first)	first=false; else displacement+=nuc.sizer/2; //I don't like this layout really
			drawTree((Graphics2D)h, nucName,(int)(displacement+getVirtualHeight()/2-camVY)); 
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
	 * Draw the lines for frames, in the background
	 */
	public void drawFrameLines(Graphics g)
		{
		if(showFrameLines)
			{
			double frameLineSkip=(int)(20/frameDist);
			if(frameLineSkip<1)
				frameLineSkip=1;
			double starti=camVX/frameDist;
			starti=((int)(starti/frameLineSkip))*frameLineSkip;
			
			Graphics2D g2=(Graphics2D)g;
			double endi=getVirtualWidth()/frameDist+1+frameLineSkip+camVX/frameDist;
			int height=getVirtualHeight();
			int textY=height-5;//5
			for(double i=starti;i<endi;i+=frameLineSkip)
				{
				int x=(int)(i*frameDist-camVX);
				g.setColor(frameLineColor);
				g.drawLine(x, 0, x, height);
				g.setColor(frameStringColor);
				g2.translate(x, textY);
				g2.rotate(-Math.PI/2);
				g.drawString(""+(int)i, 0, 0);
				g2.rotate(Math.PI/2);
				g2.translate(-x, -textY);
				}
			g.setColor(curFrameLineColor);
			int curFrameX=getVirtualWidth()/2;
			g.drawLine(curFrameX, 0, curFrameX, height);
			}
		}
	
	/**
	 * Draw expression profile
	 */
	private void drawExpression(Graphics g, String nucName, int midr, NucLineage.Nuc nuc)
		{
		if(showExpDot || showExpSolid || showExpLine)
			for(Map.Entry<String, NucExp> e:nuc.exp.entrySet())
				if(!e.getValue().level.isEmpty())
					{
//					double expScale=0.2;
		
		
					//Only draw if potentially visible
					int minframe=e.getValue().level.firstKey();
					int maxframe=e.getValue().level.lastKey();
					boolean visible=midr>=0 && f2c(maxframe)>=0 && f2c(minframe)<getVirtualWidth() &&
													midr-e.getValue().getMaxLevel()*expScale<getVirtualHeight();
					if(visible)
						{
						g.setColor(e.getValue().expColor);
						boolean hasLastCoord=false;
						int lastX=0, lastY=0;
						for(Map.Entry<Integer, Double> ve:e.getValue().level.entrySet())
							{
							int y=(int)(-ve.getValue()*expScale+midr);
							int x=f2c(ve.getKey());
							if(hasLastCoord)
								{
								if(showExpLine)
									g.drawLine(lastX, lastY, x, y);
								if(showExpSolid)
									g.fillPolygon(new int[]{lastX,lastX,x,x}, new int[]{midr,lastY,y,midr}, 4);
								}
							if(showExpDot)
								g.drawRect(x-expDotSize, y-expDotSize, 2*expDotSize, 2*expDotSize);
							hasLastCoord=true;
							lastX=x;
							lastY=y;
							}
						}
					}
		}
	
	/**
	 * Recursive function to draw a tree
	 * @param internal Which node to recurse from
	 */
	private void drawTree(Graphics2D g, String nucName, int midr)
		{
		int childNoPosBranchLength=30;
		NucLineage.Nuc nuc=currentLin.nuc.get(nucName);
		if(nuc==null)
			{
			//This is a hack in my opinion. Better if it can be found during tree structure planner
			//but this is more flexible right now. will give some artifacts
			System.out.println(nucName+" not found while drawing. bug!!!?");
			getDrawCache().nucInternal.remove(nucName);
			return;
			}
		
		Internal internal=getNucinfo(nucName);

		String namePrefix="";
		if(nuc.overrideEnd!=null && nuc.child.size()>0)
			namePrefix="!!! ";
		
		//If there are no keyframes then this gotta be handled somehow. it shouldn't happen
		//but cope with it as well as possible
		int startc;
		int endc;
		if(nuc.pos.isEmpty())
			{
			startc=0;
			if(nuc.parent!=null)
				{
				Internal pInternal=getNucinfo(nuc.parent);
				startc=pInternal.lastVXend+childNoPosBranchLength;
				System.out.println("warn: no coord");
				namePrefix="!!! ";
				}
			endc=startc;
			}
		else
			{
			int firstFrame=nuc.pos.firstKey();
			int lastFrame=nuc.lastFrame();
			startc=f2c(firstFrame);
			endc=f2c(lastFrame);
			}
		
		//Draw expression
		drawExpression(g,nucName,midr,nuc);
		
		//Draw line spanning frames
		if(nuc.colorNuc!=null)
			{
			g.setColor(nuc.colorNuc);
			g.drawLine(startc, midr, endc, midr);
			g.drawLine(startc, midr-1, endc, midr-1);
			g.drawLine(startc, midr+1, endc, midr+1);
			}
		else
			{
			g.setColor(Color.BLACK);
			g.drawLine(startc, midr, endc, midr);
			}
		if(nuc.overrideEnd!=null && nuc.child.size()==0)
			drawNucEnd(g, f2c(nuc.overrideEnd), midr);
		internal.lastVXstart=startc;
		internal.lastVXend=endc;
		internal.lastVY=midr;
		
		//Draw keyframes
		int virtualWidth=getVirtualWidth();
		if(showKeyFrames && midr>-keyFrameSize && midr<getVirtualHeight()+keyFrameSize &&
				endc>=-keyFrameSize && startc<=virtualWidth+keyFrameSize)
			{
			g.setColor(Color.RED);
			//Test for complete visibility first, makes clipping cheap
			if(startc>=-keyFrameSize && endc<virtualWidth+keyFrameSize)
				for(int frame:nuc.pos.keySet())
					drawKeyFrame(g,f2c(frame), midr, nucName, frame);
			else
				for(int frame:nuc.pos.keySet())
					{
					int x=f2c(frame); //This might be slower than just drawing though
					if(x>-keyFrameSize && x<virtualWidth+keyFrameSize)
						drawKeyFrame(g,x, midr, nucName, frame);
					}
			}
		
		//Draw children
		if(internal.expanded)
			{
			for(String cName:nuc.child)
				{
				NucLineage.Nuc c=currentLin.nuc.get(cName);
					{
					Internal cInternal=getDrawCache().nucInternal.get(cName);
					//Draw connecting line
					g.setColor(Color.BLACK);
					if(!c.pos.isEmpty())
						g.drawLine(endc,midr,f2c(c.pos.firstKey()),midr+cInternal.centerDisplacement);
					else
						g.drawLine(endc,midr,endc+childNoPosBranchLength,midr+cInternal.centerDisplacement);
					//Recurse down
					drawTree(g,cName, midr+cInternal.centerDisplacement);
					}
				}
			}
		
		//Draw expander
		if(nuc.child.size()>0)
			drawExpanderSymbol(g,nucName, endc,midr,internal.expanded);

		//Draw name of nucleus. Warn if something is wrong
	/*	if(nuc.end!=null && nuc.child.size()>0)
			drawNucName(g, "!!! ", new NucPair(currentLin, nucName), midr, endc);
		else
			drawNucName(g, "", new NucPair(currentLin, nucName), midr, endc);*/
		if((nuc.child.isEmpty() && showLeafLabel) || (!nuc.child.isEmpty() && showTreeLabel))
			drawNucName(g, namePrefix, new NucPair(currentLin, nucName), midr, endc);
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
	 * Draw text name
	 */
	private void drawNucName(Graphics g, String prefix, NucPair nucPair, int midr, int endc)
		{
		String nucName=nucPair.snd();
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
		if(!expanded)
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
//				if(showTreeLabel)
					cInternal.centerDisplacement=10;
	//			else
		//			cInternal.centerDisplacement=10;
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
		if(internal.sizer<fontHeight && nuc.child.isEmpty())
			internal.sizer=fontHeight;
		
		//Scale
		internal.sizer*=branchScale;
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
		public int lastVY, lastVXstart, lastVXend;
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
		if(showHorizontalTree)
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
