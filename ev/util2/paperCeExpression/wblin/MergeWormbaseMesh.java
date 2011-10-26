/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.wblin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.lineage.Lineage;
import endrov.mesh3d.Mesh3D;
import endrov.util.EvDecimal;

public class MergeWormbaseMesh
	{
	
	
	public static void main(String[] arg)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();

		EvData dataLin=EvData.loadFile(new File("/Volumes/TBU_main06/wblineage/lin.ost"));
		
		Lineage lin=(Lineage)dataLin.getChild("lin");
		
    
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

      Map<String,String> lowercasePartName=new HashMap<String,String>();
      for(String name:lin.particle.keySet())
      	lowercasePartName.put(name.toLowerCase(), name);
      
	    //Check which parts matches up
	    int linkpartcount=0;
	    int deletepartcount=0;
	    for(String objName:moddata.metaObject.keySet())
	    	{
	    	String lowerobjpart=objName.toLowerCase();
	    	
	    	if(lowercasePartName.containsKey(lowerobjpart))
	    		{
	    		linkpartcount++;
	    		
	    		String pname=lowercasePartName.get(lowerobjpart);
	  	    lin.particle.get(pname).meshs.put(EvDecimal.ZERO, (Mesh3D)moddata.metaObject.get(objName));
	  	    System.out.println("match "+objName+"  "+pname);
	    		}
	    	else if(deleted.contains(lowerobjpart))
	    		{
	    		System.out.println("has deleted object "+objName);
	    		deletepartcount++;
	    		}
	    	else
	    		System.out.println("Missing nuc for "+objName);
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
    
    	
    	
    try
			{
			dataLin.saveDataAs(new File("/Volumes/TBU_main06/wblineage/mod.ost"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
    	
    	
    	
    System.exit(0);
		}
	
	
	
	
	
	}
