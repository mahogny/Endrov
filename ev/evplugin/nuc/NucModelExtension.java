package evplugin.nuc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.*;
import org.jdom.Element;

import evplugin.modelWindow.*;
import evplugin.nuc.NucLineage.NucInterp;
import evplugin.data.EvObject;
import evplugin.ev.*;


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
	
	public static class NucModelWindowHook implements ModelWindowHook, ActionListener
		{
		private final HashMap<Integer,NucPair> selectColorMap=new HashMap<Integer,NucPair>();
		private Vector<Map<NucPair, NucLineage.NucInterp>> interpNuc=new Vector<Map<NucPair, NucLineage.NucInterp>>();
		private final ModelWindow w;
		
		public void fillModelWindomMenus(){}
		

		
		public JCheckBoxMenuItem miShowAllNucNames=new JCheckBoxMenuItem("Names: Show all");
		public JCheckBoxMenuItem miShowSelectedNucNames=new JCheckBoxMenuItem("Names: Show for selected");
		public JMenuItem miShowSelectedNuc=new JMenuItem("Nuclei: Unhide selected"); 
		public JMenuItem miHideSelectedNuc=new JMenuItem("Nuclei: Hide selected"); 
		
		public NucModelWindowHook(ModelWindow w)
			{
			this.w=w;
			
			JMenu miNuc=new JMenu("Nuclei/Lineage");
			
			miNuc.add(miShowAllNucNames);
			miNuc.add(miShowSelectedNucNames);
			miNuc.add(miShowSelectedNuc);
			miNuc.add(miHideSelectedNuc);
			w.menuModel.add(miNuc);
			
			miShowAllNucNames.addActionListener(this);
			miShowSelectedNuc.addActionListener(this);
			miHideSelectedNuc.addActionListener(this);
			}
		
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void datachangedEvent(){}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==miShowSelectedNuc)
				{
				for(evplugin.nuc.NucPair p:NucLineage.selectedNuclei)
					NucLineage.hiddenNuclei.remove(p);
				w.view.repaint();
				}
			else if(e.getSource()==miHideSelectedNuc)
				{
				for(evplugin.nuc.NucPair p:NucLineage.selectedNuclei)
					NucLineage.hiddenNuclei.add(p);
				w.view.repaint();
				}
			else if(e.getSource()==miShowAllNucNames)
				w.view.repaint();
			else if(e.getSource()==miShowSelectedNucNames)
				w.view.repaint();
			}
		
		public boolean canRender(EvObject ob)
			{
			return ob instanceof NucLineage;
			}

		
		public Collection<NucLineage> getLineages()
			{
			Vector<NucLineage> v=new Vector<NucLineage>();
			for(NucLineage lin:NucLineage.getLineages(w.metaCombo.getMeta()))
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
		 * Render graphics
		 */
		public void displayFinal(GL gl)
			{
			initDrawSphere(gl);
			
			for(Map<NucPair, NucLineage.NucInterp> inter:interpNuc)
				{
				for(NucPair nucPair:inter.keySet())
					{
					//Render nuc body
					renderNuc(gl, nucPair, inter.get(nucPair));
					
					//Draw connecting line
					if(nucPair.getRight().equals(NucLineage.connectNuc[0]))
						for(NucPair nucPair2:inter.keySet())
							if(nucPair2.getRight().equals(NucLineage.connectNuc[1]))
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
					renderNucLabel(gl,nucPair, inter.get(nucPair));
				}
			}

		
		/**
		 * Select a nucleus
		 */
		public void select(int pixelid)
			{
			NucLineage.currentHover=selectColorMap.get(pixelid);
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
			if(nuc.frameBefore==null)
				return;
			
			gl.glEnable(GL.GL_CULL_FACE);

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);

	    //Decide color based on if the nucleus is selected
			float lightDiffuse[];
	    if(NucLineage.selectedNuclei.contains(nucPair))
	    	lightDiffuse=new float[]{1,0,1};
	    else
	    	lightDiffuse=new float[]{1,1,1};
			float lightAmbient[] = { 0.3f, 0.3f, 0.3f, 0.0f };
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);   
	    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);   
			gl.glEnable(GL.GL_LIGHT0);    
	    	
	    if(NucLineage.hiddenNuclei.contains(nucPair))
	    	{
	    	//Hidden cell
	    	gl.glColor3d(lightDiffuse[0], lightDiffuse[1], lightDiffuse[2]);
	    	drawHiddenSphere(gl, nuc.pos.r);
	    	}
	    else
	    	{
	    	//Visible cell
	      gl.glEnable(GL.GL_LIGHTING);
	      gl.glColor3d(1,1,1);
	      
	    	drawVisibleSphere(gl, nuc.pos.r);
	    	
	      gl.glDisable(GL.GL_LIGHTING);
	      if(false)//w->slab1->value()!=-5000)
	      	{
		      gl.glScalef(-1,-1,-1);
		      drawVisibleSphere(gl, nuc.pos.r);
	      	}
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
		
		
		private void drawVisibleSphere(GL gl, double r)
			{
    	double ir=1.0/r;
			gl.glScaled(r,r,r);
    	gl.glCallList(displayListVisibleSphere);
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
		private void renderNucLabel(GL gl, NucPair nucPair, NucLineage.NucInterp nuc)
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
				String nucName=nucPair.getRight();
	    	w.view.renderString(gl, w.view.renderer, (float)(0.005*nuc.pos.r), nucName);
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
		public Collection<Vector3D> autoCenterMid()
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
				return Collections.singleton(new Vector3D(meanx,meany,meanz));
				}
			}
		
		
		/**
		 * Given a middle position, figure out radius required to fit objects
		 */
		public Collection<Double> autoCenterRadius(Vector3D mid, double FOV)
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


