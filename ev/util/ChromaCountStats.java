/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;

import java.io.File;
import java.util.LinkedList;
import java.util.*;

import endrov.chromacountkj.ChromaCountKJ;
import endrov.data.EvData;
import endrov.ev.EV;

/**
 * ChromaCount KJ stats
 * @author Johan Henriksson
 */
public class ChromaCountStats
	{
	public static void incInt(Map<Integer,Integer> map, int i)
		{
		Integer c=map.get(i);
		if(c==null)
			c=0;
		c++;
		map.put(i,c);
		}
	
	
	public static void main(String[] arg)
		{
		EV.loadPlugins();

		List<EvData> datas=new LinkedList<EvData>();
		
		TreeMap<Integer, int[]> countForFrame=new TreeMap<Integer, int[]>();
		
		int[] num=new int[4];
		for(File file:(new File("/home/tbudev3/jepp/ny/1034")).listFiles())
			if(file.isDirectory() && file.getName().endsWith(".ost"))
				{
				System.out.println(file);
				EvData data=EvData.loadFile(file);
				datas.add(data);
				
				Map<Integer,Integer> groupCount=new HashMap<Integer, Integer>();
				for(ChromaCountKJ c:data.getObjects(ChromaCountKJ.class))
					incInt(groupCount,c.group);
				
				for(int v:groupCount.values())
					num[v]++;
				
				
				}

		countForFrame.put(0, num);
		
		for(int i=0;i<4;i++)
			System.out.println(""+i+" > "+num[i]);
		
		System.exit(0);
		
		}
	}

