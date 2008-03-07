package evplugin.imagesetOME;

import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;

import ome.api.*;
import ome.model.core.Image;
import ome.parameters.Parameters;

//http://warlock.openmicroscopy.org.uk:5555/job/OMERO/javadoc/
//http://trac.openmicroscopy.org.uk/omero/wiki/OmeroClientLibrary

//http://cvs.openmicroscopy.org.uk/svn/omero/tags/omero-2.5/components/common/src/ome/model/

//goldmine:
//http://warlock.openmicroscopy.org.uk:5555/job/OMERO/ws/trunk/components/importer/src/ome/formats/OMEROMetadataStore.java/*view*/
//http://cvs.openmicroscopy.org.uk/svn/shoola/tags/3.0-Beta2.3/SRC/org/openmicroscopy/shoola/env/data/OmeroImageServiceImpl.java

//TODO: remove all Vector<>? List might be faster.

/**
 * For keeping track of an OME session. Note that some of the code is GPL infected.
 * @author Johan Henriksson
 * @author Some code taken and modified from OME
 */
public class EVOME
	{
	ome.system.ServiceFactory sf;
	long userUID;
	
	
  //private RawPixelsStore pservice;
  private IQuery iQuery;
  //private IUpdate iUpdate;
  private ome.model.meta.Experimenter exp;
  
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
      //iUpdate = sf.getUpdateService();
			
      exp = iQuery.findByString(ome.model.meta.Experimenter.class, "omeName", d.dbUser);
      
			userUID = sf.getAdminService().getEventContext().getCurrentUserId();
			
			
			
			return true;
			}
		catch (Exception e)
			{
			JOptionPane.showMessageDialog(null, "Could not login ("+e.getMessage()+")");
			return false;
			}
		}
	
	/** Info about experimenter: OME name */
	public String getExperimenterName()
		{
		return exp.getOmeName();
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
	 * GPL. Get the list of datasets for a project
	 */
	 public List<ome.model.containers.Dataset> getDatasets(ome.model.containers.Project project)
		 {
		 List<ome.model.containers.Dataset> list = iQuery.findAllByQuery(
				 "from Dataset where id in "+
				 "(select link.child.id from ProjectDatasetLink link where "+
				 "link.parent.id = :id)", new Parameters().addId(project.getId()));
		 return list;
		 }

	 /**
		 * GPL. Get the list of images for a dataset
		 */
		 public List<ome.model.core.Image> getImages(ome.model.containers.Dataset ds)
			 {
			 List<ome.model.core.Image> list = iQuery.findAllByQuery(
					 "from Image where id in "+
					 "(select link.child.id from DatasetImageLink link where "+
					 "link.parent.id = :id)", new Parameters().addId(ds.getId()));
			 return list;
			 }
	 
	 
	 //http://cvs.openmicroscopy.org.uk/svn/omero/tags/omero-3.0-alpha/components/common/src/ome/
	

	 /*
	 public List<ome.model.core.Image> getImages()
		 {
		 List<ome.model.core.Image> list=iQuery.findAllByQuery(
				"select i from Image i " +
		 		"where i.details.owner.id = :id", new Parameters().addId(userUID));
		 return list;
		 }*/

	 
	/**
	 * GPL. Retrieves the dimensions in microns of the specified pixels set.
	 */
	 ome.model.core.PixelsDimensions getPixelsDimensions(long pixelsID)
		{
		ome.model.core.Pixels pixs = iQuery.get(ome.model.core.Pixels.class, pixelsID);
		return iQuery.get(ome.model.core.PixelsDimensions.class,
				pixs.getPixelsDimensions().getId().longValue());
		}

	
	/**
	 * GPL. Get pixel data
	 */
	 /*
	ome.model.core.Pixels getPixels(long pixelsID)
		{
		return (ome.model.core.Pixels) iQuery.findByQuery(
				"select p from Pixels as p " +
				"left outer join fetch p.pixelsType as pt " +
				"left outer join fetch p.pixelsDimensions " +
				"where p.id = :id",
				new Parameters().addId(new Long(pixelsID)));
		}
	*/

	/**
	 * GPL. Get channel information for pixel data
	 */
	List<ome.model.core.Channel> getChannelsData(long pixelsID)
		{
		ome.model.core.Pixels pixs = iQuery.findByQuery(
				"select p from Pixels as p " +
				"left outer join fetch p.pixelsType as pt " +
				"left outer join fetch p.channels as c " +
				"left outer join fetch p.pixelsDimensions " +
				"left outer join fetch c.logicalChannel as lc " +
				"left outer join fetch c.statsInfo where p.id = :id",
				new Parameters().addId(new Long(pixelsID)));
		LinkedList<ome.model.core.Channel> clist=new LinkedList<ome.model.core.Channel>();
		for(Object o:pixs.getChannels())
			clist.add((ome.model.core.Channel)o);
		return clist;//		return pixs.getChannels();
		}
	
	ome.model.core.LogicalChannel getLogicalChannel(ome.model.core.Channel ch)
		{
		return (ome.model.core.LogicalChannel) iQuery.findByQuery(
				"select p from LogicalChannel as p " +
				"left outer join fetch p.name " +
				"where p.id = :id",
				new Parameters().addId(ch.getLogicalChannel().getId()));
		}
	
	
	
	
	
	/**
	 * GPL.
	 * Returns the XY-plane identified by the passed z-section, timepoint 
	 * and wavelength.
	 * 
	 * @param pixelsID 	The id of pixels containing the requested plane.
	 * @param z			The selected z-section.
	 * @param t			The selected timepoint.
	 * @param c			The selected wavelength.
	 * @return See above.
	 * 
	 * 
	 * need to know bytes per pixel. Was synchronized
	 * 
	 */
	byte[] getPlane(long pixelsID, int z, int t, int c)
		{
		RawPixelsStore pservice = sf.createRawPixelsStore();
		pservice.setPixelsId(pixelsID);
		return pservice.getPlane(z, c, t);
		}

	
	
	
	//Pixels pixels;
	//pixels.getSizeT()

	public void getFile(long fileID)
		{
    RawFileStore rawFileStore = sf.createRawFileStore(); //how fast is this anyway?
		rawFileStore.setFileId(fileID);
		//rawFileStore.read(arg0, arg1);
		
		}
	
	
	
	/**
	 * Get pixel data
	 */
	public List<ome.model.core.Pixels> getPixels()
		{
		List<ome.model.core.Pixels> list=iQuery.findAllByQuery(
				"select p from Pixels as p " +
				"left outer join fetch p.pixelsType as pt " +
				"left outer join fetch p.pixelsDimensions " +
//				"where p.id = :id",
				"where p.details.owner.id = :id",
				new Parameters().addId(new Long(userUID)));
		return list;
		}
	
	//http://trac.openmicroscopy.org.uk/omero/ticket/857
	
	/**
	 * Get pixel data
	 */
	public List<ome.model.core.Pixels> getPixels(Image image)
		{
		long imageid=image.getId();
		List<ome.model.core.Pixels> list=iQuery.findAllByQuery(
				"select p from Pixels as p " +
				"left outer join fetch p.channels as c " +
				"left outer join fetch p.pixelsType as pt " +
				"left outer join fetch p.pixelsDimensions " +
				"where p.image.id = :id",
				new Parameters().addId(imageid));
		return list;
		}
	
	}
