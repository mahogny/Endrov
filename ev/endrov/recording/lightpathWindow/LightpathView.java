/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.lightpathWindow;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class LightpathView extends JPanel
	{
	private static final long serialVersionUID = 1L;

	
	
	@Override
	protected void paintComponent(Graphics g)
		{
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		
		//For speed, possible to render this to an image and cache it.
		
		int mainX=50;

		
		Color cup=Color.green;
		Color cright=new Color(100,100,255);
		Color cdown=Color.red;
		
		Color cbelow=Color.yellow;
		
		int focusDist=20;
		
		int toEyeY=30;
		int splitEyeCamY=toEyeY+50;
		int beamSplitY=splitEyeCamY+50;
		int startObjY=beamSplitY+50;
		int endObjY=startObjY+35;
		int stageY=endObjY+focusDist;
		int condenserY=stageY+focusDist;
		int lowerFilterY=condenserY+50;
		int lowerLightSourceY=lowerFilterY+50;
		
		int toCamX=mainX+100;
		int upperFilterX=mainX+100;
		int upperLightX=upperFilterX+100;
		
		

		LightpathUtil.drawLightBeamVertical(g,cbelow, mainX, lowerFilterY, lowerLightSourceY);

		LightpathUtil.drawFilterVertical(g, Color.BLACK, 0.5, mainX, lowerFilterY);
		LightpathUtil.drawLightBeamVertical(g,cbelow, mainX, condenserY, lowerFilterY);
		LightpathUtil.drawLenseVertical(g, mainX, condenserY);
		LightpathUtil.drawBeamWaistLower(g, cbelow, mainX, stageY, condenserY);
		LightpathUtil.drawStage(g, mainX, stageY);
		LightpathUtil.drawBeamWaistUpper(g, cdown, mainX, endObjY, stageY);
		LightpathUtil.drawObjective(g, mainX, startObjY);
		LightpathUtil.drawLightSource(g, mainX, lowerLightSourceY);

		LightpathUtil.drawLightBeamHorizonal(g,cup, mainX, toCamX, splitEyeCamY);		
		LightpathUtil.drawToCam(g, toCamX, splitEyeCamY);

		LightpathUtil.drawLightBeamVertical(g,cup, mainX, toEyeY, beamSplitY);
		LightpathUtil.drawLightBeamVertical(g,cdown, mainX, beamSplitY, startObjY);

		LightpathUtil.drawLightBeamHorizonal(g,cright, upperFilterX, upperLightX, beamSplitY);		
		LightpathUtil.drawFilterHorizontal(g, Color.BLACK, 0.5, upperFilterX, beamSplitY);
		LightpathUtil.drawLightBeamHorizonal(g,cright, mainX, upperFilterX, beamSplitY);		
		LightpathUtil.drawLightSource(g, upperLightX, beamSplitY);

		

		
		LightpathUtil.drawToEye(g, mainX, toEyeY);
		LightpathUtil.drawBeamsplit(g, mainX, splitEyeCamY, cup, cup, cup,true,false);
		LightpathUtil.drawBeamsplit(g, mainX, beamSplitY, cup, cdown, cright,true,true);
		
		
		
		}
	
	
	
	
	
	public static void main(String[] args)
		{
		JFrame f=new JFrame();
		f.add(new LightpathView());
		f.setSize(500, 500);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}
