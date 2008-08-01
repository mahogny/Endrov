package util2.converter;

import java.io.*;
import java.util.*;

public class TransposeKaplan
	{

	public static int startIndex=5;
	public static int numdays=37;

	public static int firstAlive=15;
	
	public static String searchStrain="dpy";
	
	public static Vector<String> readLine(BufferedReader in) throws Exception
		{
		String line=in.readLine();
		if(line==null)
			return null;
		line=line+",";
		
		//StringTokenizer stok=new StringTokenizer(line,",");
		Vector<String> lineel=new Vector<String>();
		
		
		while(line.indexOf(',')!=-1)
			{
			int i=line.indexOf(',');
			String s=line.substring(0,i);
			//System.out.println("n "+s+" "+i);
			lineel.add(s);
			line=line.substring(i+1);
			}
		
		
//		while(stok.hasMoreElements())
//			lineel.add(stok.nextToken());
		while(lineel.size()<numdays+startIndex)
			lineel.add("");
//		System.out.println(lineel.get(0));
		return lineel;
		}
	
	public static void dropStart(Vector<String> lineel)
		{
		for(int i=0;i<startIndex;i++)
			lineel.remove(0);
		}
	
	public static Integer getPos(Vector<String> list,int i)
		{
		if(list.size()>i)
			{
			String s=list.get(i);
			if(s.equals(""))
				return null;
			else
				return Integer.parseInt(s);
			}
		else
			return null;
		}

	/*
	public static Integer getPosLeft(Vector<String> list,int i)
		{
		Integer here=getPos(list, i);
		if(here==null)
			{
			here=Integer.parseInt(list.get(i-1));
			list.set(i,""+here);
			}
		return here;
		}*/

	
	public static Integer getPos0(Vector<String> list,int i)
		{
		Integer here=getPos(list, i);
		if(here==null)
			return 0;
		else
			return here;
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		
		try
			{
			File fileIn=new File("/Volumes/TBU_main03/userdata/ivana/allplatesallfieldsmod01.csv");
			BufferedReader in=new BufferedReader(new FileReader(fileIn));
			File fileOut=new File("/Volumes/TBU_main03/userdata/ivana/outfile"+searchStrain+".csv");
			PrintWriter out=new PrintWriter(new FileWriter(fileOut));
//			BufferedWriter out=new BufferedWriter(new FileWriter(fileOut));
			
			out.println("some.txt");
			out.println("asdasdasdasd\t123");
			out.println("plate\tday\talive\tdead\tegl\tgone\tcorr.censor\tcomments");

			
			TreeMap<Integer, Vector<String>> preout=new TreeMap<Integer, Vector<String>>();
			
			
			
//			Vector<String> lineDate;
			int lastPlateNum=0;
			while((/*lineDate=*/readLine(in))!=null)
				{
				Vector<String> lineDay=readLine(in);
				Vector<String> lineAlive=readLine(in);
				Vector<String> lineGone=readLine(in);
				Vector<String> lineEgl=readLine(in);
				Vector<String> lineExpl=readLine(in);
				
				String strain=lineDay.get(0);
				if(!strain.equals(searchStrain))
					continue;
				
				String plateNums=lineDay.get(2);
				int plateNum=lastPlateNum+1;
				if(!plateNums.equals("backup"))
					plateNum=Integer.parseInt(lineDay.get(2));
				lastPlateNum=plateNum;
				
				dropStart(lineDay);
				dropStart(lineAlive);
				dropStart(lineGone);
				dropStart(lineEgl);
				dropStart(lineExpl);
				
				//Base case
				Integer lastNumAlive=firstAlive;
//				Integer lastNumAlive=getPos(lineAlive,0);
	//			for(int i=1;lastNumAlive==null;i++)
		//			lastNumAlive=getPos(lineAlive, 1);
				
				//Induction
				for(int day=0;day<numdays;day++)
					{
					Integer curAlive=getPos(lineAlive,day);
					if(curAlive==null)
						curAlive=lastNumAlive;
					lineAlive.set(day, ""+curAlive);
					

					//For this day and plate
					int curGone=getPos0(lineGone,day);
					int curEgl=getPos0(lineEgl,day);
					int curExpl=getPos0(lineExpl, day);
					int totCensor=curGone+curEgl+curExpl;
			
					if(curAlive>lastNumAlive)
						{
						System.out.println("!!!!! zombie !!!!");
						lastNumAlive=curAlive;
						}

					int curDead=lastNumAlive-curAlive-totCensor;
					int curDay=301+day;

					System.out.println("curalive "+curAlive+" wasalive "+lastNumAlive+" totcensor "+totCensor+" "+curDead);
					if(curDead<0)
						{
						System.out.println(""+curGone+" "+curEgl+" "+curExpl);
						totCensor=0;
						curDead=0;
						}
					
					
					Vector<String> preoutOne=preout.get(curDay);
					if(preoutOne==null)
						preout.put(curDay, preoutOne=new Vector<String>());
					String outs=""+plateNum+"\t"+curDay+"\t"+curAlive+"\t"+curDead+"\t"+curEgl+"\t"+curExpl+"\t"+totCensor;
					preoutOne.add(outs);
					
					//next
					lastNumAlive=curAlive;					
					}
				
				
				
				}
			
			//out: 
			for(Vector<String> lines:preout.values())
				for(String s:lines)
					out.println(s);
			out.close();
			
			}
		catch (Exception e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}

		
		
		
		
		// TODO Auto-generated method stub

		}

	}
