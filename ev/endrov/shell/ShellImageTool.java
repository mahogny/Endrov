/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.shell;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import endrov.basicWindow.*;
import endrov.data.*;
import endrov.imageWindow.*;
import endrov.keyBinding.KeyBinding;


/**
 * Image Window Extension: Edit shells
 * @author Johan Henriksson
 */
public class ShellImageTool implements ImageWindowTool
	{
	private final ImageWindow w;
	
	private boolean holdTranslate=false;
	private boolean holdRotate=false;

	
	
	public ShellImageTool(ImageWindow w)
		{
		this.w=w;
		}

	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Shell/Define");
		mi.setSelected(w.getTool()==this);
		final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
		}

	public void deselected() {}
	
	
	private ShellImageRenderer getRenderer(ImageWindow w)
		{
		return w.getRendererClass(ShellImageRenderer.class); 
		}
	
	private Shell getCurrentShell(ImageWindow w)
		{		
		return getRenderer(w).hoverShell;
		}
	
	private static double square(double x)
		{
		return x*x;
		}
	
	/**
	 * Update currently "hovered" shell
	 */
	private void updateCurrentShell(int mx, int my, boolean acceptNull)
		{
		Vector2d v=w.transformPointS2W(new Vector2d(mx,my));

		ShellImageRenderer r=getRenderer(w);
		
		double wx=v.x;
		double wy=v.y;
		double wz=w.getZ().doubleValue();
		
		for(EvObject ob:w.getRootObject().metaObject.values())
			if(ob instanceof Shell)
				{
				Shell shell=(Shell)ob;
				if(square(wx-shell.midx)+square(wy-shell.midy)+square(wz-shell.midz)<square((shell.major+shell.minor)/2.0))
					{
					r.hoverShell=shell;
					return;
					}
				}
		if(acceptNull)
			r.hoverShell=null;
		}

	
	public void mouseClicked(final  MouseEvent e, Component invoker)
		{
		}
	
	public void mouseDragged(final  MouseEvent e, int dx, int dy)
		{
		Shell shell=getCurrentShell(w);
		if(SwingUtilities.isLeftMouseButton(e))
			{
			if(shell!=null)
				{
				//Resize
				Vector3d dv=new Vector3d(w.scaleS2w(dx),w.scaleS2w(dy),0);
				Vector3d ma=shell.getMajorAxis();
				ma.normalize();
				Vector3d majora=shell.getMajorAxis();
				majora.normalize();
				Vector3d minora=shell.getMinorAxis();
				minora.normalize();
				
				shell.major+=dv.dot(majora);
				shell.minor+=dv.dot(minora);
				
				if(shell.major<0) shell.major=0;
				if(shell.minor<0) shell.minor=0;
				w.updateImagePanel();
				}
			}
		}
	
	
	public void mousePressed(MouseEvent e)
		{
		ShellImageRenderer r=getRenderer(w);
		updateCurrentShell(e.getX(), e.getY(),true);
		Shell shell=getCurrentShell(w);
		if(SwingUtilities.isLeftMouseButton(e))
			{
			if(shell==null)
				{
				if(BasicWindow.showConfirmYesNoDialog("Want to create new shell?"))
					{				
					//Create new shell
					shell=new Shell();
					
					Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));

					shell.midx=v.x;
					shell.midy=v.y;
					shell.midz=w.getZ().doubleValue();
					shell.major=w.scaleS2w(80);
					shell.minor=w.scaleS2w(50);
					w.getRootObject().addMetaObject(shell);
					r.hoverShell=shell;
					w.updateImagePanel();
					}
				}
			}
		}

	public void mouseReleased(MouseEvent e)
		{
		BasicWindow.updateWindows();
		}

	
	public void mouseMoved(MouseEvent e, int dx, int dy)
		{
		updateCurrentShell(e.getX(), e.getY(),false);
		Shell shell=getCurrentShell(w);
		if(shell!=null)
			{
			if(holdRotate)
				{
				//Rotate
				shell.angle+=dy/80.0;
				}
			else if(holdTranslate)
				{
				//Translate
				Vector2d diff=w.transformVectorS2W(new Vector2d(dx,dy));
				shell.midx+=diff.x;
				shell.midy+=diff.y;
				}
			BasicWindow.updateWindows();
			}
		}
	
	public void mouseExited(final  MouseEvent e) {}
	
	public void keyPressed(final  KeyEvent e)
		{
		if(KeyBinding.get(Shell.KEY_TRANSLATE).typed(e))
			holdTranslate=true;
		if(KeyBinding.get(Shell.KEY_ROTATE).typed(e))
			holdRotate=true;

		Shell shell=getCurrentShell(w);
		if(KeyBinding.get(Shell.KEY_SETZ).typed(e) && shell!=null)
			{
			//Bring shell to this Z
			shell.midz=w.getZ().doubleValue();
			BasicWindow.updateWindows();
			}
		}

	public void keyReleased(KeyEvent e)
		{
		if(KeyBinding.get(Shell.KEY_TRANSLATE).typed(e))
			holdTranslate=false;
		if(KeyBinding.get(Shell.KEY_ROTATE).typed(e))
			holdRotate=false;
		}

	
	public void paintComponent(Graphics g)
		{
		}
		

	}

