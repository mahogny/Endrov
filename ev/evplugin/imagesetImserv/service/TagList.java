package evplugin.imagesetImserv.service;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * Tag list panel
 * 
 * @author Johan Henriksson
 *
 */
public class TagList extends JPanel implements MouseListener
	{
	public static final long serialVersionUID=0;
	
	private Vector<ListDescItem> tags=new Vector<ListDescItem>();
	public Set<ListDescItem> selected=new HashSet<ListDescItem>();
	
	private final Font font=Font.decode("Dialog PLAIN "); //+12
	private final int fonth,fonta;
	private final FontMetrics fm;
	private final int csize;
	private final int totc;
	
	public TagList()
		{
		tags.add(ListDescItem.makeChan("foo"));
		tags.add(ListDescItem.makeTag("bar"));
		fm=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB).getGraphics().getFontMetrics(font);
		fonth=fm.getHeight();
		fonta=fm.getAscent();
		csize=(fonth/2-2)*2;
		totc=csize*2+8;
		}


	public void setList()
		{
		//TODO
		
		//Eliminate selected ones that no longer exist
		repaint();
		}
	
	
	
	protected void paintComponent(Graphics g)
		{
		Dimension d=getSize();
		
		//Clear
		g.setColor(Color.WHITE);
		g.fillRect(0,0, d.width, d.height);

		for(int i=0;i<tags.size();i++)
			{
			int y=(fonth+1)*i;
			int cy=y+1;
			int cxa=1;
			int cxb=1+3+csize;
			
			if(selected.contains(tags.get(i)))
				{
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0, y, d.width, fonth);
				}
			
			g.setColor(Color.BLUE);
			g.fillOval(cxa, cy, csize, csize);
			g.fillOval(cxb, cy, csize, csize);
			
			g.setColor(Color.WHITE);
			g.drawLine(cxa+1, cy+csize/2, cxa+csize-1, cy+csize/2);
			g.drawLine(cxa+csize/2, cy+1, cxa+csize/2, cy+csize-1);
			g.drawLine(cxb+1, cy+csize/2, cxb+csize-1, cy+csize/2);
			
			
			g.setColor(Color.BLACK);
			g.drawString(tags.get(i).toString(), totc, y+fonta);
			}
		}

	public Dimension getPreferredSize()
		{
		int w=0;
		for(ListDescItem s:tags)
			{
			int nw=fm.stringWidth(s.toString());
			if(w<nw)
				w=nw;
			}
		return new Dimension(w+totc,tags.size()*fonth);
		}


	public void mouseClicked(MouseEvent e)
		{
		}


	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	
	
	}
