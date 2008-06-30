package util2;

import java.io.*;
import java.util.*;

import org.jdom.*;

import evplugin.ev.EvXMLutils;

//import evplugin.imagesetOST.OstImageset;

/**
 * Convert our filemaker database to ImServ tags
 * @author Johan Henriksson
 */
public class ConvertFilemaker
	{
	public static void main(String arg[])
		{
		try
			{
			BufferedReader bf=new BufferedReader(new FileReader(new File("/Volumes/TBU_main02/recordinfiles.csv")));
			System.out.println("+++");
			String line;
			while((line=bf.readLine())!=null)
				{
				List<String> str=new LinkedList<String>();
				int index;
				line=line.substring(1)+",\"";
				while((index=line.indexOf("\",\""))!=-1)
					{
					String ss=line.substring(0,index);
					line=line.substring(index+3);
//					System.out.println("-"+ss);
					str.add(ss);
					}
				
				List<String> tags=new LinkedList<String>();
				Map<String,String> attr=new HashMap<String, String>();
				
				
				File imsetFile=new File(str.get(2));
				File imservFile=new File(imsetFile.getParentFile(),imsetFile.getName()+".imserv");
				

				if(imsetFile.exists())
					{
				
					//Tag author
					String author=str.get(11);
					if(author.length()>0)
						{
						if(author.endsWith("Hench"))
							author="JŸrgen Hench";
						if(author.startsWith("Martin"))
							author="Martin LŸppert";
						attr.put("author", author);
						}

					//Tag type of image
					String imsetFiles=imsetFile.getAbsolutePath();
					if(imsetFiles.indexOf("good")!=-1)
						tags.add("good");
					if(imsetFiles.indexOf("failed")!=-1)
						tags.add("failed");
					if(imsetFiles.indexOf("3d")!=-1)
						tags.add("3d");
					if(imsetFiles.indexOf("4d")!=-1)
						tags.add("4d");
					
					
					//Gene
					String gene=str.get(1);
					if(gene.length()>0)
						tags.add("gene:"+gene);
					//Tags from description
					String descLine=str.get(4);
					if(descLine.indexOf("drifts")!=-1)
						tags.add("drifts");

					//Full description
					attr.put("desc", descLine);
					
					//date
					String date=str.get(3);
					attr.put("entry_date",date);
					
					/*
					OstImageset rec=new OstImageset(imsetFile);
					rec.meta.metaDescript=descLine;
					System.out.println("desc: "+descLine);
					*/
					
					//tag suggestions
					//embryo
					//L1, L2, dauer etc
					//gene=....
					//strain=...
					//pol-gamma
					//histone::mCherry
					//Nile red
					//confocal
					//EM
					//hoechst
					//3d
					//4d
					//failed?
					//author=Akram Abouzied
					//agmount
					
					//can show a tree list for =-tags
					
					
					try
						{
						Element root=new Element("imserv");
						Document doc=new Document(root);
						for(String tag:tags)
							{
							Element e=new Element("tag");
							e.setAttribute("name", tag);
							root.addContent(e);
							}
						for(Map.Entry<String, String> entry:attr.entrySet())
							{
							Element e=new Element("tag");
							e.setAttribute("name", entry.getKey());
							e.setAttribute("value", entry.getValue());
							root.addContent(e);
							}
						EvXMLutils.writeXmlData(doc, imservFile);
						}
					catch (Exception e)
						{
						System.out.println(imservFile);
						e.printStackTrace();
						}
					
					
					}
				
				
				
				
				}
			}
		catch (FileNotFoundException e)
			{
			e.printStackTrace();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		System.out.println("---done---");
		System.exit(0);
		
		}
	}
