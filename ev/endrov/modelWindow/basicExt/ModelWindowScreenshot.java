/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindow.basicExt;

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
import endrov.modelWindow.ModelWindow;
import endrov.modelWindow.ModelWindowExtension;
import endrov.modelWindow.ModelWindowHook;
import endrov.modelWindow.TransparentRender;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;

/**
 * Grid in model window
 * 
 * @author Johan Henriksson
 */
public class ModelWindowScreenshot implements ModelWindowExtension
	{
	
	public void newModelWindow(final ModelWindow w)
		{
		w.modelWindowHooks.add(new ModelWindowGridHook(w));
		}
	private class ModelWindowGridHook implements ModelWindowHook, ActionListener
		{
		private ModelWindow w;
		
		public JMenuItem miScreenshot=new JMenuItem("Screenshot"); 
		
		public ModelWindowGridHook(ModelWindow w)
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
		
		
			
		public Collection<Double> adjustScale()
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
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
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
		ModelWindow.modelWindowExtensions.add(new ModelWindowScreenshot());
		}

	}
