/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperStdCelegans;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.frameTime.FrameTime;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.EvMathUtil;
import endrov.util.Tuple;

/**
 * 
 * Check time normalization of model
 * 
 * celegans2008.2 in old format with frametime
 * linear with sulston except AB, P1'. this is a rather expected result.
 * code frametime properly?
 * 
 * @author tbudev3
 *
 */

public class StdCeRenorm
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		EvData data=EvData.loadFile(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost"));
		//EvData data=EvData.loadFile(new File("/Volumes3/TBU_main03/ost4dgood/AnglerUnixCoords.ost"));
//		EvData data=EvData.loadFile(new File("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords_no_AP_radius.ost"));
		NucLineage lin=data.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
		

		
		List<Double> sec=new LinkedList<Double>();
		List<Double> frame=new LinkedList<Double>();
		List<Double> w=new LinkedList<Double>();
		List<String> cell=new LinkedList<String>();
		//Should take frames from model
		
		//min.add(18.0); cell.add("P1'"); //cannot use this one
		//min.add(17.0); cell.add("AB"); //cannot use this one  //APPEARS at 17! we cannot trust our start-time annotation 
		w.add(5.0); sec.add(60*40.0); cell.add("EMS");
		w.add(5.0); sec.add(60*34.0); cell.add("ABa");
		w.add(3.0); sec.add(60*52.0); cell.add("ABar");
		w.add(1.0); sec.add(60*72.0); cell.add("ABara");
		w.add(1.0); sec.add(60*74.0); cell.add("ABpla");		
		w.add(1.0); sec.add(60*104.0); cell.add("ABaraa");
		w.add(1.0); sec.add(60*133.0); cell.add("ABarapa");
		w.add(1.0); sec.add(60*133.0); cell.add("ABplapa");
		w.add(1.0); sec.add(60*172.0); cell.add("ABarappa");
		w.add(1.0); sec.add(60*230.0); cell.add("ABprppapa");
		w.add(1.0); sec.add(60*236.0); cell.add("Eala");
		w.add(1.0); sec.add(60*260.0); cell.add("MSapaap");

		
		for(String n:cell)
			frame.add(lin.nuc.get(n).pos.firstKey().doubleValue());
		
//		for(int i=0;i<min.size();i++)
//			System.out.println(min.get(i)+"\t"+frame.get(i));
		
		System.out.println();

		//min=frame*k+m
		
		//Enforce time of P1'? or rather a child since P1' is hm-hm
		
//		Tuple<Double,Double> km=EvMathUtil.fitLinear1D(min, frame);
		Tuple<Double,Double> km=EvMathUtil.fitWeightedLinear1D(sec, frame, w);
		System.out.println(km);
		double k=km.fst();
		double m=km.snd();
		//frametime=frame*k+m
		//
		
		//Create frametime mapping
		FrameTime ft=new FrameTime();
//		ft.add(new EvDecimal(1000), new EvDecimal(1000*k+m));

		
		//ft.add(new EvDecimal(-m/k), new EvDecimal(0));
		//ft.add(new EvDecimal(1500), new EvDecimal(1500*k+m));
		
		ft.add(new EvDecimal(0),new EvDecimal(0*k+m));
		ft.add(new EvDecimal(1500),new EvDecimal(1500*k+m));

		ft.updateMaps();
	
		System.out.println();
		System.out.println("sec\tframe\tfitsec");
		for(int i=0;i<sec.size();i++)
			System.out.println(
					sec.get(i)+"\t"+
					frame.get(i)+"\t"+
					ft.mapFrame2Time(new EvDecimal(frame.get(i)))+"\t"+
					cell.get(i));
		System.out.println();
		
		data.metaObject.put("sulstontime", ft); //"sulstontime"
		data.saveData();
		
		//TODO in case of celegans2008.2, this mapping should be applied on the frames? or?
		
		System.exit(0);
		
		}

	}
