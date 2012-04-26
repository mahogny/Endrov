package util2.hui;

import java.io.File;

import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
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
	
	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
		
		
		try
			{
			File flowFile=new File("/Volumes/TBU_main06/customer/hui/onenuc.ostxml");
			EvData dataFlow=EvData.loadFile(flowFile);
			System.out.println(dataFlow);
			
			EvPath pathFlowNuc=EvPath.parse(dataFlow, "flownuc");
			EvPath pathFlowLipids=EvPath.parse(dataFlow, "flowlipids");
			
			//TODO what about inputs to the flow object? need to code to put in file path!!!
			
			File imageFile=new File("/Volumes/TBU_main06/customer/hui/2012-02-09_000");
			EvData dataImages=EvData.loadFile(imageFile);
			
			String[] wellsX=new String[]{"A","B","C","D","E","F","G","H"};
			String[] wellsY=new String[]{"01","02","03","04","05","06","07","08","09","10","11","12"};
			
			for(int i=0;i<wellsX.length;i++)
				for(int j=0;j<wellsY.length;j++)
					{
					String well=wellsX[i]+wellsY[j];
					EvPath pathWell=EvPath.parse(dataImages, well);
					
					System.out.println("------------ "+well);
					
					doOne(dataFlow, pathFlowNuc,    dataImages, pathWell, new File("/Volumes/TBU_main06/customer/hui/data-2012-02-09_000/nuc_"+well+".csv"));
					doOne(dataFlow, pathFlowLipids, dataImages, pathWell, new File("/Volumes/TBU_main06/customer/hui/data-2012-02-09_000/lipid_"+well+".csv"));
					
					//System.exit(0);
					
					}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		

		
		}
	
	}
