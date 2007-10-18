package evplugin.frameTime;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JMenu;

import evplugin.data.*;
import evplugin.ev.*;

import org.jdom.*;
//import org.jdom.output.*;
//import javax.swing.*;
//this will replace FrameTime later on

public class FrameTime extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="frametime";
	
	private static final String metaElement="ft";
	
	public static void initPlugin() {}
	static
		{
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				FrameTime meta=new FrameTime();
				
				for(Object oframetime:e.getChildren())
					{
					Element e2=(Element)oframetime;
					int frame=Integer.parseInt(e2.getAttribute("frame").getValue());
					double frametime=Double.parseDouble(e2.getAttribute("frame").getValue());
					meta.list.add(new Pair(frame,frametime));
					}
				
				return meta;
				}
			});
		}

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	//A sorted map would be better but...
	
	public final Vector<Pair> list=new Vector<Pair>();

	public String getMetaTypeDesc()
		{
		return metaType;
		}
	
	/**
	 * Save down data
	 */
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		for(Pair p:list)
			{
			Element f=new Element(metaElement);
			f.setAttribute("frame", ""+p.frame);
			f.setAttribute("time",  ""+p.frametime);
			e.addContent(f);
			}
		}
	

	public void buildMetamenu(JMenu menu)
		{
		}

	
	/**
	 * Add new frame/frametime to this object
	 */
	public void add(int frame, double frametime)
		{
		list.add(new Pair(frame, frametime));
		}
	
	

	/**
	 * Take a frame, figure out time. Assumes the list is sorted.
	 * @param frame Frame
	 * @return Time, minimum or maximum time if out of range, 0 otherwise
	 */
	public double interpolateTime(int frame)
		{
		if(list.size()==0)
			return 0;
		else if(list.size()==1)
			return list.get(0).frametime;
		else
			{
			int i=0;
			while(i<list.size() && frame>list.get(i).frame)
				i++;
			if(i==0)
				return list.get(0).frametime; //Out of range below
			else if(i==list.size())
				return list.get(list.size()-1).frametime; //Out of range above
			else
				{
				//In range, interpolate
				int frame1=list.get(i-1).frame;
				int frame2=list.get(i).frame;
				double time1=list.get(i-1).frametime;
				double time2=list.get(i).frametime;
				double x=(frame-frame1)/(double)(frame2-frame1);
				return (1-x)*time1+x*time2;
				}
			}
		}
	
	
	/**
	 * Save down in a pure text file
	 * @param filename Name of file
	 */
	public void storeTextFile(String filename)
		{
		try
			{
			BufferedWriter fp = new BufferedWriter(new FileWriter(filename));
			for(Pair p:list)
				fp.write(""+p.frame+" "+p.frametime+"\n");
			fp.close();
			}
		catch (IOException e)
			{
			Log.printError("Error writing file",e);
			}
		}
		
	}
