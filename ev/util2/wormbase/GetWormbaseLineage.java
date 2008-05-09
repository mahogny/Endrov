package util2.wormbase;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import evplugin.ev.EV;

public class GetWormbaseLineage
	{
	public static class WBCell
		{
		int btime;
		Integer duration=null;
		List<String> children=new LinkedList<String>();
		}
	
	
	public static void main(String[] arg)
		{
		Map<String,WBCell> cells=new HashMap<String, WBCell>();
		
		
		//Read XML
    Document document = null;
    try 
    	{
  		FileInputStream fileInputStream = new FileInputStream("/Volumes/TBU_main02/wbxml180/Cell");
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
        				cell.children.add(child3.getAttributeValue("value"));
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
    for(Map.Entry<String, WBCell> e:cells.entrySet())
    	{
    	WBCell cell=e.getValue();
    	if(!cell.children.isEmpty())
    		{
    		cell.duration=cells.get(cell.children.iterator().next()).btime-cell.btime;
				System.out.println(e.getKey()+" "+cell.duration);

    		}
    	}
    
		}
	}
