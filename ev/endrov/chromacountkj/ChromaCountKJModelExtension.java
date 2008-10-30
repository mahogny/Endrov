package endrov.chromacountkj;

import java.util.*;

import javax.media.opengl.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.EvObject;
import endrov.modelWindow.*;


/**
 * Extension to Model Window: shows image annotation
 * @author Johan Henriksson
 */
public class ChromaCountKJModelExtension implements ModelWindowExtension
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
			return ob instanceof ChromaCountKJ;
			}

		
		public Collection<ChromaCountKJ> getAnnot()
			{
			List<ChromaCountKJ> v=new LinkedList<ChromaCountKJ>();
			for(ChromaCountKJ lin:ChromaCountKJ.getObjects(w.getSelectedData()))
				if(w.showObject(lin))
					v.add(lin);
			return v;
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
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			for(ChromaCountKJ ia:getAnnot())
				renderOne(gl, ia,transparentRenderers);
			}


		
		/**
		 * Render label of one nucleus
		 */
		
		private void renderOne(GL gl, ChromaCountKJ ia,List<TransparentRender> transparentRenderers)
			{
			//Save world coordinate
			gl.glPushMatrix();

			//Move to cell center = local coordinate
			gl.glTranslated(ia.pos.x,ia.pos.y,ia.pos.z);

			gl.glScalef(-1,-1,-1); //remove later


			//Unrotate camera, then move a bit closer to the camera
			w.view.camera.unrotateGL(gl);

			gl.glRotated(180,   0.0, 0.0, 1.0);
			//also consider setting size such that it does not vary with distance
			//3d text at all? overlay rendering should be faster
			float size=1; //(float)(0.005*nuc.pos.r) //trouble! relate to camera distance TODO
			String s=""+ia.group;
			w.view.renderString(gl, transparentRenderers, size, s);


			//Go back to world coordinates
			gl.glPopMatrix();
			}	
		

		
		/**
		 * Adjust the scale
		 */
		public Collection<Double> adjustScale()
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
		public Collection<Double> autoCenterRadius(Vector3d mid, double FOV)
			{
			return Collections.emptySet();
			}
		
		};
	}


