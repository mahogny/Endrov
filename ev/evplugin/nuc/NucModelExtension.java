package evplugin.nuc;

import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import evplugin.modelWindow.*;
import evplugin.nuc.NucLineage.NucInterp;
import evplugin.ev.*;


/**
 * Extension to Model Window: shows nuclei
 * @author Johan Henriksson
 */
public class NucModelExtension implements ModelWindowExtension
	{
	public void newModelWindow(ModelWindow w)
		{
		w.modelWindowHooks.add(new NucModelWindowHook(w));
		}
	
	public static class NucModelWindowHook implements ModelWindowHook
		{
		private final HashMap<Integer,NucPair> selectColorMap=new HashMap<Integer,NucPair>();
		private Map<NucPair, NucLineage.NucInterp> interpNuc=new HashMap<NucPair, NucLineage.NucInterp>();
		private final ModelWindow w;
		
		public NucModelWindowHook(ModelWindow w)
			{
			this.w=w;
			}
		
		
		/**
		 * Prepare for rendering
		 */
		public void displayInit(GL gl)
			{
			selectColorMap.clear();

			//Get lineage
			NucLineage lin=NucLineage.getOneLineage(w.view.getMetadata());
			if(lin==null)
				interpNuc.clear();
			else
				interpNuc=lin.getInterpNuc(w.frameControl.getFrame());
			}
		
		/**
		 * Render for selection
		 */
		public void displaySelect(GL gl)
			{
							
			//Render nuclei
			if(EV.debugMode)
				System.out.println("#nuc to render: "+interpNuc.size());
			for(NucPair nucPair:interpNuc.keySet())
				{
				int rawcol=w.view.reserveSelectColor(this);
				selectColorMap.put(rawcol, nucPair);
				w.view.setReserveColor(gl, rawcol);
				renderNucSel(gl,nucPair, interpNuc.get(nucPair));
				}
			}

		
		/**
		 * Render graphics
		 */
		public void displayFinal(GL gl)
			{
			//Render nuc body
			for(NucPair nucPair:interpNuc.keySet())
				{
				renderNuc(gl, nucPair, interpNuc.get(nucPair));
				
				//Draw connecting line
				if(nucPair.getRight().equals(NucLineage.connectNuc[0]))
					for(NucPair nucPair2:interpNuc.keySet())
						if(nucPair2.getRight().equals(NucLineage.connectNuc[1]))
							{
							NucInterp n=interpNuc.get(nucPair);
							NucInterp m=interpNuc.get(nucPair2);
							gl.glBegin(GL.GL_LINES);
							gl.glColor3d(1, 1, 1);
							gl.glVertex3d(n.pos.x,n.pos.y,n.pos.z);
							gl.glVertex3d(m.pos.x,m.pos.y,m.pos.z);
							gl.glEnd();
							}
				}
			
			//Render nuclei text
			for(NucPair nucPair:interpNuc.keySet())
				renderNucLabel(gl,nucPair, interpNuc.get(nucPair));
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
		public void adjustScale()
			{
			if(interpNuc.size()>=2)
				{
				double maxx=-1000000,maxy=-1000000,maxz=-1000000;
				double minx= 1000000,miny= 1000000,minz= 1000000;

				//Calculate bounds
				for(NucLineage.NucInterp nuc:interpNuc.values())
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

				//Select pan speed
				w.view.panspeed=dist/1000.0;
				
				//Select grid size
				w.view.gridsize=Math.pow(10, (int)Math.log10(dist));
				if(w.view.gridsize<1)
					w.view.gridsize=1;
				}
			}

		
		
		/**
		 * Render body of one nucleus
		 */
		private void renderNuc(GL gl, NucPair nucPair, NucLineage.NucInterp nuc)
			{
			String nucName=nucPair.getRight();
			
			//Visibility rule
			if(nuc.frameBefore==null)
				return;
			
			gl.glEnable(GL.GL_CULL_FACE);

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);
			
			//Draw nucleus
	    GLU glu=new GLU();
	    GLUquadric q=glu.gluNewQuadric(); 

	    //Decide color based on if the nucleus is selected
			float lightDiffuse[];
	    if(NucLineage.selectedNuclei.contains(nucPair))
	    	lightDiffuse=new float[]{1,0,1};
	    else
	    	lightDiffuse=new float[]{1,1,1};
			float lightAmbient[] = { 0.3f, 0.3f, 0.3f, 0.0f };
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);   // Setup The Ambient Light
	    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);   // Setup The Diffuse Light
			gl.glEnable(GL.GL_LIGHT0);    
	    	
	    int NUC_SHOW_DIV=12;
	    int NUC_HIDE_DIV=6;
	    //int NUC_POINT_DIV=6;
	    
	    if(NucLineage.hiddenNuclei.contains(nucName))
	    	{
	    	//Hidden cell
	      glu.gluQuadricDrawStyle(q, GLU.GLU_LINE);
	      glu.gluSphere(q,nuc.pos.r,NUC_HIDE_DIV,NUC_HIDE_DIV);
	    	}
	    else
	    	{
	    	//Visible cell
	      gl.glEnable(GL.GL_LIGHTING);
	    	glu.gluSphere(q,nuc.pos.r,NUC_SHOW_DIV,NUC_SHOW_DIV);
	      gl.glDisable(GL.GL_LIGHTING);
	      gl.glScalef(-1,-1,-1);
	      if(false)//w->slab1->value()!=-5000)
	       glu.gluSphere(q,nuc.pos.r,NUC_SHOW_DIV,NUC_SHOW_DIV);
	    	}
	    glu.gluDeleteQuadric(q);
	    
	    //Go back to world coordinates
	    gl.glPopMatrix();
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
	    		|| w.miShowAllNucNames.isSelected() 
	    		|| (NucLineage.selectedNuclei.contains(nucPair) && w.miShowSelectedNucNames.isSelected()))
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
	    	{
	      int NUC_POINT_DIV=6;
	      GLU glu=new GLU();
	      GLUquadric q=glu.gluNewQuadric(); 
	    	glu.gluSphere(q,nuc.pos.r,NUC_POINT_DIV,NUC_POINT_DIV);
	      glu.gluDeleteQuadric(q);
	    	}    
	    //Go back to world coordinates
	    gl.glPopMatrix();
			}
		

		
		/**
		 * Give suitable center of all objects
		 */
		public Vector3D autoCenterMid()
			{
			NucLineage lin=NucLineage.getOneLineage(w.view.getMetadata());
			if(lin!=null)
				{
				Map<NucPair, NucLineage.NucInterp> interpNuc=lin.getInterpNuc(w.frameControl.getFrame());
				if(interpNuc.size()!=0)
					{
					//Calculate center
					double meanx=0, meany=0, meanz=0;
					for(NucLineage.NucInterp nuc:interpNuc.values()) //what about non-existing ones?
						{
						meanx+=nuc.pos.x;
						meany+=nuc.pos.y;
						meanz+=nuc.pos.z;
						}
					double num=interpNuc.size();
					meanx/=num;
					meany/=num;
					meanz/=num;
					return new Vector3D(meanx,meany,meanz);
					}
				}
			return null;
			}
		
		
		/**
		 * Given a middle position, figure out radius required to fit objects
		 */
		public Double autoCenterRadius(Vector3D mid, double FOV)
			{
			NucLineage lin=NucLineage.getOneLineage(w.view.getMetadata());
			if(lin!=null)
				{
				Map<NucPair, NucLineage.NucInterp> interpNuc=lin.getInterpNuc(w.frameControl.getFrame());
				if(interpNuc.size()!=0)
					{
					//Calculate maximum radius
					double maxr=0;
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
					
					//Find how far away the camera has to be
					return maxr/Math.sin(FOV);
					}
				}
			return null;
			}
		
		};
	}


