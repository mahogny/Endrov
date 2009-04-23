package endrov.histEqualizer;

import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import endrov.util.EvArrayUtil;

public class HistogramEqWidget extends JPanel
	{
	
	/**
	 * From color, to color.
	 * Map is never inverted
	 */
	
	private SortedMap<Double, Double> points=new TreeMap<Double, Double>();
	
	
	public HistogramEqWidget()
		{
		//Maybe separate mapping from the widget? can do later
		
		points.put(0.0,0.0);
		points.put(256.0, 256.0);
		
		
		}
	
	/*
	private double map(double d)
		{
		EvArrayUtil.
		points.h
		}*/
	
	
	
	public static void main(String[] args)
		{
		JFrame f=new JFrame();
		f.setSize(100, 30);
		
		HistogramEqWidget h=new HistogramEqWidget();
		f.add(h);
		
		f.setVisible(true);
		
		
		
		
		
		// TODO Auto-generated method stub

		}

	}
