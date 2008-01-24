package evplugin.imageannot;

import java.util.*;

import javax.media.opengl.*;
import org.jdom.Element;

import evplugin.modelWindow.*;
import evplugin.data.EvObject;
import evplugin.ev.*;


/**
 * Extension to Model Window: shows nuclei
 * @author Johan Henriksson
 */
public class ImageAnnotModelExtension implements ModelWindowExtension
	{
  
  public void newModelWindow(ModelWindow w)
		{
		w.modelWindowHooks.add(new NucModelWindowHook(w));
		}
	
	public static class NucModelWindowHook implements ModelWindowHook
		{
		private final ModelWindow w;
		
		public void fillModelWindomMenus(){}
		

		
		public NucModelWindowHook(ModelWindow w)
			{
			this.w=w;
			
//			JMenu miNuc=new JMenu("Image Annotation");
		
//			w.menuModel.add(miNuc);
			
			}
		
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void datachangedEvent(){}
		
		
		public boolean canRender(EvObject ob)
			{
			return ob instanceof ImageAnnot;
			}

		
		public Collection<ImageAnnot> getLineages()
			{
			Vector<ImageAnnot> v=new Vector<ImageAnnot>();
			for(ImageAnnot lin:ImageAnnot.getObjects(w.metaCombo.getMeta()))
				if(w.showObject(lin))
					v.add(lin);
			return v;
			}
		
		public void select(int pixelid){}
		
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
		 * Render graphics
		 */
		public void displayFinal(GL gl)
			{
			/*
			initDrawSphere(gl);
			
			for(Map<NucPair, ImageAnnot.NucInterp> inter:interpNuc)
				{
				for(NucPair nucPair:inter.keySet())
					{
					//Render nuc body
					renderNuc(gl, nucPair, inter.get(nucPair));
					
					//Draw connecting line
					if(nucPair.getRight().equals(ImageAnnot.connectNuc[0]))
						for(NucPair nucPair2:inter.keySet())
							if(nucPair2.getRight().equals(ImageAnnot.connectNuc[1]))
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
				*/
			}

		
		

		
		/**
		 * Adjust the scale
		 */
		public Collection<Double> adjustScale()
			{
			return Collections.emptySet();
			}

		
		
		/**
		 * Render body of one nucleus
		 */
		/*
		private void renderNuc(GL gl, NucPair nucPair, ImageAnnot.NucInterp nuc)
			{
			//Visibility rule
			if(nuc.frameBefore==null)
				return;
			
			gl.glEnable(GL.GL_CULL_FACE);

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);
			
			//Draw nucleus
//	    GLU glu=new GLU();
	//    GLUquadric q=glu.gluNewQuadric(); 

	    //Decide color based on if the nucleus is selected
			float lightDiffuse[];
	    if(NucLineage.ImageAnnot.contains(nucPair))
	    	lightDiffuse=new float[]{1,0,1};
	    else
	    	lightDiffuse=new float[]{1,1,1};
			float lightAmbient[] = { 0.3f, 0.3f, 0.3f, 0.0f };
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);   
	    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);   
			gl.glEnable(GL.GL_LIGHT0);    
	    	
	    //int NUC_POINT_DIV=6;
	    
	    if(NucLineage.ImageAnnot.contains(nucPair))
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
	    	
	    	
	    	//glu.gluSphere(q,nuc.pos.r,NUC_SHOW_DIV,NUC_SHOW_DIV);
	    	
	      gl.glDisable(GL.GL_LIGHTING);
	      if(false)//w->slab1->value()!=-5000)
	      	{
		      gl.glScalef(-1,-1,-1);
		      drawVisibleSphere(gl, nuc.pos.r);
//	      	glu.gluSphere(q,nuc.pos.r,NUC_SHOW_DIV,NUC_SHOW_DIV);
	      	}
	    	}
//	    glu.gluDeleteQuadric(q);
	    
	    //Go back to world coordinates
	    gl.glPopMatrix();
			}
*/
		
		
		
		/**
		 * Render label of one nucleus
		 */
		/*
		private void renderNucLabel(GL gl, NucPair nucPair, ImageAnnot.NucInterp nuc)
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
	    if(NucLineage.ImageAnnot.equals(nucPair) 
	    		|| miShowAllNucNames.isSelected() 
	    		|| (NucLineage.ImageAnnot.contains(nucPair) && miShowSelectedNucNames.isSelected()))
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
		*/
		
		
		/**
		 * Give suitable center of all objects
		 */
		public Collection<Vector3D> autoCenterMid()
			{
			return Collections.emptySet();
			}
		
		
		/**
		 * Given a middle position, figure out radius required to fit objects
		 */
		public Collection<Double> autoCenterRadius(Vector3D mid, double FOV)
			{
			return Collections.emptySet();
			}
		
		};
	}


