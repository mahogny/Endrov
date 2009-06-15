package endrov.shell;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.vecmath.Vector2d;

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
	private final ShellImageRenderer r;
	
	private boolean holdTranslate=false;
	private boolean holdRotate=false;

	public void deselected() {}
	
	public ShellImageTool(ImageWindow w, ShellImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	/*
	public boolean isToggleable()
		{
		return true;
		}
	public String toolCaption()
		{
		return "Shell/Define";
		}
	
	public boolean enabled()
		{
		return true;
		}
		*/
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

	
	private Shell getCurrentShell()
		{
		return r.currentShell;
		}
	
	private double square(double x)
		{
		return x*x;
		}
	
	/**
	 * Update currently "hovered" shell
	 */
	private void updateCurrentShell(int mx, int my, boolean acceptNull)
		{
		Vector2d v=w.transformS2W(new Vector2d(mx,my));

		
		double wx=v.x;
		double wy=v.y;
//		double wx=w.s2wx(mx);
	//	double wy=w.s2wy(my);
		
		
		double wz=w.frameControl.getModelZ().doubleValue();
		//w.s2wz(w.frameControl.getZ());
		
		for(EvObject ob:w.getImageset().metaObject.values())
			if(ob instanceof Shell)
				{
				Shell shell=(Shell)ob;
				if(square(wx-shell.midx)+square(wy-shell.midy)+square(wz-shell.midz)<square((shell.major+shell.minor)/2.0))
					{
					r.currentShell=shell;
//					System.out.println("found shell");
					return;
					}
				}
		if(acceptNull)
			r.currentShell=null;
		}

	
	public void mouseClicked(MouseEvent e)
		{
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		Shell shell=getCurrentShell();
		if(SwingUtilities.isLeftMouseButton(e))
			{
			if(shell!=null)
				{
				//Resize
				shell.major+=w.scaleS2w(dx);
				shell.minor+=w.scaleS2w(dy);
				if(shell.major<0) shell.major=0;
				if(shell.minor<0) shell.minor=0;
				w.updateImagePanel();
				}
			}
		}
	
	
	public void mousePressed(MouseEvent e)
		{
		updateCurrentShell(e.getX(), e.getY(),true);
		Shell shell=getCurrentShell();
		if(SwingUtilities.isLeftMouseButton(e))
			{
			if(shell==null)
				{
				int option = JOptionPane.showConfirmDialog(null, "Want to create new shell?", "EV Shell", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION)
					{				
					//Create new shell
					shell=new Shell();
					
					Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));

					shell.midx=v.x;
					shell.midy=v.y;
//					shell.midx=w.s2wx(e.getX());
	//				shell.midy=w.s2wy(e.getY());
					shell.midz=w.frameControl.getModelZ().doubleValue();
					//w.s2wz(w.frameControl.getZ());
					shell.major=w.scaleS2w(80);
					shell.minor=w.scaleS2w(50);
					w.getImageset().addMetaObject(shell);
					r.currentShell=shell;
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
		Shell shell=getCurrentShell();
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
				shell.midx+=w.scaleS2w(dx);
				shell.midy+=w.scaleS2w(dy);
				}
			BasicWindow.updateWindows();
			}
		}
	
	public void mouseExited(MouseEvent e) {}
	
	public void keyPressed(KeyEvent e)
		{
		if(KeyBinding.get(Shell.KEY_TRANSLATE).typed(e))
			holdTranslate=true;
		if(KeyBinding.get(Shell.KEY_ROTATE).typed(e))
			holdRotate=true;

		Shell shell=getCurrentShell();
		if(KeyBinding.get(Shell.KEY_SETZ).typed(e) && shell!=null)
			{
			//Bring shell to this Z
			shell.midz=w.frameControl.getModelZ().doubleValue();
			//w.s2wz(w.frameControl.getZ());
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


//TODO: run when tool selected?
/*
if(shell!=null && shell.exists())
	{
	shell.midx=s2wx(getWidth()/2);
	shell.midy=s2wy(getHeight()/2);
	shell.midz=s2wz(frameControl.getZ());
	}
	*/
