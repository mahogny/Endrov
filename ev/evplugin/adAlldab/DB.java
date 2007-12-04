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
	
	
//	private String sqlAutoincrement="PRIMARY KEY AUTOINCREMENT";
	private String sqlKeyAutoincrement="INTEGER PRIMARY KEY AUTOINCREMENT";
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
		sqlKeyAutoincrement="SERIAL PRIMARY KEY";
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
		sqlKeyAutoincrement="INTEGER PRIMARY KEY AUTO_INCREMENT";
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
		sqlKeyAutoincrement="INTEGER PRIMARY KEY AUTOINCREMENT";
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
		//TODO autoincrement
		try
			{
			Statement stmt = createStatement();
			/*********************************
			Information about this database. It is easier to read for example version information
			from a table than to use rather non-standardized commands for table information etc
			*/
			stmt.addBatch(
				"CREATE TABLE vwb_db ("+
				"    propname VARCHAR(64) PRIMARY KEY,"+
				"    propval  TEXT NOT NULL"+
				");");			
			/*********************************
			Nucleus general information
			*/
			String vwb_sampleinfo=
				" CREATE TABLE vwb_sampleinfo ("+
		    "     sampleid          "+sqlKeyAutoincrement+","+ //had AUTOINCREMENT in sqlite. SERIAL in postgresql
		    "     sample_upcount    INTEGER NOT NULL,"+
		    "     shell_x           REAL    NOT NULL,"+ //Set all shell param to 0 if it doesn't exist
		    "     shell_y           REAL    NOT NULL,"+
		    "     shell_z           REAL    NOT NULL,"+
		    "     shell_angle       REAL    NOT NULL,"+
		    "     shell_angleinside REAL    NOT NULL,"+
		    "     shell_major       REAL    NOT NULL,"+
		    "     shell_minor       REAL    NOT NULL,"+
		    "     sample_author     VARCHAR(1024) NOT NULL,"+
		    "     sample            VARCHAR(1024) NOT NULL UNIQUE,"+
		    " 	  sample_imageset   VARCHAR(1024) NOT NULL"+
				");";
			stmt.addBatch(vwb_sampleinfo);
			/*********************************
			Overall information about a sample.
			* Sample might not correspond to imageset name; scenario: multiple people
			  lineage the same thing independently and wish to compare. this requires one sample id each.
			* upcount is used in case multiple people are working on the same set.
			  after every atomic operation this variable is incremented by 1 in
			  database and in memory. if variable mismatches then client must
			  reload data. this variable should periodically be checked.
			* imageset obviously refers to which imageset was used. for convenience,
			  full path might work, but it is strategically better to just have the last
			  part of the name in case files are relocated (two groups have the set etc).
			*/
			String vwb_nuc=
				" CREATE TABLE vwb_nuc ("+
				" 		sampleid INTEGER NOT NULL,"+
				" 		nucid   INTEGER NOT NULL,"+
				" 		nucend  INTEGER,"+
				" 		nucname VARCHAR(1024) NOT NULL,"+
				" 		PRIMARY KEY(nucid),"+
				" 		FOREIGN KEY(sampleid) REFERENCES vwb_sampleinfo(sampleid) ON UPDATE CASCADE ON DELETE CASCADE"+
				" 		);";
			stmt.addBatch(vwb_nuc);
			/*********************************
			This is one coordinate for some nuclei.
			Nothing special
			*/
			String vwb_nuccoord=
				" CREATE TABLE vwb_nuccoord ("+
		    "     nucid    INTEGER NOT NULL,"+
		    "     frame    INTEGER NOT NULL,"+
		    "     nucx REAL   NOT NULL,"+
		    "     nucy REAL   NOT NULL,"+
		    "     nucz REAL   NOT NULL,"+
		    "     nucr REAL   NOT NULL,"+
		    "     PRIMARY KEY(nucid, frame),"+
		    "     FOREIGN KEY(nucid) REFERENCES vwb_nuc(nucid) ON UPDATE CASCADE ON DELETE CASCADE"+
				");";
			stmt.addBatch(vwb_nuccoord);
			/*********************************
			Lineage - which nuclei are parents and children
			*/
			String vwb_lineage=
				" CREATE TABLE vwb_lineage ("+
		    "     parentid INTEGER NOT NULL,"+
		    "     childid  INTEGER NOT NULL,"+
		    "     PRIMARY KEY(parentid,childid),"+
		    "     FOREIGN KEY(parentid) REFERENCES vwb_nuc(nucid) ON UPDATE CASCADE ON DELETE CASCADE,"+
		    "     FOREIGN KEY(childid)  REFERENCES vwb_nuc(nucid) ON UPDATE CASCADE ON DELETE CASCADE"+
				");";
			stmt.addBatch(vwb_lineage);
			/*********************************
			Frametime - The map between frame and time
			*/
			stmt.addBatch(
				" CREATE TABLE vwb_frametime ("+
		    "     sampleid  INTEGER NOT NULL,"+
		    "     frame     INTEGER NOT NULL,"+
		    "     frametime REAL   NOT NULL,"+
		    "     PRIMARY KEY(sampleid, frame, frametime),"+
		    "     FOREIGN KEY(sampleid) REFERENCES vwb_sampleinfo(sampleid) ON UPDATE CASCADE ON DELETE CASCADE"+
				");");
			/** The master view of nuclei */
			stmt.addBatch(
				" CREATE VIEW vwb_nucview AS"+
				" SELECT * FROM vwb_nuc NATURAL JOIN vwb_nuccoord NATURAL JOIN vwb_sampleinfo;");
			/** Nuclei that form roots of lineage trees */
			/** note. this one isn't used anymore iirc */
			stmt.addBatch(
				" CREATE VIEW vwb_lineage_root AS"+
				" SELECT parentid as nucid FROM vwb_lineage"+
				" WHERE NOT parentid IN (SELECT childid FROM vwb_lineage AS a);");			
			/** Helps keeping track of when some child begins */   
			/** maybe replace with lineageview */
			stmt.addBatch(
				" CREATE VIEW vwb_childframeafter AS"+
				" SELECT parentid as nucid, childid, childframe FROM"+
				" ((SELECT nucid as childid, frame as childframe FROM vwb_nuccoord) AS foo"+
				" NATURAL JOIN"+
				" vwb_lineage);");			
			/** Nuclei without parents */
			stmt.addBatch(
				" CREATE VIEW vwb_orphans AS"+
				" SELECT nucid, nucname FROM vwb_nuc AS nuc"+
				" WHERE NOT EXISTS( SELECT * FROM vwb_lineage AS lin WHERE nuc.nucid=lin.childid);");

			stmt.executeBatch();
			setDatabaseVersion(latestMajorVersion,latestMinorVersion);
			System.out.println("Tables created in database");
			}
		catch (Exception e)
			{
			System.out.println(e.getMessage());
			e.printStackTrace();
			}
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
	 * Create a frame for a nucleus. The function will make sure to remove any prior frame as to update the value
	 * @param sample Sample name
	 * @param nucname Name of nucleus
	 * @param frame Frame
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 * @param r radius
	 */
	/*
	public void makeNucFrame(String sample, String nucname, int frame, double x, double y, double z, double r)
		{
		String delexp=
			" DELETE FROM vwb_nuccoord WHERE frame="+frame+" AND"+
			" EXISTS (SELECT * FROM vwb_nucview"+
			" WHERE vwb_nucview.nucid=vwb_nuccoord.nucid"+
			" AND sample='"+sample+"' AND nucname='"+nucname+"')";		
		String insexp=
			" INSERT INTO vwb_nuccoord"+
			" SELECT nucid, "+frame+" as frame, "+x+" as nucx, "+y+" as nucy, "+z+" as nucz, "+r+" as nucr FROM"+
			" (SELECT * FROM"+
			" vwb_nuc NATURAL JOIN vwb_sampleinfo"+
			" WHERE sample='"+sample+"' AND nucname='"+nucname+"')";
		try
			{
			Statement stmt = createStatement();
			stmt.addBatch(delexp);
			stmt.addBatch(insexp);
			stmt.executeBatch();
			}
		catch (Exception e)
			{
			System.out.println(e.getMessage());
			e.printStackTrace();
			}
		}
	*/
	
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
