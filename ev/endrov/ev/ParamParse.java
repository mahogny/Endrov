package endrov.ev;

import java.io.*;
import java.util.*;


public class ParamParse
	{
	StringTokenizer ft;

	/**
	 * Parse parameters from a file
	 * @param file File to read
	 */
	public ParamParse(File file)
		{
		String filecontent=readFile(file);
		if(filecontent==null)
			filecontent="";
		ft = new StringTokenizer(filecontent,";");
		}


	/**
	 * Parse parameters from a string
	 * @param s String to read
	 */
	public ParamParse(String s)
		{
		if(s==null)
			s="";
		ft = new StringTokenizer(s,";");
		}
	
	
	/**
	 * Check if there is more data to read
	 */
	public boolean hasData()
		{
		return ft.hasMoreTokens();
		}
	
	/**
	 * Obtain the next data post from the file
	 * @return Arguments
	 */
	public Vector<String> nextData()
		{
		if(ft.hasMoreTokens())
			{
			String tok=ft.nextToken();
			Vector<String> arg=new Vector<String>();
			
			char[] list=tok.toCharArray();

			int i=0;
			for(;;)
				{
				//Skip whitespace
				while(i<list.length && isWhite(list[i]))
					i++;

				//Did we reach the end?
				if(i>=list.length)
					break;
				
				//Read argument
				String curarg="";
				if(list[i]=='"')
					{
					i++;
					while(list[i]!='"')
						{
						curarg+=list[i];
						i++;
						}
					arg.add(curarg);
					i++;
					}
				else
					{
					while(i<list.length && !isWhite(list[i]))
						{
						curarg+=list[i];
						i++;
						}
					if(curarg!="")
						arg.add(curarg);
					}
				}
			
			
			return arg;
			}
		else
			return null;
		}
	
	
	/**
	 * Check if a character is a whitespace character
	 */
	private boolean isWhite(char c)
		{
		return c==' ' || c=='\t' || c=='\n' || c=='\r';
		}
	
	
	/**
	 * Read a file off disk
	 * @param aFile File to read
	 * @return Content of file or null
	 */
	private static String readFile(File aFile)
		{
		
		StringBuffer contents = new StringBuffer();

    BufferedReader input = null;
    try
    	{
      input = new BufferedReader( new FileReader(aFile) );
      String line = null;
      while (( line = input.readLine()) != null)
      	{
        contents.append(line);
        contents.append(" ");
        //       contents.append(System.getProperty("line.separator"));
      	}
    	}
    catch (FileNotFoundException ex)
    	{
    	Log.printError("File not found: "+aFile,null);
    	return null;
    	}
    catch (IOException ex)
    	{
      ex.printStackTrace();
      System.exit(1);
    	}
    finally
    	{
      try
      	{
        if (input!= null)
          input.close();
      	}
      catch (IOException ex)
      	{
        ex.printStackTrace();
        System.exit(1);
      	}
    	}
    return contents.toString();
    }
		
	
	}
