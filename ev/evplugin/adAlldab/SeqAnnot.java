package evplugin.adAlldab;

import java.sql.*;
import java.util.*;

/**
 * 
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
		
		//Can potentially query for additional information on-the-fly if not all columns are asked for. This lazy approach
		//is slower if all data is wanted but can be faster if only a sparse set is needed.
		}
	
	/*
	public static class AnnotationSet
		{
		private ResultSet rs;
		public AnnotationSet(ResultSet rs)
			{
			this.rs=rs;
			}
		public Annotation next()
			{
			try
				{
				if(rs.next())
					{
					Annotation a=new Annotation();
					
					return a;
					}
				else
					return null;
				}
			catch (SQLException e)
				{
				e.printStackTrace();
				return null;
				}
			}
		}
	public AnnotationSet getRange(int start, int stop)
		{
		//Or generate a map right away?
		try
			{
			psSeqannotattr.setInt(1, stop);
			psSeqannotattr.setInt(2, start);
			ResultSet rs=psSeqannotattr.executeQuery();
			return new AnnotationSet(rs);
			}
		catch (SQLException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
			}

		}
	*/
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
			}

		}
	
	}
