package endrov.typeParticleMeasure;

import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import endrov.typeParticleMeasure.ParticleMeasure.Particle;
import endrov.typeParticleMeasure.ParticleMeasure.Well;
import endrov.util.math.EvDecimal;

public class ParticleMeasureIO
	{

	
	
	

	
	/**
	 * Write data as a CSV-style table
	 */
	public static void saveCSV(ParticleMeasure pm, Writer io, boolean addHeader, String fieldDelim)
		{
		System.out.println("field delim:"+fieldDelim+":");
		
		PrintWriter pw=new PrintWriter(io);
		
		Set<String> col=pm.getColumns();
		
		//Add header
		if(addHeader)
			{
			pw.print("frame");
			pw.print(fieldDelim);
			pw.print("particle");
			for(String s:col)
				{
				pw.print(fieldDelim);
				pw.print(s);
				}
			pw.println();
			}

		//Write the data
		for(String wellName:pm.getWellNames())
			{
			Well well=pm.getWell(wellName);
			for(EvDecimal frame:well.getFrames())
				{
				for(Map.Entry<Integer, Particle> e:well.getFrame(frame).entrySet())
					{
					pw.print(wellName);
					pw.print(fieldDelim);
					pw.print(frame);
					pw.print(fieldDelim);
					pw.print(e.getKey());
					Particle p=e.getValue();
					//Map<String,Object> props=e.getValue().en;
					for(String columnName:col)
						{
						pw.print(fieldDelim);
						pw.print(p.getString(columnName));
						}
					pw.println();
					}
				
				}
			}
		
		pw.flush();
		}
	
	
	/**
	 * Save data to SQL database
	 */
	public static void saveSQL(ParticleMeasure pm, Connection conn, String dataid, String tablename) throws SQLException
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
	public static void createSQLtable(ParticleMeasure pm, Connection conn, String dataid, String tablename) throws SQLException
		{
		StringBuffer createTable=new StringBuffer();
		createTable.append("create table "+tablename+" (");
		createTable.append("dataid TEXT, well TEXT, frame DECIMAL, particle INTEGER");
		for(String column:pm.getColumns())
			createTable.append(", "+column+" DECIMAL"); //TODO types
		createTable.append(");");
		PreparedStatement stmCreateTable=conn.prepareStatement(createTable.toString());
		//for(String column:columns) //TODO also columns as ?
		stmCreateTable.execute();
		}

	/**
	 * Drop the entire table
	 */
	public static void dropSQLtable(Connection conn, String dataid, String tablename) throws SQLException
		{
		StringBuffer dropTable=new StringBuffer();
		dropTable.append("drop table "+tablename+";");
		PreparedStatement stmDropTable=conn.prepareStatement(dropTable.toString());
		stmDropTable.execute();
		}

	/**
	 * Delete these values from the SQL table
	 */
	public static void deleteFromSQLtable(Connection conn, String dataid, String tablename) throws SQLException
		{
		StringBuffer deleteTable=new StringBuffer();
		deleteTable.append("delete from "+tablename+" where dataid=?;");
		PreparedStatement stmDeleteTable=conn.prepareStatement(deleteTable.toString());
		stmDeleteTable.setString(1, dataid);
		stmDeleteTable.execute();
		}
		
	/**
	 * Insert values into table
	 */
	public static void insertIntoSQLtable(ParticleMeasure pm, Connection conn, String dataid, String tablename) throws SQLException
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
		PreparedStatement stmInsertTable=conn.prepareStatement(insert.toString());
		
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
