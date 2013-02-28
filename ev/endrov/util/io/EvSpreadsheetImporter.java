/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.io;

import java.io.*;
import java.util.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.*;
import org.jdom.Element;

/**
 * Import Excel and CSV tables
 * @author Johan Henriksson
 */
public class EvSpreadsheetImporter
	{
	public List<ArrayList<String>> rows=new LinkedList<ArrayList<String>>();
	

	/**
	 * Import CSV file given delimiter
	 */
	public void importCSV(Reader is, char fieldDelim, char quoteCharacter) throws IOException
		{
		rows.clear();

		int lastCapacity=0;
		
		
		Scanner s=new Scanner(is);
		while(s.hasNextLine())
			{
			//Read line
			String line=s.nextLine();
			LinkedList<Character> ll=new LinkedList<Character>();
			for(char c:line.toCharArray())
				ll.add(c);
			ArrayList<String> onerow=new ArrayList<String>(lastCapacity);
			
			//For every column
			while(!ll.isEmpty())
				{
				char c=ll.removeFirst();
				StringBuffer tok=new StringBuffer();
				if(c==quoteCharacter)
					{
					//Text within text delimiter
					for(;;)
						{
						while(ll.isEmpty())
							{
							line=s.nextLine();
							for(char c2:line.toCharArray())
								ll.add(c2);
							tok.append("\n");
							}
						c=ll.poll();
						if(c==quoteCharacter)
							break; 
						else
							tok.append(c);
						}
					onerow.add(tok.toString());
					if(!ll.isEmpty())
						ll.removeFirst(); //Move past next text delimiter
					}
				else
					{
					//No text delimiter
					tok.append(c);
					while(!ll.isEmpty())
						{
						c=ll.poll();
						if(c==fieldDelim)
							break;
						else
							tok.append(c);
						}
					onerow.add(tok.toString());
					}
				}
			
			rows.add(onerow);
			lastCapacity=onerow.size();
			}
		s.close();
		}
	
	
	/**
	 * Import Excel file
	 */
	public void importExcel(String filename) throws Exception
		{
		rows.clear();
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		//Take first sheet
		HSSFSheet sheet = wb.getSheetAt(0);

		int lastCapacity=0;
		for(int rowi=0;sheet.getRow(rowi)!=null;rowi++)
			{
			HSSFRow row = sheet.getRow(rowi);
			ArrayList<String> a=new ArrayList<String>(lastCapacity);

			for(int coli=0;row.getCell((short)coli)!=null;coli++)
				{
				HSSFCell c=row.getCell((short)coli);
				if(c.getCellType()==HSSFCell.CELL_TYPE_STRING)
					a.add(c.getRichStringCellValue().getString());
				else if(c.getCellType()==HSSFCell.CELL_TYPE_NUMERIC)
					a.add(""+c.getNumericCellValue());
				}
			rows.add(a);
			lastCapacity=a.size();
			}
		}
	
	
	public List<String> getColumnNames()
		{
		if(rows.size()==0)
			return new LinkedList<String>();
		else
			return rows.get(0);
		}
	
	
	public void show()
		{
		System.out.println("---start---");
		for(List<String> r:rows)
			{
			for(String s:r)
				System.out.print(s+"\t");
			System.out.println("");
			}
		System.out.println("---end---");
		}
	
	
	public void intoXML(Element e, String elementName)
		{
		for(int r=1;r<rows.size();r++)
			{
			Element e2=new Element(elementName);
			e.addContent(e2);
			
			List<String> row=rows.get(r);
			for(int c=0;c<getColumnNames().size();c++)
				{
				Element ea=new Element(getColumnNames().get(c));
				ea.addContent(row.get(c));
				e2.addContent(ea);
				}
			}
		}

	
	public ArrayList<String> readLine()
		{
		if(rows.isEmpty())
			return null;
		else
			return rows.remove(0);
		}
	}
