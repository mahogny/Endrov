/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nuc.ccm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.util.EvDecimal;


/**
 * General cell contact map
 * 
 * @author Johan Henriksson
 *
 */
public class CellContactMap extends EvObject
	{
	private static final String metaType="ccm";
	
	/**
	 * Further information about one nucleus 
	 */
	public static class CellInfo
		{
		public EvDecimal firstFrame;
		public EvDecimal lastFrame;
		}
	
	//Map: cell -> cell -> frames they coexist
	public Map<String,Map<String,SortedSet<EvDecimal>>> contactsf=new TreeMap<String, Map<String,SortedSet<EvDecimal>>>();

	//Which frames have been evaluated
	public TreeSet<EvDecimal> framesTested=new TreeSet<EvDecimal>();
	
	//Cell metadata
	public Map<String, CellInfo> cellInfo=new HashMap<String, CellInfo>();
	
	/**
	 * Get container for cell metadata
	 */
	public CellInfo getCreateInfo(String name)
		{
		CellInfo info=cellInfo.get(name);
		if(info==null)
			cellInfo.put(name,info=new CellInfo());
		return info;
		}

	/**
	 * For two cells, add frame with contact a <-> b
	 */
	public void addFrame(String a, String b, EvDecimal f)
		{
		addFrame1(a, b, f);
		addFrame1(b, a, f);
		}
		
	
	/**
	 * Add frame with contact a -> b(?)
	 */
	private void addFrame1(String a, String b, EvDecimal f)
		{
		Map<String,SortedSet<EvDecimal>> na=contactsf.get(a);
		if(na==null)
			contactsf.put(a,na=new TreeMap<String,SortedSet<EvDecimal>>());
		SortedSet<EvDecimal> sa=na.get(b);
		if(sa==null)
			na.put(b, sa=new TreeSet<EvDecimal>());
		sa.add(f);
		}
		


	@Override
	public void buildMetamenu(JMenu menu)
		{
		}


	@Override
	public String getMetaTypeDesc()
		{
		return "CCM";
		}



	/**
	 * List of decimals -> Generate String
	 */
	private static StringBuffer decimalToString(Collection<EvDecimal> framesTested)
		{
		StringBuffer sbTested=new StringBuffer();

		Iterator<EvDecimal> itframe=framesTested.iterator();
		if(itframe.hasNext())
			sbTested.append(itframe.next().toString());
		while(itframe.hasNext())
			{
			sbTested.append(",");
			sbTested.append(itframe.next().toString());
			}
		return sbTested;
		}

	/**
	 * Parse string of decimals
	 */
	private static List<EvDecimal> stringToDecimal(String s)
		{
		LinkedList<EvDecimal> list=new LinkedList<EvDecimal>();
		StringTokenizer tok=new StringTokenizer(s,",");
		while(tok.hasMoreTokens())
			list.add(new EvDecimal(tok.nextToken()));
		return list;
		}
	
	

	@Override
	public void loadMetadata(Element e)
		{
		for(Object o:e.getChildren())
			{
			Element sub=(Element)o;
			
			if(sub.getName().equals("tf"))
				framesTested.addAll(stringToDecimal(sub.getText()));
			else //name is "nuc"
				{
				String nucName=sub.getAttributeValue("name");
				CellInfo info=getCreateInfo(nucName);
				info.firstFrame=new EvDecimal(sub.getAttributeValue("firstFrame"));
				info.lastFrame=new EvDecimal(sub.getAttributeValue("lastFrame"));

				
				Map<String,SortedSet<EvDecimal>> thisContacts=contactsf.get(nucName);
				if(thisContacts==null)
					contactsf.put(nucName,thisContacts=new HashMap<String, SortedSet<EvDecimal>>());
				
				for(Object oo:sub.getChildren())
					{
					Element otherEl=(Element)oo;
					String otherName=otherEl.getAttributeValue("name");

					Map<String,SortedSet<EvDecimal>> otherContacts=contactsf.get(otherName);
					if(otherContacts==null)
						contactsf.put(otherName,otherContacts=new HashMap<String, SortedSet<EvDecimal>>());
					
					SortedSet<EvDecimal> frames=new TreeSet<EvDecimal>();
					thisContacts.put(otherName, frames);
					otherContacts.put(nucName, frames);
					
					frames.addAll(stringToDecimal(otherEl.getText()));
					}
				
				}
			}
		
		//Must make sure contactsf is filled up
		for(String cellNameA:cellInfo.keySet())
			{
			for(String cellNameB:cellInfo.keySet())
				{
				Map<String,SortedSet<EvDecimal>> thisContacts=contactsf.get(cellNameA);
				if(thisContacts.get(cellNameB)==null)
					{
					Map<String,SortedSet<EvDecimal>> otherContacts=contactsf.get(cellNameB);
					SortedSet<EvDecimal> frames=new TreeSet<EvDecimal>();
					thisContacts.put(cellNameB, frames);
					otherContacts.put(cellNameA, frames);
					}
				}
			}
		
		}

	@Override
	public String saveMetadata(Element e)
		{
		Element elTestedFrame=new Element("tf");
		elTestedFrame.setText(decimalToString(framesTested).toString());
		e.addContent(elTestedFrame);
		
		for(String nucName:cellInfo.keySet())
			{
			CellInfo info=cellInfo.get(nucName);
			
			Element elCell=new Element("nuc");
			e.addContent(elCell);
			elCell.setAttribute("name",nucName);
			elCell.setAttribute("firstFrame",info.firstFrame.toString());
			elCell.setAttribute("lastFrame",info.lastFrame.toString());
			
			for(Map.Entry<String, SortedSet<EvDecimal>> entry:contactsf.get(nucName).entrySet())
				{
				String otherName=entry.getKey();
				if(otherName.compareTo(nucName)<=0 && !entry.getValue().isEmpty())
					{
					Element elContact=new Element("contact");
					elContact.setAttribute("name",otherName);
					elContact.setText(decimalToString(entry.getValue()).toString());
					elCell.addContent(elContact);
					}
				}
			
			}
		return metaType;
		}
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,CellContactMap.class);
		}


	}
