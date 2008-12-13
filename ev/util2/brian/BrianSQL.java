package util2.brian;

import java.io.File;
import java.sql.*;

import endrov.util.EvFileUtil;


/*
 * 
 * CREATE TABLE blastfromce
(
   organism text, 
   cegene text, 
   blastout text, 
    PRIMARY KEY (organism, cegene)
) WITH (OIDS=FALSE)
;
ALTER TABLE blastfromce OWNER TO tbudev3;

 * 
 * 
 */




/**
 * 
 * CREATE TABLE reverseblast
(
   organism text, 
   cegene text, 
   blastout text, 
    PRIMARY KEY (organism, cegene)
) WITH (OIDS=FALSE)
;
ALTER TABLE reverseblast OWNER TO tbudev3;
 *
 */




public class BrianSQL
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
		
	public static void insertBlastResult(String table, String organism, String gene, File f) throws Exception
		{
		String content=EvFileUtil.readFile(f);
		insertBlastResult(table, organism, gene, content);
		}
		
	public static void insertBlastResult(String table, String organism, String gene, String content) throws Exception
		{
		PreparedStatement ps=conn.prepareStatement("insert into "+table+" values(?,?,?)");
		ps.setString(1, organism);
		ps.setString(2, gene);
		if(content.length()==0)
			ps.setString(3, null);
		else
			ps.setString(3, content);
		ps.execute();
		}
	
	public static String getBlastResult(String table, String organism, String gene) throws Exception
		{
		PreparedStatement ps=conn.prepareStatement("select * from "+table+" where organism=? and cegene=?");
		ps.setString(1, organism);
		ps.setString(2, gene);
		ResultSet rs=ps.executeQuery();
		while(rs.next())
			return rs.getString(3);
		throw new Exception("Does not exist");
		}
	
	}
