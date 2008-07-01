package util2;

import java.io.File;
import java.util.TreeMap;

import org.jdom.*;

import evplugin.ev.EvXMLutils;

public class FilemakerToSql
	{

	public static void main(String[] args)
		{
		TreeMap<String, String> mapFM2SQL=new TreeMap<String, String>();
		
		mapFM2SQL.put("Gene_name","genename");
		mapFM2SQL.put("Gene_Class","geneclass");
		mapFM2SQL.put("Old_Gene_Name","oldgenename");
		mapFM2SQL.put("Sequence_Name","seqname"); //ADD
		mapFM2SQL.put("Cosmid","cosmid");
		mapFM2SQL.put("WB_Listed_Strains","wblistedstrains");
		mapFM2SQL.put("BC_Genome_GFP_Strains","bcgenomegfpstrains");
		mapFM2SQL.put("BC_embryonic_expression","bcembexp");
		mapFM2SQL.put("Ex_Transgene","extransgene");
		mapFM2SQL.put("Is_Transgene","istransgene");
		mapFM2SQL.put("Investigated","investigated");
		mapFM2SQL.put("TB__Strain_available","tbstrainavailable");
		mapFM2SQL.put("TB__Integration_Process","tbintegproc");
		mapFM2SQL.put("GFP_Diss._Scope","gfpdissscope");
		mapFM2SQL.put("GFP_highpower","gfphighpower"); //CHANGE
		mapFM2SQL.put("Integration_Start","integstart");
		mapFM2SQL.put("fourD_Recording","fourdrec");
		mapFM2SQL.put("ToDo1","todo");
		mapFM2SQL.put("re_freezing","refreezing");
		mapFM2SQL.put("integratedstrains","numintstrains");
		mapFM2SQL.put("strings_good_rec","goodrec");
		mapFM2SQL.put("not_homeobox","markerfor");
		
		try
			{
			Document doc=EvXMLutils.readXML(new File("/Volumes/TBU_main02/homeoExport2.xml"));
			Element root=doc.getRootElement();
			
			for(Object ro:root.getChildren())
				{
				Element rowe=(Element)ro;
				//One record
				
				System.out.println("-------------------");
		
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
						String newval=value;
						if(newval!=null)
							{
							//Convert
							}
						
						insert.put(mapto, newval);
						}
					else
						{
						System.out.println("unsupported attr: "+attrname);
						}
					
					}
				
				}
			
			//
			
			
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		
		
		
		}

	}
