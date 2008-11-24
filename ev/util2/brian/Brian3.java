package util2.brian;


import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.StringTokenizer;

import endrov.util.EvParallel;

import bioserv.seqserv.io.*;


public class Brian3
{
public static Connection conn=null;



/**
 * Connect to custom SQL server
 * @param classname Name of class/driver to load
 * @param url The location parameter
 * @param user Username
 * @param password Password
 * @return Returns if connection was successful
 */
public static boolean connectCustom(String classname, String url, String user, String password)
	{
	try
		{
		Class.forName(classname);
		conn = DriverManager.getConnection(url, user, password);
		return true;
		}
	catch (ClassNotFoundException e)
		{
		System.out.println("Couldn't find the class for the driver! "+e.getMessage());
		return false;
		}
	catch (Exception e)
		{
		System.out.println(e.getMessage());
		System.out.println("classname: "+classname+" url: "+url+" user: "+user+" password: "+password);
		e.printStackTrace();
		return false;
		}
	}


public static boolean connectPostgres(String location, String user, String password)
	{
	boolean status=connectCustom("org.postgresql.Driver", "jdbc:postgresql:"+location, user, password);
	return status;
	}

	
	public static Statement createStatement() throws SQLException
		{
		return conn.createStatement();
		}
	
	public static ResultSet runQuery(String s) throws SQLException
		{
		//System.out.println(s);
		Statement stmt = createStatement();
		return stmt.executeQuery(s);
		}
	
	public static void runUpdate(String s) throws SQLException
		{
		//System.out.println(s);
		Statement stmt = createStatement();
		stmt.executeUpdate(s);
		}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		System.out.println(connectPostgres("//193.11.32.108/brian", "postgres", "wuermli"));
	
	
		for(final String otherOrg:new String[]{"ppatens","creinhardtii"})
			{
			EvParallel.map_(Arrays.asList(new File("/home/tbudev3/bioinfo/brian/blast",otherOrg+".reverse").listFiles()), 
					new EvParallel.FuncAB<File, Object>(){
					public Object func(File bfile)
						{
						try
							{
	
							System.out.println(bfile);
							Blast2 b=Blast2.readModeXML(bfile);
	
							String wbGene=bfile.getName();
	
							//What is the rank?
							int rank=0;
							runUpdate("delete from blastrank where organism='"+otherOrg+"' and cegene='"+wbGene+"'");
							for(Blast2.Entry e:b.entry)
								{
								//TODO: how to parse?
								StringTokenizer stok=new StringTokenizer(e.subjectid,"|");  //WBGene00007201|exos-4.1
								String thisWbGene=stok.nextToken();
								if(thisWbGene.equals(wbGene))
									{
									System.out.println("Found "+wbGene+" rank# "+rank);
	
									runUpdate("insert into blastrank values('"+otherOrg+"','"+wbGene+"',"+rank+")");
	
									break;
									}
								rank++;
								}
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}
	
						return null;
						}
			});
	
	
			/*
					for(File bfile:new File("/home/tbudev3/bioinfo/brian/blast",otherOrg+".reverse").listFiles())
						{
						System.out.println(bfile);
						Blast2 b=Blast2.readModeXML(bfile);
	
						String wbGene=bfile.getName();
	
						//What is the rank?
						int rank=0;
						runUpdate("delete from blastrank where organism='"+otherOrg+"' and cegene='"+wbGene+"'");
						for(Blast2.Entry e:b.entry)
							{
							//TODO: how to parse?
							StringTokenizer stok=new StringTokenizer(e.subjectid,"|");  //WBGene00007201|exos-4.1
							String thisWbGene=stok.nextToken();
							if(thisWbGene.equals(wbGene))
								{
								System.out.println("Found "+wbGene+" rank# "+rank);
	
								runUpdate("insert into blastrank values('"+otherOrg+"','"+wbGene+"',"+rank+")");
	
								break;
								}
							rank++;
							}
	
	
						}
	
	
	
			 */
			}
		System.out.println("main done");
		}
	
	}
