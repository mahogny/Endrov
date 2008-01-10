package evplugin.customData;

import java.io.*;
import java.util.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.*;
import org.jdom.Element;

/**
 * Import Excel and CSV tables
 * @author Johan Henriksson
 */
public class ImportTable
	{
	public Vector<Vector<String>> rows=new Vector<Vector<String>>();
	

	/**
	 * Import CSV file given delimiter
	 */
	public void importCSV(String filename, char fieldDelim, char textDelim) throws Exception
		{
		rows.clear();

		Scanner s=new Scanner(new FileInputStream(filename));
		while(s.hasNextLine())
			{
			String line=s.nextLine();
			LinkedList<Character> ll=new LinkedList<Character>();
			for(char c:line.toCharArray())
				ll.add(c);
			Vector<String> row=new Vector<String>();
			rows.add(row);
			
			//For every column
			while(!ll.isEmpty())
				{
				char c=ll.poll();
				StringBuffer tok=new StringBuffer();
				if(c==textDelim)
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
						if(c==textDelim)
							break; 
						else
							tok.append(c);
						}
					row.add(tok.toString());
					if(!ll.isEmpty())
						ll.poll(); //Move past next text delimiter
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
					System.out.println("# "+tok.toString());
					row.add(tok.toString());
					}
				}
			}
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

		for(int rowi=0;sheet.getRow(rowi)!=null;rowi++)
			{
			HSSFRow row = sheet.getRow(rowi);
			Vector<String> a=new Vector<String>();

			for(int coli=0;row.getCell((short)coli)!=null;coli++)
				{
				HSSFCell c=row.getCell((short)coli);
				if(c.getCellType()==HSSFCell.CELL_TYPE_STRING)
					a.add(c.getRichStringCellValue().getString());
				else if(c.getCellType()==HSSFCell.CELL_TYPE_NUMERIC)
					a.add(""+c.getNumericCellValue());
				}
			rows.add(a);
			}
		}
	
	
	public Vector<String> getColumnNames()
		{
		if(rows.size()==0)
			return new Vector<String>();
		else
			return rows.get(0);
		}
	
	
	public void show()
		{
		System.out.println("---start---");
		for(Vector<String> r:rows)
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
			
			Vector<String> row=rows.get(r);
			for(int c=0;c<getColumnNames().size();c++)
				{
				Element ea=new Element(getColumnNames().get(c));
				ea.addContent(row.get(c));
				e2.addContent(ea);
				}
			}
		}
	
	}
