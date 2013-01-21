package endrov.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * A connection to an SQL database
 * 
 * @author Johan Henriksson
 *
 */
public class EvSQLConnection
	{
	public static final String sqlDriverPostgres="org.postgresql.Driver";
	public static final String sqlDriverMysql="com.mysql.jdbc.Driver";

	
	public String connDriver=sqlDriverPostgres;
	public String connURL="jdbc:postgresql://localhost/mydb";
	private String connUser="";
	private String connPass="";

	private Connection conn;

	
	public Connection getConnection()
		{
		return conn;
		}
	
	@Override
	public String toString()
		{
		return connUser+"@"+connURL;
		}
	
	public void setUserPass(String user, String pass)
		{
		this.connUser=user;
		this.connPass=pass;
		}
	
	public boolean connect() throws SQLException
		{
    try
			{
			Class.forName(connDriver).newInstance();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			throw new SQLException("Could not find JDBC driver class");
			}

    
    try
			{
			conn = DriverManager.getConnection(connURL, connUser, connPass);
			return true;
			}
		catch (SQLException e)
			{
			return false;
			}      
		}
	
	
	public void disconnect() throws SQLException
		{
		if(conn!=null)
			conn.close();
		conn=null;
		}


	public static String[] getCommonSQLdrivers()
		{
		return new String[]{
				sqlDriverPostgres,
				sqlDriverMysql
		};
		}
	
	}
