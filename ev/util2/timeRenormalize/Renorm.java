package util2.timeRenormalize;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.frameTime.FrameTime;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.EvMathUtil;
import endrov.util.Tuple;

public class Renorm
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		EvData data=EvData.loadFile(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost"));
		//EvData data=EvData.loadFile(new File("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords.ost"));
		NucLineage lin=data.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
		

		
		List<Double> min=new LinkedList<Double>();
		List<Double> frame=new LinkedList<Double>();
		List<String> cell=new LinkedList<String>();
		//Should take frames from model
		
		//min.add(18.0); cell.add("P1'"); //cannot use this one
		min.add(34.0); cell.add("ABa");
		min.add(52.0); cell.add("ABar");
		min.add(72.0); cell.add("ABara");
		min.add(74.0); cell.add("ABpla");		
		min.add(104.0); cell.add("ABaraa");
		min.add(133.0); cell.add("ABarapa");
		min.add(133.0); cell.add("ABplapa");
		min.add(172.0); cell.add("ABarappa");
		min.add(230.0); cell.add("ABprppapa");
		min.add(236.0); cell.add("Eala");
		min.add(260.0); cell.add("MSapaap");

		
		for(String n:cell)
			frame.add(lin.nuc.get(n).pos.firstKey().doubleValue());
		
		for(int i=0;i<min.size();i++)
			System.out.println(min.get(i)+"\t"+frame.get(i));
		
		System.out.println();

		//min=frame*k+m
		
		//Enforce time of P1'? or rather a child since P1' is hm-hm
		
		Tuple<Double,Double> km=EvMathUtil.fitLinear1D(min, frame);
		System.out.println(km);
		double k=km.fst();
		double m=km.snd();
		//frametime=frame*k+m
		//
		
		//Create frametime mapping
		FrameTime ft=new FrameTime();
		ft.add(new EvDecimal(0*k+m), new EvDecimal(0));
		ft.add(new EvDecimal(1000*k+m), new EvDecimal(1000));
		data.metaObject.put("cetime", ft);
		data.saveData();
		
		//TODO in case of celegans2008.2, this mapping should be applied on the frames
		
		System.exit(0);
		
		}

	}
