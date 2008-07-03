package endrov.imagesetOME;

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
				
				for(ome.model.core.Image im:session.getImages(ds))
					{
					System.out.println(" %% "+im.getName()+ " %% "+im.isLoaded()+" %% "+im.isValid());
					
					for(ome.model.core.Pixels pix:session.getPixels(im))
						{
						System.out.println("asdasd "+pix.getId()+" "+pix.getImage());
						for(Object oc:pix.getChannels())
							{
							ome.model.core.Channel omechannel=(ome.model.core.Channel)oc;
							System.out.println("ch: "+omechannel.getLogicalChannel());						
							}
						}
					
					}
				
				}
			
			
			
			}
		
/*
		for(ome.model.core.Pixels p:session.getPixels())
			{
			System.out.println("asdasd "+p.getId()+" "+p.getImage());
			}
*/
		
		}
	}
