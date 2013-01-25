package endrov.flowBasic;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Utility class for rendering flows
 * 
 * @author Johan Henriksson
 */
public class RendererFlowUtil extends JComponent
	{
	private static final long serialVersionUID = 1L;

	
	public static final Color colControl=new Color(200,200,200);
	public static final Color colConstant=new Color(150,255,150);
	public static final Color colOperation=new Color(180,180,255);


	public static void drawBox(Graphics g, int x1, int y1, int x2, int y2, Color c, boolean selected)
		{
		Graphics2D g2=(Graphics2D)g;
		
		
		Color colBorder=c;
		if(selected)
			colBorder=Color.MAGENTA;
		
		Color c1=c;
		Color c2=new Color((int)(c.getRed()*0.8),(int)(c.getGreen()*0.8),(int)(c.getBlue()*0.8));
		
		Color c3=new Color((int)(colBorder.getRed()*0.6),(int)(colBorder.getGreen()*0.6),(int)(colBorder.getBlue()*0.6));
		Color c4=new Color((int)(colBorder.getRed()*0.4),(int)(colBorder.getGreen()*0.4),(int)(colBorder.getBlue()*0.4));
		

		int w=x2-x1+1;
		int h=y2-y1+1;
		

		g2.setColor(Color.BLACK);
		
		
		GradientPaint paint=new GradientPaint(x1,y1,c1,  x2,y2,c2);
		g2.setPaint(paint);
		
		g2.fillRect(x1, y1, w,h);
		
		g2.setColor(c3);
		g2.drawLine(x1, y1-1, x2, y1-1);
		g2.drawLine(x1, y2+1, x2, y2+1);
		g2.drawLine(x1-1, y1, x1-1, y2);
		g2.drawLine(x2+1, y1, x2+1, y2);
		
		g2.drawLine(x1, y1, x1, y1);
		g2.drawLine(x2, y1, x2, y1);
		g2.drawLine(x1, y2, x1, y2);
		g2.drawLine(x2, y2, x2, y2);
		
		g2.setColor(c4);
		g2.drawLine(x1, y1-2, x2, y1-2);
		g2.drawLine(x1, y2+2, x2, y2+2);
		g2.drawLine(x1-2, y1, x1-2, y2);
		g2.drawLine(x2+2, y1, x2+2, y2);
		
		g2.drawLine(x1-1, y1-1, x1-1, y1-1);
		g2.drawLine(x2+1, y1-1, x2+1, y1-1);
		g2.drawLine(x1-1, y2+1, x1-1, y2+1);
		g2.drawLine(x2+1, y2+1, x2+1, y2+1);

		/*
		Font font=new Font("Arial", Font.PLAIN, 12);
		g2.setFont(font);
		
		g2.setColor(Color.BLACK);
		g2.drawString("Hejsan", x1+20, y1+12);*/
		
		}
	
	protected void paintComponent(Graphics g)
		{
		Graphics2D g2=(Graphics2D)g;

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		
    
    

		g2.setColor(Color.WHITE);
		
		g2.fillRect(0, 0, getWidth(), getHeight());
		

		int x1=50;
		int y1=50;
		int x2=x1+100;
		int y2=y1+15;

		drawBox(g2, x1, y1, x2, y2, colControl, false);
		
		drawBox(g2, x1, y1+50, x2, y2+50, colConstant, false);
		
		
		drawBox(g2, x1, y1+100, x2, y2+100, colOperation, false);
		
		
		}
	
	
	public static void main(String[] args)
		{
		System.setProperty("awt.useSystemAAFontSettings","on");
	  System.setProperty("swing.aatext", "true");
	  
		JFrame f=new JFrame();
		
		RendererFlowUtil u=new RendererFlowUtil();
		
		
		f.add(u);
		f.setSize(300, 300);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		
		
		
		}
	}
