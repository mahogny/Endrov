package util2.integrateExpression.compare;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import util2.integrateExpression.ExpUtil;
import util2.integrateExpression.FindAnnotatedStrains;


import endrov.flowColocalization.ColocCoefficients;
import endrov.springGraph.Graph;
import endrov.springGraph.GraphPanel;
import endrov.springGraph.SimpleGraphRenderer;
import endrov.springGraph.SpringGraphLayout;
import endrov.util.Tuple;

public class FindClosest
	{

	
	

	public static void main(String[] args)
		{
		
		Set<File> datas=FindAnnotatedStrains.getAnnotated();

		final Map<Tuple<File,File>, ColocCoefficients> comparison;
		comparison=CompareAll.loadCache(datas, CompareAll.cachedValuesFileT);
		
		File searchFor=new File("/Volumes/TBU_main06/ost4dgood/TB1200_070803.ost");

		
		List<Tuple<File,File>> toshow=new LinkedList<Tuple<File,File>>();
		
		for(Tuple<File,File> pair:comparison.keySet())
			if(pair.fst().equals(searchFor))
				toshow.add(pair);

		Collections.sort(toshow, new Comparator<Tuple<File,File>>()
			{
			public int compare(Tuple<File,File> o1, Tuple<File,File> o2)
				{
				ColocCoefficients c1=comparison.get(o1);
				ColocCoefficients c2=comparison.get(o2);
				return -Double.compare(Math.abs(c1.getCovXY()), Math.abs(c2.getCovXY()));
				};
			});
		
		for(Tuple<File,File> pair:toshow)
			{
			System.out.println(pair.fst().getName()+"\t"+pair.snd().getName()+"\t"+comparison.get(pair).getCovXY());
			}
		
		}
	}
