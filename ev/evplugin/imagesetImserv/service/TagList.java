package evplugin.imagesetImserv.service;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.JPanel;


//TODO: not shift on mac

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
	
	private final Font font=Font.decode("Dialog PLAIN");
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
		addMouseListener(this);
		}

	
	public ListDescItem[] getSelectedValues()
		{
		return selected.toArray(new ListDescItem[]{});
		}

	public void setList(Collection<ListDescItem> c)
		{
		tags.clear();
		tags.addAll(c);
		//Eliminate selected ones that no longer exist TODO
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
			
			if(tags.get(i).type==ListDescItem.TAG)
				{
				g.setColor(Color.BLUE);
				g.fillOval(cxa, cy, csize, csize);
				g.fillOval(cxb, cy, csize, csize);
				
				g.setColor(Color.WHITE);
				g.drawLine(cxa+1, cy+csize/2, cxa+csize-1, cy+csize/2);
				g.drawLine(cxa+csize/2, cy+1, cxa+csize/2, cy+csize-1);
				g.drawLine(cxb+1, cy+csize/2, cxb+csize-1, cy+csize/2);
				}
			
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
		int i=(e.getY()-2)/fonth;
		if(e.getX()<csize+3)
			{
			if(i<tags.size())
				{
				ListDescItem item=tags.get(i);
				for(TagListListener l:listeners.keySet())
					l.tagListAddRemove(item, true);
				}
			}
		else if(e.getX()<csize*2+6)
			{
			if(i<tags.size())
				{
				ListDescItem item=tags.get(i);
				for(TagListListener l:listeners.keySet())
					l.tagListAddRemove(item, false);
				}
			}
		else
			{
			if((e.getModifiersEx()&MouseEvent.SHIFT_DOWN_MASK)==0)
				selected.clear();
			if(i<tags.size())
				selected.add(tags.get(i));
			for(TagListListener l:listeners.keySet())
				l.tagListSelect();
			repaint();
			}
		}


	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	
	private WeakHashMap<TagListListener, Object> listeners=new WeakHashMap<TagListListener, Object>();
	
	public void addTagListListener(TagListListener listener)
		{
		listeners.put(listener,null);
		}
	//or change?
	
	
	public static interface TagListListener
		{
		public void tagListSelect();
		public void tagListAddRemove(ListDescItem item, boolean toAdd);
		}
	
	}
