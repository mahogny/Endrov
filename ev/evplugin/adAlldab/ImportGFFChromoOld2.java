package evplugin.adAlldab;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

//to import: repeats , gene_IDs, gene_interpolated_map_positions, genetic_interactions, swissprot_mappings


//elegans_pmap has attributes:
//Clone xxx
//Note Sequenced


//Clone C05A2; Note Sequenced; Accession number Z73968<tab>
//ctg313	sequenced	clone	59870	60200	.	.		Clone B0024; Note Sequenced; Accession number Z71178

//from spec: <seqname> <source> <feature> <start> <end> <score> <strand> <frame> [attributes] [comments]

///Volumes/TBU_xeon01_500GB02/userdata/biodb/celegans/incoming/gff2/elegans_pmapWS183.gff
//field 2 ex: yac, sequenced, cosmid, fosmid, plasmid
//field 3 ex: clone, contig

///Volumes/TBU_xeon01_500GB02/userdata/biodb/celegans/incoming/gff2/elegansWS183.gff
//incompatible
//field 1: chromosome
//field 2: program it came from or something
//field 3: 
//field 4: start
//field 5: end
//field 6: .
//field 7: -
//field 8: .
//field 9: attribs
//attrib CDS "F31C3.2a"
//attrib Transcript "F31C3.2a"




/**
 * For importing a genome annotation, chromosome GFF
 * 
 * @author Johan Henriksson
 */
public class ImportGFFChromoOld2
	{
	public static class GffRecord
		{
		public String seqname, source, feature;
		public int start, end;
		public String score;
		//strand
		//frame
		Vector<String> attribs=new Vector<String>();
		//comments
		
		public String desc=null;
		}
	
	public static interface GffRecordHandler
		{
		public void gffRecord(GffRecord r);
		}

	private static Pattern ptab=Pattern.compile("\t");
	private static Pattern pcit=Pattern.compile("\"");

	public static String getUntilCitation(String s)
		{
		String inp[]=pcit.split(s,2);
		return inp[0];
		}
	
	public static void importGFF(File infile, GffRecordHandler h) throws IOException
		{
		BufferedReader input = new BufferedReader(new FileReader(infile));
		int curline=0;
		String line = null;
		while (( line = input.readLine()) != null)
			{
			
		
			try
				{
				//		System.out.println(line);
				String inp[]=ptab.split(line, 10);
				GffRecord gff=new GffRecord();
				gff.seqname=inp[0];
				gff.source=inp[1];
				gff.feature=inp[2];
				gff.start=Integer.parseInt(inp[3]);
				gff.end=Integer.parseInt(inp[4]);
				//5,6,7 ignore

				StringTokenizer sta=new StringTokenizer(inp[8],";"); // "; "
				while(sta.hasMoreTokens())
					{
					String at=sta.nextToken().trim();
					gff.attribs.add(at);
					if(at.startsWith("Target "))
						gff.desc=getUntilCitation(at.substring(8));
					else if(at.startsWith("CDS "))
						gff.desc=getUntilCitation(at.substring(5));
					else if(at.startsWith("Confirmed_EST ") && gff.desc==null)
						gff.desc=at.substring(14);
					else if(at.startsWith("Gene "))
						gff.desc=at.substring(5);
					else if(at.startsWith("Target ") && gff.desc==null)
						gff.desc=getUntilCitation(at.substring(8));
					
					}

				h.gffRecord(gff);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				System.out.println("bad line:"+line);
				System.out.println("on line "+curline+": "+e.getMessage());
				}
			curline++;
			}
		}
	
	
	public static class AutoString
		{
		String tablename;
		DB db;
		HashMap<String,Integer> t=new HashMap<String,Integer>();
		int seqid=0;
		
		
		
		PreparedStatement psi;
		public AutoString(DB db, String tablename) throws SQLException
			{
			this.db=db;
			this.tablename=tablename;
			psi=db.conn.prepareStatement("INSERT INTO "+tablename+" VALUES (?,?);");
			}
		
		public int getID(String s) throws SQLException
			{
			Integer i=t.get(s);
			if(i==null)
				{
				psi.setInt(1, seqid);
				psi.setString(2, s);
				psi.execute();
				int curseqid=seqid;
				seqid++;
				t.put(s, curseqid);
				return curseqid;
				}
			else
				return i;
			}
		}
	
	public static int annotid=0;
	
	public static void main(String[] args)
		{
		
		try 
			{
			final DB db=new DB();

			//not that useful
			//db.conn.setAutoCommit(false);
			
			db.connectPostgres("//sargas/vwb", "vwb", "vwb");
			final PreparedStatement psSeqannot=db.conn.prepareStatement("INSERT INTO seqannot (startpos,endpos,seqid,sourceid,featureid,annotid) VALUES (?,?,?,?,?,?);");
			final PreparedStatement psSeqannotattr=db.conn.prepareStatement("INSERT INTO seqannotattr (annotid, attr) VALUES (?,?);");

			final AutoString seqname   =new AutoString(db,"seqannotSeq");
			final AutoString seqsource =new AutoString(db,"seqannotSource");
			final AutoString seqfeature=new AutoString(db,"seqannotFeature");

			
//			File infile=new File("/Volumes/TBU_xeon01_500GB02/userdata/biodb/celegans/incoming/gff2/elegans_pmapWS183.gff");
			File infile=new File("/Volumes/TBU_xeon01_500GB02/userdata/biodb/celegans/incoming/gff2/elegansWS183.gff");
			importGFF(infile, new GffRecordHandler()
				{
				public void gffRecord(GffRecord gff)
					{
					//System.out.print(" "+gff.seqname);
//					System.out.println("- "+gff.seqname+"\t"+gff.feature+"\t"+gff.start+"\t"+gff.end);
				
					try
						{
						int seqid=seqname.getID(gff.seqname);
						int sourceid=seqsource.getID(gff.source);
						int featureid=seqfeature.getID(gff.feature);
						
						
						
						psSeqannot.setInt(1, gff.start);
						psSeqannot.setInt(2, gff.end);
						psSeqannot.setInt(3, seqid);
						psSeqannot.setInt(4, sourceid);
						psSeqannot.setInt(5, featureid);
						psSeqannot.setInt(6, annotid);
						psSeqannot.addBatch();
						
						for(String a:gff.attribs)
							{
							psSeqannotattr.setInt(1,annotid);
							psSeqannotattr.setString(2, a);
							//System.out.println("psattr "+psSeqannotattr);
							psSeqannotattr.addBatch();
							}
							
						
						
						annotid++;
						if(annotid%1000==0)
							{
							psSeqannot.executeBatch();
							psSeqannotattr.executeBatch();
							System.out.println("Count: "+annotid);
							}
						}
					catch (SQLException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						while((e=e.getNextException())!=null)
							System.out.println(e.getMessage());
						
						System.out.println("annotid "+annotid);
						System.exit(0);
						}
					
					}
				});
			
			psSeqannot.executeBatch();
			psSeqannotattr.executeBatch();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.print("Done");
		}
	}
