package evplugin.imagesetImserv.service;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import evplugin.data.EvData;
import evplugin.imagesetImserv.ImservImageset;


/**
 * Panel with all objects from imserv
 * 
 * @author Johan Henriksson
 */
public class DataIconPane extends JPanel implements Scrollable
	{
	public static final long serialVersionUID=0;
	
	private List<DataIconPaneListener> listeners=new Vector<DataIconPaneListener>();
	
	public interface DataIconPaneListener
		{
		public void dataIconSelection();
		public void dataIconActivate(DataIF s);
		}
	
	public void addIconPaneListener(DataIconPaneListener list)
		{
		listeners.add(list);
		}
	
	
	
	private Area a=new Area();
	private JScrollPane scroll=new JScrollPane(a);
	public Set<String> selectedId=new HashSet<String>();
	private ImservConnection conn;
	private ImPostLoader impostloader=new ImPostLoader();
	private Map<String, Image> thumbs=Collections.synchronizedMap(new HashMap<String, Image>());
	public List<String> obList=new ArrayList<String>();
	
	private int iconw=100;
	private int iconh=120;
	private int riconw=80;
	private int riconh=80;
	private int shifty=5;
	private String filter="";
	
	
	
	/**
	 * Thread to continuously download images
	 * *************************************************************************
	 */
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
						BufferedImage image=SendFile.getImageFromBytes(data.getThumb());
						thumbs.put(sid,image);
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
	
	/**
	 * Custom drawing area
	 * *************************************************************************
	 */
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
			System.out.println("ret h "+height);
			return new Dimension(scroll.getViewport().getWidth(),height);
			}

//		public Dimension getMaximumSize(){return getPreferredSize();}
//		public Dimension getMinimumSize(){return getPreferredSize();}

		
		


		protected void paintComponent(Graphics g)
			{
			Rectangle r=scroll.getViewport().getViewRect();
			impostloader.clear();
			
			//Clear
			g.setColor(Color.WHITE);
			g.fillRect(r.x, r.y, r.width, r.height);
			
			
			//Draw icons
			g.setFont(Font.decode("Dialog PLAIN "+9));
			int numcol=r.width/iconw;
			int lastcol=(r.y+r.height)/iconh+1;
			for(int ar=r.y/iconh;ar<lastcol;ar++)
				for(int ac=0;ac<numcol;ac++)
					drawData(g, ar, ac, numcol*ar+ac);
			}
		
		
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
					if(!thumbs.containsKey(sid))
						impostloader.add(sid);
					int x1=x+(iconw-riconw)/2;
					g.drawRect(x1, y, riconw, riconh);
					g.drawLine(x1, y, x1+riconw, y+riconh);
					g.drawLine(x1+riconw, y, x1, y+riconh);
					}
				
				
				if(selectedId.contains(obList.get(id)))
					{
					g.setColor(Color.RED);
					g.drawRect(x+(iconw-riconw)/2-2, y-2, riconw+3, riconh+3);
					}
				
				int strw=g.getFontMetrics().stringWidth(sid);
				String sid2=null;
				if(strw>iconw)
					{
					for(int i=sid.length()-1;i>=0;i--)
						{
						String ns=sid.substring(0,i);
						int nstrw=g.getFontMetrics().stringWidth(ns);
						if(nstrw<=iconw)
							{
							strw=nstrw;
							sid2=sid.substring(i);
							sid=ns;
							break;
							}
						}
					}
				
				int strh=g.getFontMetrics().getHeight();
				
				int tx=x+(iconw-strw)/2;
				int ty=y+riconh+1+strh;
				
	//			g.setColor(Color.BLUE);
		//		g.fillRect(tx-2, ty-2-strh+2+1, strw+4,strh+4-2);
				
				g.setColor(Color.BLACK);
				g.drawString(sid, tx, ty);
				if(sid2!=null)
					{
					strw=g.getFontMetrics().stringWidth(sid2);
					tx=x+(iconw-strw)/2;
					g.drawString(sid2, tx, ty+strh);
					}
				}
			
			}



		public void mouseClicked(MouseEvent e)
			{
			//if(e.getClickCount()==1)
				//{
				int col=e.getX()/iconw;
				int row=(e.getY()-shifty)/iconh;
				int dx=e.getX()-(col*iconw+riconw/2);
				//int dy=e.getY()-row*iconh+riconh/2;
				if(Math.abs(dx)<riconw/2)
					{
					Rectangle r=scroll.getViewport().getViewRect();
					int numcol=r.width/iconw;
					int id=numcol*row+col;
					String sid=id<obList.size() && id>=0 ? obList.get(id) : null;
					
					if((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)==0)
						selectedId.clear();
					if(sid!=null)
						selectedId.add(sid);
					
					for(DataIconPaneListener listener:listeners)
						listener.dataIconSelection();
					
					repaint();
					}
				//}
			//else 
				if(e.getClickCount()==2)
				{
				System.out.println("dbl");
				
				try
					{
					String openName=selectedId.iterator().next();
					DataIF data=conn.imserv.getData(openName);
					
					for(DataIconPaneListener listener:listeners)
						listener.dataIconActivate(data);
					
					//wrong place, temp
					ImservImageset rec=new ImservImageset(data);
					EvData.registerOpenedData(rec);
					}
				catch (Exception e1)
					{
					e1.printStackTrace();
					}
				
				
				
				}
			}

		public void mouseEntered(MouseEvent arg0){}
		public void mouseExited(MouseEvent arg0){}
		public void mousePressed(MouseEvent arg0){}
		public void mouseReleased(MouseEvent arg0){}
		}
	
	
	
	
	public DataIconPane(ImservConnection conn)
		{
		this.conn=conn;
		setLayout(new GridLayout(1,1));
		setFilter("");
		
		add(scroll);
		impostloader.start();
		}
	
	public void setConn(ImservConnection conn)
		{
		this.conn=conn;
		update();
		}
	
	
	public void setFilter(String s)
		{
		filter=s;
		update();
		}

	public void update()
		{
		try
			{
			obList.clear();
			if(conn!=null)
				{
				String[] keys=conn.imserv.getDataKeys(filter);
				if(keys!=null)
					for(String k:keys)
						obList.add(k);
				}
			revalidate();
			scroll.revalidate();
			repaint();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}


	
	
	
	////////// Scrollable
	
	public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
		{return getScrollableUnitIncrement(visibleRect, orientation, direction);}
	public boolean getScrollableTracksViewportHeight(){return false;}
	public boolean getScrollableTracksViewportWidth(){return true;}
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
		{
		if(orientation==SwingConstants.VERTICAL)
			return iconw;
		else
			return iconh;
		}
	
	
	}
