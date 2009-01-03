package endrov.recording.recmetManual;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import endrov.basicWindow.icon.BasicIcon;
import endrov.hardware.HardwarePath;
import endrov.recording.HWStage;
import endrov.util.EvSwingTools;
import endrov.util.JImageButton;
import endrov.util.JImageToggleButton;

/**
 * Widget to move microscope stage 
 * @author Johan Henriksson
 *
 */
public class StagePanel implements ActionListener 
	{
	static final long serialVersionUID=0;
	
	private static int asize=3;
	private static final int arrowTextDisp=4;
	private static int digitHeight=10; 
	private static int digitWidth=8;
	private static final int spacing=2;
	private static final int numIntDigits=10;
	private static final int numFracDigits=3;
	
	static
	{
	BufferedImage im=new BufferedImage(1,1,BufferedImage.TYPE_BYTE_GRAY);
	FontMetrics fm=im.getGraphics().getFontMetrics();
	Rectangle2D r=fm.getStringBounds("234567890", im.getGraphics());
	digitWidth=(int)(r.getWidth()/9);
//	digitHeight=(int)r.getHeight();
	}
	public static ImageIcon iconStageAllDown=new ImageIcon(StagePanel.class.getResource("iconStageAllDown.png"));
	
	
	private HWStage hw;
	//private String devName;
	
	public StagePanel(HardwarePath devName,final HWStage hw, ManualExtension.Hook hook)
		{
		this.hw=hw;
	//	this.devName=devName;
		
//		JPanel p=new JPanel(new GridLayout(hw.getNumAxis(),1));
		for(int curaxis=0;curaxis<hw.getNumAxis();curaxis++)
			{
			JLabel lab=new JLabel("Axis "+hw.getAxisName()[curaxis]+" [um] ");
			lab.setToolTipText(devName+" ("+hw.getDescName()+") - "+hw.getAxisName()[curaxis]);
			
			OneAxisPanel a=new OneAxisPanel();
			a.axisid=curaxis;
			
			
			JToggleButton toggleStageDown=new JImageToggleButton(iconStageAllDown,"Move stage all the way down");
			JImageButton bController=new JImageButton(BasicIcon.iconController,"Gamepad mapping");
			JPanel pb=new JPanel(new GridLayout(1,2));
			pb.add(toggleStageDown);
			pb.add(bController);

			
			hook.add1(EvSwingTools.borderLR(null, lab, EvSwingTools.borderLR(null, a, pb)));
//			hook.add1(EvSwingTools.borderLR(lab, a, pb));
//			p.add(EvSwingTools.borderLR(lab, a, pb));
			}
//		setLayout(new GridLayout(1,1));
//		add(p);
	
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
		int tickDist=8;
		int yOffset=0;
		int xOffset=0;

		Integer holdDigit=null;
		int mouseLastTickY=0;

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
			else if(numid<numIntDigits+1)
				return numIntDigits-numid;
				//return numid;
			else if(numid<numIntDigits+numFracDigits+1 && numid>numIntDigits)
				return numIntDigits-numid+1;
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
				axis[axisid]-=Math.pow(10, aDown);
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

				if(ticks!=0)
					{
					double[] axis=hw.getStagePos();
					axis[axisid]-=ticks*Math.pow(10, holdDigit);
					hw.setStagePos(axis);
					repaint();
					}
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
			String s=nf.format(pos);
			if(!s.startsWith("-"))
				s="+"+s;
			return s;
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
			return new Dimension(digitWidth*(numIntDigits+numFracDigits+2)+20,oneAxisH());
			}
		protected void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			String poss=getPosString();			

			//g.setColor(Color.red);			g.fillRect(0,0,getWidth(),getHeight());
			
			//g.setColor(Color.);
			//g.fillRect(xOffset, yOffset+asize*2, (numIntDigits+numFracDigits+2)*digitWidth, digitHeight+spacing*2);


			g.setColor(Color.BLACK);
			for(int i=0;i<poss.length();i++)
				{
				int x=xOffset+i*digitWidth;

				if(i!=numIntDigits+1 && i!=0)
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
