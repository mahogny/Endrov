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
 * General cell contact maps
 * @author Johan Henriksson
 *
 */
public class CellContactMap extends EvObject
	{
	private static final String metaType="ccm";
	
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,CellContactMap.class);
		}

	
	//nuc -> nuc -> frames
	public Map<String,Map<String,SortedSet<EvDecimal>>> contactsf=new TreeMap<String, Map<String,SortedSet<EvDecimal>>>();

	public TreeSet<EvDecimal> framesTested=new TreeSet<EvDecimal>();
	public Map<String, NucInfo> nucInfo=new HashMap<String, NucInfo>();
	
	public static class NucInfo
		{
		public EvDecimal firstFrame, lastFrame;
		}
	
	public NucInfo getCreateInfo(String name)
		{
		NucInfo info=nucInfo.get(name);
		if(info==null)
			nucInfo.put(name,info=new NucInfo());
		return info;
		}
	
	/**
	 * Add to life length
	 */
	/*
	public void addLifelen(String a)
		{
		NucInfo info=nucInfo.get(a);
		info.lifeLen++;*/
		/*
		Integer len=lifelen.get(a);
		if(len==null)
			len=0;
		len++;
		lifelen.put(a,len);
		*/
//		}
	
	/**
	 * Add frame with contact a <-> b
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
			else //"nuc"
				{
				String nucName=sub.getAttributeValue("name");
				NucInfo info=getCreateInfo(nucName);
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
		for(String nucName:nucInfo.keySet())
			{
			for(String otherName:nucInfo.keySet())
				{
				Map<String,SortedSet<EvDecimal>> thisContacts=contactsf.get(nucName);
				if(thisContacts.get(otherName)==null)
					{
					Map<String,SortedSet<EvDecimal>> otherContacts=contactsf.get(otherName);
					SortedSet<EvDecimal> frames=new TreeSet<EvDecimal>();
					thisContacts.put(otherName, frames);
					otherContacts.put(nucName, frames);
					}
				}
			}
		
		}

	@Override
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		
		Element elTestedFrame=new Element("tf");
		elTestedFrame.setText(decimalToString(framesTested).toString());
		e.addContent(elTestedFrame);
		
		for(String nucName:nucInfo.keySet())
			{
			NucInfo info=nucInfo.get(nucName);
			
			Element elNuc=new Element("nuc");
			e.addContent(elNuc);
			elNuc.setAttribute("name",nucName);
			elNuc.setAttribute("firstFrame",info.firstFrame.toString());
			elNuc.setAttribute("lastFrame",info.lastFrame.toString());
			
			for(Map.Entry<String, SortedSet<EvDecimal>> entry:contactsf.get(nucName).entrySet())
				{
				String otherName=entry.getKey();
				if(otherName.compareTo(nucName)<=0 && !entry.getValue().isEmpty())
					{
					Element elContact=new Element("contact");
					elContact.setAttribute("name",otherName);
					elContact.setText(decimalToString(entry.getValue()).toString());
					elNuc.addContent(elContact);
					}
				}
			
			}
		
		}
	
	
	}