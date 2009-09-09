package endrov.nuc;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;

import endrov.util.EvFileUtil;
import endrov.util.EvXmlUtil;

/**
 * Grouping of cells e.g. cells belonging to a certain tissue
 * @author Johan Henriksson
 *
 */
public class CellGrouping
	{
	public static String normalExportXMLEnding=".cgrp";
	
	/**
	 * group -> cells
	 */
	public TreeMap<String,TreeSet<String>> groups=new TreeMap<String, TreeSet<String>>();
	
	
	
	public void add(String group, String cell)
		{
		TreeSet<String> sets=groups.get(group);
		if(sets==null)
			groups.put(group,sets=new TreeSet<String>());
		sets.add(cell);
		}
	
	
	/**
	 * Import from XML-file
	 */
	public void importXML(File inFile) throws IOException
		{
		try
			{
			groups.clear();
			Element root=EvXmlUtil.readXML(inFile).getRootElement();
			
			for(Object o:root.getChildren())
				{
				Element eGroup=(Element)o;
				
				String groupName=eGroup.getAttributeValue("name");
				for(Object oo:eGroup.getChildren())
					{
					Element eCell=(Element)oo;
					String cellName=eCell.getAttributeValue("name");
					add(groupName,cellName);
					}
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			throw new IOException(e.getMessage());
			}
		
		}
	
	
	/**
	 * Export to XML-file
	 */
	public void exportXML(File outFile) throws IOException
		{
		try
			{
			outFile=EvFileUtil.makeFileEnding(outFile, normalExportXMLEnding);
			
			Element outRoot=new Element("groups");
			Document outDoc=new Document(outRoot);
			
			for(Map.Entry<String, TreeSet<String>> e:groups.entrySet())
				{
				Element eGroup=new Element("group");
				eGroup.setAttribute("name",e.getKey());
				for(String cellName:e.getValue())
					{
					Element eCell=new Element("cell");
					eCell.setAttribute("name",cellName);
					eGroup.addContent(eCell);
					}
				outRoot.addContent(eGroup);
				}
			
			EvXmlUtil.writeXmlData(outDoc, outFile);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			throw new IOException(e.getMessage());
			}
		}
	
	
	
	}
