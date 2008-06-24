package util2;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class ConvertFilemaker
	{
	public static void main(String arg[])
		{
		try
			{
			BufferedReader bf=new BufferedReader(new FileReader(new File("/Volumes/TBU_main02/recordinfiles.csv")));
			System.out.println("+++");
			String line;
			while((line=bf.readLine())!=null)
				{
				List<String> str=new LinkedList<String>();
				int index;
				line=line.substring(1)+",\"";
				while((index=line.indexOf("\",\""))!=-1)
					{
					String ss=line.substring(0,index);
					line=line.substring(index+3);
					System.out.println("-"+ss);
					str.add(ss);
					}
				
				File imsetFile=new File(str.get(2));
				File imservFile=new File(imsetFile.getParentFile(),imsetFile.getName()+".imserv");
				System.out.println(imservFile);

				String descLine=str.get(4);
				if(descLine.indexOf("")!=-1)
					{
					//add tag for this?
					}
				
				String author=str.get(11);
				if(author.length()>0)
					;
				//Tag author
				
				
				//tag suggestions
				//embryo
				//L1, L2, dauer etc
				//gene=....
				//strain=...
				//pol-gamma
				//histone::mCherry
				//Nile red
				//confocal
				//EM
				//hoechst
				//3d
				//4d
				//failed?
				//author=Akram Abouzied
				//agmount
				
				//can show a tree list for =-tags
				
				System.out.println("---");
				System.out.println("---");
				System.out.println("---");
				
				
				
				
				}
			}
		catch (FileNotFoundException e)
			{
			e.printStackTrace();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
		}
	}
