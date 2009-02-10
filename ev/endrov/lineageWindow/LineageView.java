package endrov.lineageWindow;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.*;

import endrov.basicWindow.*;
import endrov.lineageWindow.print.Print2DtoPostScript;
import endrov.nuc.*;
import endrov.util.EvDecimal;
import endrov.util.Tuple;


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
	/////////////////////////// State ///////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	
	public NucLineage currentLin=null;

//	public double currentFrame=0;
	
	private double frameDist=5;
	private double branchScale=1;

	public double expScale=1;
	
	public boolean showFrameLines=true;
	public boolean showKeyFrames=true;	
	public boolean showExpLine=true;
	public boolean showExpSolid=true;
	public boolean showExpDot=true;
	public boolean showTreeLabel=true;
	public boolean showLeafLabel=true;
	public boolean showScale=true;
	
	
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
		public int x, y;
		public EvDecimal frame;
		public String nuc;
		}
	
	

	/**
	 * Draw key frame icon
	 */
	private void drawKeyFrame(Graphics g, int x, int y, String nuc, EvDecimal frame)
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
	/////////////////////////// Camera for view /////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	Camera camera=new Camera();
	
	public class Camera
		{
		public double camVY, camVX;
		public boolean showHorizontalTree=true;	

		/** Get width of view as if horizontal */
		public int getVirtualWidth()
			{
			return showHorizontalTree ? getWidth() : getHeight();
			}
		/** Get height of view as if horizontal */
		public int getVirtualHeight()
			{
			return showHorizontalTree ? getHeight() : getWidth();
			}
		/** Get frame from screen x,y coordinate */
		public EvDecimal getFrameFromCursor(int x, int y)
			{
			return new EvDecimal(showHorizontalTree ? c2f(x) : c2f(y));
			}
		/** Convert frame position to coordinate */
		public int f2c(double f)
			{
			return (int)(f*frameDist-camVX);
			}	
		/** Convert coordinate to frame position */
		public int c2f(int c)
			{
			return (int)((c+camVX)/frameDist);
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

		
		}
	
  /////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// The rest ////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	
	
	public NucLineage getLineage()
		{
		return currentLin;
		}
	
	public LineageView()
		{
		addMouseMotionListener(new MouseMotionListener(){
			public void mouseDragged(MouseEvent e)
				{
				}
			public void mouseMoved(MouseEvent e)
				{
				String hovers=null;
				ClickRegion r=getClickRegion(e);
				if(r!=null)
					hovers=r.getHoverString();
				setToolTipText(hovers);
				}
		});
		}
	
	/**
	 * Go to the first root
	 */
	public void goRoot()
		{	
		Tuple<EvDecimal, String> found=currentLin.firstFrameOfLineage();
		if(found!=null)
			goInternalNuc(getNucinfo(found.snd()),camera);
		/*
		Camera cam=camera;
		EvDecimal allMinFrame=null;
		Set<String> roots=getRootNuc();
		String rootName=null;
		for(String nucName:roots)
			{
			NucLineage.Nuc nuc=currentLin.nuc.get(nucName);
			if((allMinFrame==null || nuc.firstFrame().less(allMinFrame)) && !nuc.pos.isEmpty())
				{
				allMinFrame=nuc.firstFrame();
				rootName=nucName;
				}
			}
		if(allMinFrame!=null)
			goInternalNuc(getNucinfo(rootName),cam);
			*/
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
			goInternalNuc(internal, camera);
			}
		}
	private void goInternalNuc(Internal internal, Camera cam)
		{
		cam.camVY+=internal.getLastVY(cam)-cam.getVirtualHeight()/2;
		cam.camVX+=internal.getLastVXstart(cam)-cam.getVirtualWidth()/2;
		repaint();
		}
	
	

	
	/**
	 * Change the frame distance (*VX) without moving camera
	 */
	public void setFrameDist(double s)
		{
		Camera cam=camera;
		if(s<0.1)	s=0.1; //Not allowed to happen, quick fix
		double h=cam.getVirtualWidth()/2.0;
		double curmid=(cam.camVX+h)/frameDist;
		frameDist=s;
		cam.camVX=curmid*frameDist-h;
		}
	
	/**
	 * Change branch scale (*VY) without moving camera
	 */
	public void setBranchScale(double s)
		{
		Camera cam=camera;
		//TODO WRONG FORMULA?
		double h=cam.getVirtualHeight()/2.0;
		double curmid=(cam.camVY+h)/branchScale;
		branchScale=s;
		cam.camVY=curmid*branchScale-h;
		}


	
	/** Move camera to show some frame */
	public void setFrame(double frame)
		{
		Camera cam=camera;
		cam.camVX=frame*frameDist-cam.getVirtualWidth()/2;
		repaint();
		}
	/** Get frame camera currently looks at */
	public double getFrame()
		{
		Camera cam=camera;
		return (cam.camVX+cam.getVirtualWidth()/2)/frameDist;
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
		Camera cam=camera;
		//Rotate image, part 1
		Graphics2D h;
		BufferedImage bim=null;
		if(cam.showHorizontalTree)
			h=(Graphics2D)g;
		else
			{
			bim=new BufferedImage(cam.getVirtualWidth(),cam.getVirtualHeight(),BufferedImage.TYPE_3BYTE_BGR);
			h=(Graphics2D)bim.getGraphics();
			}
		
		paintEverything((Graphics2D)h, true,cam);


		//Rotate image part 2
		if(bim!=null)
			{
			AffineTransform af = new AffineTransform(); 
			af.translate(cam.getVirtualHeight(), 0);
			af.rotate(Math.PI/2.0);
			((Graphics2D)g).drawImage(bim, af, null);
			}
		
		
		}
	
	
	public void saveToDisk()
		{
//	TEMP: draw to file
		//Print2DtoPostScript print=
		new Print2DtoPostScript(new File("/tmp/out.ps")){
			public int getHeight()
				{
				return 0;
				}
			public int getWidth()
				{
				return 0;
				}
			public void paintComponent(Graphics2D g)
				{
				System.out.println("here savetodisk");
				Camera cam=new Camera();
				paintEverything(g, false, cam);
				}
		};		
		}
	
	private void paintEverything(Graphics2D h, boolean toScreen, Camera cam)
		{
		//Redo list of clickable regions
		regionClickList.clear();
		drawnKeyFrames.clear();
		
		//Fill background
		h.setColor(Color.WHITE);
		h.fillRect(0, 0, cam.getVirtualWidth(), cam.getVirtualHeight());

		//Draw frame lines
		if(toScreen)
			drawFrameLines(h,cam);

		//Update tree structure
		for(String nucName:getRootNuc())
			updateTreeFormat(h,nucName);
		
		//Draw all trees
		int displacement=0;
		boolean first=true;
		for(String nucName:getRootNuc())
			{
			Internal nuc=getNucinfo(nucName);
			if(first)	first=false; else displacement+=nuc.sizer/2; //I don't like this layout really
			drawTree((Graphics2D)h, nucName,(int)(displacement+cam.getVirtualHeight()/2-cam.camVY),toScreen,cam); 
			displacement+=nuc.sizer/2; //maybe really half this and half next?
			}

		//Draw scale bar
		drawScalebar(h);
		}
	
	
	/**
	 * Adaptively draw scale bar
	 */
	public void drawScalebar(Graphics g)
		{
		if(showScale)
			{
			int yshift=10;
			int sh=2*getHeight()/3;
			int x=20;
			
			double upper=Math.pow(10, (int)Math.log10(sh/expScale));
			int toti=10;
			
			if(upper*expScale*5<sh)
				{
				upper*=5;
				toti=5;
				}
			
			g.setColor(Color.BLACK);
			int y1=yshift;
			int y2=yshift+(int)(upper*expScale);
			g.drawLine(x, y1, x,y2);
			g.drawString(""+upper+" um", x+10, y2);
			
			int x1=x-2, x2=x+2;
			for(int i=0;i<toti+1;i++)
				{
				int y3=(int)(yshift+upper*expScale*i/toti);
				g.drawLine(x1, y3, x2, y3);
				}
			}
		}
	
	
	
	/**
	 * Draw the lines for frames, in the background
	 */
	public void drawFrameLines(Graphics g, Camera cam)
		{
		if(showFrameLines)
			{
			double frameLineSkip=(int)(20/frameDist);
			if(frameLineSkip<1)
				frameLineSkip=1;
			double starti=cam.camVX/frameDist;
			starti=((int)(starti/frameLineSkip))*frameLineSkip;
			
			Graphics2D g2=(Graphics2D)g;
			double endi=cam.getVirtualWidth()/frameDist+1+frameLineSkip+cam.camVX/frameDist;
			int height=cam.getVirtualHeight();
			int textY=height-5;//5
			for(double i=starti;i<endi;i+=frameLineSkip)
				{
				int x=(int)(i*frameDist-cam.camVX);
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
			int curFrameX=cam.getVirtualWidth()/2;
			g.drawLine(curFrameX, 0, curFrameX, height);
			}
		}
	
	/**
	 * Draw expression profile
	 */
	private void drawExpression(Graphics g, String nucName, int endc, int midr, NucLineage.Nuc nuc, boolean toScreen, Camera cam)
		{
//		int colorIndex=-1;
//		EvColor colorList[]=EvColor.colorList;
		if(showExpAtAll())
			for(Map.Entry<String, NucExp> e:nuc.exp.entrySet())
				if(!e.getValue().level.isEmpty())
					{
					if(e.getKey().equals("divDev")) //Division time deviation, special rendering
						{
						int y1=midr+expanderSize+2;
						int y2=y1-1;
						
						double level=e.getValue().level.values().iterator().next()*frameDist;
						int x1=endc-(int)level;
						int x2=endc+(int)level;
						
						g.setColor(Color.BLACK);
						g.drawLine(x1, y1, x2, y1);
						g.drawLine(x1, y1, x1, y2);
						g.drawLine(x2, y1, x2, y2);
						}
					else //Ordinary level curve
						{
//						colorIndex=(colorIndex+1)%colorList.length;

						//Only draw if potentially visible
						EvDecimal minframe=e.getValue().level.firstKey();
						EvDecimal maxframe=e.getValue().level.lastKey();
						boolean visible=(midr>=0 && cam.f2c(maxframe.doubleValue())>=0 && cam.f2c(minframe.doubleValue())<cam.getVirtualWidth() &&
								midr-e.getValue().getMaxLevel()*expScale<cam.getVirtualHeight()) || !toScreen;
						if(visible)
							{
							g.setColor(e.getValue().expColor);
							boolean hasLastCoord=false;
							int lastX=0, lastY=0;
							for(Map.Entry<EvDecimal, Double> ve:e.getValue().level.entrySet())
								{
								int y=(int)(-ve.getValue()*expScale+midr);
								int x=cam.f2c(ve.getKey().doubleValue());
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
		}
	
	
	private boolean showExpAtAll()
		{
		return showExpDot || showExpSolid || showExpLine;
		}
	
	/**
	 * Recursive function to draw a tree
	 * @param internal Which node to recurse from
	 */
	private void drawTree(Graphics2D g, String nucName, int midr, boolean toScreen, Camera cam)
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
		
		//If there are no first or last frames then handle it as well as possible 
		int startc;
		int endc;
		EvDecimal firstFrame=nuc.firstFrame();
		if(firstFrame==null)
			{
			startc=0;
			if(nuc.parent!=null)
				{
				Internal pInternal=getNucinfo(nuc.parent);
				startc=pInternal.getLastVXend(cam)+childNoPosBranchLength;
				//System.out.println("warn: no coord");
				namePrefix="!!! ";
				}
			}
		else
			startc=cam.f2c(firstFrame.doubleValue());

		EvDecimal lastFrame=nuc.lastFrame();
		if(lastFrame==null)
			endc=startc;
		else
			endc=cam.f2c(lastFrame.doubleValue());
		
		//System.out.println(nucName+"  "+firstFrame+" "+lastFrame);
		
		//Draw expression
		drawExpression(g,nucName,endc,midr,nuc,toScreen,cam);
		
		//System.out.println(nucName+" "+firstFrame+"    -    "+lastFrame);
		
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
			drawNucEnd(g, cam.f2c(nuc.overrideEnd.doubleValue()), midr);
		internal.setLastVXstart(cam,startc);
		internal.setLastVXend(cam,endc);
		internal.setLastVY(cam,midr);
		
		//Draw keyframes
		int virtualWidth=cam.getVirtualWidth();
		if(showKeyFrames && 
				((midr>-keyFrameSize && midr<cam.getVirtualHeight()+keyFrameSize &&
				endc>=-keyFrameSize && startc<=virtualWidth+keyFrameSize) || !toScreen))
			{
			g.setColor(Color.RED);
			//Test for complete visibility first, makes clipping cheap
			if((startc>=-keyFrameSize && endc<virtualWidth+keyFrameSize) || !toScreen)
				for(EvDecimal frame:nuc.pos.keySet())
					drawKeyFrame(g,cam.f2c(frame.doubleValue()), midr, nucName, frame);
			else
				for(EvDecimal frame:nuc.pos.keySet())
					{
					int x=cam.f2c(frame.doubleValue()); //This might be slower than just drawing though
					if(x>-keyFrameSize && x<virtualWidth+keyFrameSize)
						drawKeyFrame(g,x, midr, nucName, frame);
					}
			}
		
		//Draw children
		if(internal.expanded)
			for(String cName:nuc.child)
				{
				NucLineage.Nuc c=currentLin.nuc.get(cName);
					{
					Internal cInternal=getDrawCache().nucInternal.get(cName);
					//Draw connecting line
					g.setColor(Color.BLACK);
					EvDecimal cFirstFrame=c.firstFrame();
					if(cFirstFrame!=null)
						g.drawLine(endc,midr,cam.f2c(cFirstFrame.doubleValue()),midr+cInternal.centerDisplacement);
					else
						g.drawLine(endc,midr,endc+childNoPosBranchLength,midr+cInternal.centerDisplacement);
					//Recurse down
					drawTree(g,cName, midr+cInternal.centerDisplacement, toScreen, cam);
					}
				}
		
		//Draw expander
		if(nuc.child.size()>0)
			drawExpanderSymbol(g,nucName, endc,midr,internal.expanded);

		//Draw name of nucleus. Warn if something is wrong
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
		regionClickList.add(new ClickRegionName(nucName, textc, textr-3*fontHeight/4, fontWidth,fontHeight));
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
		private int lastVY, lastVXstart, lastVXend;
		public int getLastVY(Camera cam)
			{
			return lastVY;
			}
		public int getLastVXstart(Camera cam)
			{
			return lastVXstart;
			}
		public int getLastVXend(Camera cam)
			{
			return lastVXend;
			}
		public void setLastVY(Camera cam, int x)
			{
			lastVY=x;
			}
		public void setLastVXstart(Camera cam, int x)
			{
			lastVXstart=x;
			}
		public void setLastVXend(Camera cam, int x)
			{
			lastVXend=x;
			}
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
		ClickRegion r=getClickRegion(e);
		if(r!=null)
			r.clickRegion(e);
		else if(SwingUtilities.isLeftMouseButton(e))
			NucLineage.selectedNuclei.clear();
		BasicWindow.updateWindows();
		}

	public ClickRegion getClickRegion(MouseEvent e)
		{
		Camera cam=camera;
		int mousex,mousey;
		if(cam.showHorizontalTree)
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
				return r;
		return null;
		}
	
	/**
	 * Mouse click handler
	 */
	public static abstract class ClickRegion
		{
		public int x=0,y=0,w=0,h=0;
		public abstract void clickRegion(MouseEvent e);
		public abstract String getHoverString();
		}

	/**
	 * Mouse click handler: on a name panel
	 */
	public class ClickRegionName extends ClickRegion
		{
		public String nucname;
		public ClickRegionName(String nucname, int x, int y, int w, int h)
			{this.nucname=nucname; this.x=x; this.y=y; this.w=w; this.h=h;}
		public void clickRegion(MouseEvent e)
			{
			if(currentLin!=null)
				{
				//System.out.println("here "+nucname+"   "+SwingUtilities.isLeftMouseButton(e)+"  "+currentLin);
				if(SwingUtilities.isLeftMouseButton(e))
					NucLineage.mouseSelectNuc(new NucPair(currentLin, nucname), (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
				}
			BasicWindow.updateWindows();
			}
		public String getHoverString()
			{
			if(currentLin!=null)
				{
				NucLineage.Nuc nuc=currentLin.nuc.get(nucname);
				if(nuc!=null)
					return nuc.description;
				}
			return null;
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
		public String getHoverString()
			{
			return null;
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
