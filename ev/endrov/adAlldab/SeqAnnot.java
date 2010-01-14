/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.adAlldab;

import java.sql.*;
import java.util.*;

/**
 * Sequence Annotation Interface
 * @author Johan Henriksson
 */
public class SeqAnnot
	{
	public DB db;
	
	PreparedStatement psSeqannotattr;
	
	public SeqAnnot(DB db) throws SQLException
		{
		this.db=db;
		
		psSeqannotattr=db.conn.prepareStatement("select annotid, source, feature, startpos, endpos, seqdesc from (select * from (select * from seqannot where startpos<? and endpos>?) as foo natural join seqannotSource natural join seqannotFeature) bar;");
		}
	
	public static class Annotation
		{
		public int annotid;
		public String source;
		public String feature;
		public int startpos;
		public int endpos;
		public String seqdesc;
		
		public String toString()
			{
			return "annotid:"+annotid+" source:"+source+" feature:"+feature+" start:"+startpos+" end:"+endpos+" desc:"+seqdesc;
			}
		
		//can be improved? only parses. doesn't try to understand the data. up to the viewer?
		public LinkedList<String> getAttributes() 
			{
			LinkedList<String> attr=new LinkedList<String>();
			StringBuffer curs=new StringBuffer();
			boolean inCite=false;
			for(char c:seqdesc.toCharArray())
				{
				if(c=='"')
					{
					inCite=!inCite;
					curs.append(c);
					}
				else if(c==';' && !inCite)
					{
					String s=curs.toString().trim();
					if(s.length()!=0)
						attr.add(s);
					curs=new StringBuffer();
					}
				else
					curs.append(c);
				}
			if(inCite)
				System.out.println("--parse error--"); //only one record fails in wormbase GFF
			else
				{
				String s=curs.toString().trim();
				if(s.length()!=0)
					attr.add(s);
				curs=new StringBuffer();
				}			
			
			return attr;
			}
		
		//Can potentially query for additional information on-the-fly if not all columns are asked for. This lazy approach
		//is slower if all data is wanted but can be faster if only a sparse set is needed.
		}
	
	
	public Map<Integer,Annotation> getRange(int start, int stop)
		{
		HashMap<Integer,Annotation> m=new HashMap<Integer,Annotation>();
		
		try
			{
			psSeqannotattr.setInt(1, stop);
			psSeqannotattr.setInt(2, start);
			ResultSet rs=psSeqannotattr.executeQuery();
			while(rs.next())
				{
				Annotation a=new Annotation();
				a.annotid=rs.getInt(1); //should it really be stored twice?
				a.source=rs.getString(2);
				a.feature=rs.getString(3);
				a.startpos=rs.getInt(4);
				a.endpos=rs.getInt(5);
				a.seqdesc=rs.getString(6);
				
				m.put(a.annotid, a);
				}
			return m;
			}
		catch (SQLException e)
			{
			e.printStackTrace();
			return null;
			}

		}
	
	}
