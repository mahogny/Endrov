/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;

import java.io.File;
import java.util.HashSet;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.particle.Lineage;
import endrov.util.EvDecimal;
import endrov.util.EvXmlUtil;

/**
 * Convert WB lineage to OST
 * 
 * wget "http://dev.wormbase.org/db/misc/xml?name=*;class=Cell"
 * 
 * @author Johan Henriksson
 *
 */
public class WBlineageToOST
	{
	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		
		Lineage lin=new Lineage();
		
		
		try
			{
			Document doc=EvXmlUtil.readXML(new File("/Volumes/TBU_main03/userdata/wblineage.xml"));
			Element root=doc.getRootElement();

			for(Object rootc:root.getChildren())
				{
				Element eCell=(Element)rootc;
				String cellName=eCell.getAttributeValue("value");

				Lineage.Particle nuc=lin.new Particle();
				
				
				//Parent and children
				Element eLineage=eCell.getChild("Lineage");
				if(eLineage==null)
					{
					System.out.println("Null lineage for "+cellName+" , skipping");
					continue;
					}
				
				
				

				
				for(Object linc:eLineage.getChildren())
					{
					Element e=(Element)linc;
					if(e.getName().equals("Daughter"))
						nuc.child.add(e.getAttributeValue("value"));
					}
				Element eParent=eLineage.getChild("Parent");
				if(eParent!=null)
					nuc.parent=eParent.getAttributeValue("value");
				
				/*
				boolean isPrimed=false;
				for(String s:new String[]{"P2'","P3'","P4'","Z2"})
					if(nuc.child.contains(s))
						isPrimed=true;
				if(isPrimed)
					cellName+="'";*/
					
				
				Element eDiv=eCell.getChild("Embryo_division_time");
				if(eDiv!=null)
					{
					String sDivTimeMin=eDiv.getAttributeValue("value");
					if(sDivTimeMin.equals(""))
						sDivTimeMin="0"; //AceDB strangety
					try
						{
						//Note: this is divtime from parent
						nuc.overrideStart=new EvDecimal(sDivTimeMin).multiply(60);
						}
					catch (Exception e)
						{
						System.out.println(">>>>>>>>>>>>>>bad endtime "+cellName+"    ("+sDivTimeMin+")");
						}
					}
				//else //Common
					//System.out.println("no div for "+cellName);
					
				for(Object cellc:eCell.getChildren())
					{
					Element eCellC=(Element)cellc;
					if(eCellC.getName().equals("Cell_group"))
						{
						String val=eCellC.getAttributeValue("value");
						if(val.equals("cells_that_die"))
							;
						}
					
					}
				
				
/*
			  <Cell_group class="Cell_group" value="cells_that_die" timestamp="2000-08-09_10:35:29_sylvi" />
		    <Cell_group class="Cell_group" value="embryonic_death" timestamp="2000-08-09_11:26:24_sylvi" />
		    <Cell_group class="Cell_group" value="AB lineage" timestamp="2001-04-19_09:46:10_sylvi" />
		    <Life_stage class="Life_stage" value="embryo" timestamp="2001-05-16_10:30:21_sylvi" />
		    <Life_stage class="Life_stage" value="gastrulating embryo" timestamp="2001-05-16_10:30:21_sylvi" />
		    <Life_stage class="Life_stage" value="late cleavage stage embryo" timestamp="2001-05-16_10:30:21_sylvi" />
		    <Life_stage class="Life_stage" value="proliferating embryo" timestamp="2001-05-16_10:30:21_sylvi" />
*/
				
				
				//TODO nuc. start. fate?
				
				if(nuc.parent!=null && nuc.parent.equals(""))
					System.out.println("sdöjdföjlsdfjölfdsjökfsdjökldgsöljksdg");
				//Element eData=eCell.getChild("Data");
				
				if(nuc.parent==null && nuc.child.isEmpty())
					{
					
					}
				else
					{
					if(nuc.parent==null)
						System.out.println("no parent for "+cellName);
					lin.particle.put(cellName, nuc);
					}
				}
			
			
			
			for(Map.Entry<String, Lineage.Particle> e:lin.particle.entrySet())
				{
				Lineage.Particle nuc=e.getValue();
				for(String cname:nuc.child)
					{
					//System.out.println(e.getKey()+":"+cname);
					lin.particle.get(cname).parent=e.getKey();
					}
				if(nuc.parent!=null)
					lin.particle.get(nuc.parent).child.add(e.getKey());
				}
			
		
			HashSet<String> toRemove=new HashSet<String>();

			for(Map.Entry<String, Lineage.Particle> e:lin.particle.entrySet())
				{
				Lineage.Particle nuc=e.getValue();
				if(nuc.parent==null && nuc.child.isEmpty())
					System.out.println("No parent/child for "+e.getKey());
				if(!lin.particle.containsKey(nuc.parent) && nuc.child.isEmpty())
					System.out.println("!!!!missing parent");
				
				//temp: do the easy ones
				if(nuc.overrideStart==null)
					toRemove.add(e.getKey());
				}
			
			for(String nucName:toRemove)
				{
				Lineage.Particle nuc=lin.particle.get(nucName);
				if(nuc.parent!=null)
					lin.particle.get(nuc.parent).child.remove(nucName);
				}
			lin.particle.keySet().removeAll(toRemove);
			
			
			/*
				{
				NucLineage.Nuc nuc=lin.nuc.get("Z4.appaapapp");
				System.out.println(">>>"+nuc.parent+"   "+nuc.child);
				}
				*/
			
			EvData data=new EvData();
			data.metaObject.put("lin", lin);
			data.saveDataAs(new File("/Volumes/TBU_main02/ostxml/wblineage.ost"));
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.exit(0);
		}
	}
