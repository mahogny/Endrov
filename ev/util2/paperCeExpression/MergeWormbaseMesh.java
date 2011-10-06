/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.lineage.Lineage;

public class MergeWormbaseMesh
	{
	
	
	public static void main(String[] arg)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		EvData data=EvData.loadFile(new File("/Volumes/TBU_main06/wblineage/lin.ost"));
		
		Lineage lin=(Lineage)data.getChild("lin");
		
    
    
    
    Map<String,String> lowercasePartname=new HashMap<String,String>();
    for(String name:lin.particle.keySet())
    	lowercasePartname.put(name.toLowerCase(), name);
    	
    
    
    EvData moddata=EvData.loadFile(new File("/Volumes/TBU_main06/customer/ceblendermodel/eek_Scene.obj"));
    
    
    
    
    
    /*
    //Stupid renaming...
    for(String oldname:new LinkedList<String>(moddata.metaObject.keySet()))
    	{
    	EvObject ob=moddata.metaObject.get(oldname);
    	if(oldname.startsWith("mu_bod_"))
    		{
    		String newname="m"
    		+oldname.substring("mu_bod_VR".length())+
    		oldname.substring("mu_bod_".length(),"mu_bod_VR".length());
    		
    		moddata.metaObject.remove(oldname);
    		moddata.metaObject.put(newname, ob);
    		}
    		
    	
    	
    	
    	}
    	*/
    
    
    //
    //TODO create hyp7-cell, daugther of all hyp7-* cells
    
    
    
    
    //Check which parts matches up
    int linkpartcount=0;
    for(String objpart:moddata.metaObject.keySet())
    	{
    	if(!lowercasePartname.containsKey(objpart.toLowerCase()))
    		System.out.println("Missing nuc for "+objpart);
    	else
    		linkpartcount++;
    	}
    System.out.println("linked "+linkpartcount+"  vs  "+moddata.metaObject.size());
    
    
    
    System.exit(0);
		}
	}
