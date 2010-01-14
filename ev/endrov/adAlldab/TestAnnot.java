/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.adAlldab;

import java.sql.SQLException;
//import java.util.Map;


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
			e.printStackTrace();
			}
		
		
		}
	}
