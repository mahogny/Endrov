package util2.paperCeExpression.compareYanai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowColocalization.ColocCoefficients;
import endrov.lineage.Lineage;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;


public class Main
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
			EvLog.listeners.add(new EvLogStdout());
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
					
					
					
						
					
					
					
					//System.out.println(eSample.getValue().dataHeader);
					
					//System.out.println(eSample.getValue().dataLine);
					
	//				System.out.println(eSample.getKey());
					
					
					}
				
				
				//System.out.println(avgs);
				
				yanaiStageAvg.remove(null); //Delete uninteresting data
				System.out.println(yanaiStageAvg.keySet());
				

				Map<String,String> mapGenename2wbid=getMapGenename2wbid();
				Map<String,String> mapORF2wbid=getMapORF2wbid();
				
				List<String> allWbids=new ArrayList<String>(yanaiStageAvg.get("4").keySet());
				
				//Integrate subregions.
				
				

				EvData linfile=EvData.loadFile(new File("/Volumes/TBU_main06/summary2.ost"));
				Lineage lin=(Lineage)linfile.getChild("t");
				Lineage totLinSingleCell=(Lineage)linfile.getChild("lin");
				
//				EvData totalData=EvData.loadFile(new File("/Volumes/TBU_main06/ost4dgood/celegans2008.2.ost"));
				//final NucLineage totLinSingleCell=linfile.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
//				FrameTime ftRef=CompareAll.buildFrametime(totLinSingleCell);
				
				
				double t4s=totLinSingleCell.particle.get("ABa").pos.firstKey().doubleValue();
				double t4e=totLinSingleCell.particle.get("ABa").pos.lastKey().doubleValue();
				double t28s=totLinSingleCell.particle.get("ABarpp").pos.firstKey().doubleValue();
				double t28e=totLinSingleCell.particle.get("ABarpp").pos.lastKey().doubleValue();
				double t55s=totLinSingleCell.particle.get("ABarppp").pos.firstKey().doubleValue();
				double t55e=totLinSingleCell.particle.get("ABarppp").pos.lastKey().doubleValue();
				double t95s=totLinSingleCell.particle.get("ABarpppp").pos.firstKey().doubleValue();
				double t95e=totLinSingleCell.particle.get("ABarpppp").pos.lastKey().doubleValue();
				double t190s=totLinSingleCell.particle.get("ABarppppp").pos.firstKey().doubleValue();
				double t190e=t190s + (t95e-t95s);

				/*
				Map<String, StringBuffer> outFiles=new HashMap<String, StringBuffer>();
				for(String cellStage:yanaiStageAvg.keySet())
					outFiles.put(cellStage, new StringBuffer());*/
				StringBuffer outFile=new StringBuffer();
				StringBuffer outFileBootstrap=new StringBuffer();
				
				Lineage.Particle nuc=lin.particle.get("_slice0");
				for(String expName:nuc.exp.keySet())
					{
					int pos=expName.indexOf("_");
					String geneName=expName.substring(0,pos);
					String wbid=mapGenename2wbid.get(geneName);
					if(wbid==null)
						wbid=mapORF2wbid.get(geneName);
					
					
					if(wbid!=null)
						{
						Average avg4=new Average();
						Average avg28=new Average();
						Average avg55=new Average();
						Average avg95=new Average();
						Average avg190=new Average();
						
						Map<String, Average> avgForStage=new HashMap<String, Average>();
						avgForStage.put("4", avg4);
						avgForStage.put("28", avg28);
						avgForStage.put("55", avg55);
						avgForStage.put("95", avg95);
						avgForStage.put("190", avg190);
						
						Map<EvDecimal, Double> level=nuc.exp.get(expName).level;
						
						for(EvDecimal time:level.keySet())
							{
							double t=time.doubleValue();
							double val=level.get(time);
							if(t<t4s && t<=t4e)
								avg4.add(val);
							else if(t>t28s && t<=t28e)
								avg28.add(val);
							else if(t>t55s && t<=t55e)
								avg55.add(val);
							else if(t>t95s && t<=t95e)
								avg95.add(val);
							else if(t>t190s && t<=t190e)
								avg190.add(val);
							}

						ColocCoefficients c=new ColocCoefficients();
						for(String cellStage:yanaiStageAvg.keySet())
							c.add(avgForStage.get(cellStage).get(), yanaiStageAvg.get(cellStage).get(wbid).get());
						double corr=c.getPearson();
						if(!Double.isNaN(corr))
							outFile.append(c.getPearson()+"\n");
						
						int numBootStrap=10000;
						for(int i=0;i<numBootStrap;i++)
							{
							int randInt=(int)(allWbids.size()*Math.random());
							String randID=allWbids.get(randInt);
							ColocCoefficients c2=new ColocCoefficients();
							for(String cellStage:yanaiStageAvg.keySet())
								c2.add(avgForStage.get(cellStage).get(), yanaiStageAvg.get(cellStage).get(randID).get());
							double corr2=c2.getPearson();
							if(!Double.isNaN(corr2))
								outFileBootstrap.append(corr2+"\n");
							}
						
						/*
						for(String cellStage:yanaiStageAvg.keySet())
							if(avgForStage.get(cellStage).count!=0)
								outFiles.get(cellStage).append(avgForStage.get(cellStage)+"\t"+yanaiStageAvg.get(cellStage).get(wbid)+"\n");
						*/
						
						}
					else
						{
						System.out.println("gives null "  +expName+"   "+mapORF2wbid.get(expName));
						}
					
					}
				
				EvFileUtil.writeFile(new File("/home/tbudev3/Desktop/yanai/corrs.csv"), outFile.toString());
				EvFileUtil.writeFile(new File("/home/tbudev3/Desktop/yanai/corrsboot.csv"), outFileBootstrap.toString());
				
				/*
				for(String cellStage:yanaiStageAvg.keySet())
					{
					EvFileUtil.writeFile(new File("/home/tbudev3/Desktop/yanai/stage"+cellStage+".csv"), outFiles.get(cellStage).toString());
					//System.out.println(outFiles.get(cellStage));
					}*/

				}
			catch (IOException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			
			
			}
	
	
	public static Map<String,String> getMapORF2wbid() throws IOException
		{
		Map<String,String> m=new HashMap<String, String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("/home/tbudev3/Desktop/yanai/wbid2ORF.txt")));
		br.readLine(); //Skip first line
		String line;
		while((line=br.readLine())!=null)
			{
			StringTokenizer st=new StringTokenizer(line,"\t");
			String wbid=st.nextToken();
			if(st.hasMoreTokens())
				{
				String genename=st.nextToken();
				m.put(genename, wbid);
				}
			}
		return m;
		}
	
	public static Map<String,String> getMapGenename2wbid() throws IOException
		{
		Map<String,String> m=new HashMap<String, String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("/home/tbudev3/Desktop/yanai/wbid2commonname.txt")));
		br.readLine(); //Skip first line
		String line;
		while((line=br.readLine())!=null)
			{
			StringTokenizer st=new StringTokenizer(line,"\t");
			String wbid=st.nextToken();
			
			String genename=st.nextToken();
			m.put(genename, wbid);
			}
		return m;
		}

	}
