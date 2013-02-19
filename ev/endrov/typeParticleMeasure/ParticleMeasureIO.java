package endrov.typeParticleMeasure;

import java.io.IOException;
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
import endrov.typeParticleMeasure.ParticleMeasure.ColumnSet;
import endrov.typeParticleMeasure.ParticleMeasure.Well;
import endrov.util.io.EvCSVWriter;
import endrov.util.io.EvSpreadsheetImporter;
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
	public static void writeCSVperwell(ParticleMeasure pm, Writer io, boolean addHeader, String fieldDelim, boolean quote) throws IOException
		{
		EvCSVWriter csvWriter=new EvCSVWriter(io, fieldDelim, quote);
		
//		System.out.println("field delim:"+fieldDelim+":");
		
		LinkedList<String> colWithSpecial=new LinkedList<String>();
		colWithSpecial.add("well");
		colWithSpecial.addAll(pm.getParticleColumns());
		
		//Add header
		if(addHeader)
			{
			for(String s:colWithSpecial)
				csvWriter.writeEntry(s);
			csvWriter.writeEndOfLine();
			}

		//For all wells
		for(String wellName:pm.getWellNames())
			{
			//For all frames
			Well well=pm.getWell(wellName);
			ColumnSet p=well.getWellColumns();
			for(String columnName:colWithSpecial)
				{
				//Get column data
				String colData;
				if(columnName.equals("well"))
					colData=wellName;
				else
					colData=p.getString(columnName);

				//Print field
				csvWriter.writeEntry(colData);
				}
			csvWriter.writeEndOfLine();
			}
		csvWriter.close();
		}

	
	
	/**
	 * Write data as a CSV-style table
	 */
	public static void writeCSVperframe(ParticleMeasure pm, Writer io, boolean addHeader, String fieldDelim, boolean quote) throws IOException
		{
		EvCSVWriter csvWriter=new EvCSVWriter(io, fieldDelim, quote);
		
//		System.out.println("field delim:"+fieldDelim+":");
		
		LinkedList<String> colWithSpecial=new LinkedList<String>();
		colWithSpecial.add("well");
		colWithSpecial.add("frame");
		colWithSpecial.addAll(pm.getParticleColumns());
		
		//Add header
		if(addHeader)
			{
			for(String s:colWithSpecial)
				csvWriter.writeEntry(s);
			csvWriter.writeEndOfLine();
			}

		//For all wells
		for(String wellName:pm.getWellNames())
			{
			//For all frames
			Well well=pm.getWell(wellName);
			for(EvDecimal frame:well.getFrames())
				{
				
				ColumnSet p=well.getFrame(frame).getFrameColumns();
				for(String columnName:colWithSpecial)
					{
					//Get column data
					String colData;
					if(columnName.equals("well"))
						colData=wellName;
					else if(columnName.equals("frame"))
						colData=frame.toString();
					else
						colData=p.getString(columnName);

					//Print field
					csvWriter.writeEntry(colData);
					}
				csvWriter.writeEndOfLine();
				}
			}
		csvWriter.close();
		}

	
	
	
	/**
	 * Write data as a CSV-style table
	 */
	public static void writeCSVperparticle(ParticleMeasure pm, Writer io, boolean addHeader, String fieldDelim, boolean quote) throws IOException
		{
		EvCSVWriter csvWriter=new EvCSVWriter(io, fieldDelim, quote);
		
//		System.out.println("field delim:"+fieldDelim+":");
		
		LinkedList<String> colWithSpecial=new LinkedList<String>();
		colWithSpecial.add("well");
		colWithSpecial.add("frame");
		colWithSpecial.add("particle");
		colWithSpecial.addAll(pm.getParticleColumns());
		
		//Add header
		if(addHeader)
			{
			for(String s:colWithSpecial)
				csvWriter.writeEntry(s);
			csvWriter.writeEndOfLine();
			}

		//For all wells
		for(String wellName:pm.getWellNames())
			{
			Well well=pm.getWell(wellName);
			//For all frames
			for(EvDecimal frame:well.getFrames())
				{
				//For all particles
				for(Map.Entry<Integer, ColumnSet> e:well.getFrame(frame).entrySet())
					{
					ColumnSet p=e.getValue();
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

						//Print field
						csvWriter.writeEntry(colData);
						}
					csvWriter.writeEndOfLine();
					}
				
				}
			}
		csvWriter.close();
		}

	
	
	/**
	 * Write data as a CSV-style table
	 */
	public static void readCSV(ParticleMeasure pm, Reader is, char fieldDelim) throws IOException
		{
//		pm.clearData();
		
		EvSpreadsheetImporter importer=new EvSpreadsheetImporter();
		importer.importCSV(is, fieldDelim, '\"');

		//Read header
		ArrayList<String> header=importer.readLine();
		boolean addedHeader=false;
		header.remove("well");
		header.remove("particle");
		header.remove("frame");

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
			
			ParticleMeasure.ColumnSet p;
			if(frameNum==null)
				{
				//This is a per-well file
				p=well.getWellColumns();

				if(!addedHeader)
					{
					for(String h:header)
						pm.addWellColumn(h);
					addedHeader=true;
					}

				}
			else
				{
				//Get frame
				ParticleMeasure.Frame frame=well.getFrame(frameNum);
				if(frame==null)
					{
					frame=new Frame();
					well.setFrame(frameNum, frame);
					}

				//Get particle, if CSV is for particles
				if(particleID==null)
					{
					//This is a per-frame file
					p=frame.getFrameColumns();
					if(!addedHeader)
						{
						for(String h:header)
							pm.addFrameColumn(h);
						addedHeader=true;
						}
					}
				else
					{
					//This is a file for particles
					p=frame.getCreateParticle(particleID);
					if(!addedHeader)
						{
						for(String h:header)
							pm.addParticleColumn(h);
						addedHeader=true;
						}
					}
				}

			//Add all attributes
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
		for(String column:pm.getParticleColumns())
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
		Set<String> col=pm.getParticleColumns();

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
				for(Map.Entry<Integer, ColumnSet> e:well.getFrame(frame).entrySet())
					{
					int particleID=e.getKey();
					stmInsertTable.setInt(4, particleID);
					
					ColumnSet particle=e.getValue();
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
