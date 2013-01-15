package endrov.imagesetBD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Reader for INI-files
 * @author Johan Henriksson
 *
 */
public class IniFile
	{

	public static class Section
		{
		Map<String,String> prop=new HashMap<String, String>(); 
		}
	
	
	public Map<String,Section> section=new HashMap<String, Section>(); 
	
	public IniFile(File f) throws IOException
		{
		BufferedReader r=new BufferedReader(new FileReader(f));
		String line=r.readLine();
		for(;;)
			{
			if(line==null)
				break;
			
			String sectionName=line.substring(1, line.length()-1);
			System.out.println(sectionName);
			Section s=new Section();
			section.put(sectionName, s);
			
			//Read attr=value
			while((line=r.readLine())!=null && !line.startsWith("["))
				{
				int index=line.indexOf('=');
				String attr=line.substring(0,index);
				String value=line.substring(index+1);
				s.prop.put(attr,value);
				}
			}
		r.close();
		}
	
	
	
	
	
	}
