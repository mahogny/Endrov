package util2.integrateExpression;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;

import evplugin.data.EvObject;
import evplugin.ev.EV;
import evplugin.ev.Log;
import evplugin.ev.StdoutLog;
import evplugin.imageset.EvImage;
import evplugin.imageset.Imageset;
import evplugin.imagesetOST.OstImageset;
import evplugin.nuc.NucLineage;
import evplugin.nuc.NucPair;

public class IntegrateExpression
	{
	
	
	
	/**
	 * Project sphere onto plane
	 * @param nucRw Radius
	 * @param nucZw Relative z
	 * @return Projected radius in pixels
	 */
	public static double projectSphere(double nucRw, double nucZw, double imageZw)
		{
		//Currently assumes resx=resy. Maybe this should be specified harder?
//		double wz=w.s2wz(w.frameControl.getZ());
		double dz=nucZw-imageZw;
		double tf=nucRw*nucRw-dz*dz;
		if(tf>0)
			{
			double wpr=Math.sqrt(tf);
			return wpr;
//			return w.scaleW2s(wpr);	
			}
		else
			return -1;
		}
	
	
	public static NucLineage getLin(Imageset ost)
		{
		for(EvObject evob:ost.metaObject.values())
			{
			if(evob instanceof NucLineage)
				{
				NucLineage lin=(NucLineage)evob;
				return lin;
				}
			}
		return null;
		}
	

	public static void main(String arg[])
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		Imageset ost=new OstImageset("/Volumes/TBU_xeon01_500GB01/ost4dgood/TB2141070621b/");


		String channelName="GFP";
		String expName="CEH-5";



		//For all lineages
		NucLineage lin=getLin(ost);

		
		Double minExpLevel=null;
		Double maxExpLevel=null;

		//For all frames
		System.out.println("num frames: "+ost.getChannel(channelName).imageLoader.size());
		for(Integer frame:ost.getChannel(channelName).imageLoader.keySet())
			{
			System.out.println();
			System.out.println("frame "+frame);

			Map<NucPair,NucLineage.NucInterp> inter=lin.getInterpNuc(frame);

			Map<String, Double> expLevel=new HashMap<String, Double>();
			Map<String, Integer> nucVol=new HashMap<String, Integer>();


			//For all images
			for(Map.Entry<Integer, EvImage> eim:ost.getChannel(channelName).imageLoader.get(frame).entrySet())
				{
				EvImage im=eim.getValue();
				BufferedImage bim=null;
				double imageZw=eim.getKey()/ost.meta.resZ;

				//For all nuc
				for(Map.Entry<NucPair,NucLineage.NucInterp> e:inter.entrySet())
//					if(e.getKey().getRight().equals("ABarappaa"))
					//	if(e.getKey().getRight().equals("AB"))
					{
					String nucName=e.getKey().snd();
					NucLineage.NucPos pos=e.getValue().pos;

					double pr=projectSphere(pos.r, pos.z, imageZw);
					int midSx=(int)im.transformWorldImageX(pos.x);
					int midSy=(int)im.transformWorldImageY(pos.y);
					int rS=(int)im.scaleWorldImageX(pr);
					if(rS>0)
						{
						if(!expLevel.containsKey(nucName))
							{
							expLevel.put(nucName, 0.0);
							nucVol.put(nucName, 0);
							}
						if(bim==null)
							bim=im.getJavaImage();

						//Integrate this image
						WritableRaster r=bim.getRaster();
						int area=0;
						double exp=0;

						int sy=midSy-rS;
						if(sy<0) sy=0;
						int ey=midSy+rS;
						if(ey>=bim.getHeight()) ey=bim.getHeight();

						int sx=midSx-rS;
						if(sx<0) sx=0;
						int ex=midSx+rS;
						if(ex>=bim.getWidth()) ex=bim.getWidth();

						for(int y=sy;y<ey;y++)
							for(int x=sx;x<ex;x++)
								{
								int dx=x-midSx;
								int dy=y-midSy;
								if(dx*dx+dy*dy<rS*rS)
									{
									int v=r.getSample(x, y, 0);
//									System.out.println(""+v);
									area++;
									exp+=v;
									}
								}
						nucVol.put(nucName,nucVol.get(nucName)+area);
						expLevel.put(nucName,expLevel.get(nucName)+exp);
						}
					}



				}

			//Store value in XML
			for(String nucName:expLevel.keySet())
				{
				double avg=expLevel.get(nucName)/nucVol.get(nucName);
//				System.out.println(nucName+" "+avg);
				NucLineage.NucExp exp=lin.nuc.get(nucName).getExpCreate(expName);
				if(lin.nuc.get(nucName).pos.lastKey()>=frame && lin.nuc.get(nucName).pos.firstKey()<=frame) 
					exp.level.put(frame,avg);
				
				if(minExpLevel==null || avg<minExpLevel)
					minExpLevel=avg;
				if(maxExpLevel==null || avg>maxExpLevel)
					maxExpLevel=avg;
				}

			}


		//Subtract background
		double expSize=maxExpLevel-minExpLevel;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			if(nuc.exp.containsKey(expName))
				for(Map.Entry<Integer, Double> e:nuc.exp.get(expName).level.entrySet())
					{
					nuc.exp.get(expName).level.put(e.getKey(), (e.getValue()-minExpLevel)*5);
					}
		
		
		
		ost.saveMeta();

		}
	}
