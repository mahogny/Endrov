package endrov.imageWindow.tools;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;

import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowTool;
import endrov.util.EvFileUtil;

//either send down variables or add accessors to imagewindow


public class ImageWindowToolScreenshot implements ImageWindowTool
	{
	JMenuItem mi=new JMenuItem("Screenshot");

	
	public ImageWindowToolScreenshot(final ImageWindow w)
		{
		mi.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent arg0)
				{
				BufferedImage image=w.getScreenshot();
				
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
		});
		}
	
	public JMenuItem getMenuItem()
		{
		return mi;
		}
	
	public void unselected(){}
	
	public void mouseClicked(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	public void mouseDragged(MouseEvent e, int dx, int dy){}
	public void paintComponent(Graphics g){}
	public void mouseMoved(MouseEvent e, int dx, int dy){}
	public void keyPressed(KeyEvent e){}
	public void keyReleased(KeyEvent e){}
	public void mouseExited(MouseEvent e){}
	}
