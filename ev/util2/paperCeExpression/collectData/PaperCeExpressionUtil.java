package util2.paperCeExpression.collectData;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import util2.paperCeExpression.integrate.ExpUtil;




import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.imageset.Imageset;
import endrov.util.Tuple;




public class PaperCeExpressionUtil
	{
	public static Connection conn=null;

	static
	{
	String pass=JOptionPane.showInputDialog("Password?");
	if(!connectPostgres("//localhost/tbudev3", "tbudev3", pass))
//	if(!connectPostgres("//pompeii.biosci.ki.se/mahogny", "postgres", pass))
		{
		System.out.println("-------- failed to connect");
		System.exit(1);
		}
	else
		System.out.println("-------- connected to sql");
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
			System.out.println("--"+ost+"--");
			System.out.println(data);
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


	public static Set<File> getAnnotated()
		{
		Set<File> doneStrains=new TreeSet<File>();
		for(File parent:new File[]{new File("/Volumes/TBU_main06/ost4dgood")})
			for(File f:parent.listFiles())
				if(f.getName().endsWith(".ost"))
					{
					if(new File(f,"tagDone4d.txt").exists())
						doneStrains.add(f);
					}
		return doneStrains;
		}


	public static Set<File> getTestSet()
		{
		Set<File> doneStrains=new TreeSet<File>();
//		doneStrains.add(new File("/Volumes/TBU_main06/ost4dgood/AH142_070827.ost"));
//		doneStrains.add(new File("/Volumes/TBU_main06/ost4dgood/AH142_070828.ost"));
		//doneStrains.add(new File("/Volumes/TBU_main06/ost4dgood/TB2098_080217.ost"));
		//doneStrains.add(new File("/Volumes/TBU_main06/ost4dgood/TB2098_080324.ost"));
		
		doneStrains.add(new File("/Volumes/TBU_main06/ost4dgood/BC10721_071109.ost"));
		
		doneStrains.add(new File("/Volumes/TBU_main06/ost4dgood/TB2161_071119.ost"));
		doneStrains.add(new File("/Volumes/TBU_main06/ost4dgood/BC15197i3_070417.ost"));
		/*doneStrains.add(new File("/Volumes/TBU_main06/ost4dgood/BC10075_070612.ost"));*/
		
		return doneStrains;
		}


	/**
	 * Find strains not annotated
	 * @param args
	 */
	public static void main2(String[] args)
		{
		Set<String> strains=new TreeSet<String>();
		Set<String> doneStrains=new TreeSet<String>();
		
		
		for(File parent:new File[]{
				new File("/Volumes/TBU_main01/ost4dgood"),
				new File("/Volumes/TBU_main02/ost4dgood"),
				new File("/Volumes/TBU_main03/ost4dgood"),
				new File("/Volumes/TBU_main04/ost4dgood"),
		})
			for(File f:parent.listFiles())
				if(f.getName().endsWith(".ost"))
					{
					
					Tuple<String,String> nameDate=ExpUtil.nameDateFromOSTName(f.getName());
					
					String strainName=nameDate.fst();
					
					strains.add(strainName);
					
					if(new File(f,"tagDone4d.txt").exists())
						doneStrains.add(strainName);
					}
		
		for(String strain:strains)
			if(!doneStrains.contains(strain))
				System.out.println("Appears missing: "+strain);
		System.exit(0);
		}

	
	/**
	 * Show tags for annotated recordings
	 * @param args
	 */
	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		
		Set<String> argsSet=new HashSet<String>();
		for(String s:args)
			argsSet.add(s);
		
		//Find recordings to compare
		Set<File> datas=PaperCeExpressionUtil.getAnnotated();
		System.out.println(datas);
		
		for(File f:datas)
			System.out.println(tagsFor(f));
		}


	}
