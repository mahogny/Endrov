/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer3D.basicExtensions;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.swing.*;
import javax.vecmath.Vector3d;

import org.jdom.*;

import endrov.data.EvObject;
import endrov.util.io.EvFileUtil;
import endrov.util.math.EvDecimal;
import endrov.windowViewer3D.BoundingBox3D;
import endrov.windowViewer3D.Viewer3DWindow;
import endrov.windowViewer3D.Viewer3DWindowExtension;
import endrov.windowViewer3D.Viewer3DHook;
import endrov.windowViewer3D.TransparentRenderer3D;

/**
 * Grid in model window
 * 
 * @author Johan Henriksson
 */
public class ModelWindowScreenshot implements Viewer3DWindowExtension
	{
	
	public void newModelWindow(final Viewer3DWindow w)
		{
		w.modelWindowHooks.add(new ModelWindowGridHook(w));
		}
	private class ModelWindowGridHook implements Viewer3DHook, ActionListener
		{
		private Viewer3DWindow w;
		
		public JMenuItem miScreenshot=new JMenuItem("Screenshot"); 
		
		public ModelWindowGridHook(Viewer3DWindow w)
			{
			this.w=w;
			w.menuModel.add(miScreenshot);
			miScreenshot.addActionListener(this);
			}
		
		
		
		public void readPersonalConfig(Element e)
			{
			}
		public void savePersonalConfig(Element e)
			{
			}
		
		

		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==miScreenshot)
				{
				BufferedImage image=w.view.getScreenshot();
				
				JFileChooser fc=new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int ret=fc.showSaveDialog(w);
				if(ret==JFileChooser.APPROVE_OPTION)
					{
					File f=fc.getSelectedFile();
					try
						{
						ImageIO.write(image, "png", EvFileUtil.makeFileEnding(f, ".png"));
						}
					catch (IOException e2)
						{
						e2.printStackTrace();
						}
					}
				
				}
			}
		
		
			
		public Collection<BoundingBox3D> adjustScale()
			{
			return Collections.emptySet();
			}
		public Collection<Vector3d> autoCenterMid(){return Collections.emptySet();}
		public double autoCenterRadius(Vector3d mid)
			{
			return 0;	
			}
		public boolean canRender(EvObject ob){return false;}
		
		public void initOpenGL(GL gl)
			{
			}

		
		public void displayInit(GL gl){}
		public void displaySelect(GL gl){}
		public void fillModelWindowMenus(){}
		public void datachangedEvent(){}

		
		/**
		 * Render all grid planes
		 */
		public void displayFinal(GL gl,List<TransparentRenderer3D> transparentRenderers)
			{
			}

		public EvDecimal getFirstFrame(){return null;}
		public EvDecimal getLastFrame(){return null;}
		}
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Viewer3DWindow.modelWindowExtensions.add(new ModelWindowScreenshot());
		}

	}
