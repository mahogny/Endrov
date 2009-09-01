package util2.integrateExpression;

import java.io.File;

import org.jdom.Document;
import org.jdom.Element;

import endrov.nuc.CellGrouping;
import endrov.util.EvXmlUtil;

/**
 * given downloaded Cell class from WB, get tissue names. create cell groups
 * @author Johan Henriksson
 *
 */
public class WblinTissue
	{
	
	public static void main(String[] args)
		{
		
		File rootdir=new File("/Volumes/TBU_main03/userdata");
		
		try
			{
			Document doc=EvXmlUtil.readXML(new File(rootdir, "wblineage.xml"));
			Element root=doc.getRootElement();
			CellGrouping grouping=new CellGrouping();
			
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
			
			grouping.exportXML(new File(rootdir, "cellgroups.cgrp"));
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		}
	

	}
