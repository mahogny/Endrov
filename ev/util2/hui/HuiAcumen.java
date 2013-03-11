package util2.hui;

import java.io.File;

import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.typeImageset.EvChannel;



public class HuiAcumen
	{


	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
		
		
		File fBase=new File("/petra/data/x/customer/hui/incoming/screen20130216-plate3/plate3");
		
		
		StringBuilder sb=new StringBuilder();

		
		
		try
			{
			String[] wellsX=new String[]{"B","C","D","E","F","G"};
			String[] wellsY=new String[]{"2","3","4","5","6","7","8","9","10","11"};
			
			for(int i=0;i<wellsX.length;i++)
				for(int j=0;j<wellsY.length;j++)
					{
					String wellName=wellsX[i]+wellsY[j];
					
					try
						{
						File fNuc=new File(fBase, "nuc (405nm) "+wellName+" Channel 1.tif");
						EvData dataNuc=EvData.loadFile(fNuc);
						EvChannel chNuc=dataNuc.getIdObjectsRecursive(EvChannel.class).values().iterator().next();
						double[] arrNuc=chNuc.getFirstStack(null).getPlane(0).getPixels().convertToDouble(true).getArrayDouble();
						
						int areaNuc=0;
						for(double d:arrNuc)
							if(d>140)
								areaNuc++;

						File fLipid=new File(fBase, "lipid (488nm) "+wellName+" Channel 2.tif");
						EvData dataLipid=EvData.loadFile(fLipid);
						EvChannel chLipid=dataLipid.getIdObjectsRecursive(EvChannel.class).values().iterator().next();
						double[] arrLipid=chLipid.getFirstStack(null).getPlane(0).getPixels().convertToDouble(true).getArrayDouble();

						double sumLipid=0, areaLipid=0;
						for(double d:arrLipid)
							if(d>80)
								{
								sumLipid+=d;
								areaLipid++;
								}

						
						//System.out.println(wellName+"\t"+countLipid/countNuc);
						sb.append(wellName+"\t"+sumLipid+"\t"+areaNuc+"\t"+areaLipid);
						sb.append("\n");
						
//						sb.append(wellName+"\t"+666);
	//					sb.append("\n");
						
						dataNuc.io.close();
						dataLipid.io.close();

						}
					catch (Exception e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						//System.out.println(wellName+"\tN/A");
						sb.append(wellName+"\t");
						sb.append("\n");
						}
					System.out.println(sb);
					//System.exit(0);
					
					}

			System.out.println("------");
			System.out.println(sb);
			
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		System.out.println("done");
		
		}
	
	}
