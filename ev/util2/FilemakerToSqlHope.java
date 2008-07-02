package util2;

import java.io.File;
import java.util.*;

import org.jdom.*;

import evplugin.ev.EvXMLutils;

public class FilemakerToSqlHope
	{

	public static String escapeSQL(String s)
		{
		StringBuffer sb=new StringBuffer();
		sb.append("E'");
		for(char c:s.toCharArray())
			{
			if(c=='\'')
				sb.append("\\'");
			else if(c=='"')
				sb.append('\"');
			else
				sb.append(c);
			}
		sb.append("'");
		return sb.toString();
		}
	
	
	public static void main(String[] args)
		{
		TreeMap<String, String> mapFM2SQL=new TreeMap<String, String>();
		for(String s:new String[]{"lastmodified","currentstatus","receiveddate","received","todaysdate"})
			mapFM2SQL.put(s,s);
		for(int i=1;i<=39;i++)
			mapFM2SQL.put("f"+i,"f"+i);
		mapFM2SQL.put("TBUgenotype","tbugenotype");
		mapFM2SQL.put("TBUphenotype","tbuphenotype");
		mapFM2SQL.put("TBUnotes","tbunotes");

		
		
		
		
		String[] areBooleanA=new String[]{/*"investigated"*/};
		Set<String> areBoolean=new HashSet<String>();
		for(String s:areBooleanA)
			areBoolean.add(s);

		String[] areIntegerA=new String[]{/*"numintstrains"*/};
		Set<String> areInteger=new HashSet<String>();
		for(String s:areIntegerA)
			areInteger.add(s);

		
		
		int idnum=0;
		
		try
			{
			Document doc=EvXMLutils.readXML(new File("/Volumes/TBU_main02/hopeExport.xml"));
			Element root=doc.getRootElement();
			
			for(Object ro:root.getChildren())
				{
				Element rowe=(Element)ro;
				if(!rowe.getName().equals("ROW"))
					continue;
				//One record
				
				TreeMap<String, Object> insert=new TreeMap<String, Object>();
				
				
				for(Object ao:rowe.getChildren())
					{
					Element attre=(Element)ao;
					
					String attrname=attre.getName();
					String value=null;
					for(Object co:attre.getContent())
						value=((Text)co).getText();

//					System.out.println(""+attrname+"="+value);
					
					String mapto=mapFM2SQL.get(attrname);
					if(mapto!=null)
						{
						Object newval=value;
						if(newval!=null)
							{
							if(areBoolean.contains(mapto))
								{
								if(((String)newval).contains("yes"))
									newval=true;
								else if(((String)newval).contains("no"))
									newval=false;
								else
									{
									System.out.println("------ bool "+newval);
									newval=false;
									}
//								newval=Boolean.parseBoolean((String)newval);
								}
							else if(areInteger.contains(mapto))
								{
//								System.out.println("---- "+newval);
								try
									{
									newval=Integer.parseInt((String)newval);
									}
								catch (RuntimeException e)
									{
									newval=1; //1?
									}
								
								}
							//Convert
							}
						
						
						
						insert.put(mapto, newval);
						}
//					else System.out.println("unsupported attr: "+attrname);
					
					}
				
				
				insert.put("hopeID",idnum);
				
				//Put insert together
				StringBuffer exp=new StringBuffer();
				StringBuffer expv=new StringBuffer();
				exp.append("insert into tbhope(");
				expv.append("(");
				boolean needComma=false;
				for(Map.Entry<String, Object> entry:insert.entrySet())
					{
					if(needComma)
						{
						exp.append(",");
						expv.append(",");
						}
					exp.append("\""+entry.getKey()+"\"");
					Object o=entry.getValue();
					if(o instanceof String)
						expv.append(escapeSQL(o.toString()));
					else
						expv.append(""+o);
					needComma=true;
					}
				exp.append(")");
				expv.append(")");
				
				String out=exp.toString()+" values "+expv.toString()+";";
				System.out.println(out);
				
				
				
				idnum++;
				
				}
			
			
			
			
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		
		
		
		}

	}
