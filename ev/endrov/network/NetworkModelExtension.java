/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.network;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.EvColor;
import endrov.basicWindow.EvColor.ColorMenuListener;
import endrov.data.EvObject;
import endrov.modelWindow.*;
import endrov.util.*;


/**
 * Extension to Model Window: shows networks
 * @author Johan Henriksson
 */
public class NetworkModelExtension implements ModelWindowExtension
	{
  private static int NUC_SHOW_DIV=12;

  public void newModelWindow(ModelWindow w)
		{
		w.modelWindowHooks.add(new NetworkModelWindowHook(w));
		}
	
	static class NetworkModelWindowHook implements ModelWindowHook, ActionListener, ModelView.GLSelectListener
		{
		final ModelWindow w;
		public void fillModelWindowMenus()
			{
			}
		
		private float traceWidth=3;
		private EvColor traceColor=EvColor.red;
		
		public JMenuItem miSetTraceWidth=new JMenuItem("Set trace width");
		public JCheckBoxMenuItem miRenderPoints=new JCheckBoxMenuItem("Render points");
		

		//For modifying nuclei
		//private NucLineage.Nuc currentOrigNuc=null;
		//private NucSel currentModifying=null;
		//private boolean hasReallyModified=false;
		/*private ModState modifyingState=null;
		
		private enum ModState
			{
			Dragging, Resizing
			}*/
		
		/**
		 * Currently modified nucleus is finalized. Commit changes.
		 * 
		 * Only for dragging at the moment
		 */
		public void commitModifyingNuc()
			{
			/*
			//Only commit if something has changed
			if(hasReallyModified)
				{
				hasReallyModified=false;
				final NucLineage lin=currentModifying.fst();
				final String name=currentModifying.snd();
				final NucLineage.Nuc currentNuc=currentModifying.getNuc().clone();
				final NucLineage.Nuc lastNuc=currentOrigNuc; 
		
				new UndoOpBasic("Modify keyframe for "+currentModifying.snd())
					{
					public void redo()
						{
						lin.nuc.put(name, currentNuc);
						BasicWindow.updateWindows();
						}
		
					public void undo()
						{
						lin.nuc.put(name, lastNuc);
						BasicWindow.updateWindows();
						}
					}.execute();
				}

			hasReallyModified=false;
			currentModifying=null;
			currentOrigNuc=null;
			modifyingState=null;
			*/
			}
		
		private void setTraceColor(EvColor c)
			{
			traceColor=c;
			}
		
		public NetworkModelWindowHook(final ModelWindow w)
			{
			this.w=w;
			
			JMenu menu=new JMenu("Network");

			
			JMenu mTraceColor=new JMenu("Set trace color");
			//JMenuItem miColorSame=new JMenuItem("Same color as nucleus");
			//mTraceColor.add(miColorSame);
			//miColorSame.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){setTraceColor(null);}});
			EvColor.addColorMenuEntries(mTraceColor, new ColorMenuListener(){
				public void setColor(EvColor c){setTraceColor(c);}
			});
			
			menu.add(mTraceColor);
			menu.add(miSetTraceWidth);
			menu.add(miRenderPoints);
			
			menu.addSeparator();

			
			w.menuModel.add(menu);

			
			miSetTraceWidth.addActionListener(this);
			miRenderPoints.addActionListener(this);

			}
		
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		
		
		public void datachangedEvent()
			{
			}
		
		public void actionPerformed(ActionEvent e)
			{
			
			if(e.getSource()==miSetTraceWidth)
				{
				String inp=BasicWindow.showInputDialog("Set trace width", ""+traceWidth);
				if(inp!=null)
					traceWidth=(float)Double.parseDouble(inp);
				}
		
			
			w.view.repaint(); //TODO modw repaint
			}
		
		
		public boolean canRender(EvObject ob)
			{
			return ob instanceof Network;
			}

		
		public Collection<Network> getObjects()
			{
			List<Network> v=new LinkedList<Network>();
			for(Network lin:w.getSelectedData().getObjects(Network.class))
				if(w.showObject(lin))
					v.add(lin);
			return v;
			}
		
		public void initOpenGL(GL gl)
			{
			initDrawSphere(gl.getGL2());
			}

		
		/**
		 * Prepare for rendering
		 */
		public void displayInit(GL gl)
			{
			}
		
		/**
		 * Render for selection
		 */
		public void displaySelect(GL gl)
			{
			}
		
		/**
		 * Render movement trace of nuc
		 */
		/*
		private void renderTrace(GL2 gl, NucLineage.Nuc nuc, boolean simple, Color col)
			{
			if(!nuc.pos.isEmpty())
				{
				gl.glLineWidth(traceWidth);
				
				Color thisTraceColor=getTraceColor(nuc);
				float colR=(float)thisTraceColor.getRed()/255.0f;
				float colG=(float)thisTraceColor.getGreen()/255.0f;
				float colB=(float)thisTraceColor.getBlue()/255.0f;
				gl.glColor3d(colR,colG,colB);
//				gl.glColor3d(1, 1, 1);
				if(simple)
					{
					gl.glBegin(GL2.GL_LINE_STRIP);
					EvDecimal f1=nuc.pos.firstKey();
					EvDecimal f2=nuc.pos.lastKey();
					NucLineage.NucPos pos1=nuc.pos.get(f1);
					NucLineage.NucPos pos2=nuc.pos.get(f2);
					Vector3d v=new Vector3d(pos2.x-pos1.x,pos2.y-pos1.y,pos2.z-pos1.z);
					double len=v.length();
					v.scale((len-w.view.getArrowLength())/len);
					//v.normalize();
					gl.glVertex3d(pos1.x,pos1.y,pos1.z);
//					gl.glVertex3d(pos2.x,pos2.y,pos2.z);
					gl.glVertex3d(pos1.x+v.x,pos1.y+v.y,pos1.z+v.z);
					gl.glEnd();
					
					Vector3d direction=pos2.getPosCopy();
					direction.sub(pos1.getPosCopy());
		      gl.glEnable(GL2.GL_LIGHTING);
					w.view.renderArrowHead(gl, pos2.getPosCopy(), direction, colR, colG, colB);
		      gl.glDisable(GL2.GL_LIGHTING);
					}
				else
					{
					gl.glBegin(GL2.GL_LINE_STRIP);
					//Vector3d last=null, secondLast=null;
					for(NucLineage.NucPos pos:nuc.pos.values())
						{
						gl.glVertex3d(pos.x,pos.y,pos.z);
						}
					gl.glEnd();
					}
				}
			}
			*/
		
		
		public Color getTraceColor()
			{
			return traceColor.getAWTColor();
			}
		
		
		/**
		 * Render graphics
		 */
		public void displayFinal(GL glin,List<TransparentRender> transparentRenderers)
			{
			GL2 gl=glin.getGL2();

			//initDrawSphere(gl);
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
			
			EvDecimal curFrame=w.getFrame();

			
			for(Network network:getObjects())
				{
				if(network.frame.containsKey(curFrame))
					{
					renderFrame(gl, network.frame.get(curFrame), curFrame);
					}
				
				}
			
			gl.glPopAttrib();
			}

		
		
		/**
		 * Render body of one nucleus
		 */
		private void renderFrame(GL2 gl, Network.NetworkFrame nf, EvDecimal curFrame)
			{
			gl.glEnable(GL2.GL_CULL_FACE);

			
			//Render all points
			if(miSetTraceWidth.isSelected())
				{
				for(Network.Point p:nf.points.values())
					{
					//Save world coordinate
			    gl.glPushMatrix();
			    
					//Move to cell center = local coordinate
			    gl.glTranslated(p.x,p.y,p.z);

			    //If there is a radius given, use it
			    double showRadius=getRadius(p);

			    float[] nucColor=new float[]{1,1,1};
					
			    drawVisibleSphere(gl, showRadius, false ,nucColor[0], nucColor[1], nucColor[2]);
			    	
			    //Go back to world coordinates
			    gl.glPopMatrix();
					}
				}

			////// Render all lines
			gl.glLineWidth(traceWidth);
			
			Color thisTraceColor=getTraceColor();
			float colR=(float)thisTraceColor.getRed()/255.0f;
			float colG=(float)thisTraceColor.getGreen()/255.0f;
			float colB=(float)thisTraceColor.getBlue()/255.0f;
			gl.glColor3d(colR,colG,colB);
			
			for(Network.Segment s:nf.segments)
				{
				gl.glBegin(GL2.GL_LINE_STRIP);
				for(int pi:s.points)
					{
					Network.Point p=nf.points.get(pi);
					gl.glVertex3d(p.x,p.y,p.z);
					}
				gl.glEnd();
				}
			
			}


		

		private void invokeDrawListVisible(GL2 gl)
			{
//  	gl.glCallList(displayListVisibleSphere);
			GLU glu=new GLU();
			GLUquadric q=glu.gluNewQuadric();
			glu.gluSphere(q,1.0,NUC_SHOW_DIV,NUC_SHOW_DIV);
			glu.gluDeleteQuadric(q);
			}

		/**
		 * Generate vertex lists for spheres
		 */
		private void initDrawSphere(GL2 gl)
			{
			/*
			GLU glu=new GLU();
			GLUquadric q=glu.gluNewQuadric();

			displayListVisibleSphere = gl.glGenLists(1);
			gl.glNewList(displayListVisibleSphere, GL2.GL_COMPILE);
			glu.gluSphere(q,1.0,NUC_SHOW_DIV,NUC_SHOW_DIV);
			//drawSphereSolid(gl, 1.0, NUC_SHOW_DIV,NUC_SHOW_DIV);
			gl.glEndList();

			displayListSelectSphere = gl.glGenLists(1);
			gl.glNewList(displayListSelectSphere, GL2.GL_COMPILE);
			glu.gluSphere(q,1.0,NUC_SELECT_DIV,NUC_SELECT_DIV);
			gl.glEndList();

			glu.gluQuadricDrawStyle(q, GLU.GLU_LINE);

			displayListHiddenSphere = gl.glGenLists(1);
			gl.glNewList(displayListHiddenSphere, GL2.GL_COMPILE);
			glu.gluSphere(q,1.0,NUC_HIDE_DIV,NUC_HIDE_DIV);
			gl.glEndList();

			glu.gluDeleteQuadric(q);
			*/
			}

/*		private int displayListVisibleSphere;
		private int displayListHiddenSphere;
		private int displayListSelectSphere;*/
		
		
		private void drawVisibleSphere(GL2 gl, double r, boolean selected, float colR, float colG, float colB)
			{
    	//double ir=1.0/r;
    	gl.glPushMatrix();
			gl.glScaled(r,r,r);
			
			if(selected)
				{
	    	gl.glColor3d(1,0,1);
				gl.glLineWidth(5);
				gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINE);
				gl.glCullFace(GL2.GL_FRONT);
				gl.glDepthFunc(GL2.GL_LEQUAL);
				invokeDrawListVisible(gl);
	    	
				gl.glCullFace(GL2.GL_BACK);
				gl.glDepthFunc(GL2.GL_LESS);
				gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
				gl.glLineWidth(1);
				}
			
      gl.glEnable(GL2.GL_LIGHTING);
    	gl.glColor3d(1,1,1);
    	gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, new float[]{colR,colG,colB}, 0);
			invokeDrawListVisible(gl);
    		
    	
      gl.glDisable(GL2.GL_LIGHTING);

    	//gl.glScaled(ir,ir,ir);
    	gl.glPopMatrix();
			}
		
		/** Keep track of what hover was before hover test started */
		//private NucSel lastHover=null;
		/** Called when hover test starts */
		public void hoverInit(int pixelid)
			{
			//Update hover
			//lastHover=NucCommonUI.currentHover;
			//NucCommonUI.currentHover=NucCommonUI.emptyHover;
			}
		/** Called when hovered */
		public void hover(int pixelid)
			{
			/*
			NucCommonUI.currentHover=selectColorMap.get(pixelid);
			//System.out.println("New hover: "+NucCommonUI.currentHover);
			//System.out.println("Last hover: "+lastHover);
			//Propagate hover. Avoid infinite recursion.
			if(!NucCommonUI.currentHover.equals(lastHover))
				{
				System.out.println("nuc rerend");
				BasicWindow.updateWindows(w);
				}
				*/
			}

		/**
		 * Adjust the scale
		 */
		public Collection<Double> adjustScale()
			{
			int count=0;
	
			double maxx=-1000000,maxy=-1000000,maxz=-1000000;
			double minx= 1000000,miny= 1000000,minz= 1000000;
	
			for(Network network:getObjects())
				{
				for(Network.NetworkFrame nf:network.frame.values())
					{
					for(Network.Point p:nf.points.values())
						{
						if(maxx<p.x) maxx=p.x;
						if(maxy<p.y) maxy=p.y;
						if(maxz<p.z) maxz=p.z;
						if(minx>p.x) minx=p.x;
						if(miny>p.y) miny=p.y;
						if(minz>p.z) minz=p.z;
						count++;
						}
					}
				}
			if(count<2)
				return Collections.emptySet();
	
			double dx=maxx-minx;
			double dy=maxy-miny;
			double dz=maxz-minz;
			double dist=dx;
			if(dist<dy) dist=dy;
			if(dist<dz) dist=dz;
			return Collections.singleton((Double)dist);

			}

		
		/**
		 * Give suitable center of all objects
		 */
		public Collection<Vector3d> autoCenterMid()
			{
			//Calculate center
			double meanx=0, meany=0, meanz=0;
			int num=0;
			for(Network network:getObjects())
				{
				for(Network.NetworkFrame nf:network.frame.values())
					{
					for(Network.Point p:nf.points.values())
						{
						meanx+=p.x;
						meany+=p.y;
						meanz+=p.z;
						num++;
						}
					}
				}
			
			if(num==0)
				return Collections.emptySet();
			else
				{
				meanx/=num;
				meany/=num;
				meanz/=num;
				return Collections.singleton(new Vector3d(meanx,meany,meanz));
				}
			}
		
		private double getRadius(Network.Point p)
			{
	    if(p.r!=null)
	    	return p.r;
	    else
	    	return 3;
			}
		
		/**
		 * Given a middle position, figure out radius required to fit objects
		 */
		public double autoCenterRadius(Vector3d mid)
			{
			//Calculate maximum radius
			double maxr=0;
			for(Network network:getObjects())
				{
				for(Network.NetworkFrame nf:network.frame.values())
					{
					for(Network.Point p:nf.points.values())
						{
						double dx=p.x-mid.x;
						double dy=p.y-mid.y;
						double dz=p.z-mid.z;
						double r=Math.sqrt(dx*dx+dy*dy+dz*dz)+getRadius(p);
						if(maxr<r)
							maxr=r;
						}
					}
				}
			//Find how far away the camera has to be
			return maxr;
			}
		
		
		public EvDecimal getFirstFrame()
			{
			EvDecimal f=null;
			for(Network network:getObjects())
				if(!network.frame.isEmpty())
					if(f==null || f.greater(network.frame.firstKey()))
						f=network.frame.firstKey();
			return f;
			}
		public EvDecimal getLastFrame()
			{
			EvDecimal f=null;
			for(Network network:getObjects())
				if(!network.frame.isEmpty())
					if(f==null || f.less(network.frame.lastKey()))
						f=network.frame.lastKey();
			return f;
			}
		
		};
		
		
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new NetworkModelExtension());
		}

	}


