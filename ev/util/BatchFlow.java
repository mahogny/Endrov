package util;

import java.io.File;

import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.flow.Flow;
import endrov.flow.FlowExec;

public class BatchFlow
	{

	
	public static void doOne(EvData dataFlow, EvPath path, File imageFile) throws Exception
		{

		System.out.println("doing "+imageFile);

		//Copy flow object to image file
		Flow flowob=(Flow)path.getObject();
		EvData dataImage=EvData.loadFile(imageFile);
		dataImage.metaObject.put("tempflow", flowob);

		//Execute flow
		FlowExec flowExec=new FlowExec(dataImage, new EvPath(dataImage, "tempflow"));

		flowExec.evaluateAll();
		
		
		
		//TODO how to control destination CSV?
		
		
		//dataImage.
		
		
		
		}
	
	
	public static void main(String[] args)
		{
		
		
		
		try
			{
			//TODO need to update flows for the watershed!
			
			File flowFile=new File("/Volumes/TBU_main06/customer/hui/test/nuc3.tif.ostxml");
			EvData dataFlow=EvData.loadFile(flowFile);
			EvPath path=EvPath.parse(dataFlow, "flow");
			
			
			
			String[] wellsX=new String[]{"A","B","C","D","E","F","G","H"};
			String[] wellsY=new String[]{"01","02","03","04","05","06","07","08","09","10","11","12"};
			
			for(int i=0;i<wellsX.length;i++)
				for(int j=0;j<wellsY.length;j++)
					{
					File imageFile=new File("/Volumes/TBU_main06/customer/hui/2012-02-09_000/Well "+wellsX[i]+wellsY[j]+"/GFP - Confocal - n000000.tif");
					
					
					doOne(dataFlow, path, imageFile);
					
					
					//trouble! stupid bioformats will load everything!!!
					
					
					}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		
		
		
		//args: FLOWFILE FLOWPATH [FILES]
		
		
		//file for hui: 4x4 fields. not sure if the BD guarantees no overlap? some stitching would be optimal. HOW?
		
		/*
		
		File flowfile=new File(args[0]);
		String flowpath=args[1];
		
		
		
		for(int i=2;i<args.length;i++)
			{
			
			
			
			
			
			
			
			
			}
		
		
		*/
		
		
		
		
		}
	}
