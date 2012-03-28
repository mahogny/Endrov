package util2.paperCeExpression.compareYanai;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.lineage.Lineage;
import endrov.lineage.LineageExp;
import endrov.util.EvDecimal;
import gnu.jpdf.PDFJob;


public class ToLineage
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
					
						Average a=avgs.get(wormbaseID);
						if(a==null)
							avgs.put(wormbaseID, a=new Average());
						
						a.add(Double.parseDouble(val));
						
						}
					
					}
				
				
				
				yanaiStageAvg.remove(null); //Delete uninteresting data
				System.out.println(yanaiStageAvg.keySet());
				

				Map<String,String> mapGenename2wbid=getMapGenename2wbid();
				Map<String,String> mapORF2wbid=getMapORF2wbid();
				
				
				EvData linfileOut=new EvData();
				Lineage lin=new Lineage();
				linfileOut.metaObject.put("yanai", lin);

				EvData reflinfile=EvData.loadFile(new File("/Volumes/TBU_main06/ostmodel/summary2.ost"));
				Lineage totLinSingleCell=(Lineage)reflinfile.getChild("ss");
				Lineage totLinT=(Lineage)reflinfile.getChild("t");
				
				EvDecimal t4s=totLinSingleCell.particle.get("ABa").pos.firstKey();
				EvDecimal t4e=totLinSingleCell.particle.get("ABa").pos.lastKey();
				EvDecimal t28s=totLinSingleCell.particle.get("ABarpp").pos.firstKey();
				EvDecimal t28e=totLinSingleCell.particle.get("ABarpp").pos.lastKey();
				EvDecimal t55s=totLinSingleCell.particle.get("ABarppp").pos.firstKey();
				EvDecimal t55e=totLinSingleCell.particle.get("ABarppp").pos.lastKey();
				EvDecimal t95s=totLinSingleCell.particle.get("ABarpppp").pos.firstKey();
				EvDecimal t95e=totLinSingleCell.particle.get("ABarpppp").pos.lastKey();
				EvDecimal t190s=totLinSingleCell.particle.get("ABarppppp").pos.firstKey();
				EvDecimal t190e=t190s.add(t95e.subtract(t95s));

				
				
				Lineage.Particle summaryNucT=totLinT.particle.get("_slice0");
				Lineage.Particle outNucT=lin.getCreateParticle("_slice0");
				
				outNucT.pos.put(EvDecimal.ZERO, new Lineage.ParticlePos());
				outNucT.pos.put(t190e, new Lineage.ParticlePos());

				
				for(String expName:summaryNucT.exp.keySet())
					{
					int pos=expName.indexOf("_");
					String geneName=expName.substring(0,pos);
					String wbid=mapGenename2wbid.get(geneName);
					if(wbid==null)
						wbid=mapORF2wbid.get(geneName);
					
					Map<Double,String> labels=new TreeMap<Double, String>();
					labels.put((t4s.doubleValue()+t4e.doubleValue())/2.0, "4c");
					labels.put((t28s.doubleValue()+t28e.doubleValue())/2.0, "28c");
					labels.put((t55s.doubleValue()+t55e.doubleValue())/2.0, "55c");
					labels.put((t95s.doubleValue()+t95e.doubleValue())/2.0, "95c");
					labels.put((t190s.doubleValue()+t190e.doubleValue())/2.0, "190c");
					
					if(wbid!=null)
						{
						LineageExp expYanai=outNucT.getCreateExp(expName+"_yanai");
						
						expYanai.level.put(t4s, yanaiStageAvg.get("4").get(wbid).get());
						expYanai.level.put(t4e, yanaiStageAvg.get("4").get(wbid).get());

						expYanai.level.put(t28s, yanaiStageAvg.get("28").get(wbid).get());
						expYanai.level.put(t28e, yanaiStageAvg.get("28").get(wbid).get());
						
						expYanai.level.put(t55s, yanaiStageAvg.get("55").get(wbid).get());
						expYanai.level.put(t55e, yanaiStageAvg.get("55").get(wbid).get());

						expYanai.level.put(t95s, yanaiStageAvg.get("95").get(wbid).get());
						expYanai.level.put(t95e, yanaiStageAvg.get("95").get(wbid).get());

						expYanai.level.put(t190s, yanaiStageAvg.get("190").get(wbid).get());
						expYanai.level.put(t190e, yanaiStageAvg.get("190").get(wbid).get());

						
						LineageExp oldExpRec=summaryNucT.getCreateExp(expName);
						LineageExp newExpRec=outNucT.getCreateExp(expName);
						
						for(Map.Entry<EvDecimal, Double> e:oldExpRec.level.entrySet())
							if(e.getKey().lessEqual(t190e))
								newExpRec.level.put(e.getKey(),e.getValue());
						
						
						File pdfroot=new File("/home/tbudev3/yanaiVsTpdf");
						pdfroot.mkdirs();
						exportPDF(new File(pdfroot,expName+".pdf"), expYanai, newExpRec, expName, labels);
						
						}
					else
						{
						System.out.println("gives null "  +expName+"   "+mapORF2wbid.get(expName));
						}
					
					}
				
				
				linfileOut.saveDataAs(new File("/Volumes/TBU_main06/ostmodel/yanai.ost"));
				
				}
			catch (IOException e)
				{
				e.printStackTrace();
				}
			
			System.exit(0);
			}
	
	private static void exportPDF(File output, LineageExp e1, LineageExp e2, String expName, Map<Double,String> labels) throws IOException
		{
		FileOutputStream fo=new FileOutputStream(output);
		PDFJob job = new PDFJob(fo);
		Graphics2D g = (Graphics2D)job.getGraphics();

		int fontSize=20;
		Font currentFont = g.getFont();
		g.setFont(new Font(currentFont.getName(),currentFont.getStyle(),fontSize));
		currentFont = g.getFont();
		
		
		double scaleX=0.9*(double)job.getPageDimension().width/100.0;
		double scaleY=0.9*(double)job.getPageDimension().height;
		
		int offsetX=10;
		int offsetY=job.getPageDimension().height/2;
	
		g.setStroke(new BasicStroke(0.8f));

		double scalePos=100.0/(4*3600);

		
		//Scaling for e1
		double max1=0;
		for(double v:e1.level.values())
			if(v>max1)
				max1=v;
		double e1scaleValue=0.2/max1;

		
		//Scaling for e2
		double max2=0;
		for(double v:e2.level.values())
			if(v>max2)
				max2=v;
		double e2scaleValue=0.2/max2;
		
		////// LLSQ between e1 & e2
		double sum1=0;
		int sum1n=0;
		double sum2=0;
		int sum2n=0;
		for(Map.Entry<EvDecimal, Double> e:e2.level.entrySet())
			{
			sum2+=e.getValue();
			sum2n++;
			
			Double y1=interpolate(e1.level, e.getKey());
			if(y1!=null)
				{
				sum1+=y1;
				sum1n++;
				}
			}
		sum1/=sum1n;
		sum2/=sum2n;
		
		
		
		//x & y scale
		g.setColor(Color.GRAY);
		g.setStroke(new BasicStroke(0.5f));
		g.drawLine(
				offsetX+(int)(0*scaleX), offsetY, 
				offsetX+(int)(90*scaleX), offsetY);
		g.drawLine(
				offsetX, offsetY+(int)(0*scaleY), 
				offsetX, offsetY-(int)(1.2*0.2*scaleY));

		
		
		//Graph-lines
		g.setStroke(new BasicStroke(1f));
		g.setColor(Color.BLACK);
		exportPDF(g, e1, scaleX, scaleY, offsetX, offsetY, scalePos, e1scaleValue);
		//exportPDF(g, e1, scaleX, scaleY, offsetX, offsetY, scalePos, e2scaleValue*sum2/sum1);
		g.setColor(Color.RED);
		exportPDF(g, e2, scaleX, scaleY, offsetX, offsetY, scalePos, e2scaleValue);
	
		
		
		
		
		
		
		//X-labels
		for(Map.Entry<Double, String> e:labels.entrySet())
			{
			g.setColor(Color.BLACK);
			int xlab=offsetX+(int)(e.getKey().doubleValue()*scalePos*scaleX);
			double w=g.getFontMetrics().getStringBounds(e.getValue(), g).getWidth();
			g.drawString(e.getValue(),	xlab-(int)w/2, offsetY+20);
			}
		
		
		fontSize=15;
		currentFont = g.getFont();
		g.setFont(new Font(currentFont.getName(),currentFont.getStyle(),fontSize));
		currentFont = g.getFont();
		
		//Title
		g.setColor(Color.BLACK);
		int xtitle=offsetX+(int)(45*scaleX);
		double wtitle=g.getFontMetrics().getStringBounds(expName, g).getWidth();
		g.drawString(expName,	xtitle-(int)wtitle/2, offsetY+45);

		
		
		g.dispose();
		job.end();
		}
	
	
	
	private static void exportPDF(Graphics2D g, LineageExp exp, double scaleX, double scaleY, int offsetX, int offsetY, double scalePos, double scaleValue) throws IOException
		{
		/*
		double max=0;
		for(double v:exp.level.values())
			if(v>max)
				max=v;
		scaleValue*=0.2/max;*/
		
		int npoints=exp.level.size();
		int[] xpoints=new int[npoints];
		int[] ypoints=new int[npoints];
		
		int i=0;
		for(Map.Entry<EvDecimal, Double> next:exp.level.entrySet())
			{
			xpoints[i]=offsetX+(int)(next.getKey().doubleValue()*scalePos*scaleX);
			ypoints[i]=offsetY-(int)(next.getValue()*scaleY*scaleValue);
			i++;
			}

		g.drawPolyline(xpoints, ypoints, npoints);
		
		}
	
	
	private static Map<String,String> getMapORF2wbid() throws IOException
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
	
	private static Map<String,String> getMapGenename2wbid() throws IOException
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
	
	
	
	
	


	/**
	 * Interpolate x given x->yTuple<EvDecimal,EvDecimal>. Returns null if x outside.
	 * 
	 * NEEDS TESTING
	 * 
	 */
	public static Double interpolate(SortedMap<EvDecimal, Double> map, EvDecimal x)
		{
		Double preciseY=map.get(x);
		if(preciseY!=null)
			return preciseY;
		
		if(map.size()>2)
			{
			SortedMap<EvDecimal, Double> hmap=map.headMap(x);
			SortedMap<EvDecimal, Double> tmap=map.tailMap(x);
			
			if(hmap.isEmpty() || tmap.isEmpty())
				return null;
			else
				{
				EvDecimal lastX=hmap.lastKey();
				EvDecimal nextX=tmap.firstKey();
				Double lastY=hmap.get(lastX);
				Double nextY=tmap.get(nextX);
				return linInterpolate(lastX, nextX, lastY, nextY, x);
				}
			}
		else
			{
			System.out.println("no x for "+x);
			return null;
			}
		}
	

	/**
	 * Linear interpolation
	 */
	private static Double linInterpolate(EvDecimal lastX,EvDecimal nextX, Double lastY, Double nextY, EvDecimal x)
		{
		double frac=x.subtract(lastX).divide(nextX.subtract(lastX)).doubleValue();
		double frac1=1-frac;
		return frac1*lastY + frac*nextY;
		}
	}
