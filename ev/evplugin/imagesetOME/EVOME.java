package evplugin.imagesetOME;

import java.util.List;
import javax.swing.JOptionPane;

import ome.api.IQuery;
import ome.api.IRepositoryInfo;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;

//http://warlock.openmicroscopy.org.uk:5555/job/OMERO/javadoc/
//http://trac.openmicroscopy.org.uk/omero/wiki/OmeroClientLibrary

//goldmine:
//http://warlock.openmicroscopy.org.uk:5555/job/OMERO/ws/trunk/components/importer/src/ome/formats/OMEROMetadataStore.java/*view*/


/**
 * For keeping track of an OME session
 * @author Johan Henriksson
 */
public class EVOME
	{
	ome.system.ServiceFactory sf;
	long userUID;
	
	
  private RawPixelsStore pservice;
  private IQuery iQuery;
  private IUpdate iUpdate;
  private IRepositoryInfo iInfo;
  private Experimenter exp;
  private RawFileStore rawFileStore;
  
	/**
	 * Try to log in to database.
	 */
	public boolean login(DialogOpenDatabase d)
		{
		try
			{
			//Connect to server
			ome.system.Login login = new ome.system.Login(d.dbUser,d.dbPassword);
			ome.system.Server server = new ome.system.Server(d.dbUrl,d.dbPort);
			sf = new ome.system.ServiceFactory(server,login); 
			
			iQuery = sf.getQueryService();
      iUpdate = sf.getUpdateService();
      pservice = sf.createRawPixelsStore();
      rawFileStore = sf.createRawFileStore();
      iInfo = sf.getRepositoryInfoService();
			
      exp = iQuery.findByString(Experimenter.class, "omeName", d.dbUser);
      
			userUID = sf.getAdminService().getEventContext().getCurrentUserId();
			
			return true;
			}
		catch (Exception e)
			{
			JOptionPane.showMessageDialog(null, "Could not login ("+e.getMessage()+")");
			return false;
			}
		}
	
	
	
	/**
	 * Get the list of projects
	 */
	public List<ome.model.containers.Project> getProjectList()
		{
//    ome.api.IQuery query = sf.getQueryService();
    ome.parameters.Filter filter = new ome.parameters.Filter().owner(userUID);
    ome.parameters.Parameters params = new ome.parameters.Parameters(filter); 
    
    params.addId(userUID);     
    java.util.List<ome.model.containers.Project> list = 
        iQuery.findAllByQuery("select p from Project p" +
                             " left outer join fetch p.datasetLinks l"+
                             " left outer join fetch l.child d"+
                             " where d.details.owner.id = :id",params);
    return list;
		}
	
	/**
	 * Get the list of datasets for a project
	 */
	 public List<ome.model.containers.Dataset> getDatasets(ome.model.containers.Project project)
		 {
		 List<ome.model.containers.Dataset> list = iQuery.findAllByQuery(
				 "from Dataset where id in "+
				 "(select link.child.id from ProjectDatasetLink link where "+
				 "link.parent.id = :id)", new Parameters().addId(project.getId()));
		 return list;
		 }

	
	
	public void foo(ome.model.containers.Project p)
		{
		ome.api.IQuery query = sf.getQueryService();
    ome.parameters.Filter filter = new ome.parameters.Filter().owner(userUID);
    
    
    
		}
	
	
	
	}
