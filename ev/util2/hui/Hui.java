package util2.hui;

import java.io.File;
import java.util.Map;

import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvPath;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowExecListener;



public class Hui
	{

	public static void doOne(EvData dataFlow, EvPath pathFlow, EvData dataImages, EvPath pathChannel,
			final File outfileName) throws Exception
		{

		//Copy flow object to channel
		Flow flowob=(Flow)pathFlow.getObject();
		pathChannel.getObject().metaObject.put("tempflow", flowob);
		
		//there is a chance this copy is not needed!!!! could make flowexec less dependent on this 

		//Set up flow. Pass filename when needed
		FlowExec flowExec=new FlowExec(dataImages, new EvPath(pathChannel, "tempflow"));
		flowExec.listener=new FlowExecListener()
			{
			public void setOutputObject(String name, Object ob)
				{
				}
				
			public Object getInputObject(String name)
				{
				if(name.equals("fname"))
					{
					return outfileName;
					}
				else
					{
					System.out.println("Unknown input: "+name);
					return null;
					}
				}
			};
			
		//Execute flow
		flowExec.evaluateAll();
		
		
		
		
		
		}
	
	
	public static void doOnePlate(File imageFile) throws Exception
		{
		File outdir=new File(imageFile.getParentFile(), imageFile.getName()+".endrovstats");
		outdir.mkdir();
		
		File flowFile=new File("/petra/data/x/customer/hui/onenuc.ostxml");
//		File flowFile=new File("/Volumes/TBU_main06/customer/hui/onenuc.ostxml");
		EvData dataFlow=EvData.loadFile(flowFile);
		System.out.println(dataFlow);
		
		EvPath pathFlowNuc=EvPath.parse(dataFlow, "flownuc");
		EvPath pathFlowLipids=EvPath.parse(dataFlow, "flowlipids");
		
		//TODO what about inputs to the flow object? need to code to put in file path!!!
		
		EvData dataImages=EvData.loadFile(imageFile);
		if(dataImages==null)
			throw new RuntimeException("Failed to open "+imageFile);
		
		for(Map.Entry<String, EvObject> e:dataImages.metaObject.entrySet())
			{
			String well=e.getKey();
			

			long startTime=System.currentTimeMillis();
			
			EvPath pathWell=EvPath.parse(dataImages, well);
			
			System.out.println("------------ "+well);
			
			doOne(dataFlow, pathFlowNuc,    dataImages, pathWell, new File(outdir,"nuc_"+well+".csv"));
			doOne(dataFlow, pathFlowLipids, dataImages, pathWell, new File(outdir,"lipid_"+well+".csv"));
			
			long endTime=System.currentTimeMillis();
			
			System.out.println("################################# Time for well: "+(endTime-startTime));
			
			
			}
		/*
		String[] wellsX=new String[]{"A","B","C","D","E","F","G","H"};
		String[] wellsY=new String[]{"01","02","03","04","05","06","07","08","09","10","11","12"};
		
		for(int i=0;i<wellsX.length;i++)
			for(int j=0;j<wellsY.length;j++)
				{
				String well=wellsX[i]+wellsY[j];
				
				//System.exit(0);
				
				}
				*/
		}
	
	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
		
		
		try
			{
	/*		
			doOnePlate(new File("/Volumes/TBU_main06/customer/hui/round2/25"));
			doOnePlate(new File("/Volumes/TBU_main06/customer/hui/round2/34"));
			doOnePlate(new File("/Volumes/TBU_main06/customer/hui/round2/52"));
			doOnePlate(new File("/Volumes/TBU_main06/customer/hui/round2/61"));
*/
/*
			doOnePlate(new File("/petra/data/x/customer/hui/round3/plate1"));
			doOnePlate(new File("/petra/data/x/customer/hui/round3/plate2"));
			doOnePlate(new File("/petra/data/x/customer/hui/round3/plate3"));
			doOnePlate(new File("/petra/data/x/customer/hui/round3/plate4"));
			*/

			//doOnePlate(new File("/petra/data/x/customer/hui/round4/manual"));
			//doOnePlate(new File("/petra/data/x/customer/hui/round4/new"));
//			doOnePlate(new File("/petra/data/x/customer/hui/round4/old"));

			
			
			for(String s:args)
				{
				System.out.println("Doing "+s);
				doOnePlate(new File(s));
				System.out.println("-------------------------------------------------");
				}
			
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		System.out.println("done");
		
		}
	
	}
