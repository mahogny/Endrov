/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageannot;

import java.awt.Color;
import java.util.*;

import javax.media.opengl.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.EvObject;
import endrov.modelWindow.*;
import endrov.util.EvDecimal;


/**
 * Extension to Model Window: shows image annotation
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
		
		public void fillModelWindowMenus(){}
		

		
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

		
		public Collection<ImageAnnot> getAnnot()
			{
			List<ImageAnnot> v=new LinkedList<ImageAnnot>();
			for(ImageAnnot lin:w.getSelectedData().getIdObjects(ImageAnnot.class).values())
				if(w.showObject(lin))
					v.add(lin);
			return v;
			}
	
		public void initOpenGL(GL gl)
			{
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
		 * Render graphics
		 */
		public void displayFinal(GL glin,List<TransparentRender> transparentRenderers)
			{
			GL2 gl=glin.getGL2();
			for(ImageAnnot ia:getAnnot())
				renderOne(gl, ia,transparentRenderers);
			}


		
		/**
		 * Render one annotation
		 */
		
		private void renderOne(GL2 gl, ImageAnnot ia,List<TransparentRender> transparentRenderers)
			{
			//Save world coordinate
			gl.glPushMatrix();

			//Move to cell center = local coordinate
			gl.glTranslated(ia.pos.x,ia.pos.y,ia.pos.z);

			gl.glScalef(-1,-1,-1); //remove later


			//Unrotate camera, then move a bit closer to the camera
			w.view.camera.unrotateGL(gl);

			
			Color colorText=Color.BLUE;
			
			gl.glRotated(180,   0.0, 0.0, 1.0);
			//also consider setting size such that it does not vary with distance
			//3d text at all? overlay rendering should be faster
			float size=1; //(float)(0.005*nuc.pos.r) //TODO trouble! relate to camera distance 
			w.view.renderString(gl, transparentRenderers, size, ia.text, colorText);


			//Go back to world coordinates
			gl.glPopMatrix();
			}	
		

		
		/**
		 * Adjust the scale
		 */
		public Collection<BoundingBox> adjustScale()
			{
			return Collections.emptySet();
			}

		
		
		
		
		
		/**
		 * Give suitable center of all objects
		 */
		public Collection<Vector3d> autoCenterMid()
			{
			return Collections.emptySet();
			}
		
		
		/**
		 * Given a middle position, figure out radius required to fit objects
		 */
		public double autoCenterRadius(Vector3d mid)
			{
			return 0;
			}
		
		public EvDecimal getFirstFrame(){return null;}
		public EvDecimal getLastFrame(){return null;};
		
		};
	}


