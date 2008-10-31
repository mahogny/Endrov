package endrov.recording.recmedManual;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import endrov.basicWindow.icon.BasicIcon;
import endrov.recording.HWStage;
import endrov.util.JImageButton;
import endrov.util.JImageToggleButton;

public class StagePanel extends JPanel implements ActionListener 
	{
	static final long serialVersionUID=0;
	
	private static int asize=3;
	private static final int arrowTextDisp=4;
	private static final int digitHeight=10; 
	private static final int digitWidth=8;
	private static final int spacing=2;
	private static final int numIntDigits=10;
	private static final int numFracDigits=3;
	
	public static ImageIcon iconStageAllDown=new ImageIcon(StagePanel.class.getResource("iconStageAllDown.png"));
	
	
	private HWStage hw;
	//private String devName;
	
	public StagePanel(String devName,final HWStage hw)
		{
		this.hw=hw;
	//	this.devName=devName;
		
		JPanel p=new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		for(int curaxis=0;curaxis<hw.getNumAxis();curaxis++)
			{
			c.gridx=0;
			c.gridy=curaxis;
			c.fill=GridBagConstraints.NONE;
			c.weightx=0;
			JLabel lab=new JLabel("Axis "+hw.getAxisName()[curaxis]+" ");
			lab.setToolTipText(devName+" ("+hw.getDescName()+") - "+hw.getAxisName()[curaxis]);
			p.add(lab,c);
			
			OneAxisPanel a=new OneAxisPanel();
			a.axisid=curaxis;
			c.fill=GridBagConstraints.HORIZONTAL;
			c.gridx=1;
			c.weightx=1;
			p.add(new JLabel(""),c);
			
			c.gridx=2;
			c.weightx=0;
			c.fill=0;
			p.add(a,c);
			
			c.gridx=3;
			JImageToggleButton toggleStageDown=new JImageToggleButton(iconStageAllDown);
			p.add(toggleStageDown,c);

			c.gridx=4;
			JImageButton bController=new JImageButton(BasicIcon.iconController);
			p.add(bController,c);

			
			}
		setLayout(new GridLayout(1,1));
		add(p);
	
		}
	

	public void actionPerformed(ActionEvent e)
		{
	
		}

	
	
	
	/******************************************************************
	 * Inner class: one axis
	 ******************************************************************/
	public class OneAxisPanel extends JPanel implements MouseListener,MouseMotionListener
		{
		static final long serialVersionUID=0;
		int axisid;
		int yOffset=0;
		int xOffset=0;

		Integer holdDigit=null;
		int mouseLastTickY=0;
		int tickDist=8;

		public OneAxisPanel()
			{
			addMouseListener(this);
			addMouseMotionListener(this);
			}
		
		/** Get position as x in 10^x */
		public Integer hitArrowUp(int x, int y)
			{
			if(y>0 && y<asize*2)
				return hitArrowAny(x);
			else
				return null;
			}
		/** Get position as x in 10^x */
		public Integer hitArrowDown(int x, int y)
			{
			if(y>asize*2+digitHeight+spacing*2)
				return hitArrowAny(x);
			else
				return null;
			}
		/** Get position as x in 10^x */
		public Integer hitDigit(int x, int y)
			{
			if(y<asize*2+digitHeight+spacing*2 && y>asize*2)
				return hitArrowAny(x);
			else
				return null;
			}
		//If updating string and setting it again, better to just give position
		private Integer hitArrowAny(int x)
			{
			int numid=x/digitWidth;
			if(numid<0)
				return null;
			else if(numid<numIntDigits)
				return numIntDigits-numid;
				//return numid;
			else if(numid<numIntDigits+numFracDigits+1 && numid>numIntDigits)
				return numIntDigits+1-numid;
				//return numid;
			else
				return null;
			}
		
		
		
		
		public void mouseClicked(MouseEvent e)
			{
			Integer aUp=hitArrowUp(e.getX(), e.getY());
			Integer aDown=hitArrowDown(e.getX(), e.getY());
			Integer digit=hitDigit(e.getX(), e.getY());
			
			if(aUp!=null)
				{
				double[] axis=hw.getStagePos();
				axis[axisid]+=Math.pow(10, aUp);
				hw.setStagePos(axis);
				repaint(); //TODO all observers
				}
			else if(aDown!=null)
				{
				double[] axis=hw.getStagePos();
				axis[axisid]+=Math.pow(10, aDown);
				hw.setStagePos(axis);
				repaint(); //TODO all observers
				}
			else if(digit!=null && e.getClickCount()==2)
				{
				String newPos=JOptionPane.showInputDialog(this, "Enter new position", getPosString());
				if(newPos!=null)
					{
					double[] axis=hw.getStagePos();
					axis[axisid]=Double.parseDouble(newPos);
					hw.setStagePos(axis);
					System.out.println("set "+hw.getStagePos()[axisid]);
					repaint();
					}
				}
			}
		public void mouseEntered(MouseEvent e)
			{
			}
		public void mouseExited(MouseEvent e)
			{
			}
		public void mousePressed(MouseEvent e)
			{
			holdDigit=hitDigit(e.getX(), e.getY());
			mouseLastTickY=e.getY();
			}
		public void mouseReleased(MouseEvent e)
			{
			holdDigit=null;
			}
		public void mouseDragged(MouseEvent e)
			{
			int dy=e.getY()-mouseLastTickY;
			if(Math.abs(dy)>=tickDist)
				{
				int ticks=dy/tickDist;
				
				System.out.println(""+holdDigit+" "+ticks+" "+mouseLastTickY);
				
				mouseLastTickY+=ticks*tickDist;
				}
			}
		public void mouseMoved(MouseEvent e)
			{
			}
		
		private String getPosString()
			{
			double pos=hw.getStagePos()[axisid];
			NumberFormat nf=NumberFormat.getInstance();
			nf.setGroupingUsed(false);
			nf.setMinimumFractionDigits(numFracDigits);
			nf.setMaximumFractionDigits(numFracDigits);
			nf.setMinimumIntegerDigits(numIntDigits);
			nf.setMaximumIntegerDigits(numIntDigits);
			return nf.format(pos);
			}

		private void drawArrowUp(Graphics g, int x, int y )
			{
			g.fillPolygon(new int[]{x-asize,x+asize,x}, new int[]{y,y,y-asize*2}, 3);
			}
		private void drawArrowDown(Graphics g, int x, int y )
			{
			g.fillPolygon(new int[]{x-asize,x+asize,x}, new int[]{y,y,y+asize*2}, 3);
			}
		
		private int oneAxisH()
			{
			return (asize*4+digitHeight+spacing*2+2);
			}
		
		public Dimension getPreferredSize()
			{
			return new Dimension(digitWidth*(numIntDigits+numFracDigits)+20,oneAxisH());
			}
		protected void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			String poss=getPosString();			
			
			g.setColor(Color.WHITE);
			g.fillRect(xOffset, yOffset+asize+2+1, (numIntDigits+numFracDigits+1)*digitWidth, digitHeight);


			g.setColor(Color.BLACK);
			for(int i=0;i<numIntDigits+numFracDigits+1;i++)
				{
				int x=xOffset+i*digitWidth;

				if(i!=numIntDigits)
					{
					drawArrowUp(g, x+arrowTextDisp, yOffset+asize*2);
					drawArrowDown(g, x+arrowTextDisp, yOffset+asize*2+digitHeight+spacing*2);
					}


				g.drawString(""+poss.charAt(i), x, yOffset+asize*2+spacing+digitHeight);

				}



			//			g.drawString(hw.getAxisName()[curaxis], xOffset-20, yOffset+asize*2+theight);

		
		
			}
		
		
		
		}
	}
