/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.wblin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


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
		
    
    Set<String> deleted=new TreeSet<String>(); 
    try
			{
			BufferedReader br=new BufferedReader(new FileReader(new File("/Volumes/TBU_main06/wblineage/deleted.txt")));
			String line=null;
			while((line=br.readLine())!=null)
				deleted.add(line.toLowerCase());
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
    
    
    
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
    
    
    	{

      Map<String,String> lowercasePartname=new HashMap<String,String>();
      for(String name:lin.particle.keySet())
      	lowercasePartname.put(name.toLowerCase(), name);
      
	    //Check which parts matches up
	    int linkpartcount=0;
	    int deletepartcount=0;
	    for(String objpart:moddata.metaObject.keySet())
	    	{
	    	if(lowercasePartname.containsKey(objpart.toLowerCase()))
	    		{
	    		linkpartcount++;
	    		}
	    	else if(deleted.contains(objpart.toLowerCase()))
	    		{
	    		System.out.println("has deleted object "+objpart);
	    		deletepartcount++;
	    		}
	    	else
	    		System.out.println("Missing nuc for "+objpart);
	    	}
	    System.out.println("mesh->lin: linked "+linkpartcount+" + "+deletepartcount+"   vs  "+moddata.metaObject.size());
    	}
    
    	{

      Map<String,String> lowercasePartname=new HashMap<String,String>();
      for(String name:moddata.metaObject.keySet())
      	lowercasePartname.put(name.toLowerCase(), name);
      
	    //Check which parts matches up
	    int linkpartcount=0;
	    int total=0;
	    for(String name:new TreeSet<String>(lin.getLeafs()))//lin.particle.keySet())
	    	{
	    	if(!lowercasePartname.containsKey(name.toLowerCase()))
	    		System.out.println("Missing mesh for "+name);
	    	else
	    		linkpartcount++;
	    	total++;
	    	}
	    System.out.println("lin->mesh: linked "+linkpartcount+"  vs  "+total);
    	}
    
    System.exit(0);
		}
	
	
	
	
	
	}
