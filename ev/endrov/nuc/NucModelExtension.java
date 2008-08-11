package endrov.nuc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvObject;
import endrov.ev.*;
import endrov.modelWindow.*;
import endrov.nuc.NucLineage.NucInterp;


/**
 * Extension to Model Window: shows nuclei
 * @author Johan Henriksson
 */
public class NucModelExtension implements ModelWindowExtension
	{
  private static int NUC_SHOW_DIV=15;
  private static int NUC_SELECT_DIV=6;
  private static int NUC_HIDE_DIV=6;

  public void newModelWindow(ModelWindow w)
		{
		w.modelWindowHooks.add(new NucModelWindowHook(w));
		}
	
	public static class NucModelWindowHook implements ModelWindowHook, ActionListener, ModelView.GLSelectListener
		{
		private final HashMap<Integer,NucPair> selectColorMap=new HashMap<Integer,NucPair>();
		private Vector<Map<NucPair, NucLineage.NucInterp>> interpNuc=new Vector<Map<NucPair, NucLineage.NucInterp>>();
		private final ModelWindow w;
		
		public void fillModelWindomMenus(){}
		

		
		public JCheckBoxMenuItem miShowAllNucNames=new JCheckBoxMenuItem("Names: Show all");
		public JCheckBoxMenuItem miShowSelectedNucNames=new JCheckBoxMenuItem("Names: Show for selected");
		
		public JMenuItem miShowSelectedNuc=new JMenuItem("Nuclei: Unhide selected"); 
		public JMenuItem miHideSelectedNuc=new JMenuItem("Nuclei: Hide selected"); 

		public JCheckBoxMenuItem miShowTraceSel=new JCheckBoxMenuItem("Traces: Show for selected"); 
		public JCheckBoxMenuItem miShowTraceCur=new JCheckBoxMenuItem("Traces: Show for current"); 
		
		public JCheckBoxMenuItem miShowDiv=new JCheckBoxMenuItem("Show division lines", true); 

		public JCheckBoxMenuItem miShowDelaunay=new JCheckBoxMenuItem("Show delaunay neighbours", false); 

		public NucModelWindowHook(ModelWindow w)
			{
			this.w=w;
			
			JMenu miNuc=new JMenu("Nuclei/Lineage");
			
			miNuc.add(NucLineage.makeSetColorMenu());
			miNuc.add(miShowAllNucNames);
			miNuc.add(miShowSelectedNucNames);
			miNuc.add(miShowSelectedNuc);
			miNuc.add(miHideSelectedNuc);
			miNuc.add(miShowTraceSel);
			miNuc.add(miShowTraceCur);
			miNuc.add(miShowDiv);
			miNuc.add(miShowDelaunay);
			w.menuModel.add(miNuc);
			
	//		miSaveColorScheme.addActionListener(this);
		//	miLoadColorScheme.addActionListener(this);
			miShowAllNucNames.addActionListener(this);
			miShowSelectedNuc.addActionListener(this);
			miHideSelectedNuc.addActionListener(this);
			miShowTraceSel.addActionListener(this);
			miShowTraceCur.addActionListener(this);
			miShowDiv.addActionListener(this);
			miShowDelaunay.addActionListener(this);
			
			w.addModelWindowMouseListener(new ModelWindowMouseListener(){
				public void mouseClicked(MouseEvent e)
					{
					//Clicking a nucleus selects it
					if(SwingUtilities.isLeftMouseButton(e))
						NucLineage.mouseSelectNuc(NucLineage.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
					}
				public boolean mouseDragged(MouseEvent e, int dx, int dy){return false;}
				public void mouseEntered(MouseEvent e){}
				public void mouseExited(MouseEvent e){}
				public void mouseMoved(MouseEvent e){}
				public void mousePressed(MouseEvent e){}
				public void mouseReleased(MouseEvent e){}
			});
			}
		
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void datachangedEvent(){}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==miShowSelectedNuc)
				{
				for(endrov.nuc.NucPair p:NucLineage.selectedNuclei)
					NucLineage.hiddenNuclei.remove(p);
				}
			else if(e.getSource()==miHideSelectedNuc)
				{
				for(endrov.nuc.NucPair p:NucLineage.selectedNuclei)
					NucLineage.hiddenNuclei.add(p);
				}
//			else if(e.getSource()==miSaveColorScheme)
				
			w.view.repaint(); //TODO modw repaint
			}
		
		public boolean canRender(EvObject ob)
			{
			return ob instanceof NucLineage;
			}

		
		public Collection<NucLineage> getLineages()
			{
			Vector<NucLineage> v=new Vector<NucLineage>();
			for(NucLineage lin:NucLineage.getLineages(w.getSelectedData()))
				if(w.showObject(lin))
					v.add(lin);
			return v;
			}
		
		/**
		 * Prepare for rendering
		 */
		public void displayInit(GL gl)
			{
			selectColorMap.clear();

			interpNuc.clear();
			for(NucLineage lin:getLineages())
				interpNuc.add(lin.getInterpNuc(w.frameControl.getFrame()));
			}
		
		/**
		 * Render for selection
		 */
		public void displaySelect(GL gl)
			{
			if(EV.debugMode)
				System.out.println("#nuc to render: "+interpNuc.size());
			for(Map<NucPair, NucLineage.NucInterp> inter:interpNuc)
				for(NucPair nucPair:inter.keySet())
					{
					int rawcol=w.view.reserveSelectColor(this);
					selectColorMap.put(rawcol, nucPair);
					w.view.setReserveColor(gl, rawcol);
					renderNucSel(gl,nucPair, inter.get(nucPair));
					}
			}
		
		/**
		 * Render movement trace of nuc
		 */
		private void renderTrace(GL gl, NucLineage.Nuc nuc)
			{
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glColor3d(1, 1, 1);
			for(NucLineage.NucPos pos:nuc.pos.values())
				gl.glVertex3d(pos.x,pos.y,pos.z);
			gl.glEnd();
			}
		
		/**
		 * Render graphics
		 */
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			initDrawSphere(gl);
			gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
			
			boolean traceCur=miShowTraceCur.isSelected();
			boolean traceSel=miShowTraceSel.isSelected();
			
			
			for(Map<NucPair, NucLineage.NucInterp> inter:interpNuc)
				{
				//Draw neighbours. Need be calculated in the background and cached
				if(miShowDelaunay.isSelected())
					{
					try
						{
						///////////////////////////// TODO TODO TODO  BAD
						double r=3000; //300 is about the embryo. embryo is not centered in reality.
						
						Map<NucPair, NucLineage.NucInterp> interX=new HashMap<NucPair, NucInterp>(inter);
						
						NucLineage.NucInterp i1=new NucLineage.NucInterp();
						i1.pos=new NucLineage.NucPos();
						i1.frameBefore=0;
						i1.pos.x=r;
						NucLineage.NucInterp i2=new NucLineage.NucInterp();
						i2.pos=new NucLineage.NucPos();
						i2.frameBefore=0;
						i2.pos.x=-r;
						NucLineage.NucInterp i3=new NucLineage.NucInterp();
						i3.pos=new NucLineage.NucPos();
						i3.frameBefore=0;
						i3.pos.y=-r;
						NucLineage.NucInterp i4=new NucLineage.NucInterp();
						i4.pos=new NucLineage.NucPos();
						i4.frameBefore=0;
						i4.pos.y=-r;

						interX.put(new NucPair(null,":::1"), i1);
						interX.put(new NucPair(null,":::2"), i2);
						interX.put(new NucPair(null,":::3"), i3);
						interX.put(new NucPair(null,":::4"), i4);
						///////////////////////////// TODO TODO TODO  BAD

						
						NucVoronoi nvor=new NucVoronoi(interX,false);
						
						/*
						for(int[] facelist:nvor.vor.vface)
	//					int size=nvor.nucnames.size();
		//				for(int i=0;i<size;i++)
							{
							boolean isfinite=true;
							for(int i:facelist)
								if(i==-1)
									isfinite=false;
							if(isfinite)
								{
								gl.glBegin(GL.GL_LINE_LOOP);
								gl.glColor3d(1, 0, 0);
								for(int i:facelist)
									{
									Vector3d v=nvor.vor.vvert.get(i);
									System.out.println(""+i+" "+v);
									gl.glVertex3d(v.x, v.y, v.z);
									}
								gl.glEnd();
								}
							}
						*/
						
						//Lines between neighbours
						//TODO The ::: is really ugly
						if(miShowDelaunay.isSelected())
							{
							gl.glBegin(GL.GL_LINES);
							gl.glColor3d(1, 0, 0);
							int size=nvor.nucnames.size();
							for(int i=0;i<size;i++)
								if(!nvor.nucnames.get(i).startsWith(":::"))
									{
									Vector3d from=nvor.nmid.get(i);
									for(int j:nvor.vneigh.dneigh.get(i))
										if(j>i && j!=-1)
											if(!nvor.nucnames.get(j).startsWith(":::"))
												{
												Vector3d to=nvor.nmid.get(j);
												gl.glVertex3d(from.x, from.y, from.z);
												gl.glVertex3d(to.x, to.y, to.z);
												}
									}
							gl.glEnd();
							}
						
						}
					catch (Exception e)
						{
						e.printStackTrace();
						}
					}
				
				
				
				for(NucPair nucPair:inter.keySet())
					{
					//Render nuc body
					renderNuc(gl, nucPair, inter.get(nucPair));
					
					if(traceCur && !traceSel)
						{
						NucLineage.Nuc nuc=nucPair.fst().nuc.get(nucPair.snd());
						renderTrace(gl,nuc);
						}
					
					//Draw connecting line
					if(nucPair.snd().equals(NucLineage.connectNuc[0]))
						for(NucPair nucPair2:inter.keySet())
							if(nucPair2.snd().equals(NucLineage.connectNuc[1]))
								{
								NucInterp n=inter.get(nucPair);
								NucInterp m=inter.get(nucPair2);
								gl.glBegin(GL.GL_LINES);
								gl.glColor3d(1, 1, 1);
								gl.glVertex3d(n.pos.x,n.pos.y,n.pos.z);
								gl.glVertex3d(m.pos.x,m.pos.y,m.pos.z);
								gl.glEnd();
								}
					}
			
				//Render nuclei text
				for(NucPair nucPair:inter.keySet())
					renderNucLabel(gl,transparentRenderers,nucPair, inter.get(nucPair));
				}
			
			if(traceSel)
				for(NucPair pair:NucLineage.selectedNuclei)
					{
					NucLineage.Nuc nuc=pair.fst().nuc.get(pair.snd());
					renderTrace(gl,nuc);
					}
			
			//Cell divisions
			if(miShowDiv.isSelected())
				{
				double curFrame=w.frameControl.getFrame();
				gl.glLineWidth(3);
				for(NucLineage lin:getLineages())
					{
					for(NucLineage.Nuc nuc:lin.nuc.values())
						if(!nuc.pos.isEmpty() && nuc.parent!=null)
							{
							int tframe=nuc.pos.firstKey();
							NucLineage.Nuc pnuc=lin.nuc.get(nuc.parent);
							if(!pnuc.pos.isEmpty())
								{
								int pframe=pnuc.pos.lastKey();
								if(curFrame>=pframe && curFrame<=tframe)
									{
									NucLineage.NucPos npos=nuc.pos.get(tframe);
									NucLineage.NucPos ppos=pnuc.pos.get(pframe);
	
									gl.glBegin(GL.GL_LINES);
									gl.glColor3d(1, 1, 0);
									gl.glVertex3d(npos.x,npos.y,npos.z);
									gl.glVertex3d(ppos.x,ppos.y,ppos.z);
									gl.glEnd();
									}
								}
							}
					}
				gl.glLineWidth(1);
				}
			gl.glPopAttrib();
			}

		
		/** Keep track of what hover was before hover test started */
		private NucPair lastHover=null;
		/** Called when hover test starts */
		public void hoverInit(int pixelid)
			{
			//Update hover
			lastHover=NucLineage.currentHover;
			NucLineage.currentHover=new NucPair();
			}
		/** Called when nucleus hovered */
		public void hover(int pixelid)
			{
			NucLineage.currentHover=selectColorMap.get(pixelid);
			//Propagate hover. Avoid infinite recursion.
			if(!NucLineage.currentHover.equals(lastHover))
				BasicWindow.updateWindows(w);
			}

		
		/**
		 * Adjust the scale
		 */
		public Collection<Double> adjustScale()
			{
			int count=0;
			for(Map<NucPair, NucLineage.NucInterp> i:interpNuc)
				count+=i.size();
			if(count>=2)
				{
				double maxx=-1000000,maxy=-1000000,maxz=-1000000;
				double minx= 1000000,miny= 1000000,minz= 1000000;

				//Calculate bounds
				for(Map<NucPair, NucLineage.NucInterp> inter:interpNuc)
					for(NucLineage.NucInterp nuc:inter.values())
						{
						if(maxx<nuc.pos.x) maxx=nuc.pos.x;
						if(maxy<nuc.pos.y) maxy=nuc.pos.y;
						if(maxz<nuc.pos.z) maxz=nuc.pos.z;
						if(minx>nuc.pos.x) minx=nuc.pos.x;
						if(miny>nuc.pos.y) miny=nuc.pos.y;
						if(minz>nuc.pos.z) minz=nuc.pos.z;
						}
				double dx=maxx-minx;
				double dy=maxy-miny;
				double dz=maxz-minz;
				double dist=dx;
				if(dist<dy) dist=dy;
				if(dist<dz) dist=dz;
				return Collections.singleton((Double)dist);
				}
			else
				return Collections.emptySet();
			}

		
		
		/**
		 * Render body of one nucleus
		 */
		private void renderNuc(GL gl, NucPair nucPair, NucLineage.NucInterp nuc)
			{
			//Visibility rule
			if(!nuc.isVisible())
//			if(nuc.frameBefore==null)
				return;
			
			gl.glEnable(GL.GL_CULL_FACE);

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);

	    //Decide color based on if the nucleus is selected
			float lightDiffuse[];
			if(nuc.colorNuc!=null)
	    	lightDiffuse=new float[]{nuc.colorNuc.getRed()/255.0f,nuc.colorNuc.getGreen()/255.0f,nuc.colorNuc.getBlue()/255.0f};
	    else
	    	lightDiffuse=new float[]{1,1,1};
			float lightAmbient[] = { lightDiffuse[0]*0.3f, lightDiffuse[1]*0.3f, lightDiffuse[2]*0.3f, 0.0f };
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);   
	    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);   
			gl.glEnable(GL.GL_LIGHT0);    
	    	
	    if(NucLineage.hiddenNuclei.contains(nucPair))
	    	{
		    if(NucLineage.selectedNuclei.contains(nucPair))
		    	lightDiffuse=new float[]{1,0,1};
		    
	    	//Hidden cell
	    	gl.glColor3d(lightDiffuse[0], lightDiffuse[1], lightDiffuse[2]);
	    	drawHiddenSphere(gl, nuc.pos.r);
	    	}
	    else
	    	{
	    	//Visible cell
	    	drawVisibleSphere(gl, nuc.pos.r, NucLineage.selectedNuclei.contains(nucPair));
	    	}
	    
	    //Go back to world coordinates
	    gl.glPopMatrix();
			}


		
		
		/**
		 * Generate vertex lists for spheres
		 */
		private void initDrawSphere(GL gl)
			{
			if(!madeDisplayLists)
				{
				madeDisplayLists=true;
				GLU glu=new GLU();
				GLUquadric q=glu.gluNewQuadric();
				
				displayListVisibleSphere = gl.glGenLists(1);
				gl.glNewList(displayListVisibleSphere, GL.GL_COMPILE);
				glu.gluSphere(q,1.0,NUC_SHOW_DIV,NUC_SHOW_DIV);
				//drawSphereSolid(gl, 1.0, NUC_SHOW_DIV,NUC_SHOW_DIV);
				gl.glEndList();
				
				displayListSelectSphere = gl.glGenLists(1);
				gl.glNewList(displayListSelectSphere, GL.GL_COMPILE);
				glu.gluSphere(q,1.0,NUC_SELECT_DIV,NUC_SELECT_DIV);
				gl.glEndList();

				glu.gluQuadricDrawStyle(q, GLU.GLU_LINE);
				
				displayListHiddenSphere = gl.glGenLists(1);
				gl.glNewList(displayListHiddenSphere, GL.GL_COMPILE);
				glu.gluSphere(q,1.0,NUC_HIDE_DIV,NUC_HIDE_DIV);
				gl.glEndList();
				
				glu.gluDeleteQuadric(q);
				}
			}
		private boolean madeDisplayLists=false;
		private int displayListVisibleSphere;
		private int displayListHiddenSphere;
		private int displayListSelectSphere;
		
		
		private void drawVisibleSphere(GL gl, double r, boolean selected)
			{
    	double ir=1.0/r;
			gl.glScaled(r,r,r);
			
			if(selected)
				{
	    	gl.glColor3d(1,0,1);
				gl.glLineWidth(5);
				gl.glPolygonMode(GL.GL_BACK, GL.GL_LINE);
				gl.glCullFace(GL.GL_FRONT);
				gl.glDepthFunc(GL.GL_LEQUAL);
	    	gl.glCallList(displayListVisibleSphere);
				gl.glCullFace(GL.GL_BACK);
				gl.glDepthFunc(GL.GL_LESS);
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
				gl.glLineWidth(1);
				}
			
      gl.glEnable(GL.GL_LIGHTING);
    	gl.glColor3d(1,1,1);
    	gl.glCallList(displayListVisibleSphere);
      gl.glDisable(GL.GL_LIGHTING);

    	gl.glScaled(ir,ir,ir);
			}
		private void drawHiddenSphere(GL gl, double r)
			{
    	double ir=1.0/r;
			gl.glScaled(r,r,r);
    	gl.glCallList(displayListHiddenSphere);
    	gl.glScaled(ir,ir,ir);
			}
		
		public void drawSelectSphere(GL gl, double r)
			{
    	double ir=1.0/r;
			gl.glScaled(r,r,r);
    	gl.glCallList(displayListSelectSphere);
    	gl.glScaled(ir,ir,ir);
			}
		
		
		/**
		 * Render labe of one nucleus
		 */
		private void renderNucLabel(GL gl, List<TransparentRender> transparentRenderers, NucPair nucPair, NucLineage.NucInterp nuc)
			{
			//Visibility rule
			if(nuc.frameBefore==null)
				return;

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);
			
      gl.glScalef(-1,-1,-1); //remove later

			
	    //Unrotate camera, then move a bit closer to the camera
	    if(NucLineage.currentHover.equals(nucPair) 
	    		|| miShowAllNucNames.isSelected() 
	    		|| (NucLineage.selectedNuclei.contains(nucPair) && miShowSelectedNucNames.isSelected()))
	    	{
	    	w.view.camera.unrotateGL(gl);
	    
	    	gl.glRotated(180,   0.0, 0.0, 1.0);
	    	gl.glTranslated(0.0, 0.0, -nuc.pos.r*1.05);
	    	//it would look better if it was toward the camera *center*
	    	//also consider setting size such that it does not vary with distance
	    	//3d text at all? overlay rendering should be faster
				String nucName=nucPair.snd();
	    	w.view.renderString(gl, transparentRenderers, (float)(0.005*nuc.pos.r), nucName);
	    	}
	    
	    //Go back to world coordinates
	    gl.glPopMatrix();
			}	
		
		
		/**
		 * Render nucleus in the invisible selection channel
		 */
		private void renderNucSel(GL gl, NucPair nucPair, NucLineage.NucInterp nuc)
			{    
			gl.glEnable(GL.GL_CULL_FACE);
			
			//Save world coordinate && Move to cell center = local coordinate
	    gl.glPushMatrix();
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);
	  	//If visible cell
	    if(!NucLineage.hiddenNuclei.contains(nucPair))
	    	drawSelectSphere(gl, nuc.pos.r);
	    //Go back to world coordinates
	    gl.glPopMatrix();
			}
		

		
		/**
		 * Give suitable center of all objects
		 */
		public Collection<Vector3d> autoCenterMid()
			{
			//Calculate center
			double meanx=0, meany=0, meanz=0;
			int num=0;
			for(NucLineage lin:getLineages())
				{
				Map<NucPair, NucLineage.NucInterp> interpNuc=lin.getInterpNuc(w.frameControl.getFrame());
				num+=interpNuc.size();
				for(NucLineage.NucInterp nuc:interpNuc.values()) //what about non-existing ones?
					{
					meanx+=nuc.pos.x;
					meany+=nuc.pos.y;
					meanz+=nuc.pos.z;
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
		
		
		/**
		 * Given a middle position, figure out radius required to fit objects
		 */
		public Collection<Double> autoCenterRadius(Vector3d mid, double FOV)
			{
			//Calculate maximum radius
			double maxr=0;
			boolean any=false;
			for(NucLineage lin:getLineages())
				{
				Map<NucPair, NucLineage.NucInterp> interpNuc=lin.getInterpNuc(w.frameControl.getFrame());
				any=true;
				for(NucLineage.NucInterp nuc:interpNuc.values())
					{
					double dx=nuc.pos.x-mid.x;
					double dy=nuc.pos.y-mid.y;
					double dz=nuc.pos.z-mid.z;
					double r=Math.sqrt(dx*dx+dy*dy+dz*dz)+nuc.pos.r;
					if(maxr<r)
						maxr=r;
					}
				if(EV.debugMode)
					System.out.println("center radius from nuc: "+(maxr/Math.sin(FOV)));

				}
			//Find how far away the camera has to be
			if(any)
				return Collections.singleton((Double)maxr/Math.sin(FOV));
			else
				return Collections.emptySet();
			}
		
		};
	}


