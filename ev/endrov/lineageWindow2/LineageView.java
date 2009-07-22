package endrov.lineageWindow2;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.data.EvSelection;
import endrov.lineageWindow2.HierarchicalPainter.Camera;
import endrov.nuc.*;
import endrov.util.EvDecimal;


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
	/////////////////////////// State of lineage for rendering //////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	private WeakHashMap<NucLineage, LinState> linState=new WeakHashMap<NucLineage, LinState>();
	
	/** 
	 * Cached information about nuclei 
	 */
	private static class LinState
		{
		public HierarchicalPainter.Camera cam=new HierarchicalPainter.Camera();
		public TreeMap<String, Internal> nucInternal=new TreeMap<String, Internal>();
		
		public boolean showHorizontalTree=true;	

		
		//Natural x-dimension: frames in seconds
		
		//No natural y-dimension
		
		//TODO
		
		/**
		 * Get information structure about nucleus
		 * @param nuc Name of nucleus
		 * @return Existing structure or a new one
		 */
		public Internal getNucinfo(String nuc)
			{
			Internal i=nucInternal.get(nuc);
			if(i==null)
				{
				i=new Internal();
				nucInternal.put(nuc, i);
				}
			return i;
			}
		
		
		public void recursiveExpand(String nucName, boolean expand, NucLineage currentLin)
			{
			if(currentLin!=null)
				{
				Internal internal=getNucinfo( nucName);
				internal.expanded=expand;
				NucLineage.Nuc nuc=currentLin.nuc.get(nucName);
				for(String childName:nuc.child)
					recursiveExpand(childName, expand, currentLin);
				}
			}


		}

	/** 
	 * Get draw cache for currently selected lineage 
	 */
	private LinState getLinState()
		{
		return getLinState(currentLin);
		}
	
	/** 
	 * Get state of lineage, never null 
	 */
	private LinState getLinState(NucLineage lin)
		{
		LinState dc=linState.get(lin);
		if(dc==null)
			linState.put(lin, dc=new LinState()); //TODO should center it
		return dc;
		}
	
	
	/*
	public HierarchicalPainter.Camera getCamera()
		{
		return getLinState().cam;
		}*/
	
	public EvDecimal getFrameFromCursor(int mx, int my)
		{
		LinState linstate=getLinState();
		//HierarchicalPainter.Camera cam=getLinState().cam;
		
		if(linstate.showHorizontalTree)
			return new EvDecimal(linstate.cam.toWorldY(my));
		else
			return new EvDecimal(linstate.cam.toWorldX(my));
		}
	
	
	//////////////////// TODO //////////////////
	
	public void setFrame(double frame){}
	public EvDecimal getFrame(){return EvDecimal.ZERO;}
	public void goRoot(){}
	public void goSelected(){}

	public void pan(int dx, int dy)
		{
		LinState linstate=getLinState();
		linstate.cam.cameraX-=linstate.cam.scaleScreenDistX(dx);
		linstate.cam.cameraY-=linstate.cam.scaleScreenDistY(dy);
		
		}

	public void zoomX(double factor, int midx)
		{
		LinState linstate=getLinState();
		linstate.cam.zoomX*=factor;
		}

	public void zoomY(double factor, int midy)
		{
		//must rotate TODO
		LinState linstate=getLinState();
		linstate.cam.zoomY*=factor;
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

	/*
	public void goRoot()
		{	
		Tuple<EvDecimal, String> found=currentLin.firstFrameOfLineage();
		if(found!=null)
			goInternalNuc(getNucinfo(found.snd()),camera);
		}

	public void goSelected()
		{
		HashSet<NucSel> selectedNuclei=NucLineage.getSelectedNuclei();
		if(!selectedNuclei.isEmpty())
			{
			String nucName=selectedNuclei.iterator().next().snd();
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
	*/
	

	
	
	public void foldAll()
		{
		for(String nucName:getRootNuc())
			getLinState(currentLin).recursiveExpand(nucName, false, currentLin);
		repaint();
		}
	public void unfoldAll()
		{
		for(String nucName:getRootNuc())
			getLinState(currentLin).recursiveExpand(nucName, true, currentLin);
		repaint();
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

	
	
	/**
	 * Draw everything
	 */
	public void paintComponent(Graphics g)
		{
		LinState linstate=getLinState(getLineage());
		
		//Option: could also put a transform onto g2d
		
		int width=getWidth();
		int height=getHeight();
		
		//Rotate image, part 1
		Graphics2D h;
		BufferedImage bim=null;
		if(linstate.showHorizontalTree)
			h=(Graphics2D)g;
		else
			{
			bim=new BufferedImage(getHeight(),getWidth(),BufferedImage.TYPE_3BYTE_BGR);
			h=(Graphics2D)bim.getGraphics();
			width=getHeight();
			height=getWidth();
			}

		//paint everything
		paintEverything((Graphics2D)h, true, linstate, width, height);


		//Rotate image part 2
		if(bim!=null)
			{
			AffineTransform af = new AffineTransform(); 
			af.translate(getWidth(), 0);
			af.rotate(Math.PI/2.0);
			((Graphics2D)g).drawImage(bim, af, null);
			}
		
		
		}
	
	
	private void paintEverything(Graphics2D h, boolean toScreen, LinState linstate, int width, int height)
		{
		//Redo list of clickable regions
		regionClickList.clear();
		drawnKeyFrames.clear();
		
		//Fill background
		h.setColor(Color.WHITE);
		h.fillRect(0, 0, width, height);

		//Draw frame lines
		if(toScreen)
			drawFrameLines(h,linstate, width, height);

		
		HierarchicalPainter hpainter=new HierarchicalPainter();
		
		//Update tree structure
		double displacement=0;
		for(String nucName:getRootNuc())
			{
			HierarchicalPainter.DrawNode dnode=new HierarchicalPainter.DrawNodeContainer();
			displacement+=updateTreeFormat(h,nucName,linstate, displacement,dnode);
			hpainter.topNodes.add(dnode);
			}
		System.out.println("cam "+linstate.cam);
		
		//Draw all trees
		hpainter.paint(h, width, height, linstate.cam);
		
		System.out.println("bb");
		System.out.println(hpainter.getTotalBoundingBox());
		
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
	
	public void drawFrameLines(Graphics2D g, LinState linstate, int width, int height)
		{
		if(showFrameLines)
			{
			double startFrame=linstate.cam.toWorldX(0);
			double endFrame=linstate.cam.toWorldX(width);
			
			int fitMaxLines=width/40;
			
			EvDecimal impreciseDeltaFrame=new EvDecimal((endFrame-startFrame)/fitMaxLines);
			
			//Use sane interval
			int numSubDiv=2;
			int pow=(int)Math.round(Math.log(impreciseDeltaFrame.doubleValue())/Math.log(numSubDiv));
			System.out.println("pow "+pow);
			EvDecimal dF;
			if(pow>=0)
				{
				dF=new EvDecimal(numSubDiv).pow(pow);
				}
			else 
				{
				pow=(int)Math.round(Math.log(impreciseDeltaFrame.doubleValue())/Math.log(10));
				dF=new EvDecimal("0.1").pow(-pow);
				}
			EvDecimal itFirstFrame=new EvDecimal(startFrame).divideToIntegralValue(dF).multiply(dF);
			EvDecimal itLastFrame=new EvDecimal(endFrame);
			
			int textY=height-5;//5
			for(EvDecimal curFrame=itFirstFrame;curFrame.lessEqual(itLastFrame);curFrame=curFrame.add(dF))
				{
				int x=linstate.cam.toScreenX(curFrame.doubleValue());
				g.setColor(frameLineColor);
				g.drawLine(x, 0, x, height);
				g.setColor(frameStringColor);
				g.translate(x, textY);
				g.rotate(-Math.PI/2);
				String fs=FrameControl.formatTime(curFrame);
				g.drawString(fs, 0, 0);
				g.rotate(Math.PI/2);
				g.translate(-x, -textY);
				}
			g.setColor(curFrameLineColor);
			int curFrameX=width/2;
			g.drawLine(curFrameX, 0, curFrameX, height);
			}
		}
	
	/**
	 * Draw expression profile
	 */
	private void drawExpression(Graphics g, String nucName, int endc, int midr, NucLineage.Nuc nuc, boolean toScreen, LinState linstate, int width, int height)
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
						boolean visible=(midr>=0 && linstate.cam.toScreenX(maxframe.doubleValue())>=0 && linstate.cam.toScreenX(minframe.doubleValue())<width &&
								midr-e.getValue().getMaxLevel()*expScale<height) || !toScreen;
						if(visible)
							{
							g.setColor(e.getValue().expColor);
							boolean hasLastCoord=false;
							int lastX=0, lastY=0;
							for(Map.Entry<EvDecimal, Double> ve:e.getValue().level.entrySet())
								{
								int y=(int)(-ve.getValue()*expScale+midr);
								int x=linstate.cam.toScreenX(ve.getKey().doubleValue());
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
	private void drawTree(Graphics2D g, String nucName, int midr, boolean toScreen, LinState linstate, int width, int height)
		{
		/*
		HierarchicalPainter.Camera cam=linstate.cam;
		
		int childNoPosBranchLength=30;
		NucLineage.Nuc nuc=currentLin.nuc.get(nucName);
		if(nuc==null)
			{
			//This is a hack in my opinion. Better if it can be found during tree structure planner
			//but this is more flexible right now. will give some artifacts
			System.out.println(nucName+" not found while drawing. bug!!!?");
			getLinState().nucInternal.remove(nucName);
			return;
			}
		
		Internal internal=linstate.getNucinfo(nucName);

		String namePrefix="";
		if(nuc.overrideEnd!=null && nuc.child.size()>0)
			namePrefix="!!! ";
		
		//If there are no first or last frames then handle it as well as possible 
		int startc;
		int endc;
		EvDecimal firstFrame=nuc.getFirstFrame();
		if(firstFrame==null)
			{
			startc=0;
			if(nuc.parent!=null)
				{
				Internal pInternal=linstate.getNucinfo(nuc.parent);
				startc=pInternal.getLastVXend(cam)+childNoPosBranchLength;
				//System.out.println("warn: no coord");
				namePrefix="!!! ";
				}
			}
		else
			startc=cam.toScreenX(firstFrame.doubleValue());

		EvDecimal lastFrame=nuc.getLastFrame();
		if(lastFrame==null)
			endc=startc;
		else
			endc=cam.toScreenX(lastFrame.doubleValue());
		
		//System.out.println(nucName+"  "+firstFrame+" "+lastFrame);
		
		//Draw expression
		drawExpression(g,nucName,endc,midr,nuc,toScreen,linstate, width, height);
		
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
			drawNucEnd(g, cam.toScreenX(nuc.overrideEnd.doubleValue()), midr);
		internal.setLastVXstart(startc);
		internal.setLastVXend(endc);
		internal.setLastVY(midr);
		
		//Draw keyframes
		int virtualWidth=width;
		if(showKeyFrames && 
				((midr>-keyFrameSize && midr<height+keyFrameSize &&
				endc>=-keyFrameSize && startc<=virtualWidth+keyFrameSize) || !toScreen))
			{
			g.setColor(Color.RED);
			//Test for complete visibility first, makes clipping cheap
			if((startc>=-keyFrameSize && endc<virtualWidth+keyFrameSize) || !toScreen)
				for(EvDecimal frame:nuc.pos.keySet())
					drawKeyFrame(g,cam.toScreenX(frame.doubleValue()), midr, nucName, frame);
			else
				for(EvDecimal frame:nuc.pos.keySet())
					{
					int x=cam.toScreenX(frame.doubleValue()); //This might be slower than just drawing though
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
					Internal cInternal=getLinState().nucInternal.get(cName);
					//Draw connecting line
					g.setColor(Color.BLACK);
					EvDecimal cFirstFrame=c.getFirstFrame();
					if(cFirstFrame!=null)
						g.drawLine(endc,midr,cam.toScreenX(cFirstFrame.doubleValue()),midr+cInternal.centerDisplacement);
					else
						g.drawLine(endc,midr,endc+childNoPosBranchLength,midr+cInternal.centerDisplacement);
					//Recurse down
					drawTree(g,cName, midr+cInternal.centerDisplacement, toScreen, linstate);
					}
				}
		
		//Draw expander
		if(nuc.child.size()>0)
			drawExpanderSymbol(g,nucName, endc,midr,internal.expanded);

		//Draw name of nucleus. Warn if something is wrong
		if((nuc.child.isEmpty() && showLeafLabel) || (!nuc.child.isEmpty() && showTreeLabel))
			drawNucName(g, namePrefix, new NucSel(currentLin, nucName), midr, endc);
		*/
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
	private void drawNucName(Graphics g, String prefix, NucSel nucPair, int midr, int endc)
		{
		String nucName=nucPair.snd();
		int fontHeight=g.getFontMetrics().getHeight();
		int fontWidth=g.getFontMetrics().stringWidth(prefix+nucName);
		int textc=endc+5;
		Graphics2D g2=(Graphics2D)g;
		if(EvSelection.isSelected(nucPair))
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
	private double updateTreeFormat(Graphics g, String nucName, LinState linstate, double displacement, HierarchicalPainter.DrawNode parentDrawNode)
		{		
		final Internal thisInternal=linstate.getNucinfo(nucName);
		NucLineage.Nuc nuc=currentLin.nuc.get(nucName);

		HierarchicalPainter.DrawNodeContainer thisDrawNode=new HierarchicalPainter.DrawNodeContainer();

		double y1=displacement;
		
		
		
		//Total width of children. 0 if none expanded
		double curChildOffset=displacement;
		
		curChildOffset+=2; //Temp
		
		//Only recurse if children are visible
		if(thisInternal.expanded)
			{
			//Sum up total width for children
			for(String cName:nuc.child)
				{
				double newDisp=updateTreeFormat(g,cName,linstate, curChildOffset, thisDrawNode);
				curChildOffset+=newDisp;
				}
			//Set displacements
			/*
			if(nuc.child.size()==1)
				{
				Internal cInternal=linstate.getNucinfo(nuc.child.first());
				thisInternal.centerY=cInternal.centerY; //TODO improve
				}
			else*/
			if(nuc.child.isEmpty())
				{
				thisInternal.centerY=displacement;
				}
			else
				{
				//Use the average
				double sum=0;
				for(String cName:nuc.child)
					{
					Internal cInternal=linstate.nucInternal.get(cName);
					sum+=cInternal.centerY;
					}
				thisInternal.centerY=sum/nuc.child.size();
				}
			}
		else
			{
			thisInternal.centerY=displacement;
			}
		double y2=curChildOffset;
		
		EvDecimal lastFrame=nuc.getLastFrame();
		double x2;
		if(lastFrame!=null)
			x2=lastFrame.doubleValue();
		else
			x2=0; //Use child
		
		thisInternal.endX=x2;
		
		EvDecimal firstFrame=nuc.getFirstFrame();
		double x1;
		if(firstFrame!=null)
			x1=firstFrame.doubleValue();
		else
			x1=x2-60; //Better than nothing

		thisInternal.startX=x1;
		
		//Compute width for this node
//		int fontHeight=g.getFontMetrics().getHeight()*2;
		//TODO enlarge if needed.
		
		
		
		HierarchicalPainter.DrawNode newnode=new HierarchicalPainter.DrawNode(x1,y1,x2,y2){
			public void paint(Graphics g, double width, double height, Camera cam)
				{
				g.setColor(Color.red);
				int y=cam.toScreenY(thisInternal.centerY);
				int x1=cam.toScreenX(thisInternal.startX);
				int x2=cam.toScreenY(thisInternal.endX);

				//System.out.println("y "+y+"  "+thisInternal.centerY);
				
				g.drawLine(x1, y, x2, y);
				
				}
		};
		thisDrawNode.addSubNode(newnode);
		
		parentDrawNode.addSubNode(thisDrawNode);
		return y2;
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
		//public int sizer=0;
		//public int centerDisplacement=0;
		
		public double centerY;
		public double startX, endX;
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
			EvSelection.unselectAll();
			//NucLineage.selectedNuclei.clear();
		BasicWindow.updateWindows();
		}

	public ClickRegion getClickRegion(MouseEvent e)
		{
		int mousex,mousey;
		NucLineage lin=getLineage();
		if(getLinState(lin).showHorizontalTree)
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
					NucLineage.mouseSelectNuc(new NucSel(currentLin, nucname), (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
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
			NucLineage lin=getLineage();
			LinState linstate=getLinState(lin);
			Internal internal=linstate.getNucinfo(nucname);
			if(SwingUtilities.isLeftMouseButton(e))
				internal.expanded=!internal.expanded;
			else if(SwingUtilities.isRightMouseButton(e))
				linstate.recursiveExpand(nucname, !internal.expanded, lin);
			repaint();
			}
		public String getHoverString()
			{
			return null;
			}
		}


	
	
	}
