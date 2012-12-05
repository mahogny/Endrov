package util2.paperCeExpression.compareYanai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowColocalization.ColocCoefficients;
import endrov.lineage.Lineage;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;


public class MainToR
	{
	
	public static class Average
		{
		double val;
		int count;
		public void add(double v)
			{
			val+=v;
			count++;
			}
		
		public double get()
			{
			return val/count;
			}
		
		@Override
		public String toString()
			{
			return ""+get();
			}
		}

	public static void main(String[] args)
			{
			EvLog.addListener(new EvLogStdout());
			EV.loadPlugins();

			Map<String,String> whichStage=new HashMap<String, String>();
			whichStage.put("GSM380366","95");
			whichStage.put("GSM380367","55");
			whichStage.put("GSM380368","28");
			whichStage.put("GSM380369","4");
			whichStage.put("GSM380370","4");
			//which.put("GSM380371","");
			whichStage.put("GSM380372","190");
			whichStage.put("GSM380373","190");
			whichStage.put("GSM380374","95");
			whichStage.put("GSM380375","55");
			whichStage.put("GSM380376","28");
			whichStage.put("GSM380377","28");
			whichStage.put("GSM380378","4");
			//which.put("GSM380379","");
			//which.put("GSM380380","");
			whichStage.put("GSM380381","95");

			Map<String,Map<String,Average>> yanaiStageAvg=new HashMap<String, Map<String,Average>>();
			
			try
				{
				SOFTFile soft=new SOFTFile(new File("/home/tbudev3/Desktop/yanai/GSE15234_family.soft"));
				
				SOFTFile.Entity eSeries=soft.entitiesOfType("PLATFORM").values().iterator().next();//getEntity("SERIES");
				int colORF=eSeries.getColumnIndex("ORF");
				int colID=eSeries.getColumnIndex("ID");
				
				//Build map from ID->ORF
				Map<String,String> mapId2ORF=new HashMap<String, String>();
				for(List<String> line:eSeries.dataLine)
					mapId2ORF.put(line.get(colID),line.get(colORF));
				
				//System.out.println(mapId2ORF);
								
				for(Map.Entry<String,SOFTFile.Entity> eSampleE:soft.entitiesOfType("SAMPLE").entrySet())
					{
					String stage=whichStage.get(eSampleE.getKey());
					Map<String,Average> avgs=yanaiStageAvg.get(stage);
					if(avgs==null)
						yanaiStageAvg.put(stage, avgs=new HashMap<String, Average>());

					SOFTFile.Entity eSample=eSampleE.getValue();
					
					int colRefId=eSample.getColumnIndex("ID_REF");
					int colValue=eSample.getColumnIndex("VALUE");
					
					for(int i=0;i<eSample.dataLine.size();i++)
						{
						String wormbaseID=mapId2ORF.get(eSample.dataLine.get(i).get(colRefId));
						String val=eSample.dataLine.get(i).get(colValue);
						//System.out.println(wormbaseID+"   "+val);
						
						Average a=avgs.get(wormbaseID);
						if(a==null)
							avgs.put(wormbaseID, a=new Average());
						
						a.add(Double.parseDouble(val));
						
						}
					
					
					
					}
				
				
				Set<String> tfs=getTFlist();
				Map<String,String> mapWbid2ORF=getMapWbid2ORF();
				Map<String,String> mapORF2genename=getMapORF2genename();
				
				////// Summarize
				String[] stageArr=new String[]{"4","28","55","95","190"};
				PrintWriter pw=new PrintWriter(new FileWriter("/home/tbudev3/Dropbox/projects/ceh5/tftimecourse.txt"));
				pw.print("genename\t");
				for(String s:stageArr)
					pw.print(s+"\t");
				pw.println();
				for(String gene:yanaiStageAvg.get("4").keySet())
					{
					//Pick only TFs
					String orf=mapWbid2ORF.get(gene);
					if(tfs.contains(orf))
						{
						//System.out.println(orf);
						
						//Get gene name
						String genename=mapORF2genename.get(orf);
						if(genename==null)
							throw new RuntimeException("null name for "+orf);
						
						if(orf!=null && orf.equals("M05B5.5"))
							System.out.println("wheeeeeeeeeeee "+genename);
						
						pw.print(genename);
						pw.print("\t");
						for(String stageName:stageArr)
							{
							Average level=yanaiStageAvg.get(stageName).get(gene);
							if(level==null)
								System.out.println("eeek");
							pw.print(level.get());
							pw.print("\t");
							}
						pw.println();
						}
					
					}
				pw.close();
				
				

				}
			catch (IOException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			
			
			}
	
	
	private static Set<String> getTFlist() throws IOException
		{
		Set<String> set=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("/home/tbudev3/Desktop/yanai/tf.csv")));
		String line;
		while((line=br.readLine())!=null)
			set.add(line);
		return set;
		}
	
	
	private static Map<String,String> getMapORF2genename() throws IOException
	{
	Map<String,String> m=new HashMap<String, String>();
	BufferedReader br=new BufferedReader(new FileReader(new File("/home/tbudev3/Desktop/yanai/genenames.txt")));
	br.readLine(); //Skip first line
	String line;
	while((line=br.readLine())!=null)
		{
		StringTokenizer st=new StringTokenizer(line,"\t");
		//String wbid=
		st.nextToken();
		if(st.hasMoreTokens())
			{
			String publicgenename=st.nextToken();
			if(st.hasMoreTokens())
				{
				String orf=st.nextToken();
				if(!publicgenename.equals(""))
					m.put(orf, publicgenename);
				}
			}
		}
	return m;
	}

	
	private static Map<String,String> getMapWbid2ORF() throws IOException
		{
		Map<String,String> m=new HashMap<String, String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("/home/tbudev3/Desktop/yanai/genenames.txt")));
		br.readLine(); //Skip first line
		String line;
		while((line=br.readLine())!=null)
			{
			StringTokenizer st=new StringTokenizer(line,"\t");
			String wbid=st.nextToken();
			if(st.hasMoreTokens())
				{
				//String publicgenename=
				st.nextToken();
				if(st.hasMoreTokens())
					{
					String orf=st.nextToken();
					m.put(wbid, orf);
					}
				}
			}
		return m;
		}
	
	}
