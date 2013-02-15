package endrov.typeParticleMeasure;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import endrov.core.EvSQLConnection;
import endrov.typeParticleMeasure.ParticleMeasure.Frame;
import endrov.typeParticleMeasure.ParticleMeasure.Particle;
import endrov.typeParticleMeasure.ParticleMeasure.Well;
import endrov.util.io.EvSpreedsheetImporter;
import endrov.util.math.EvDecimal;

/**
 * Methods for importing/exporting measured data
 * 
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureIO
	{
	
	/**
	 * Write data as a CSV-style table
	 */
	public static void saveCSV(ParticleMeasure pm, Writer io, boolean addHeader, String fieldDelim, boolean quote)
		{
//		System.out.println("field delim:"+fieldDelim+":");
		
		PrintWriter pw=new PrintWriter(io);
		
		LinkedList<String> colWithSpecial=new LinkedList<String>();
		colWithSpecial.add("well");
		colWithSpecial.add("frame");
		colWithSpecial.add("particle");
		colWithSpecial.addAll(pm.getColumns());
		
		//Add header
		if(addHeader)
			{
			for(String s:colWithSpecial)
				{
				pw.print(fieldDelim);
				if(quote)
					pw.print("\"");
				pw.print(s);
				if(quote)
					pw.print("\"");
				}
			pw.println();
			}

		//For all wells
		for(String wellName:pm.getWellNames())
			{
			Well well=pm.getWell(wellName);
			//For all frames
			for(EvDecimal frame:well.getFrames())
				{
				//For all particles
				for(Map.Entry<Integer, Particle> e:well.getFrame(frame).entrySet())
					{
					boolean firstCol=true;
					Particle p=e.getValue();
					for(String columnName:colWithSpecial)
						{
						//Get column data
						String colData;
						if(columnName.equals("well"))
							colData=wellName;
						else if(columnName.equals("frame"))
							colData=frame.toString();
						else if(columnName.equals("particle"))
							colData=e.getKey().toString();
						else
							colData=p.getString(columnName);

						//Handle special chars
						colData.replace("\\", "\\\\");
						colData.replace("\n", "\\n");
						colData.replace(fieldDelim, "\\"+fieldDelim);
						
						//Print field
						if(!firstCol)
							pw.print(fieldDelim);
						firstCol=false;
						if(quote)
							pw.print("\"");
						pw.print(colData);
						if(quote)
							pw.print("\"");
						}
					pw.println();
					}
				
				}
			}
		
		pw.flush();
		}

	
	
	/**
	 * Write data as a CSV-style table
	 */
	public static void readCSV(ParticleMeasure pm, Reader is, char fieldDelim) throws IOException
		{
		pm.clearData();
		
		EvSpreedsheetImporter importer=new EvSpreedsheetImporter();
		importer.importCSV(is, fieldDelim, '\"');

		//Read header
		ArrayList<String> header=importer.readLine();
		for(String h:header)
			if(!(h.equals("well") || h.equals("particle") || h.equals("frame")))
				pm.addColumn(h);

		int genParticleID=0;
		
		ArrayList<String> line;
		while((line=importer.readLine())!=null)
			{
			//Read line

			String wellName=null;
			Integer particleID=null;
			EvDecimal frameNum=null;
			HashMap<String, Object> values=new HashMap<String, Object>();
			for(int i=0;i<header.size();i++)
				{
				String h=header.get(i);
				String c=line.get(i);
				if(h.equals("well"))
					wellName=c;
				else if(h.equals("particle"))
					particleID=Integer.parseInt(c);
				else if(h.equals("frame"))
					frameNum=new EvDecimal(c);
				else
					values.put(h,Double.parseDouble(c)); //Right now treat all as doubles
				}

			if(wellName==null)
				throw new IOException("Column data for well missing");
			ParticleMeasure.Well well=pm.getWell(wellName);
			if(well==null)
				{
				well=new Well();
				pm.setWell(wellName, well);
				}
			
			if(frameNum==null)
				throw new IOException("Column data for frame missing");
			ParticleMeasure.Frame frame=well.getFrame(frameNum);
			if(frame==null)
				{
				frame=new Frame();
				well.setFrame(frameNum, frame);
				}

			if(particleID==null)
				particleID=genParticleID++;
			ParticleMeasure.Particle p=frame.getCreateParticle(particleID);
			
			for(Map.Entry<String, Object> e:values.entrySet())
				p.put(e.getKey(), e.getValue());
			}
		
		}

	
	/**
	 * Save data to SQL database
	 */
	public static void saveSQL(ParticleMeasure pm, EvSQLConnection conn, String dataid, String tablename) throws SQLException
		{
		dropSQLtable(conn, dataid, tablename);

		//Clean up this dataid
		deleteFromSQLtable(conn, dataid, tablename);
		
		//Create table if needed. Make sure it has the right columns
		createSQLtable(pm, conn, dataid, tablename);
		
		
		//Insert all data
		insertIntoSQLtable(pm, conn, dataid, tablename);
		
		}

	/**
	 * Create the table
	 */
	public static void createSQLtable(ParticleMeasure pm, EvSQLConnection conn, String dataid, String tablename) throws SQLException
		{
		StringBuffer createTable=new StringBuffer();
		createTable.append("create table "+tablename+" (");
		createTable.append("dataid TEXT, well TEXT, frame DECIMAL, particle INTEGER");
		for(String column:pm.getColumns())
			createTable.append(", "+column+" DECIMAL"); //TODO types
		createTable.append(");");
		PreparedStatement stmCreateTable=conn.getConnection().prepareStatement(createTable.toString());
		//for(String column:columns) //TODO also columns as ?
		stmCreateTable.execute();
		}

	/**
	 * Drop the entire table
	 */
	public static void dropSQLtable(EvSQLConnection conn, String dataid, String tablename) throws SQLException
		{
		StringBuffer dropTable=new StringBuffer();
		dropTable.append("drop table "+tablename+";");
		PreparedStatement stmDropTable=conn.getConnection().prepareStatement(dropTable.toString());
		stmDropTable.execute();
		}

	/**
	 * Delete these values from the SQL table
	 */
	public static void deleteFromSQLtable(EvSQLConnection conn, String dataid, String tablename) throws SQLException
		{
		StringBuffer deleteTable=new StringBuffer();
		deleteTable.append("delete from "+tablename+" where dataid=?;");
		PreparedStatement stmDeleteTable=conn.getConnection().prepareStatement(deleteTable.toString());
		stmDeleteTable.setString(1, dataid);
		stmDeleteTable.execute();
		}
		
	/**
	 * Insert values into table
	 */
	public static void insertIntoSQLtable(ParticleMeasure pm, EvSQLConnection conn, String dataid, String tablename) throws SQLException
		{
		Set<String> col=pm.getColumns();

		StringBuffer insert=new StringBuffer();
		insert.append("insert into "+tablename+" (");
		insert.append("dataid, well, frame, particle");
		for(String column:col)
			insert.append(","+column); //TODO types
		insert.append(") VALUES (");
		insert.append("?, ?, ?, ?");
		for(int i=0;i<col.size();i++)
			insert.append(",?");
		insert.append(");");
		
		System.out.println(insert);
		PreparedStatement stmInsertTable=conn.getConnection().prepareStatement(insert.toString());
		
		stmInsertTable.setString(1, dataid);
		for(String wellName:pm.getWellNames())
			{
			Well well=pm.getWell(wellName);
			stmInsertTable.setString(2, wellName);
			for(EvDecimal frame:well.getFrames())
				{
				stmInsertTable.setBigDecimal(3, frame.toBigDecimal());			
				for(Map.Entry<Integer, Particle> e:well.getFrame(frame).entrySet())
					{
					int particleID=e.getKey();
					stmInsertTable.setInt(4, particleID);
					
					Particle particle=e.getValue();
					int colid=5;
					for(String columnName:col)
						{
						Object p=particle.getObject(columnName);
						if(p instanceof Double)
							stmInsertTable.setDouble(colid, (Double)p);
						else if(p instanceof Integer)
							stmInsertTable.setInt(colid, (Integer)p);
						else
							stmInsertTable.setInt(colid, (Integer)(-1));
						colid++;
						}
					
					stmInsertTable.execute();
					}
				}
			}
		}

	
	
	
	}
