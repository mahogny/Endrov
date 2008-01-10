package evplugin.adAlldab;

import java.sql.SQLException;
//import java.util.Map;

//import evplugin.adAlldab.SeqAnnot.Annotation;

public class TestAnnot
	{
	public static void main(String arg[])
		{
		try
			{
			DB db=new DB();
			db.connectPostgres("//sargas/vwb", "vwb", "vwb");
			
			SeqAnnot sa=new SeqAnnot(db);
			
			long startTime=System.currentTimeMillis();
			//Map<Integer,Annotation> sas=
			sa.getRange(0, 110);
			System.out.println(": "+(System.currentTimeMillis()-startTime));
/*
			for(SeqAnnot.Annotation a: sas.values())
				{
				System.out.println(a);
				for(String s:a.getAttributes())
					System.out.println("** "+s);
				}
			*/
			
			}
		catch (SQLException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		
		}
	}
