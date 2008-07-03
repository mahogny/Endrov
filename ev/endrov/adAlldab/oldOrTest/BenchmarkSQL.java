package endrov.adAlldab.oldOrTest;

import java.sql.ResultSet;

import endrov.adAlldab.DB;

//import java.sql.Statement;

//http://www.postgresql.org/docs/8.2/interactive/functions-string.html

public class BenchmarkSQL
	{
	public static void main(String[] arg)
		{
		DB db=new DB();
		
		db.connectPostgres("//sargas/vwb", "vwb", "vwb");
		
		
		try
			{
			
			db.runUpdate("DROP TABLE teststring;");
			
			db.runUpdate(
					"CREATE TABLE teststring ("+
					"    organism VARCHAR(64) PRIMARY KEY,"+
					//should be seqname
					"    seq  TEXT NOT NULL,"+
					"    pos  INTEGER"+
					");");			
			
			db.runUpdate(
					"ALTER TABLE teststring ALTER seq SET STORAGE EXTERNAL");
			
			String seq2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
			StringBuffer seq=new StringBuffer();
			for(int i=0;i<10000;i++)
				seq.append(seq2);
			
			db.runUpdate("" +
					"DELETE FROM teststring;");
			db.runUpdate(
					"INSERT INTO teststring VALUES ('ce','"+seq+"',null);");
			int seqlen=10;
			int seqstart=100000;
//			int seqstart=5;
			int numloop=1000;
			long starttime=System.currentTimeMillis();

			String qtext="SELECT substring(seq from "+seqstart+" for "+seqlen+") FROM teststring;";
//			String qtext="SELECT seq FROM teststring;";
			
			for(int i=0;i<numloop;i++)
				{
				ResultSet rs=db.runQuery(qtext);
				while(rs.next())
					{
					//String s=
					rs.getString(1);
					//System.out.println(s);
					}
				}
			long endtime=System.currentTimeMillis();

			System.out.println("t: "+(endtime-starttime));
			
			}
		catch (Exception e)
			{
			System.out.println(e.getMessage());
			e.printStackTrace();
			}
		
		
		
		
		}
	}
