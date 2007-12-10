package evplugin.adAlldab;


import java.sql.*;
import java.util.*;

import javax.swing.*;


public class DB
	{	
	/******************************************************************************************************
	 *                               Database creation and maintenance                                    *
	 *****************************************************************************************************/
	public Connection conn=null;
	public String id=null;
	public String dbClassname="", dbUrl="", dbUser="", dbPassword="";

	/** Latest supported db version */
	public static final int latestMajorVersion=1, latestMinorVersion=1;
	/** Version of this db */
	public int majorVersion=-1, minorVersion=-1;
	
	/**
	 * Client version of database sample_up - Used to determine if client
	 * is in synch with the database. See separate documentation
	 */
	//public HashMap<String,Integer> sample_up=new HashMap<String,Integer>();
	
	/** All databases */
	public static HashMap<String,DB> databases=new HashMap<String,DB>();

	
	

	/**
	 * Connect to custom SQL server
	 * @param classname Name of class/driver to load
	 * @param url The location parameter
	 * @param user Username
	 * @param password Password
	 * @return Returns if connection was successful
	 */
	public boolean connectCustom(String classname, String url, String user, String password)
		{
		try
			{
			dbClassname=classname;
			dbUrl=url;
			dbUser=user;
			dbPassword=password;
			Class.forName(classname);
			conn = DriverManager.getConnection(url, user, password);
			id=classname+"://"+user+":"+password+"@"+url;
			return checkDatabase();
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
	

	/**
	 * Connect to PostgreSQL server
	 * @param location The location parameter
	 * @param user Username
	 * @param password Password
	 * @return Returns if connection was successful
	 */
	public boolean connectPostgres(String location, String user, String password)
		{
		boolean status=connectCustom("org.postgresql.Driver", "jdbc:postgresql:"+location, user, password);
		id="PGSQL://"+user+":"+password+"@"+location;
		return status;
		}
	

	/**
	 * Connect to MySQL server
	 * @param location The location parameter
	 * @param user Username
	 * @param password Password
	 * @return Returns if connection was successful
	 */
	public boolean connectMySQL(String location, String user, String password)
		{
		boolean status=connectCustom("com.mysql.jdbc.Driver", "jdbc:mysql:"+location, user, password);
		id="MySQL://"+user+":"+password+"@"+location;
		return status;
		}
	
	
	/**
	 * Connect to SQLite database
	 * http://www.zentus.com/sqlitejdbc/index.html
	 * Create tables if they don't exist already.
	 * @param filename Filename for database or "" for memory database
	 * @return Returns if connection was successful
	 */
	public boolean connectSQLite(String filename)
		{
		boolean status=connectCustom("org.sqlite.JDBC", "jdbc:sqlite:"+filename, "","");
		id="SQLite://"+filename;
		return status;
		}
	
	
	/**
	 * Connect to Derby server
	 * @param location The location parameter
	 * @param user Username
	 * @param password Password
	 * @return Returns if connection was successful
	 */
	public boolean connectDerby(String location, String user, String password)
		{
		boolean status=connectCustom("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:"+location, user, password);
		id="Derby://"+user+":"+password+"@"+location;
		return status;
		}	
	
	/**
	 * Disconnect from database
	 */
	public void disconnect()
		{
		if(conn!=null)
			try
				{
				conn.close();
				conn=null;
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
		}

	/**
	 * Make sure database connection is closed ASAP
	 */
	public void finalize()
		{
		disconnect();
		}
		
	
	/**
	 * Check database compatibility. Suggest update if needed.
	 * @return If database is compatible with this program
	 */
	public boolean checkDatabase()
		{
		//Check if database is populated
		if(readDatabaseVersion())
			{
			System.out.println("Database populated since before, schema version "+majorVersion+"."+minorVersion);
			if(majorVersion<latestMajorVersion)
				{
				int answer=JOptionPane.showConfirmDialog(null, 
						"This database is too old for this program. Do you want to upgrade?",
						"Update?", JOptionPane.YES_NO_OPTION);
				if(answer==JOptionPane.YES_OPTION)
					{
					updateDatabase();
					return true;
					}
				else
					return false;
				}
			else if(majorVersion>latestMajorVersion)
				{
				JOptionPane.showMessageDialog(null, "This software is too old to read the database. Please upgrade.");
				return false;
				}
			else if(minorVersion<latestMinorVersion)
				{
				int answer=JOptionPane.showConfirmDialog(null, 
						"This database need to be upgraded to support the latest functions. " +
						"Do you want to upgrade? The function of the program is not guaranteed if run with old databases.",
						"Update?", JOptionPane.YES_NO_OPTION);
				if(answer==JOptionPane.YES_OPTION)
					updateDatabase();
				return true;
				}
			else if(minorVersion>latestMinorVersion)
				{
				JOptionPane.showMessageDialog(null, 
						"The database supports features not available in this software. It will " +
						"be run in backward compatibility mode. Consider updating");
				return true;
				}
			else
				{
				//Perfect version match
				System.out.println("No update required, proceeding");
				return true;
				}
			}
		else
			{
			//Database has not been populated. Do so now
			createNewTables();
			return true;
			}
		}
	
	
	/**
	 * Update database. Assumes version number has been read.
	 */
	public void updateDatabase()
		{
		JOptionPane.showMessageDialog(null, 
				"During update, you should be the only user. Make sure no one accesses the database during this operation");
		//Maybe still allow a way out
		//Add updates here
		}
	
	
	/**
	 * Create tables in this database for the latest format. Assumes the database is empty.
	 * Tables are described separately.
	 */
	public void createNewTables()
		{
		}


	
	

	
	
	
	
	
	
	
	
	/******************************************************************************************************
	 *                                         SQL Queries                                                *
	 *****************************************************************************************************/
	
	
	
	
	/**
	 * Read the version number
	 * @return If table exists
	 */
	public boolean readDatabaseVersion()
		{
		//TODO
		try
			{
			Statement stmt = createStatement();
			ResultSet rsmajor = stmt.executeQuery("SELECT * FROM vwb_db WHERE propname='formatmajorversion'");
			rsmajor.next(); majorVersion=rsmajor.getInt("propval");
			ResultSet rsminor = stmt.executeQuery("SELECT * FROM vwb_db WHERE propname='formatminorversion'");
			rsminor.next();	minorVersion=rsminor.getInt("propval");
			return true;
			}
		catch (SQLException e)
			{
			System.out.println("Table vwb_db not found in database. Assuming that the database is new.");
			//System.out.println(e.getMessage());
			//e.printStackTrace();
			return false;
			}			
		}
	
	/**
	 * Update the version property of the database
	 * @param major Major version
	 * @param minor Minor version
	 */
	public void setDatabaseVersion(int major, int minor)
		{
		//TODO
		try
			{
			Statement stmt = createStatement();
			stmt.executeUpdate("DELETE FROM vwb_db WHERE propname='formatmajorversion'");
			stmt.executeUpdate("DELETE FROM vwb_db WHERE propname='formatminorversion'");
			stmt.executeUpdate("INSERT INTO vwb_db VALUES('formatmajorversion',"+major+")");
			stmt.executeUpdate("INSERT INTO vwb_db VALUES('formatminorversion',"+minor+")");
			}
		catch (Exception e)
			{
			System.out.println(e.getMessage());
			e.printStackTrace();
			}			
		}
	
	
	
	
	/**
	 * Quick access to the database object
	 * @throws SQLException
	 */
	public Statement createStatement() throws SQLException
		{
		return conn.createStatement();
		}
	
	
	
	/**
	 * Quickly run a query
	 * @param s Query
	 * @return Statement or null if failed
	 */
	public ResultSet runQuery(String s) throws SQLException
		{
		//System.out.println(s);
		Statement stmt = createStatement();
		return stmt.executeQuery(s);
		}

	/**
	 * Quickly run an update
	 * @param s Query
	 */
	public void runUpdate(String s) throws SQLException
		{
		//System.out.println(s);
		Statement stmt = createStatement();
		stmt.executeUpdate(s);
		}


	}
