package util2.integrateExpression.compare;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import util2.integrateExpression.FindAnnotatedStrains;



import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.imageset.Imageset;




public class CompareSQL
	{
	public static Connection conn=null;

	static
	{
	String pass=JOptionPane.showInputDialog("Password?");
	connectPostgres("//pompeii.biosci.ki.se/mahogny", "postgres", pass);
	}

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
		Statement stmt = createStatement();
		return stmt.executeQuery(s);
		}
	
	public static void runUpdate(String s) throws SQLException
		{
		Statement stmt = createStatement();
		stmt.executeUpdate(s);
		}
	
	public static List<String> tagsFor(File ost)
		{
		LinkedList<String> tags=new LinkedList<String>();
		String ostid=ost.getName();
		
		try
			{
			PreparedStatement ps=conn.prepareStatement("select ostid,tag from osttags where ostid=?");
			ps.setString(1, ostid);
			ResultSet rs=ps.executeQuery();
			while(rs.next())
				tags.add(rs.getString(2));
			}
		catch (SQLException e)
			{
			e.printStackTrace();
			}

		if(tags.isEmpty())
			{
			EvData data=EvData.loadFile(ost);
			java.util.Iterator<Imageset> it=data.getIdObjects(Imageset.class).values().iterator();
			if(it.hasNext())
				{
				Imageset imset=it.next();
				tags.addAll(imset.tags);

				for(String tag:imset.tags)
					{
					try
						{
						PreparedStatement ps=conn.prepareStatement("insert into osttags values (?,?)");
						ps.setString(1, ostid);
						ps.setString(2, tag);
						ps.execute();
						}
					catch (SQLException e)
						{
						e.printStackTrace();
						}
					}
				if(tags.isEmpty())
					System.out.println("------------------------------------- no tags for "+ostid);
				}
			
			}
		
		return tags;
		}
	

	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		
		Set<String> argsSet=new HashSet<String>();
		for(String s:args)
			argsSet.add(s);
		
		//Find recordings to compare
		Set<File> datas=FindAnnotatedStrains.getAnnotated();
		System.out.println(datas);
		
		for(File f:datas)
			System.out.println(tagsFor(f));
		}


	/**
	 * How to get gene name from strain name?
	 * genotype makes more sense. deffiz claims it exists as a field
	 */
	public static String getGeneName(File data)
		{
		String name=data.getName();
		for(String tag:tagsFor(data))
			if(tag.startsWith("gfpgene:"))
				name=tag.substring("gfpgene:".length());
		return name;
		}
	
	}
