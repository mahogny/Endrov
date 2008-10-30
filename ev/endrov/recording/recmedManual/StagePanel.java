package endrov.recording.recmedManual;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import endrov.recording.HWStage;

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
	
	
	private HWStage hw;
	private String devName;
	
	public StagePanel(String devName,final HWStage hw)
		{
		this.hw=hw;
		this.devName=devName;
		
		JPanel p=new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		for(int curaxis=0;curaxis<hw.getNumAxis();curaxis++)
			{
			c.gridx=0;
			c.gridy=curaxis;
			c.fill=GridBagConstraints.NONE;
			c.weightx=0;
			JLabel lab=new JLabel(hw.getAxisName()[curaxis]);
			lab.setToolTipText(hw.getDescName()+" - "+hw.getAxisName()[curaxis]);
			p.add(lab,c);
			OneAxisPanel a=new OneAxisPanel();
			c.fill=GridBagConstraints.HORIZONTAL;
			c.weightx=1;
			c.gridx=1;
			p.add(a,c);
			a.axisid=curaxis;
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
	public class OneAxisPanel extends JPanel
		{
		static final long serialVersionUID=0;
		int axisid;
		
		
		
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
		
			int midy=hw.getNumAxis()*oneAxisH()/2;
		
		

			int yOffset=0;

			int xOffset=0;

			g.setColor(Color.WHITE);
			g.fillRect(xOffset, yOffset+asize+2+1, (numIntDigits+numFracDigits)*digitWidth, digitHeight);


			g.setColor(Color.BLACK);
			for(int i=0;i<numIntDigits+numFracDigits;i++)
				{
				int x=xOffset+i*digitWidth;

				drawArrowUp(g, x+arrowTextDisp, yOffset+asize*2);
				drawArrowDown(g, x+arrowTextDisp, yOffset+asize*2+digitHeight+spacing*2);


				g.drawString("0", x, yOffset+asize*2+spacing+digitHeight);

				}



			//			g.drawString(hw.getAxisName()[curaxis], xOffset-20, yOffset+asize*2+theight);

		
		
			}
		
		
		
		}
	}
