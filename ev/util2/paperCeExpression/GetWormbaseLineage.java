/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.lineage.Lineage;

public class GetWormbaseLineage
	{
	public static class WBCell
		{
		int btime;
		Integer duration=null;
		List<String> parent=new LinkedList<String>();
		List<String> child=new LinkedList<String>();
		}
	
	
	public static void main(String[] arg)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		Map<String,WBCell> cells=new HashMap<String, WBCell>();
		Map<String,String> maleorigin=new HashMap<String, String>(); //Cell -> parent cell
		Map<String,String> eqorigin=new HashMap<String, String>(); //Cell -> parent cell
		
		Set<String> hasSexDimorph=new HashSet<String>(); 
		
		//Read XML
    Document document = null;
    try 
    	{
  		FileInputStream fileInputStream = new FileInputStream("/Volumes/TBU_main06/wblineage/Cell.xml");
  		SAXBuilder saxBuilder = new SAXBuilder();
  		document = saxBuilder.build(fileInputStream);
  		Element element = document.getRootElement();
  		for(Element child:EV.castIterableElement(element.getChildren()))
  			{
  			String cellname=child.getAttributeValue("value");
  			WBCell cell=new WBCell();
  			cells.put(cellname,cell);
    		for(Element child2:EV.castIterableElement(child.getChildren()))
    			{
    			if(child2.getName().equals("Embryo_division_time"))
    				{
    				cell.btime=child2.getAttribute("value").getIntValue();
//    				System.out.println(cellname+" "+cell.btime);
    				}
    			else if(child2.getName().equals("Lineage"))
    				{
    				for(Element child3:EV.castIterableElement(child2.getChildren()))
        			{
        			if(child3.getName().equals("Daughter"))
        				cell.child.add(child3.getAttributeValue("value"));
        			else if(child3.getName().equals("Parent"))
        				cell.parent.add(child3.getAttributeValue("value"));
        			}
    				
    				Element childEqOrigin=child2.getChild("Equivalence_origin");
  					if(childEqOrigin!=null)
  						{
    					String origin=childEqOrigin.getAttributeValue("value");//childMaleOrigin.getText();
    					if(!origin.equals(cellname))
    						{
	    					System.out.println(origin+" ----eq----> "+cellname);
	    					eqorigin.put(cellname, origin);
    						}
  						}
  					
    				Element childSex=child2.getChild("Sex_dimorphism");
    				if(childSex!=null)
    					{
    					
    					hasSexDimorph.add(cellname);
    					
    					Element childMaleOrigin=childSex.getChild("Male_origin");
    					if(childMaleOrigin!=null)
    						{
	    					String origin=childMaleOrigin.getAttributeValue("value");//childMaleOrigin.getText();
	    					if(origin!=null)
	    						{
	    						if(origin.equals(cellname))
	    							{
	    							//System.out.println("cyclic dep "+cellname);
//	    							System.exit(1);
	    							}
	    						else
	    							{
			    					//System.out.println(origin);
			    					maleorigin.put(cellname, origin);
	    							}
	    						}
    						}
    					}
    				
    				}
    			}
  			}
    	} 
    catch (Exception e) 
    	{
    	e.printStackTrace();
    	} 
    
    
    
    
    //Derive data
    /*
    for(Map.Entry<String, WBCell> e:cells.entrySet())
    	{
    	WBCell cell=e.getValue();
    	if(!cell.children.isEmpty())
    		{
    		String firstChild=cell.children.iterator().next();
    		
    		cell.duration=cells.get(firstChild).btime-cell.btime;
				System.out.println(e.getKey()+" "+cell.duration);

    		}
    	}
    */
    
    System.out.println(cells.keySet());

    
    
    //Create lineage
    Lineage lin=new Lineage();
    for(Map.Entry<String, WBCell> e:cells.entrySet())
    	{
    	String name=e.getKey();
    	WBCell cell=e.getValue();
      Lineage.Particle p=lin.getCreateParticle(name);

      //Connect with children
      p.child.addAll(cell.child);
      for(String cname:cell.child)
      	{
      	Lineage.Particle cp=lin.getCreateParticle(cname);
      	cp.parents.add(name);
      	}
      
      //Connect with parents
      p.parents.addAll(cell.parent);
      for(String pname:cell.parent)
      	{
      	Lineage.Particle pp=lin.getCreateParticle(pname);
      	pp.child.add(name);
      	}
      
    	}
    

    //Connect male cells
    for(String name:maleorigin.keySet())
    	{
    	String parentname=maleorigin.get(name);
    	lin.getCreateParticle(parentname).child.add(name);
    	lin.getCreateParticle(name).parents.add(parentname);
    	}
    
    //Connect with equivalence
    for(String name:eqorigin.keySet())
    	{
    	String parentname=eqorigin.get(name);
    	lin.getCreateParticle(parentname).child.add(name);
    	lin.getCreateParticle(name).parents.add(parentname);
    	}
    	
    
    
    System.out.println();
    System.out.println();

   
    
    //Delete male cells, version 2, do not use
    /*
    for(String malecell:maleorigin.keySet())
	    if(lin.particle.containsKey(malecell))
	    	{
	    	System.out.println("to delete (male) " +lin.getRecursiveChildNames(malecell));
	    	for(String name:lin.getRecursiveChildNames(malecell))
	    		lin.removeParticle(name);
	    	}
    */
    	
    
    //Delete stupid cells. typos
    lin.removeParticle("hyp7_P5.p");
    lin.removeParticle("hyp7-V6R.pppa");
    lin.removeParticle("AMLR");
    lin.removeParticle("Z1.pppaaa"); 
    lin.removeParticle("IDLDR");
    lin.removeParticle("M3");
    lin.removeParticle("proct-B_gamma.arv");

    //Has been split in * L/R
    lin.removeParticle("PDE");
    lin.removeParticle("PLN");
    lin.removeParticle("PVD");
    lin.removeParticle("Z1.aap");
    
    
    //lost in space
    ///?? R8A, R8B, R8st, 9*
    //SPsh3VL/R
    //V6L: V6R exists, did they not annotate this symm? except, is there symm? it got children. see xml
    
    //TODO Z1.pa - incomplete???
    
    //Missing cell
    lin.getCreateParticle("Z1.paa");
    lin.createParentChild("Z1.pa", "Z1.paa");
    
    //A whole bunch of cells do not exist
    for(String name:new LinkedList<String>(lin.particle.keySet()))
    	if(name.startsWith("Z1.paa") && !name.equals("Z1.paa"))
      	lin.removeParticle(name);
    for(String name:new LinkedList<String>(lin.particle.keySet()))
    	if(name.startsWith("Z1.a") && !name.equals("Z1.a"))
      	lin.removeParticle(name);
    for(String name:new LinkedList<String>(lin.particle.keySet()))
    	if(name.startsWith("Z4.p") && !name.equals("Z4.p"))
      	lin.removeParticle(name);

    //Missing hierarchy - Z4.aaa TODO
    

    //Delete gender-specific
    TreeSet<String> toDelete=new TreeSet<String>(); 
    for(String cellname:hasSexDimorph)
    	if(lin.particle.containsKey(cellname))
	    	toDelete.addAll(lin.getRecursiveChildNames(cellname));
  	System.out.println("to delete (sexdimorph) " + toDelete);
  	for(String name:toDelete)
  		lin.removeParticle(name);
  		
    
    //Create common cells
    commonCell(lin, "hyp7", "hyp7.");
    commonCell(lin, "hyp7", "hyp7-");
    commonCell(lin, "hyp7", "hyp7_");

    commonCell(lin, "seam", "seam-");
    commonCell(lin, "se_herm", "se_herm-");
    commonCell(lin, "proct", "proct-");
    commonCell(lin, "proct", "proct_");

    commonCell(lin, "hyp_hook", "hyp_hook.");

    commonCell(lin, "um1", "um1-");
    commonCell(lin, "um2", "um2-");
    commonCell(lin, "vm1", "vm1-");
    commonCell(lin, "vm2", "vm2-");

    


    //System.out.println("wheee "+lin.getCreateParticle("hyp7-P5.p").child);
    //System.out.println("wheee2 "+lin.getCreateParticle("hyp7-P5.p").parents);
    
    
    /*
    Lineage.Particle phyp7=lin.getCreateParticle("hyp7");
    for(String name:new LinkedList<String>(lin.particle.keySet()))
    	{
    	if(name.startsWith("hyp7_") || name.startsWith("hyp7.") || name.startsWith("hyp7-"))
    		{
      	lin.particle.remove(name);
      	
    		name=name.substring("hyp7.".length());
    		phyp7.parents.add(name);
    		lin.getCreateParticle(name).child.add("hyp7");
    		}
    	}
    	*/
    
    
    //Add timings
    /*
    for(String name:lin.particle.keySet())
    	{
    	Lineage.Particle p=lin.particle.get(name);
    	p.overrideStart=new EvDecimal(70);
    	p.overrideEnd=new EvDecimal(70);
    	}
    */
    

    System.out.println();
    System.out.println();
    System.out.println();
    System.out.println();
    System.out.println("equivalences "+eqorigin);
    //System.out.println(maleorigin);
    
    
    System.out.println("num final cells: "+lin.getLeafs().size());
    
    Map<String,String> lowercasePartname=new HashMap<String,String>();
    for(String name:lin.particle.keySet())
    	lowercasePartname.put(name.toLowerCase(), name);
    	
    
    
    
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
    
    
    
    try
			{
			EvData data=new EvData();
			data.metaObject.put("lin", lin);
			
			data.saveDataAs(new File("/Volumes/TBU_main06/wblineage/lin.ost"));
			}
		catch (IOException e1)
			{
			e1.printStackTrace();
			}
    
    System.exit(0);
		}
	
	
	
	
	
	public static void commonCell(Lineage lin, String commonname, String prefix)
		{
    for(String name:new LinkedList<String>(lin.particle.keySet()))
    	{
    	if(name.startsWith(prefix))
    		{
      	lin.removeParticle(name);
    		name=name.substring(prefix.length());

    		if(lin.particle.containsKey(name)) //If it does not exist then do not create connection
    			{
          Lineage.Particle pCommon=lin.getCreateParticle(commonname);
      		pCommon.parents.add(name);
      		lin.getCreateParticle(name).child.add(commonname);
    			}
    		
    		}
    	}
		}
	
	
	
	
	}
