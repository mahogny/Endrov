/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineageWindow;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.vecmath.Vector2d;


import endrov.basicWindow.*;
import endrov.data.EvSelection;
import endrov.lineageWindow.HierarchicalPainter.Camera;
import endrov.particle.*;
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
	
	private static final double sizeOfBranch=32;

	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// State ///////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	
	public Lineage currentLin=null;

	//public double expScale=1;
	
	public boolean showFrameLines=true;
	public boolean showKeyFrames=true;
	public boolean showEvents=true;
	public boolean showExpLine=true;
	public boolean showExpSolid=true;
	public boolean showExpDot=true;
	public boolean showLabel=true;
	public boolean showScale=true;
	public boolean showHorizontalTree=true;	
	
	
	public boolean getShowKeyFrames(){return showKeyFrames;}
	public boolean getShowLabel(){return showLabel;}
	public boolean getShowEvents(){return showEvents;};
	/**
	 * Get width and height of the screen, taking rotation into account
	 */
	public Tuple<Integer,Integer> getRotatedWidthHeight()
		{
		if(showHorizontalTree)
			return Tuple.make(getWidth(), getHeight());
		else
			return Tuple.make(getHeight(), getWidth());
		}

	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Expression rendering settings ///////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	public static class ExpRenderSetting
		{
		public final static int typeGraphOnTop=0;
		public final static int typeColorIntensity=1;
		public final static int typeColorIntensityDiff=2;
		public final static int typeTimeDev=3;
		public final static int typeLineThickness=4;
		
		public EvColor color;
		
		public int type;
		public String expname1="", expname2="";
		public Double scale1; //If null, then calculate
		
		@Override
		public String toString()
			{
			return "Exprendersettings "+color+" "+type+" expname1: "+expname1+" expname2: "+expname2+" "+scale1;
			}
		}
		
	
	public final LinkedList<ExpRenderSetting> listExpRenderSettings=new LinkedList<ExpRenderSetting>();
	
	
	public void setExpRenderSettings(Collection<ExpRenderSetting> settings)
		{
		listExpRenderSettings.clear();
		listExpRenderSettings.addAll(settings);
		repaint();
		}

	
	
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// State of lineage for rendering //////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	private WeakHashMap<Lineage, LineageState> linState=new WeakHashMap<Lineage, LineageState>();
	
	
	/**
	 * Cached information about particle
	 */
	public static class ParticleState
		{
		public boolean isExpanded=true;
		public double centerY;
		public double startX, endX;
		}

	
	/** 
	 * Cached information about lineage 
	 */
	private class LineageState
		{
		public HierarchicalPainter.Camera cam=new HierarchicalPainter.Camera();
		public TreeMap<String, ParticleState> particleState=new TreeMap<String, ParticleState>();
		
		public LineageState(Lineage lin)
			{
			cam.zoomX=1.0/60;
			zoomAll(lin);
			}

		/**
		 * Zoom camera so that it shows everything
		 */
		public void zoomAll(Lineage lin)
			{
			Tuple<Integer,Integer> wh=getRotatedWidthHeight();
			HierarchicalPainter hpainter=new HierarchicalPainter();
			layoutAllTrees(lin, this, hpainter, wh.fst(), wh.snd());
			HierarchicalPainter.BoundingBox bb=hpainter.getTotalBoundingBox();
			if(bb!=null)
				{
				if(bb.x1==bb.x2)
					bb=new HierarchicalPainter.BoundingBox(bb.x1-20, bb.y1, bb.x2+20, bb.y2);
				if(bb.y1==bb.y2)
					bb=new HierarchicalPainter.BoundingBox(bb.x1, bb.y1-20, bb.x2, bb.y2+20);
				cam.showArea(bb, wh.fst(),wh.snd());
				}
			else
				System.out.println("No content to zoom at");
			}
		
		/**
		 * Get information structure about particle
		 * @param name Name of particle
		 * @return Existing structure or a new one
		 */
		public ParticleState getParticleState(String name)
			{
			ParticleState i=particleState.get(name);
			if(i==null)
				{
				i=new ParticleState();
				particleState.put(name, i);
				}
			return i;
			}
		
		/**
		 * Recursively set expand status of branches
		 */
		public void recursiveExpand(String name, boolean expand, Lineage currentLin)
			{
			if(currentLin!=null)
				{
				ParticleState internal=getParticleState(name);
				internal.isExpanded=expand;
				Lineage.Particle nuc=currentLin.particle.get(name);
				for(String childName:nuc.child)
					recursiveExpand(childName, expand, currentLin);
				}
			}

		
		/**
		 * Move camera to given position, repaint
		 */
		private void goToPosition(double frame, double y)
			{
			cam.panCorrespondenceX(getRotatedWidthHeight().fst()/2, frame);
			cam.panCorrespondenceY(getRotatedWidthHeight().snd()/2, y);
			repaint();
			}

		
		
		}

	public Set<String> collectExpNames()
		{
		if(currentLin!=null)
			return currentLin.getAllExpNames();
		else
			return Collections.emptySet();
		}

	
	/** 
	 * Get state of lineage, never null 
	 */
	private LineageState getLinState(Lineage lin)
		{
		LineageState dc=linState.get(lin);
		if(dc==null)
			linState.put(lin, dc=new LineageState(lin)); 
		return dc;
		}
	
	
	public EvDecimal getFrameFromCursor(int mx, int my)
		{
		Lineage lin=getLineage();
		LineageState linstate=getLinState(lin);
		Vector2d uv=xyToUV(new Vector2d(mx,my));
		return new EvDecimal(linstate.cam.toWorldX(uv.x));
/*		if(showHorizontalTree)
			return new EvDecimal(linstate.cam.toWorldX(mx));
		else
			return new EvDecimal(linstate.cam.toWorldX(my));*/
		}
	
	
	
	/**
	 * Move camera. Does not redraw
	 */
	public void pan(int dx, int dy)
		{
		Lineage lin=getLineage();
		LineageState linstate=getLinState(lin);
	//	Vector2d duv=xyToUV(new Vector2d(dx,dy));
		
		if(showHorizontalTree)
			{
			linstate.cam.cameraX-=linstate.cam.scaleScreenDistX(dx);
			linstate.cam.cameraY-=linstate.cam.scaleScreenDistY(dy);
			}
		else
			{
			linstate.cam.cameraX-=linstate.cam.scaleScreenDistX(dy);
			linstate.cam.cameraY+=linstate.cam.scaleScreenDistY(dx);
			}
//		linstate.cam.cameraX-=linstate.cam.scaleScreenDistX(duv.x);
//		linstate.cam.cameraY-=linstate.cam.scaleScreenDistY(duv.y);
		}


	/**
	 * UV are coordinates after the tree has optionally been rotated.
	 * untested!!
	 */
	public Vector2d xyToUV(Vector2d v)
		{
		if(showHorizontalTree)
			return v;
		else
			return new Vector2d(v.y,v.x);
		}
	public Vector2d uvToXY(Vector2d v)
		{
		if(showHorizontalTree)
			return v;
		else
			return new Vector2d(v.y,v.x);
		}
		
	
	/**
	 * General zooming relative to a point on the screen
	 */
	public void zoom(double factorX, int midx, double factorY, int midy)
		{
		Lineage lin=getLineage();
		LineageState linstate=getLinState(lin);
		if(!showHorizontalTree)
			{
			//Invert
			int tempi=midx;
			midx=midy;
			midy=tempi;
			
			double tempd=factorX;
			factorX=factorY;
			factorY=tempd;
			}

		double worldx=linstate.cam.toWorldX(midx);
		double worldy=linstate.cam.toWorldY(midy);
		
		linstate.cam.zoomX*=factorX;
		linstate.cam.zoomY*=factorY;
		
		double worldx2=linstate.cam.toWorldX(midx);
		double worldy2=linstate.cam.toWorldY(midy);
	
		linstate.cam.cameraX-=linstate.cam.scaleWorldDistX(worldx2-worldx);
		linstate.cam.cameraY-=linstate.cam.scaleWorldDistY(worldy2-worldy);
		}
	
	public void zoomX(double factor, int midx){zoom(factor,midx,1,0);}
	public void zoomY(double factor, int midy){zoom(1,0,factor,midy);}

	
	/**
	 * Set frame position
	 */
	public void setFrame(double frame)
		{
		Lineage lin=getLineage();
		LineageState linstate=getLinState(lin);
		linstate.cam.panCorrespondenceX(getRotatedWidthHeight().fst()/2, frame);
		repaint();
		}
	
	
	/**
	 * Get frame position
	 */
	public EvDecimal getFrame()
		{
		Lineage lin=getLineage();
		LineageState linstate=getLinState(lin);
		double worldx=linstate.cam.toWorldX(getRotatedWidthHeight().fst()/2);
		return new EvDecimal(worldx);
//		return EvDecimal.ZERO;
		}
	

	
	/**
	 * Move camera to root, repaint
	 */
	public void goToRoot()
		{
		Tuple<EvDecimal, String> found=currentLin.firstFrameOfLineage();
		if(found!=null)
			{
			LineageState linstat=getLinState(currentLin);
			double worldX=found.fst().doubleValue();
			double worldY=linstat.getParticleState(found.snd()).centerY;
			linstat.goToPosition(worldX,worldY);
			}
		}
		
	/**
	 * Move camera to selected particles
	 */
	public void goToSelected()
		{
		LineageState linstat=getLinState(currentLin);
		HashSet<LineageSelParticle> selectedParticles=LineageCommonUI.getSelectedParticles();
		if(!selectedParticles.isEmpty())
			{
			Vector2d v=new Vector2d();
			int cnt=0;
			LineageSelParticle sel=selectedParticles.iterator().next();
			if(sel.fst()==currentLin)
				{
				Lineage.Particle nuc=sel.getNuc();
				EvDecimal frame=nuc.getLastFrame();
				v.add(new Vector2d(frame.doubleValue(),linstat.getParticleState(sel.snd()).centerY));
				cnt++;
				}
			if(cnt>0)
				{
				v.scale(1.0/cnt);
				linstat.goToPosition(v.x, v.y);
				}
			}
		else
			BasicWindow.showErrorDialog("No particles selected");
		}
	
	/**
	 * Zoom to fit everything. Redraw.
	 */
	public void zoomAll()
		{
		Lineage lin=getLineage();
		getLinState(lin).zoomAll(lin);
		repaint();
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
		g.drawLine(x, y-keyFrameSize, x, y+keyFrameSize);
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
	
	
	public Lineage getLineage()
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

	
	
	public void foldAll()
		{
		for(String nucName:getRootParticles(getLineage()))
			getLinState(currentLin).recursiveExpand(nucName, false, currentLin);
		repaint();
		}
	public void unfoldAll()
		{
		for(String nucName:getRootParticles(getLineage()))
			getLinState(currentLin).recursiveExpand(nucName, true, currentLin);
		repaint();
		}

	
	
	/**
	 * Get all root particles (those without parents)
	 */
	private static SortedSet<String> getRootParticles(Lineage lin)
		{
		SortedSet<String> list=new TreeSet<String>();
		if(lin!=null)
			for(String nucName:lin.particle.keySet())
				if(lin.particle.get(nucName).parent==null)
					list.add(nucName);
		return list;
		}

	
	
	/**
	 * Draw everything
	 */
	public void paintComponent(Graphics g)
		{
		Lineage lin=getLineage();
		LineageState linstate=getLinState(lin);
		
		//Option: could also put a transform onto g2d
		
		int width=getWidth();
		int height=getHeight();
		
		//Rotate image, part 1
		Graphics2D h;
		BufferedImage bim=null;
		if(showHorizontalTree)
			h=(Graphics2D)g;
		else
			{
			bim=new BufferedImage(getHeight(),getWidth(),BufferedImage.TYPE_3BYTE_BGR);
			h=(Graphics2D)bim.getGraphics();
			width=getHeight();
			height=getWidth();
			}

		//paint everything
		paintToGraphics((Graphics2D)h, true, lin, linstate, width, height);

		//Rotate image part 2
		if(bim!=null)
			{
			AffineTransform af = new AffineTransform(); 
			af.translate(getWidth(), 0);
			af.rotate(Math.PI/2.0);
			((Graphics2D)g).drawImage(bim, af, null);
			}
		
		
		}
	
	/**
	 * Render to graphics area
	 */
	public void paintToGraphics(Graphics2D h, boolean toScreen, Lineage lin, LineageState linstate, int width, int height)
		{
		//Redo list of clickable regions
		regionClickList.clear();
		drawnKeyFrames.clear();
		
		listScaleBars.clear();
		
		//Fill background
		h.setColor(Color.WHITE);
		h.fillRect(0, 0, width, height);

		//Draw frame lines
		if(toScreen)
			drawFrameLines(h,linstate, width, height);

		//Find scales
		for(final ExpRenderSetting expsetting:listExpRenderSettings)
			{
			if(expsetting.scale1==null && lin!=null)
				{
				Tuple<Double,Double> maxMin1=lin.getMaxMinExpLevel(expsetting.expname1);
				if(maxMin1!=null)
					{
					double absmax=Math.max(Math.abs(maxMin1.fst()), Math.abs(maxMin1.snd()));
					expsetting.scale1=sizeOfBranch/absmax;
					}
				else
					expsetting.scale1=1.0;
				}
			}
		
		//Add scalebars
		for(final ExpRenderSetting expsetting:listExpRenderSettings)
			if(expsetting.scale1!=null)
				if(expsetting.type==ExpRenderSetting.typeGraphOnTop || expsetting.type==ExpRenderSetting.typeLineThickness)
					{
					ScaleBar sb=new ScaleBar();
					sb.name=expsetting.expname1;
					sb.scale=expsetting.scale1;
					sb.unit=""; //TODO (no scalebar if no unit?)
					listScaleBars.add(sb);
					}
		
		//Draw all trees
		HierarchicalPainter hpainter=new HierarchicalPainter();
		layoutAllTrees(lin, linstate, hpainter, width, height);
		hpainter.paint(h, width, height, linstate.cam);
		
		//Draw scale bar
		drawScalebars(h,linstate.cam);
		}
	

	
	/**
	 * Prepare rendering of tree
	 */
	private void layoutAllTrees(Lineage lin, LineageState linstate, HierarchicalPainter hpainter, int width, int height)
		{
		double displacement=0;
		for(String nucName:getRootParticles(getLineage()))
			{
			HierarchicalPainter.DrawNode dnode=new HierarchicalPainter.DrawNodeContainer();
			displacement=layoutTreeRecursive(lin, nucName,linstate, displacement,dnode);
			hpainter.topNodes.add(dnode);
			}
		}
	
	/**
	 * Prepare rendering of a tree branch
	 */
	private double layoutTreeRecursive(Lineage lin, final String nucName, final LineageState linstate, 
			double displacement, HierarchicalPainter.DrawNode parentDrawNode)
		{		
		final ParticleState thisInternal=linstate.getParticleState(nucName);
		final Lineage.Particle nuc=lin.particle.get(nucName);

		HierarchicalPainter.DrawNodeContainer thisDrawNode=new HierarchicalPainter.DrawNodeContainer();

		double y1=displacement;
		
		//Total width of children. 0 if none expanded
		double curChildOffset=displacement;
		
		final double fontHeightAvailable;
		
		//Only recurse if children are visible
		if(thisInternal.isExpanded && !nuc.child.isEmpty())
			{
			
			
			
			//Sum up total width for children
			for(String cName:nuc.child)
				{
				double newDisp=layoutTreeRecursive(lin, cName,linstate, curChildOffset, thisDrawNode);
				curChildOffset=newDisp;
				}
			
			
			//Set displacement of this node
			if(nuc.child.size()==1)
				{
				//
				thisInternal.centerY=linstate.particleState.get(nuc.child.first()).centerY+sizeOfBranch/2;
				curChildOffset+=sizeOfBranch/2;
				
				//curChildOffset+sizeOfBranch/2;
				
				
				//curChildOffset+=sizeOfBranch;
				fontHeightAvailable=sizeOfBranch;
				
//				Internal cInternal=linstate.getNucinfo(nuc.child.first());
	//			thisInternal.centerY=cInternal.centerY; //TODO handle 1 child
				}
			else
				{
				//Use the average
				double sum=0;
				double miny=Double.MAX_VALUE;
				double maxy=Double.MIN_VALUE;
				for(String cName:nuc.child)
					{
					ParticleState cInternal=linstate.particleState.get(cName);
					if(cInternal.centerY<miny) miny=cInternal.centerY;
					if(cInternal.centerY>maxy) maxy=cInternal.centerY;
					sum+=cInternal.centerY;
					}
				thisInternal.centerY=sum/nuc.child.size();
				fontHeightAvailable=maxy-miny;
				}
			
			}
		else
			{
			thisInternal.centerY=curChildOffset+sizeOfBranch/2;
			curChildOffset+=sizeOfBranch;
			fontHeightAvailable=sizeOfBranch;
			}
		double y2=curChildOffset;
		
		EvDecimal firstFrame=nuc.getFirstFrame();
		double x1;
		if(firstFrame!=null)
			x1=firstFrame.doubleValue();
		else
			{
			if(nuc.parent==null)
				x1=0;
			else
				{
				x1=linstate.getParticleState(nuc.parent).endX+60; //Better than nothing
				}
			}

		
		EvDecimal lastFrame=nuc.getLastFrame();
		double x2;
		if(lastFrame!=null)
			x2=lastFrame.doubleValue();
		else
			x2=x1+60; //Better than nothing
		
		thisInternal.endX=x2;
		thisInternal.startX=x1;

		
		/////////// need to extend x2 here to children start pos that is furthest away ////////////////
		
		//Attach a renderer of this branch
		final LineageSelParticle nucsel=new LineageSelParticle(lin,nucName);
		HierarchicalPainter.DrawNode newnode=new HierarchicalPainter.DrawNode(x1,y1,x2,y2){
			public void paint(Graphics g, double width, double height, Camera cam)
				{
				int spaceAvailable=cam.toScreenY(fontHeightAvailable)-cam.toScreenY(0);

				int thisMidY=cam.toScreenY(thisInternal.centerY);
				int thisStartX=cam.toScreenX(thisInternal.startX);
				int thisEndX=cam.toScreenX(thisInternal.endX);

				//Line and keyframes
				if(nuc.color!=null)
					g.setColor(nuc.color);
				else
					g.setColor(Color.black);
				g.drawLine(thisStartX, thisMidY, thisEndX, thisMidY);
				if(nuc.getLastFrame()==null)
					drawParticleExtendsToInfinity(g, thisEndX, thisMidY);				
				g.setColor(Color.black);
				if(getShowKeyFrames())
					{
					//Possible to optimize, but not by much
					for(EvDecimal frame:nuc.pos.keySet())
						drawKeyFrame(g, cam.toScreenX(frame.doubleValue()), thisMidY, nucsel.snd(), frame);
					}

				if(getShowEvents())
					{
					g.setColor(new Color(0,128,0));
					for(EvDecimal frame:nuc.events.keySet())
						{
						int posX=cam.toScreenX(frame.doubleValue());
						g.drawLine(posX, thisMidY, posX, thisMidY+10);
						g.drawString(nuc.events.get(frame), posX, thisMidY+20);
						}
					}
				
				
				//Lines to children
				if(thisInternal.isExpanded && !nuc.child.isEmpty())
					{
					for(String cname:nuc.child)
						{
						ParticleState cstate=linstate.getParticleState(cname);
						int cStartX=cam.toScreenX(cstate.startX);
						int cStartY=cam.toScreenY(cstate.centerY);
						g.drawLine(thisEndX, thisMidY, cStartX, cStartY);
						}
					}
				if(!nuc.child.isEmpty())
					{
					drawExpanderSymbol(g, nucName, thisEndX, thisMidY, thisInternal.isExpanded, spaceAvailable);
					}
				
				
				//Label
				if(getShowLabel())
					{
					boolean canDrawLabel=true;
					if(spaceAvailable<16)
						canDrawLabel=false;
					if(canDrawLabel)
						drawNucName(g, "", nucsel, thisMidY, thisEndX);
					else
						drawNucNameUnexpanded(g, "", nucsel, thisMidY, thisEndX);
					}

				}
		};
		thisDrawNode.addSubNode(newnode);

		
		for(final ExpRenderSetting expsetting:listExpRenderSettings)
			{
			final LineageExp nucexp=nuc.exp.get(expsetting.expname1);
			//System.out.println("get exp "+expsetting.expname1+" "+nucexp);
			
			//System.out.println("have "+expsetting.expname1);
			
			if(nucexp!=null && !nucexp.level.isEmpty())
				{

				if(expsetting.type==ExpRenderSetting.typeGraphOnTop)
					{
					//TODO bounds
					HierarchicalPainter.DrawNode drawExpNode=new HierarchicalPainter.DrawNode(x1,y1,x2,y2)
						{
						public void paint(Graphics g, double width, double height, Camera cam)
							{
							int thisMidY=cam.toScreenY(thisInternal.centerY);

							double scale=cam.scaleWorldDistY(expsetting.scale1);
							g.setColor(expsetting.color.getAWTColor());
							boolean hasLastCoord=false;
							int lastX=0, lastYAbove=0;
							for(Map.Entry<EvDecimal, Double> ve:nucexp.level.entrySet())
								{
								int yAbove=(int)(-ve.getValue()*scale+thisMidY);
								//int yBelow=(int)(+ve.getValue()*scale+thisMidY);
								int x=linstate.cam.toScreenX(ve.getKey().doubleValue());
								if(hasLastCoord)
									{
									if(showExpLine)
										g.drawLine(lastX, lastYAbove, x, yAbove);
									if(showExpSolid)
										g.fillPolygon(new int[]{lastX,lastX,x,x}, new int[]{thisMidY,lastYAbove,yAbove,thisMidY}, 4);
									}
								if(showExpDot)
									g.drawRect(x-expDotSize, yAbove-expDotSize, 2*expDotSize, 2*expDotSize);
								hasLastCoord=true;
								lastX=x;
								lastYAbove=yAbove;
								}


							}
						};
					thisDrawNode.addSubNode(drawExpNode);
					}
				else if(expsetting.type==ExpRenderSetting.typeLineThickness)
					{
					
					//TODO bounds
					HierarchicalPainter.DrawNode drawExpNode=new HierarchicalPainter.DrawNode(x1,y1,x2,y2)
						{
						public void paint(Graphics g, double width, double height, Camera cam)
							{
							int thisMidY=cam.toScreenY(thisInternal.centerY);

							double scale=cam.scaleWorldDistY(expsetting.scale1);
							g.setColor(Color.BLACK);
							boolean hasLastCoord=false;
							int lastX=0, lastYAbove=0, lastYBelow=0;
							for(Map.Entry<EvDecimal, Double> ve:nucexp.level.entrySet())
								{
								int dy=(int)Math.round(ve.getValue()*scale);
								int yAbove=-dy+thisMidY;
								int yBelow=+dy+thisMidY;
								int x=linstate.cam.toScreenX(ve.getKey().doubleValue());
								if(hasLastCoord)
									g.fillPolygon(new int[]{lastX,lastX,x,x}, new int[]{lastYBelow,lastYAbove,yAbove,yBelow}, 4);
								hasLastCoord=true;
								lastX=x;
								lastYAbove=yAbove;
								lastYBelow=yBelow;
								}


							}
						};
					thisDrawNode.addSubNode(drawExpNode);
					}
				else if(expsetting.type==ExpRenderSetting.typeColorIntensity)
					{
					HierarchicalPainter.DrawNode drawExpNode=new HierarchicalPainter.DrawNode(x1,thisInternal.centerY-2,x2,thisInternal.centerY+2)
						{
						public void paint(Graphics g, double width, double height, Camera cam)
							{
							int thisMidY=cam.toScreenY(thisInternal.centerY);
							//int thisStartX=cam.toScreenX(thisInternal.startX);
							//int thisEndX=cam.toScreenX(thisInternal.endX);
							
							float scale=(float)(double)expsetting.scale1;
							float colR=(float)expsetting.color.getRedDouble();
							float colG=(float)expsetting.color.getGreenDouble();
							float colB=(float)expsetting.color.getBlueDouble();
							
							boolean hasLastCoord=false;
							int lastX=0;
							for(Map.Entry<EvDecimal, Double> ve:nucexp.level.entrySet())
								{
								int x=linstate.cam.toScreenX(ve.getKey().doubleValue());
								double level=ve.getValue()*scale;
								Color nextCol=new Color(clamp01((float)(colR*level)),clamp01((float)(colG*level)),clamp01((float)(colB*level)));
								g.setColor(nextCol);
								if(hasLastCoord)
									g.drawLine(lastX, thisMidY, x, thisMidY);
								hasLastCoord=true;
								lastX=x;
								}
							
							
							}
						};
					thisDrawNode.addSubNode(drawExpNode);
					}
				else if(expsetting.type==ExpRenderSetting.typeTimeDev)
					{
					//bounds TODO
					HierarchicalPainter.DrawNode drawExpNode=new HierarchicalPainter.DrawNode(x1,thisInternal.centerY-5,x2,thisInternal.centerY+5)
						{
						public void paint(Graphics g, double width, double height, Camera cam)
							{
							int thisMidY=cam.toScreenY(thisInternal.centerY);
							//int thisStartX=cam.toScreenX(thisInternal.startX);
							int thisEndX=cam.toScreenX(thisInternal.endX);
							
							int y1=thisMidY+expanderSize+2;
							int y2=y1-1;

							double level=cam.scaleWorldDistX(nucexp.level.get(nucexp.level.firstKey()));
							
							int x1=thisEndX-(int)level;
							int x2=thisEndX+(int)level;

							g.setColor(expsetting.color.getAWTColor());
							g.drawLine(x1, y1, x2, y1);
							g.drawLine(x1, y1, x1, y2);
							g.drawLine(x2, y1, x2, y2);
							}
						};
					thisDrawNode.addSubNode(drawExpNode);
					}
				}

			}
		
		
		parentDrawNode.addSubNode(thisDrawNode);
		return y2;
		}
	
	public static float clamp01(float x)
		{
		if(x<0)
			return 0;
		else if(x>1)
			return 1f;
		else
			return x;
		}
	
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Click positions /////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	

	
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
			{
			EvSelection.unselectAll();
			BasicWindow.updateWindows();
			}
		}

	public ClickRegion getClickRegion(MouseEvent e)
		{
		int mousex,mousey;
		//NucLineage lin=getLineage();
		if(/*getLinState(lin).*/showHorizontalTree)
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
				//long startTime=System.currentTimeMillis();
				if(SwingUtilities.isLeftMouseButton(e))
					LineageCommonUI.mouseSelectParticle(new LineageSelParticle(currentLin, nucname), (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
				//System.out.println("time "+(System.currentTimeMillis()-startTime));
				}
			}
		public String getHoverString()
			{
			if(currentLin!=null)
				{
				Lineage.Particle nuc=currentLin.particle.get(nucname);
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
			Lineage lin=getLineage();
			LineageState linstate=getLinState(lin);
			ParticleState internal=linstate.getParticleState(nucname);
			if(SwingUtilities.isLeftMouseButton(e))
				internal.isExpanded=!internal.isExpanded;
			else if(SwingUtilities.isRightMouseButton(e))
				linstate.recursiveExpand(nucname, !internal.isExpanded, lin);
			repaint();
			}
		public String getHoverString()
			{
			return null;
			}
		}


	

	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Scale bar rendering /////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	

	public static class ScaleBar
		{
		public String name;
		public String unit;
		public double scale;
		}
	
	private LinkedList<ScaleBar> listScaleBars=new LinkedList<ScaleBar>(); 
	
	/**
	 * Draw all scalebars
	 */
	public void drawScalebars(Graphics g, HierarchicalPainter.Camera cam)
		{
		int x=20;
		int xdelta=50;
		if(showScale)
			for(ScaleBar sb:listScaleBars)
				{
				int yshift=10;
				int sh=2*getHeight()/3;
				
				double scale=cam.scaleWorldDistY(sb.scale);
				
				double upper=Math.pow(10, (int)Math.log10(sh/scale));
				int toti=10;
				
				if(upper*scale*5<sh)
					{
					upper*=5;
					toti=5;
					}
				
				g.setColor(Color.BLACK);
				int y1=yshift;
				int y2=yshift+(int)(upper*scale);
				g.drawLine(x, y1, x,y2);
				g.drawString(""+upper+" "+sb.unit, x+10, y2);
				
				int x1=x-2, x2=x+2;
				for(int i=0;i<toti+1;i++)
					{
					int y3=(int)(yshift+upper*scale*i/toti);
					g.drawLine(x1, y3, x2, y3);
					}
				
				x+=xdelta;
				}			
		}
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Helpers for rendering ///////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * Draw the lines for frames, in the background
	 */
	public void drawFrameLines(Graphics2D g, LineageState linstate, int width, int height)
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
			//System.out.println("pow "+pow);
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
	 * Draw arrow pointing out that the particle continue existing
	 */
	private void drawParticleExtendsToInfinity(Graphics g, int endc, int midr)
		{
		int size=10;
		g.setColor(Color.BLUE);
		g.drawLine(endc, midr-size, endc, midr+size);
		}
	
	
	
	
	/**
	 * Draw text name
	 */
	private void drawNucName(Graphics g, String prefix, LineageSelParticle nucPair, int midy, int x)
		{
		String nucName=nucPair.snd();
		int fontHeight=g.getFontMetrics().getHeight();
		int fontWidth=g.getFontMetrics().stringWidth(prefix+nucName);
		int textc=x+5;
		Graphics2D g2=(Graphics2D)g;
		if(EvSelection.isSelected(nucPair))
			g2.setColor(Color.RED);
		else
			g2.setColor(Color.BLUE);
		
		//Graphics
		int textr=midy+fontHeight/4+2;
		g2.translate(textc, textr);
		g2.drawString(nucName, 0, 0);
		g2.translate(-textc, -textr);
		//Make it clickable
		regionClickList.add(new ClickRegionName(nucName, textc, textr-3*fontHeight/4, fontWidth,fontHeight));
		}

	/**
	 * Draw text name replacement, when selected
	 */
	private void drawNucNameUnexpanded(Graphics g, String prefix, LineageSelParticle nucPair, int midy, int x)
		{
		if(EvSelection.isSelected(nucPair))
			{
			int fontWidth=20;//g.getFontMetrics().stringWidth(prefix+nucName);
			int textc=x+5;
			Graphics2D g2=(Graphics2D)g;
			
			g2.setColor(Color.RED);
			//Graphics
			int textr=midy;
			g2.translate(textc, textr);
			g2.drawLine(0, 0, fontWidth, 0);
			g2.translate(-textc, -textr);
			}
		}

	/**
	 * Draw the [+] and [-] symbol
	 * @param name Name
	 * @param x Mid x coordinate
	 * @param y Mid y coordinate
	 * @param expanded If a + or - should be shown
	 */
	private void drawExpanderSymbol(Graphics g, String name, int x, int y, boolean expanded, int spaceAvailable)
		{
		if(spaceAvailable>expanderSize*2+1)
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
			regionClickList.add(new ClickRegionExpander(name, x-expanderSize, y-expanderSize, 2*expanderSize,2*expanderSize));
			}
		}
	
	


	}
