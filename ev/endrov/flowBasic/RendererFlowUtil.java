package endrov.flowBasic;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class RendererFlowUtil extends JComponent
	{
	private static final long serialVersionUID = 1L;


	protected void paintComponent(Graphics g)
		{
		Graphics2D g2=(Graphics2D)g;

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		
		g2.setColor(Color.WHITE);
		
		g2.fillRect(0, 0, getWidth(), getHeight());

		Color c1=new Color(255,100,0);
		Color c2=new Color((int)(c1.getRed()*0.8),(int)(c1.getGreen()*0.8),(int)(c1.getBlue()*0.8));
		Color c3=new Color(170,80,0);
		
		Color c4=new Color(100,80,0);
		
		int x1=50;
		int y1=50;
		int x2=x1+100;
		int y2=y1+15;
		
		int w=x2-x1+1;
		int h=y2-y1+1;
		
		int arc=10;
		

		g2.setColor(Color.BLACK);
		
		
		GradientPaint paint=new GradientPaint(x1,y1,c1,  x2,y2,c2);
		g2.setPaint(paint);
		//g2.drawRoundRect(x1, y1, w,h, arc, arc);
		
		g2.fillRect(x1, y1, w,h);
		
		g2.setColor(c3);
		g2.drawLine(x1, y1-1, x2, y1-1);
		g2.drawLine(x1, y2+1, x2, y2+1);
		g2.drawLine(x1-1, y1, x1-1, y2);
		g2.drawLine(x2+1, y1, x2+1, y2);
		
		g2.drawLine(x1, y1, x1, y1);
		
		g2.setColor(c4);
		g2.drawLine(x1, y1-2, x2, y1-2);
		g2.drawLine(x1, y2+2, x2, y2+2);
		g2.drawLine(x1-2, y1, x1-2, y2);
		g2.drawLine(x2+2, y1, x2+2, y2);
		
		g2.drawLine(x1-1, y1-1, x1-1, y1-1);
		
		Font font=new Font("Arial", Font.PLAIN, 12);
		g2.setFont(font);
		
		g2.setColor(Color.BLACK);
		g2.drawString("Hejsan", x1+20, y1+12);
		
		// TODO Auto-generated method stub
		super.paintComponent(g);
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
