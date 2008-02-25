package evplugin.imagesetOME;



//http://trac.openmicroscopy.org.uk/omero/wiki/OmeroClientLibrary

//http://warlock.openmicroscopy.org.uk:5555/job/OMERO/javadoc/

public class Test2
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		String username="root";
		String password="wuermli";
		String serverName="localhost";
		int serverPort=1099;
		String datasetName="baz";
		
		//Connect to server
		ome.system.Login login = new ome.system.Login(username,password);
    ome.system.Server server = new ome.system.Server(serverName,serverPort);
    ome.system.ServiceFactory sf = new ome.system.ServiceFactory(server,login); //()=use local.properties
    
	  
		}
		

	}
