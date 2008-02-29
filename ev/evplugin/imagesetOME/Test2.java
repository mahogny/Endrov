package evplugin.imagesetOME;

public class Test2
	{
	public static void main(String[] args)
		{
		
		DialogOpenDatabase dia=new DialogOpenDatabase(null);
		EVOME session=dia.run();
		
		for(ome.model.containers.Project p:session.getProjectList())
			{
			
			for(ome.model.containers.Dataset ds:session.getDatasets(p))
				{
				
				
				
				}
			
			
			
			}
		
		}
	}
