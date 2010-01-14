/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package docs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestBufIm extends JPanel
	{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	BufferedImage bi;
	
	public TestBufIm()
		{
		int w=200,h=100;
		//short 32767=white
		int dataType=DataBuffer.TYPE_SHORT;
		SampleModel sm=new ComponentSampleModel(dataType, w, h, 0, 0, new int[]{0}); 
		WritableRaster raster = Raster.createWritableRaster(sm, new Point(0,0));
		ColorModel cm=new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, true, Transparency.OPAQUE, dataType); 
		bi = new BufferedImage(cm, raster, true, new Hashtable<Object, Object>());
		
		Graphics g=bi.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,50,50);
		System.out.println(raster.getSample(0, 0, 0));
		}
	
	protected void paintComponent(Graphics g)
		{
		g.drawImage(bi,0,0,null);
		
		
		}

	public static void main(String[] arg)
		{
		//float, double, signed short and int works
		JFrame f=new JFrame();
		f.setLayout(new GridLayout(1,1));
		f.add(new TestBufIm());
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		}
	
	
	}
