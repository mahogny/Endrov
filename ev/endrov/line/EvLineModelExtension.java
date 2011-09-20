/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.line;

import java.util.*;

import javax.media.opengl.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.EvObject;
import endrov.modelWindow.*;
import endrov.util.EvDecimal;


/**
 * Extension to Model Window: shows lines
 * @author Johan Henriksson
 */
public class EvLineModelExtension implements ModelWindowExtension
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
			}
		
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void datachangedEvent(){}
		
		
		public boolean canRender(EvObject ob)
			{
			return ob instanceof EvLine;
			}

		
		public Collection<EvLine> getAnnot()
			{
			List<EvLine> v=new LinkedList<EvLine>();
			for(EvLine lin:w.getSelectedData().getObjects(EvLine.class))
				if(w.showObject(lin))
					v.add(lin);
			return v;
			}
		
		public void select(int pixelid){}
		
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
			for(EvLine ia:getAnnot())
				{
				//Save world coordinate
				gl.glPushMatrix();
				
				gl.glColor3d(0, 1.0, 0);
				if(ia.pos.size()>1)
					{
					EvDecimal curFrame=w.getFrame();
					
					//TODO bad bd comparison. double
					if(ia.pos.get(0).frame.equals(curFrame) && ia.pos.size()>1)
						{
						gl.glBegin(GL.GL_LINE_STRIP);
						for(int i=0;i<ia.pos.size();i++)
							gl.glVertex3d(ia.pos.get(i).v.x,ia.pos.get(i).v.y,ia.pos.get(i).v.z);
						gl.glEnd();
						}
					}
				
				//Go back to world coordinates
				gl.glPopMatrix();
				}
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
		public double autoCenterRadius(Vector3d mid)
			{
			return 0;
			}
		
		public EvDecimal getFirstFrame(){return null;}
		public EvDecimal getLastFrame(){return null;};
		};
	}


