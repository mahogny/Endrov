package util2.converter;

import java.io.*;
import java.util.*;

import org.jdom.*;

import endrov.util.EvXmlUtil;

//import evplugin.imagesetOST.OstImageset;

/**
 * Convert our filemaker database to ImServ tags
 * @author Johan Henriksson
 */
public class FilemakerToImserv
	{
	public static void main(String arg[])
		{
		try
			{
			Document docin=EvXmlUtil.readXML(new File("/Volumes/TBU_main03/userdata/henriksson/fromfilemaker090223/recfiles"));
			for(Object ro:docin.getRootElement().getChildren())
				{
				Element rowe=(Element)ro;
				if(!rowe.getName().equals("ROW"))
					continue;
				Namespace sp=rowe.getNamespace();
				//One record

				
				Set<String> tags=new TreeSet<String>();
				Map<String,String> attr=new HashMap<String, String>();

				//String recID=rowe.getChildText("recordingID",sp);
				String entryDate=rowe.getChildText("entry_date",sp);
				String univGeneName=rowe.getChildText("universal_gene_name",sp);
				String comment=rowe.getChildText("comment",sp);
				//String sgood=rowe.getChildText("good",sp);
				String username=rowe.getChildText("username",sp);

				String author=username;
				if(author.endsWith("Hench"))
					author="J�rgen Hench";
				if(author.startsWith("Martin"))
					author="Martin L�ppert";

				attr.put("author", author);

				File imsetFile=new File(rowe.getChildText("storage_path",sp));
				imsetFile=new File(imsetFile.getParentFile(),imsetFile.getName()+".ost");
				File oldImservFile=new File(new File(new File(imsetFile.getParentFile(),imsetFile.getName()),"data"),"imset.txt");
				File imservFile=new File(new File(new File(imsetFile.getParentFile(),imsetFile.getName()),"data"),"imserv.txt");

				if(oldImservFile.exists())
					oldImservFile.delete();
					
				
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

				if(comment.indexOf("drifts")!=-1)
					tags.add("drifts");

				//Gene
				if(univGeneName.length()>0)
					tags.add("gene:"+univGeneName);

				//Full description
				attr.put("desc", comment);

				//date
				attr.put("entry_date",entryDate);


				System.out.println(imsetFile);
				System.out.println(tags);
				System.out.println(attr);
				if(!imsetFile.exists())
					System.out.println("Does not exist: "+imsetFile);
				else
					{

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
						if(imservFile.exists())
							System.out.println("Skipping");
						else
							{
							System.out.println("write");
							imservFile.mkdir();
							EvXmlUtil.writeXmlData(doc, imservFile);
							}
						}
					catch (Exception e)
						{
						System.out.println(imservFile);
						e.printStackTrace();
						}


					}
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}


		System.out.println("---done---");
		System.exit(0);

		}
	}
