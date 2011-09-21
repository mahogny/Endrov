/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineage.util;

import java.io.File;

import org.jdom.Document;
import org.jdom.Element;

import endrov.lineage.LineageParticleGrouping;
import endrov.util.EvXmlUtil;

/**
 * given downloaded Cell class from WB, get tissue names. create cell groups
 * @author Johan Henriksson
 *
 */
public class ImportWBcellgroups
	{
	
	public static void main(String[] args)
		{
		
		File rootdir=new File("/media/TBU_extra02/userdata/datasets");
		
		try
			{
			Document doc=EvXmlUtil.readXML(new File(rootdir, "wblineage.xml"));
			Element root=doc.getRootElement();
			LineageParticleGrouping grouping=new LineageParticleGrouping();
			
			for(Object o:root.getChildren())
				{
				Element c=(Element)o;
				String cellName=c.getAttributeValue("value");
				
				for(Object oo:c.getChildren("Cell_group"))
					{
					Element cc=(Element)oo;
					String groupName=cc.getAttributeValue("value");
					grouping.add("Cells:"+groupName, cellName);
					}
				
				
				for(Object oo:c.getChildren("Life_stage"))
					{
					Element cc=(Element)oo;
					String groupName=cc.getAttributeValue("value");
					grouping.add("LifeStage:"+groupName,cellName);
					}
				
				}
			
			grouping.exportXML(new File("/tmp", "cellgroups.cgrp"));
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		}
	

	}
