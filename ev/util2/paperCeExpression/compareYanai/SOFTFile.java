package util2.paperCeExpression.compareYanai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Parser for SOFT microarray data files, as obtained from NCBI
 * 
 * http://www.ncbi.nlm.nih.gov/projects/geo/info/soft2.html#SOFTformat
 * 
 * 
 * TODO files can also be external tab-delimited or CHP-files. These has to be supported as well (part of SOFT)
 * 
 * @author Johan Henriksson
 *
 */
public class SOFTFile
	{
	private Map<String, Entity> entity=new HashMap<String, Entity>();
	
	
	

	public static class Entity
		{
		public String type;
		public Map<String, String> attributes=new HashMap<String, String>();
		public List<HashLine> dataHeader=new ArrayList<HashLine>();
		public List<List<String>> dataLine=new ArrayList<List<String>>();
		
		public int getColumnIndex(String name)
			{
			for(int i=0;i<dataHeader.size();i++)
				if(dataHeader.get(i).name.equals(name))
					return i;
			return -1;
			}
		
		@Override
		public String toString()
			{
			return "type:"+type+"\n"+attributes+"\n"+dataHeader+"\n"+dataLine;
			}
		}

	public static class HashLine
		{
		public String name;
		public String value;
		
		@Override
		public String toString()
			{
			return name+="="+value;
			}
		}

	public static class Attribute
		{
		public String value;
		//data lines
		//hashline
		
		@Override
		public String toString()
			{
			return value;
			}
		
		}
	
	
	public Entity getEntity(String name)
		{
		return entity.get(name);
		}
	
	
	public Map<String,Entity> entitiesOfType(String type)
		{
		Map<String,Entity> m=new HashMap<String, Entity>();
		for(Map.Entry<String,Entity> e:entity.entrySet())
			if(e.getValue().type.equals(type))
				m.put(e.getKey(), e.getValue());
		return m;
		}
	
	
	private static class ParsedEqLine
		{
		public String name;
		public String value;
		}
	
	
	public SOFTFile(File f) throws IOException
		{
		BufferedReader in=new BufferedReader(new FileReader(f));
		
		Entity curEntity=null;
		boolean skipFirstData=true;
		
		String line;
		while((line=in.readLine())!=null)
			{
			if(line.startsWith("^"))
				{
				ParsedEqLine peq=parseEqLine(line);
				Entity e=new Entity();
				e.type=peq.name;
				entity.put(peq.value, e);
				curEntity=e;
				skipFirstData=true;
				}
			else if(line.startsWith("!"))
				{
				ParsedEqLine peq=parseEqLine(line);
				//Attribute attr=new Attribute();
				//curAttr=attr;
				//attr.value=peq.value;
				curEntity.attributes.put(peq.name, peq.value);
				}
			else if(line.startsWith("#"))
				{
				ParsedEqLine peq=parseEqLine(line);
				
				HashLine h=new HashLine();
				h.name=peq.name;
				h.value=peq.value;
				
				curEntity.dataHeader.add(h);
				}
			else //Data line
				{
				if(skipFirstData)
					skipFirstData=false;
				else
					{
					ArrayList<String> columns=new ArrayList<String>();

					//TODO be able to handle empty values in table
					StringTokenizer st=new StringTokenizer(line, "\t");
					while(st.hasMoreTokens())
						columns.add(st.nextToken());
					
					curEntity.dataLine.add(columns);
					
					}
				
				}
			
			
			
			}
		
		
		}
	
	
	
	@Override
	public String toString()
		{
		return entity.toString();
		}
	
	private static ParsedEqLine parseEqLine(String line)
		{
		ParsedEqLine peq=new ParsedEqLine();
		int index=line.indexOf("=");
		if(index==-1)
			peq.name=line;
		else
			{
			peq.name=line.substring(1,index-1);
			peq.value=line.substring(index+2);
			}
		return peq;
		}
	
	}
