package util2.brian;

import java.io.File;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import endrov.util.EvParallel;

import bioserv.seqserv.io.Blast2;
import bioserv.seqserv.io.Fasta;

public class BrianTot
	{
	public static String[] orgs=new String[]{"creinhardtii","ppatens","bdendrobatidis","scerevisiae","cpombe","athaliana","bdendrobatidis","hsapiens"};
	
	public static void main(String[] args)
		{
		System.out.println(BrianSQL.connectPostgres("//193.11.32.108/brian", "postgres", "wuermli"));
		part1();
		part2();
		part3();
		}
	
	public static void part1()
		{
		try
			{
			final Fasta cegenes=new Fasta(new File("/home/tbudev3/bioinfo/incdata/celegans/protein.faa"));
			System.out.println("read genes");
			
			System.out.println("# of C.e genes: "+cegenes.seq.size());
			
			for(final String otherOrg:orgs)
				{
				//Which genes are left to do?
				final TreeSet<String> geneNotTODO=new TreeSet<String>(cegenes.seq.keySet());
				ResultSet rs=BrianSQL.runQuery("select cegene,organism from blastfromce where organism='"+otherOrg+"'");
				while(rs.next())
					geneNotTODO.add(rs.getString(1));
				System.out.println("organism: "+otherOrg);
				System.out.println("Genes not todo: "+geneNotTODO.size());
				
				/*
				bdendrobatidis	WBGene00007261
				bdendrobatidis	WBGene00007261
				bdendrobatidis	WBGene00007261
				bdendrobatidis	WBGene00007261*/
				//Problem: one gene can produce several proteins
				
				
				//Blast every gene
				EvParallel.map_(4,new TreeSet<String>(cegenes.seq.keySet()),
						new EvParallel.FuncAB<String, Object>(){
						public Object func(String key)
							{
							try
								{
								StringTokenizer ntok=new StringTokenizer(key);
								ntok.nextToken(); //something
								ntok.nextToken(); //something
								String wbGene=ntok.nextToken(); //WBname

								if(!geneNotTODO.contains(wbGene))
									{
									String ceseq=cegenes.seq.get(key);

									File out=File.createTempFile("foo", "");
									System.out.println(otherOrg+"\t"+wbGene);

									/*System.out.println(out.getPath());
						System.exit(1);*/

									Blast2.invokeTblastnToFile(otherOrg, wbGene,ceseq,out,Blast2.MODE_XML);
									BrianSQL.insertBlastResult("blastfromce", otherOrg, wbGene, out);
									out.delete();
									}
								//else
								//System.out.println("Skipping "+wbGene);
								}
							catch (Exception e)
								{
								e.printStackTrace();
								}
							return null;
							}
				});
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("main1 done");
		}
	
	
	
	
	public static void part2()
		{
		try
			{
			for(final String otherOrg:orgs)
				{
				ResultSet rs=
				BrianSQL.runQuery(
						"select cegene from (" +
						"select cegene,organism from blastfromce where organism='"+otherOrg+"' " +
						"and blastout is not null and (cegene,organism) not in (select cegene,organism from reverseblast)" +
						") as foo1");
				TreeSet<String> geneTODO=new TreeSet<String>();
				while(rs.next())
					geneTODO.add(rs.getString(1));
				System.out.println("TODO: "+geneTODO.size());
				
				EvParallel.map_(geneTODO, //Was 500,
						new EvParallel.FuncAB<String, Object>(){
						public Object func(String wbGene)
							{
							try
								{
								System.out.println(otherOrg+"\t"+wbGene);
								String finContent=BrianSQL.getBlastResult("blastfromce", otherOrg, wbGene);
								Blast2 b=Blast2.readModeXML(new StringReader(finContent));
								
								if(!b.entry.isEmpty())
									{	
									Blast2.Entry e=b.entry.iterator().next();
									String found=e.hseq;
								
									found=found.replaceAll("\\p{Punct}", "");
									System.out.println("Found: "+found);

									File outfile=File.createTempFile("foo", "");
									Blast2.invokeTblastnToFile("celegans", "",found,outfile,Blast2.MODE_XML);
									BrianSQL.insertBlastResult("reverseblast", otherOrg, wbGene, outfile);
									outfile.delete();
									}
								else
									BrianSQL.insertBlastResult("reverseblast", otherOrg, wbGene, "");
								}
							catch (Exception e)
								{
								e.printStackTrace();
								}
							return null;
							}
				});
				
				/*
				for(String wbGene:geneTODO)
					{
					
					}*/
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("main2 done");
		}
	
	
	public static void part3()
		{
		try
			{
			for(final String otherOrg:orgs)
				{
				//Which records need be done?
				System.out.println("Finding work: "+otherOrg);
				ResultSet rs=
				BrianSQL.runQuery(
						"select cegene from (" +
						"select cegene,organism from reverseblast as t1 where organism='"+otherOrg+"' " +
						"and blastout is not null and (cegene,organism) not in (select cegene,organism from blastrank2 as t2 where t2.cegene=t1.cegene)" +
				") as foo1");
				LinkedList<String> geneTODO=new LinkedList<String>();
				while(rs.next())
					geneTODO.add(rs.getString(1));
				System.out.println("TODO: "+geneTODO.size());
	
				//CPU not limiting, use many threads
				//why is it not??
				//is it the XML validation????
				
				//Do records (in parallel)
				EvParallel.map_(geneTODO, //Was 500,
						new EvParallel.FuncAB<String, Object>(){
						public Object func(String wbGene)
							{
							try
								{
								//Read BLAST output
								System.out.println(wbGene);
								Blast2 bforward=Blast2.readModeXML(new StringReader(BrianSQL.getBlastResult("blastfromce", otherOrg, wbGene)));
								Blast2 brev=Blast2.readModeXML(new StringReader(BrianSQL.getBlastResult("reverseblast", otherOrg, wbGene)));
	
								//What is the rank?
								int rank=0;
								BrianSQL.runUpdate("delete from blastrank2 where organism='"+otherOrg+"' and cegene='"+wbGene+"'");
								Integer foundRank=null;
								Double reveval=null;
								for(Blast2.Entry e:brev.entry)
									{
									StringTokenizer stok=new StringTokenizer(e.subjectid,"|");  //WBGene00007201|exos-4.1
									String thisWbGene=stok.nextToken();
									if(thisWbGene.equals(wbGene))
										{
										System.out.println("Found "+wbGene+" rank# "+rank);
										foundRank=rank;
										reveval=e.evalue;
										break;
										}
									rank++;
									}

								//E-value in forward
								double foreval=bforward.entry.get(0).evalue;
								
								
								BrianSQL.runUpdate("insert into blastrank2 values('"+otherOrg+"','"+wbGene+"',"+foreval+","+reveval+","+foundRank+")");
								}
							catch (Exception e)
								{
								e.printStackTrace();
								}
	
							return null;
							}
				});
				}
			}
		catch (SQLException e)
			{
			e.printStackTrace();
			}
		System.out.println("main3 done");
		}
	
	
	
	}
