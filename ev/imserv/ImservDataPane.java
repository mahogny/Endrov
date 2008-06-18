package imserv;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


/**
 * Panel with all objects from imserv
 * 
 * @author Johan Henriksson
 */
public class ImservDataPane extends JPanel
	{
	public static final long serialVersionUID=0;
	
	private Area a=new Area();
	private JScrollPane scroll=new JScrollPane(a);
	private Set<Integer> selectedId=new HashSet<Integer>();
	private ImservConnection conn;
	
	private Map<String, Image> thumbs=Collections.synchronizedMap(new HashMap<String, Image>());

	private List<String> obList=new ArrayList<String>();
	
	
	private int iconw=100;
	private int iconh=100;
	private int shifty=5;
	
	ImPostLoader impostloader=new ImPostLoader();
	
	public class ImPostLoader extends Thread
		{
		private LinkedList<String> toload=new LinkedList<String>();
		public synchronized void clear()
			{
			toload.clear();
			}
		public synchronized void add(String s)
			{
			toload.add(s);
			notify();
			}
		private synchronized String get() throws InterruptedException
			{
			while (toload.isEmpty())
				wait();
			return toload.removeFirst();
			}
		public void run()
			{
			for(;;)
				{
				try
					{
					String sid=get();
					
					try 
						{
						DataIF data=conn.imserv.getData(sid);
						ImageIcon icon=SendFile.getImageFromBytes(data.getThumb());
						thumbs.put(sid,icon.getImage());
						SwingUtilities.invokeLater(new Runnable(){
						public void run()
							{
							repaint();
							}
						});
						} 
					catch (Exception e) 
						{
						System.out.println("exception: " + e.getMessage());
						e.printStackTrace();
						}
					
					
					
					
					}
				catch (InterruptedException e){}
				}
			}
		}
	
	public class Area extends javax.swing.JPanel implements MouseListener
		{
		public static final long serialVersionUID=0;
		
		public Area()
			{
			addMouseListener(this);
			}

		
		
		
		public Dimension getPreferredSize()
			{
			Rectangle r=scroll.getViewport().getViewRect();
			int numcol=r.width/iconw;
			int numrow=(int)Math.ceil(obList.size()/(double)numcol);
			int height=numrow*iconh;
			//System.out.println("h "+numrow+" "+height);
			return new Dimension(scroll.getViewport().getWidth(),height);
			}

		protected void paintComponent(Graphics g)
			{
			Rectangle r=scroll.getViewport().getViewRect();
			impostloader.clear();
			
			//Clear
			g.setColor(Color.WHITE);
			g.fillRect(r.x, r.y, r.width, r.height);
			
			//Draw icons
			int numcol=r.width/iconw;
			int lastcol=(r.y+r.height)/iconh+1;
			for(int ar=r.y/iconh;ar<lastcol;ar++)
				for(int ac=0;ac<numcol;ac++)
					{
					int id=numcol*ar+ac;
					drawData(g, ar, ac, id);
					}
			}
		
		int riconw=80;
		int riconh=80;
		
		private void drawData(Graphics g, int row, int col, int id)
			{
			if(id<obList.size())
				{
				String sid=obList.get(id);
				int x=col*iconw;
				int y=row*iconh+shifty;
				
				g.setColor(Color.BLACK);
				
				
				Image im=thumbs.get(sid);
				if(im!=null)
					g.drawImage(im, x+(iconw-riconw)/2, y, im.getWidth(null),im.getHeight(null), null);
				else
					{
					//Not totally fast. could condense into one synch call
					impostloader.add(sid);
					g.drawRect(x+(iconw-riconw)/2, y, riconw, riconh);
					}
				
				
				if(selectedId.contains(id))
					{
					g.setColor(Color.BLUE);
					g.drawRect(x+(iconw-riconw)/2-2, y-2, riconw+4, riconh+4);
					}
				
				int strw=g.getFontMetrics().stringWidth(sid);
				int strh=g.getFontMetrics().getHeight();
				
				int tx=x+(iconw-strw)/2;
				int ty=y+riconh+1+strh;
				
	//			g.setColor(Color.BLUE);
		//		g.fillRect(tx-2, ty-2-strh+2+1, strw+4,strh+4-2);
				
				g.setColor(Color.BLACK);
				g.drawString(sid, tx, ty);
				}
			
			}



		public void mouseClicked(MouseEvent e)
			{
			int col=e.getX()/iconw;
			int row=(e.getY()-shifty)/iconh;
			int dx=e.getX()-(col*iconw+riconw/2);
			//int dy=e.getY()-row*iconh+riconh/2;
			System.out.println(""+dx);
			if(Math.abs(dx)<riconw/2)
				{
				Rectangle r=scroll.getViewport().getViewRect();
				int numcol=r.width/iconw;
				int id=numcol*row+col;
				
				if((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)==0)
					selectedId.clear();
				
				selectedId.add(id);
				repaint();
				}
			
			}

		public void mouseEntered(MouseEvent arg0){}
		public void mouseExited(MouseEvent arg0){}
		public void mousePressed(MouseEvent arg0){}
		public void mouseReleased(MouseEvent arg0){}
		}
	
	
	
	
	public ImservDataPane(ImservConnection conn)
		{
		this.conn=conn;
		setLayout(new GridLayout(1,1));
		
		try
			{
			String[] keys=conn.imserv.getDataKeys();
			for(String k:keys)
				obList.add(k);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		add(scroll);
		impostloader.start();
		}
	}
