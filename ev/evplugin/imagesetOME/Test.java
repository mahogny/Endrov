package evplugin.imagesetOME;

import java.util.Collections;
import java.util.Set;


//http://trac.openmicroscopy.org.uk/omero/wiki/OmeroClientLibrary

//http://warlock.openmicroscopy.org.uk:5555/job/OMERO/javadoc/

public class Test
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
    
  

    ome.api.IUpdate update = sf.getUpdateService();
    long userUID = sf.getAdminService().getEventContext().getCurrentUserId();

    
    /*
     * The counterpart to IUpdate is IQuery, which is the basis
     * for all read operations on an OMERO database. The classes under ome.parameters
     * help to build arbitrarily complex queries. 
     */
    
    
    /*
    ome.api.IQuery query = sf.getQueryService();
    ome.parameters.Filter filter = new ome.parameters.Filter().owner(userUID);
    java.util.List<ome.model.containers.Project> list = query.findAll(ome.model.containers.Project.class, filter);
*/
    
    
    /*
     * The returned entities from queries will have un-"fetched"
     * items, that is, items not specifically mentioned in the query,
     * nulled (in the case of collections) or "unloaded" (in the case
     * of entities).  
     */
    
    /*
    for (ome.model.containers.Project project : list) {
        // datasetLinks was nulled.
        assert project.sizeOfDatasetLinks() < 0;
    }
*/
    /*
     * If you'd like to work with that collection, it is necessary
     * to request it in your query. (Here we are also querying the
     * Dataset instances to prevent a security violation. See:
     * https://trac.openmicroscopy.org.uk/omero/ticket/663
     */
    
    /*
    ome.parameters.Parameters params = new ome.parameters.Parameters(filter); 
    params.addId(userUID);
    list = 
        query.findAllByQuery("select p from Project p" +
                             " left outer join fetch p.datasetLinks l"+
                             " left outer join fetch l.child d"+
                             " where d.details.owner.id = :id",params);



    ome.model.containers.Dataset dataset = new ome.model.containers.Dataset();
    dataset.setName(datasetName);
    for (ome.model.containers.Project project : list) {
        assert project.sizeOfDatasetLinks() > -1;
        project.linkDataset(dataset);
        project = update.saveAndReturnObject(project);
        assert project.sizeOfDatasetLinks() > 0;
    }


*/

    /*
     * It is often not necessary to manually write the HQL
     * (Hibernate Query Language) statements, and instead, an
     * existing interface like ome.api.IPojos can be used.
     */ 
    java.util.Set<Long> ids = new java.util.HashSet<Long>(java.util.Arrays.asList(1L,2L,3L));
    ome.api.IPojos pojos = sf.getPojosService();
    java.util.Set<ome.model.containers.Category> contHi = pojos.loadContainerHierarchy(ome.model.containers.Category.class, ids, null);
    for(ome.model.containers.Category i:contHi)
    	System.out.println(" "+i.getClass().toString()+ " " +i);

    
    Set<ome.model.core.Image> userImages=pojos.getUserImages(Collections.EMPTY_MAP);
    for(ome.model.core.Image i:userImages)
    	System.out.println(" "+i.getClass().toString()+ " " +i);
    
    
//    Set<ome.model.core.Image> set=pojos.getImages(ome.model.containers., arg1, arg2)

//	  myImages = 
//	  sf.getQueryService().findAllByQuery("select i from Image i where i.details.owner.id = :id",params);
		}
	
	
	
	
	
	
	public static void saveimage(ome.system.ServiceFactory sf)
		{
	  
	  
		/*
	   * First we create an Image. All of the model objects
	   * that compose OMERO's metadata are in packages and 
	   * subpackages under ome.model in the "common" component.
	   * See https://trac.openmicroscopy.org.uk/omero/browser/trunk/components/common
	   */
	  ome.model.core.Image image = new ome.model.core.Image();
	  image.setName("foo");
	  image.setDescription("bar");
	
	
		
	  /*
	   * Once a ServiceFactory has been created, individual service
	   * instances can be obtained. The interfaces beginning with
	   * "I" and obtained via "get...()" methods represent stateless
	   * services. Any two instances will behave identically. Other
	   * interfaces obtained via "create...()" methods are stateful,
	   * and their lifecycle must be managed by the client. See the
	   * documentation for these interfaces for more information.
	   */
	  ome.api.IUpdate update = sf.getUpdateService();
	
	
	  /*
	   * IUpdate is responsible for saving all data to the database.
	   * Other services with write logic are built on top of it. Any
	   * graph of objects from the ome.model.* packages can be
	   * passed to IUpdate methods and be transparently persisted,
	   * whether the individual entities are new or updated or a
	   * mixture of both.  The server manages all security checks
	   * and assignment of identities.  This means that the method
	   * may throw an exception if the user is not permitted to make
	   * desired changes, and that it is often necessary to catch the
	   * return value to check for the newly assigned id.
	   */
	  assert image.getId() == null;
	  image = update.saveAndReturnObject(image);
	  assert image.getId() != null;
	
	  /*
	   * Images can be placed in "containers" (currently,
	   * ome.model.containers.Dataset and
	   * ome.model.containers.Category) which in turn can also be
	   * placed into containers (Project and CategoryGroup). All
	   * containers have a mandatory "name" and an optional
	   * "description".
	   */
	  ome.model.containers.Category cat = new ome.model.containers.Category();
	  cat.setName("hit");
	  cat.linkImage(image);
	  update.saveObject(cat);
	
	  
		}
		

	}
