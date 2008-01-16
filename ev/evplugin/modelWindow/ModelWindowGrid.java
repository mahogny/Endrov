package evplugin.modelWindow;

import java.awt.event.*;
import javax.media.opengl.GL;
import javax.swing.*;
import org.jdom.*;

import evplugin.data.EvObject;
import evplugin.ev.Vector3D;

public class ModelWindowGrid implements ModelWindowExtension
	{
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new ModelWindowGrid());
		}
	
	private class ModelWindowGridHook implements ModelWindowHook, ActionListener
		{
		private ModelWindow w;
		
		/** Size of the grid in um */
		public double gridsize=1;

		public JCheckBoxMenuItem miShowGrid=new JCheckBoxMenuItem("Show grid"); 

		
		
		public ModelWindowGridHook(ModelWindow w)
			{
			this.w=w;
			w.menuModel.add(miShowGrid);
			miShowGrid.addActionListener(this);
			setShowGrid(true);
			}
		
		
		
		public void readPersonalConfig(Element e)
			{
			try{setShowGrid(e.getAttribute("showGrid").getBooleanValue());}
			catch (DataConversionException e1){}
			}
		public void savePersonalConfig(Element e)
			{
			e.setAttribute("showGrid",""+miShowGrid.isSelected());
			}
		
		

		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==miShowGrid)
				w.repaint();
			}
		
		
		
		/**
		 * View setting: display grid?
		 */
		public void setShowGrid(boolean b)
			{
			miShowGrid.setSelected(b);
			}
			
			
		public void adjustScale(){}
		public Vector3D autoCenterMid(){return null;}
		public Double autoCenterRadius(Vector3D mid, double FOV){return null;}
		public boolean canRender(EvObject ob){return false;}
		public void displayInit(GL gl){}
		public void displaySelect(GL gl){}
		public void select(int id){}
		public void fillModelWindomMenus(){}
		public void datachangedEvent(){}

		
		/**
		 * Render all grid planes
		 */
		public void displayFinal(GL gl)
			{
			if(miShowGrid.isSelected())
				{
				boolean ruler=false;
				gl.glPushMatrix(); 
				gl.glRotatef(90,0,1,0); 
				gl.glRotatef(90,1,0,0); 
				gl.glColor3d(0.4,0,0); 
				renderGridPlane(gl,gridsize); 
				if(ruler)
					{
					gl.glColor3d(1,1,1); 
					renderRuler(gl,gridsize);
					}
				gl.glPopMatrix();

				gl.glColor3d(0,0.4,0);  
				renderGridPlane(gl,gridsize); 
				if(ruler)
					{
					gl.glColor3d(1,1,1); 
					renderRuler(gl,gridsize);
					}
				
				gl.glPushMatrix(); 
				gl.glRotatef(90,0,0,1); 
				gl.glRotatef(90,1,0,0); 
				gl.glColor3d(0,0,0.4); 
				renderGridPlane(gl,gridsize); 
				if(ruler)
					{
					gl.glColor3d(1,1,1); 
					renderRuler(gl,gridsize);
					}
				gl.glPopMatrix();
				
				}
			
			
			
			}

		/**
		 * Render one grid plane
		 */
		private void renderGridPlane(GL gl, double gsize)
			{
			int gnum=10;
			gl.glBegin(GL.GL_LINES);
			for(int i=-gnum;i<=gnum;i++)
				{
				gl.glVertex3d(0,-gsize*gnum, i*gsize);
				gl.glVertex3d(0, gsize*gnum, i*gsize);
				gl.glVertex3d(0,i*gsize, -gsize*gnum);
				gl.glVertex3d(0,i*gsize,  gsize*gnum);
				}
			gl.glEnd();/*
			gl.glLineWidth(5);
			gl.glBegin(GL.GL_LINES);
				{
				gl.glVertex3d(0,0,0);
				gl.glVertex3d(0,gsize*gnum,0);
				}
			gl.glEnd();
			gl.glLineWidth(1);*/
			}
		
		
		/**
		 * Render scale
		 * TODO faster
		 */
		public void renderRuler(GL gl, double gsize)
			{
			int gnum=10;
			for(int i=-gnum;i<=gnum;i++)
				if(i!=0)
					{
					gl.glPushMatrix();
					gl.glTranslated(0, i*gsize, 0);
					w.view.renderString(gl, w.view.renderer, 0.02f, ""+i*gsize);
					gl.glPopMatrix();
					}
			}
		
		
		}
	
	
	public static double getGridSize(ModelWindow w)
		{
		for(ModelWindowHook h:w.modelWindowHooks)
			if(h instanceof ModelWindowGridHook)
				return ((ModelWindowGridHook)h).gridsize;
		return 0; //should never reach here
		}
	
	public static void setGridSize(ModelWindow w, double g)
		{
		for(ModelWindowHook h:w.modelWindowHooks)
			if(h instanceof ModelWindowGridHook)
				((ModelWindowGridHook)h).gridsize=g;
		}
	
	
	public void newModelWindow(final ModelWindow w)
		{
		w.modelWindowHooks.add(new ModelWindowGridHook(w));
		}
	}
