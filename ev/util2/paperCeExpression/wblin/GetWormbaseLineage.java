/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.wblin;

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

import endrov.core.EndrovCore;
import endrov.core.EndrovUtil;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.typeLineage.Lineage;
import endrov.util.io.EvFileUtil;

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
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();

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
  		for(Element child:EndrovUtil.castIterableElement(element.getChildren()))
  			{
  			String cellname=child.getAttributeValue("value");
  			WBCell cell=new WBCell();
  			cells.put(cellname,cell);
    		for(Element child2:EndrovUtil.castIterableElement(child.getChildren()))
    			{
    			if(child2.getName().equals("Embryo_division_time"))
    				{
    				cell.btime=child2.getAttribute("value").getIntValue();
//    				System.out.println(cellname+" "+cell.btime);
    				}
    			else if(child2.getName().equals("Lineage"))
    				{
    				for(Element child3:EndrovUtil.castIterableElement(child2.getChildren()))
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

   
    
    	
    
    //Delete stupid cells. typos
    lin.removeParticle("hyp7_P5.p");
    lin.removeParticle("hyp7-V6R.pppa");
    lin.removeParticle("AMLR");
    lin.removeParticle("Z1.pppaaa"); 
    lin.removeParticle("IDLDR");
    lin.removeParticle("M3");
    lin.removeParticle("hyp");
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
    

    //Delete all gender-specific
    /*
    TreeSet<String> toDelete=new TreeSet<String>(); 
    for(String cellname:hasSexDimorph)
    	if(lin.particle.containsKey(cellname))
	    	toDelete.addAll(lin.getRecursiveChildNames(cellname));
  	System.out.println("to delete (sexdimorph) " + toDelete);
  	for(String name:toDelete)
  		lin.removeParticle(name);
  	*/
  	

    //Delete all male cells
    TreeSet<String> toDelete=new TreeSet<String>(); 
    for(String malecell:maleorigin.keySet())
	    if(lin.particle.containsKey(malecell))
	    	toDelete.addAll(lin.getRecursiveChildNames(malecell));
  	System.out.println("to delete (male) " + toDelete);
  	for(String name:toDelete)
  		lin.removeParticle(name);
    
  	try
			{
			StringBuffer sb=new StringBuffer();
			for(String name:toDelete)
				sb.append(name+"\n");
			EvFileUtil.writeFile(new File("/Volumes/TBU_main06/wblineage/deleted.txt"), sb.toString());
			}
		catch (IOException e2)
			{
			e2.printStackTrace();
			}
  	
  	
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
    
    
    /**
     * 
     * 
    
    to delete (sexdimorph) [ABarpapaaap, ABplaaaaaap, ABplpaapapp, ABprpaapapp, B, B.a, B.al, B.ala, B.alaa, B.alaalda, B.alaarda, B.alap, B.alapa, B.alapaa, B.alapaad, B.alapaav, B.alapap, B.alapapa, B.alapapp, B.alapp, B.alappd, B.alappv, B.alp, B.alpa, B.alpaa, B.alpap, B.alpapa, B.alpp, B.ar, B.ara, B.araa, B.araalda, B.araarda, B.arap, B.arapa, B.arapaa, B.arapaad, B.arapaav, B.arapap, B.arapapa, B.arapapp, B.arapp, B.arappd, B.arappv, B.arp, B.arpa, B.arpaa, B.arpap, B.arpapa, B.arpp, B.p, B.pa, B.paa, B.pap, B.pp, B.ppa, B.ppaa, B.ppp, B.pppa, B.pppp, B.ppppp, B_alpha, B_alpha.l, B_alpha.lv, B_alpha.r, B_alpha.rv, B_beta, B_beta.l, B_beta.ld, B_beta.r, B_beta.rd, B_delta, B_delta.l, B_delta.r, B_gamma, B_gamma.a, B_gamma.al, B_gamma.ald, B_gamma.alv, B_gamma.ar, B_gamma.ard, B_gamma.arv, B_gamma.p, CA1, CA2, CA3, CA4, CA5, CA6, CA7, CA8, CA9, CANL, CANR, CEMDL, CEMDR, CEMVL, CEMVR, CP0, CP1, CP2, CP3, CP4, CP5, CP6, CP7, CP8, CP9, DVE, DVF, DX1, DX2, DX3, DX4, EF1, EF2, EF3, EF4, F, F.l, F.ld, F.lv, F.lvd, F.lvda, F.lvdp, F.lvv, F.r, F.rd, F.rv, F.rvd, F.rvda, F.rvdp, F.rvv, H1L.apa, H1L.appa, H1L.p, H2R.ap, H2R.pa, H2R.ppa, H2R.pppa, H2R.pppp, HOA, HOB, HOsh, HOso, HSNL, HSNR, M.dlp, M.dlpa, M.dlpp, M.drp, M.drpa, M.drpp, M.vlpa, M.vlpaa, M.vlpaaa, M.vlpaaaa, M.vlpaaaaa, M.vlpaaaap, M.vlpaaap, M.vlpaaapa, M.vlpaaapp, M.vlpaap, M.vlpaapa, M.vlpaapaa, M.vlpaapap, M.vlpaapp, M.vlpaappa, M.vlpaappp, M.vlpap, M.vrpaa, M.vrpaaa, M.vrpaaaa, M.vrpaaaaa, M.vrpaaaap, M.vrpaaap, M.vrpaaapa, M.vrpaaapp, M.vrpaap, M.vrpaapa, M.vrpaapaa, M.vrpaapap, M.vrpaapp, M.vrpaappa, M.vrpaappp, P10.aap, P10.p, P10.pa, P10.paa, P10.paaa, P10.paap, P10.pap, P10.papa, P10.papp, P10.pp, P10.ppa, P10.ppp, P10.pppp, P11.aap, P11.p, P11.pa, P11.paa, P11.paaa, P11.pap, P11.papa, P11.pp, P11.ppa, P11.ppaa, P11.ppap, P11.ppp, P12.aap, P2.aap, P3.aa, P3.aaa, P3.aap, P3.p, P3.pa, P3.pp, P4.aa, P4.aaa, P4.aap, P4.p, P4.pa, P4.pp, P5.aap, P5.p, P5.pa, P5.paa, P5.paaa, P5.paap, P5.pap, P5.papa, P5.papp, P5.pp, P5.ppa, P5.ppal, P5.ppar, P5.ppp, P6.aap, P6.p, P6.pa, P6.paa, P6.paal, P6.paar, P6.pap, P6.papl, P6.papr, P6.pp, P6.ppa, P6.ppal, P6.ppar, P6.ppp, P6.pppl, P6.pppr, P7.aap, P7.p, P7.pa, P7.paa, P7.pap, P7.papl, P7.papr, P7.pp, P7.ppa, P7.ppaa, P7.ppap, P7.ppp, P7.pppa, P7.pppp, P8.aap, P8.p, P8.pa, P8.pp, P9.aap, PCAL, PCAR, PCBL, PCBR, PCCL, PCCR, PChL, PChR, PCshL, PCshR, PCsoL, PCsoR, PDA, PDC, PGA, PVV, PVX, PVY, PVZ, R1AL, R1AR, R1BL, R1BR, R1stL, R1stR, R2AL, R2AR, R2BL, R2BR, R2stL, R2stR, R3AL, R3AR, R3BL, R3BR, R3stL, R3stR, R4AL, R4AR, R4BL, R4BR, R4stL, R4stR, R5AL, R5AR, R5BL, R5BR, R5stL, R5stR, R6AL, R6AR, R6BL, R6BR, R6stL, R6stR, R7AL, R7AR, R7BL, R7BR, R7stL, R7stR, R8AL, R8AR, R8BL, R8BR, R8stL, R8stR, R9AL, R9AR, R9BL, R9BR, R9stL, R9stR, SM1L, SM1L.a, SM1L.aa, SM1L.aaa, SM1L.aap, SM1L.ap, SM1L.apa, SM1L.app, SM1L.p, SM1L.pa, SM1L.paa, SM1L.pap, SM1L.pp, SM1L.ppa, SM1L.ppp, SM1R, SM1R.a, SM1R.aa, SM1R.aaa, SM1R.aap, SM1R.ap, SM1R.apa, SM1R.app, SM1R.p, SM1R.pa, SM1R.paa, SM1R.pap, SM1R.pp, SM1R.ppa, SM1R.ppp, SM2L, SM2L.a, SM2L.aa, SM2L.aaa, SM2L.aap, SM2L.ap, SM2L.apa, SM2L.app, SM2L.p, SM2L.pa, SM2L.paa, SM2L.pap, SM2R, SM2R.a, SM2R.aa, SM2R.aaa, SM2R.aap, SM2R.ap, SM2R.apa, SM2R.app, SM2R.p, SM2R.pa, SM2R.paa, SM2R.pap, SM2R.pp, SM3L, SM3L.a, SM3L.aa, SM3L.aaa, SM3L.aap, SM3L.ap, SM3L.apa, SM3L.app, SM3L.p, SM3L.pa, SM3L.pp, SM3R, SM3R.a, SM3R.aa, SM3R.aaa, SM3R.aap, SM3R.ap, SM3R.apa, SM3R.app, SM3R.p, SM3R.pa, SM3R.pp, SMBDL, SMBDR, SMBVL, SMBVR, SMDDL, SMDDR, SMDVL, SMDVR, SPCL, SPCR, SPDL, SPDR, SPVL, SPVR, SPshDL, SPshDR, SPshVL, SPshVR, SPso1L, SPso1R, SPso2L, SPso2R, SPso3L, SPso3R, SPso4L, SPso4R, TL.aa, TL.apaa, TL.apap, TL.apapa, TL.apapp, TL.apappa, TL.apappaa, TL.apappaaa, TL.apappaap, TL.apappap, TL.apappapa, TL.apappapp, TL.apappp, TL.appa, TL.appaa, TL.appaaa, TL.appaaaa, TL.appaaaaa, TL.appaaaap, TL.appaaap, TL.appaaapa, TL.appaaapp, TL.appaap, TL.appap, TL.appapa, TL.appapaa, TL.appapaaa, TL.appapaap, TL.appapap, TL.appapapa, TL.appapapp, TL.appapp, TR.apap, TR.apapa, TR.apapp, TR.apappa, TR.apappaa, TR.apappaaa, TR.apappaap, TR.apappap, TR.apappapa, TR.apappapp, TR.apappp, TR.appa, TR.appaa, TR.appaaa, TR.appaaaa, TR.appaaaaa, TR.appaaaap, TR.appaaap, TR.appaaapa, TR.appaaapp, TR.appaap, TR.appap, TR.appapa, TR.appapaa, TR.appapaaa, TR.appapaap, TR.appapap, TR.appapapa, TR.appapapp, TR.appapp, U, U.l, U.la, U.laa, U.lap, U.lp, U.r, U.ra, U.raa, U.rap, U.rp, V5L.ppp, V5L.pppa, V5L.pppaa, V5L.pppap, V5L.pppapa, V5L.pppapp, V5L.pppp, V5L.ppppa, V5L.ppppp, V5L.pppppa, V5L.pppppaa, V5L.pppppaap, V5L.pppppap, V5L.pppppp, V5R.ppp, V5R.pppa, V5R.pppaa, V5R.pppap, V5R.pppapa, V5R.pppapp, V5R.pppp, V5R.ppppa, V5R.ppppp, V5R.pppppa, V5R.pppppaa, V5R.pppppaap, V5R.pppppap, V5R.pppppp, V6L.pap, V6L.papa, V6L.papaa, V6L.papap, V6L.papapa, V6L.papapaa, V6L.papapaaa, V6L.papapaap, V6L.papapap, V6L.papapapp, V6L.papapp, V6L.papp, V6L.pappa, V6L.pappp, V6L.papppa, V6L.papppaa, V6L.papppaaa, V6L.papppaap, V6L.papppap, V6L.papppapa, V6L.papppapp, V6L.papppp, V6L.ppp, V6L.pppa, V6L.pppaa, V6L.pppap, V6L.pppapa, V6L.pppapaa, V6L.pppapaaa, V6L.pppapaap, V6L.pppapap, V6L.pppapapa, V6L.pppapapp, V6L.pppapp, V6L.pppp, V6L.ppppa, V6L.ppppaa, V6L.ppppaaa, V6L.ppppaaaa, V6L.ppppaaap, V6L.ppppaap, V6L.ppppaapa, V6L.ppppaapp, V6L.ppppap, V6L.ppppp, V6L.pppppa, V6L.pppppaa, V6L.pppppaaa, V6L.pppppaap, V6L.pppppap, V6L.pppppapa, V6L.pppppapp, V6L.pppppp, V6R.pap, V6R.papa, V6R.papaa, V6R.papap, V6R.papapa, V6R.papapaa, V6R.papapaaa, V6R.papapaap, V6R.papapap, V6R.papapapa, V6R.papapapp, V6R.papapp, V6R.papp, V6R.pappa, V6R.pappp, V6R.papppa, V6R.papppaa, V6R.papppaaa, V6R.papppaap, V6R.papppap, V6R.papppapa, V6R.papppapp, V6R.papppp, V6R.ppp, V6R.pppa, V6R.pppaa, V6R.pppap, V6R.pppapa, V6R.pppapaa, V6R.pppapaaa, V6R.pppapaap, V6R.pppapap, V6R.pppapapa, V6R.pppapapp, V6R.pppapp, V6R.pppp, V6R.ppppa, V6R.ppppaa, V6R.ppppaaa, V6R.ppppaaaa, V6R.ppppaaap, V6R.ppppaap, V6R.ppppaapa, V6R.ppppaapp, V6R.ppppap, V6R.ppppp, V6R.pppppa, V6R.pppppaa, V6R.pppppaaa, V6R.pppppaap, V6R.pppppap, V6R.pppppapa, V6R.pppppapp, V6R.pppppp, VA3, VA4, VB4, VB5, VC1, VC2, VC3, VC4, VC5, VC6, Y, Y', Y.a, Y.p, Y.pl, Y.pla, Y.plaa, Y.plap, Y.plp, Y.plpa, Y.plpp, Y.plppd, Y.plppv, Y.pr, Y.pra, Y.praa, Y.prap, Y.prp, Y.prpa, Y.prpp, Y.prppd, Y.prppv, Z1.a, Z1.paa, Z1.papaaaa, Z1.papaaad, Z1.papaaap, Z1.papaaav, Z1.papaapa, Z1.papaapaa, Z1.papaapap, Z1.papaapd, Z1.papaapp, Z1.papaappa, Z1.papaappaa, Z1.papaappap, Z1.papaappp, Z1.papaapv, Z1.ppaaaaa, Z1.ppaaaap, Z1.pppaaaa, Z1.pppaaap, Z1.pppaap, Z4.aaa, Z4.aaaa, Z4.aaaaa, Z4.aaaaaa, Z4.aaaaaaa, Z4.aaaaaap, Z4.aaaaap, Z4.aaaaapa, Z4.aaaaapaa, Z4.aaaaapap, Z4.aaaaapp, Z4.aaaaappa, Z4.aaaaappaa, Z4.aaaaappap, Z4.aaaaappp, Z4.aaaap, Z4.aaaapa, Z4.aaaapd, Z4.aaaapp, Z4.aaaapv, Z4.aaap, Z4.aaapa, Z4.aaapad, Z4.aaapav, Z4.aaapp, Z4.aaappd, Z4.aaappv, Z4.aapaaaa, Z4.aapaaap, Z4.aapaad, Z4.aapaapaa, Z4.aapaapap, Z4.aapaappaa, Z4.aapaappap, Z4.aapaappp, Z4.aapaav, Z4.aapapaa, Z4.aapapap, Z4.aapapd, Z4.aapappa, Z4.aapappp, Z4.aapapv, Z4.aappaaa, Z4.aappaap, Z4.aappapa, Z4.aappapp, Z4.aapppaa, Z4.aapppap, Z4.aappppa, Z4.aappppp, Z4.apaaaaa, Z4.apaaaad, Z4.apaaaap, Z4.apaaaav, Z4.apaaapd, Z4.apaaapv, Z4.apaapaaa, Z4.apaapaap, Z4.apaapapa, Z4.apaapapp, Z4.apaappaa, Z4.apaappap, Z4.apaapppa, Z4.apaapppp, Z4.apapaaaa, Z4.apapaaap, Z4.apapaapa, Z4.apapaapp, Z4.apapapaa, Z4.apapapap, Z4.apapappa, Z4.apapappp, Z4.apappad, Z4.apappav, Z4.apapppd, Z4.apapppv, Z4.appaaaa, Z4.appaaap, Z4.appaapaa, Z4.appaapapa, Z4.appaapapp, Z4.appaappaa, Z4.appaappap, Z4.appaapppa, Z4.appaapppp, Z4.appapaa, Z4.appapap, Z4.appappa, Z4.appappp, Z4.apppaap, Z4.p, cc_DL, cc_DR, cc_male_D, gon_herm_dtc_A, gon_herm_dtc_B, gon_male_link, hyp7-H1L.apa, hyp7-H1L.appa, hyp7-H1L.p, hyp7-H2R.ap, hyp7-H2R.pa, hyp7-H2R.ppa, hyp7-H2R.pppa, hyp7-H2R.pppp, hyp7-P10.p, hyp7-P11.p, hyp7-P3.p, hyp7-P3.pa, hyp7-P3.pp, hyp7-P4.p, hyp7-P4.pa, hyp7-P4.pp, hyp7-P5.p, hyp7-P6.p, hyp7-P7.p, hyp7-P8.p, hyp7-P8.pa, hyp7-P8.pp, hyp7-TL.aa, hyp7-TL.apaa, hyp7-TR.apap, hyp7-TR.apapa, hyp7-TR.apappp, hyp7-TR.appaap, hyp7-TR.appapp, hyp7-V5L.pppa, hyp7-V5L.pppaa, hyp7-V5L.pppapa, hyp7-V5R.pppa, hyp7-V5R.pppaa, hyp7-V5R.pppapa, hyp7-V5R.ppppa, hyp7-V6L.papa, hyp7-V6L.papaa, hyp7-V6L.pappa, hyp7-V6L.pppa, hyp7-V6L.pppaa, hyp7-V6L.ppppa, hyp7-V6L.ppppap, hyp7-V6R.papa, hyp7-V6R.papaa, hyp7-V6R.pappa, hyp7-V6R.pppaa, hyp7-V6R.ppppa, hyp7-V6R.ppppap, hyp7_TL.apap, hyp7_TL.apapa, hyp7_TL.apappp, hyp7_TL.appaap, hyp7_TL.appapp, proct-B.alapaad, proct-B.alapaav, proct-B.alapapa, proct-B.alapapp, proct-B.alappv, proct-B.arapaad, proct-B.arapaav, proct-B.arapap, proct-B.arapapa, proct-B.arapapp, proct-B.arappv, proct-B.paa, proct-B.pap, proct-B.pppa, proct-B.ppppp, proct-B_alpha.rv, proct-B_alphalv, proct-B_delta.l, proct-B_delta.r, proct-B_gamma.ald, proct-B_gamma.alv, proct-B_gamma.ard, proct-B_gamma.arv, proct-F.lvv, proct-F.rvv, rectum_B, rectum_F, rectum_U, se_herm-TL.appa, se_herm-TR.appa, seam-V5L.pppapp, seam-V5L.ppppp, seam-V5R.pppapp, seam-V5R.ppppp, seam-V6L.pappp, seam-V6L.ppppp, seam-V6R.pappp, seam-V6R.ppppp, set-V5L.pppppp, set-V5R.pppppp, set-V6L.papapp, set-V6L.papppp, set-V6L.pppapp, set-V6L.pppppp, set-V6R.papapp, set-V6R.papppp, set-V6R.pppapp, set-V6R.pppppp, um1-M.vlpaaaap, um1-M.vrpaaaap, um2-M.vlpaaaaa, um2-M.vrpaaaaa, vm1-M.vlpaaapp, vm1-M.vrpaaapp, vm2-M.vlpaaapa, vm2-M.vrpaaapa]

    
    
    
    
     * 
     */
    
    
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
