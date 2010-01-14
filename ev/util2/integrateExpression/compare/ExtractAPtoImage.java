/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.integrateExpression.compare;

import java.io.File;
import java.io.IOException;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowColocalization.ColocCoefficients;
import endrov.imageset.Imageset;
import endrov.util.Tuple;

public class ExtractAPtoImage
	{
	public static void printArr(double[][] arr)
		{
		int len=arr[0].length;
		
		System.out.print("[");
		for(double[] a:arr)
			{
			if(a==null)
				{
				System.out.print("0");
				for(int i=1;i<len;i++)
					System.out.print(","+0);
				}
			else
				{
				System.out.print(a[0]);
				for(int i=1;i<a.length;i++)
					System.out.print(","+a[i]);
				}
			System.out.print(";");
			}
		System.out.println("];");
		
		
		
		}
	
	
	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		
		String expName="exp";
		int ap=1;
		
		for(String s:args)
			System.out.println(">"+s);
		
		EvData dataA=EvData.loadFile(new File(args[0]));
		if(dataA==null)
			{
			System.out.println("Cannot load "+args[0]);
			System.exit(1);
			}
		
		Imageset imsetA = dataA.getObjects(Imageset.class).get(0);
		String chanNameA=imsetA.getChild("GFP")!=null ? "GFP" : "RFP";
		double[][] imtA=CompareAll.apToArray(dataA, "AP"+ap+"-"+chanNameA, expName, CompareAll.coordLineageFor(dataA));
		printArr(imtA);
		
		if(args.length<2)
			System.exit(0);
		
		System.out.println();
		EvData dataB=EvData.loadFile(new File(args[1]));
		if(dataB==null)
			{
			System.out.println("Cannot load "+args[1]);
			System.exit(1);
			}
		
		Imageset imsetB = dataB.getObjects(Imageset.class).get(0);
		String chanNameB=imsetB.getChild("GFP")!=null ? "GFP" : "RFP";
		double[][] imtB=CompareAll.apToArray(dataB, "AP"+ap+"-"+chanNameB, expName, CompareAll.coordLineageFor(dataB));
		printArr(imtB);
		
		
		System.out.println();
		ColocCoefficients coeffT=CompareAll.colocAP(imtA, imtB);
		System.out.println("coeffT "+coeffT.n+" "+coeffT.sumX+" "+coeffT.sumXX+" "+coeffT.sumY);
		System.out.println("pearsonT "+ coeffT.getPearson());

		
		
		
		System.exit(0);
		
		}
	}
