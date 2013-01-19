/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.converter;

import java.io.File;
import java.util.*;

import org.jdom.*;

import endrov.util.io.EvXmlUtil;

public class FilemakerStrainToSql
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
	
	
	public static String sqldec(String var, Map<String,String> type)
		{
		String t=type.get(var);
		if(t==null)
			t="text";
		return var+" "+t;
		}
	
	public static void main(String[] args)
		{
		HashMap<String,String> types=new HashMap<String, String>();
		types.put("ID","integer primary key");
		
		

		
		
		try
			{
			Document doc=EvXmlUtil.readXML(new File("/Volumes/TBU_main03/userdata/henriksson/fromfilemaker090223/strains.xml"));
			Element root=doc.getRootElement();
			
			boolean created=false;
			for(Object ro:root.getChildren())
				{
				Element rowe=(Element)ro;
				if(!rowe.getName().equals("ROW"))
					continue;
				//One record
				
				TreeMap<String, Object> insert=new TreeMap<String, Object>();
				int idnum=rowe.getAttribute("RECORDID").getIntValue();
				
				for(Object ao:rowe.getChildren())
					{
					Element attre=(Element)ao;
					String attrname=attre.getName();
					attrname=attrname.replace(".", "_");
					String value=null;
					for(Object co:attre.getContent())
						value=((Text)co).getText();

					if(attrname.equals("challeleinfo"))
						continue;
//					System.out.println(""+attrname+"="+value);
					
//					String mapto=mapFM2SQL.get(attrname);
//					if(mapto!=null)
						{
						Object newval=value;
						if(newval!=null)
							{
							if(types.get(attrname)!=null && types.get(attrname).contains("boolean"))
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
							else if(types.get(attrname)!=null && types.get(attrname).contains("integer"))
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
						
						
						
						insert.put(attrname, newval);
//						insert.put(mapto, newval);
						}
//					else System.out.println("unsupported attr: "+attrname);
					
					}
				
				String tableName="origstrain";
				
				insert.put("ID",idnum);
				
				if(!created)
					{
					System.out.println("create table "+tableName+" (");
					Iterator<String> itkey=insert.keySet().iterator();
					System.out.println(sqldec(itkey.next(),types));
					while(itkey.hasNext())
						System.out.println(","+sqldec(itkey.next(),types));
					System.out.println(");");
					created=true;
					}
					
				//Put insert together
				StringBuffer exp=new StringBuffer();
				StringBuffer expv=new StringBuffer();
				exp.append("insert into "+tableName+"(");
				expv.append("(");
				boolean needComma=false;
				for(Map.Entry<String, Object> entry:insert.entrySet())
					{
					if(needComma)
						{
						exp.append(",");
						expv.append(",");
						}
//					exp.append("\""+entry.getKey()+"\"");
					exp.append(entry.getKey());
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

				}
			
			
			
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		
		
		
		}

	}
