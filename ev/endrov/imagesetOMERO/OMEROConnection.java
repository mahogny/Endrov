package endrov.imagesetOMERO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import omero.ServerError;
import omero.client;
import omero.api.IContainerPrx;
import omero.api.IMetadataPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.Dataset;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Project;
import omero.sys.ParametersI;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.ProjectData;

/**
 * Connection to an OMERO database
 * @author Johan Henriksson
 *
 */
public class OMEROConnection
	{
	private client client;
	private ServiceFactoryPrx entry;
	
	public void connect(String hostName, int port, String userName, String password, boolean encrypted) throws CannotCreateSessionException, PermissionDeniedException, ServerError
		{
		client=new client(hostName, port);
		entry = getClient().createSession(userName, password);

		if(!encrypted)
			{
			client unsecureClient = client.createClient(false);
			entry = unsecureClient.getSession();
			}
		}
	
	public Long getMyUserId() throws ServerError
		{
		return entry.getAdminService().getEventContext().userId;
		}
	
	
	/**
	 * Get all users this connection has access to(?)
	 * not tested
	 */
	public Set<Long> getUserIDs() throws ServerError
		{
		//entry.getAdminService().getMemberOfGroupIds(experimenter)
		//Experimenter experimenter=entry.getAdminService().getExperimenter(getMyUserId());
		
		List<ExperimenterGroup> expgroups=entry.getAdminService().containedGroups(getMyUserId());
		Set<Long> expIds=new HashSet<Long>(); 
		for(ExperimenterGroup g:expgroups)
			{
			//g.get
			for(Experimenter e:entry.getAdminService().containedExperimenters(g.getId().getValue()))
				expIds.add(e.getId().getValue());
					//exp
			//expIds.addAll());
			}
		
		for(long id:expIds)
			{
			System.out.println(entry.getAdminService().getExperimenter(id).getFirstName().toString());
			}
		
		return expIds;
		}
	
	public Long getMyGroupId() throws ServerError
		{
		return entry.getAdminService().getEventContext().groupId;
		}
	
	
	public void disconnect()
		{
		getClient().closeSession();
		}
	
	
	/**
	 * Get projects for a user
	 */
	public Set<ProjectData> getProjectsForUser(long userId) throws ServerError
		{
		IContainerPrx proxy = getEntry().getContainerService();
		ParametersI param = new ParametersI();
		param.exp(omero.rtypes.rlong(userId));
		param.leaves(); //indicate to load the images
		//param.noLeaves(); //no images loaded, this is the default value.
		List<IObject> results = proxy.loadContainerHierarchy(Project.class.getName(), new ArrayList<Long>(), param);

		
		Set<ProjectData> datasets=new HashSet<ProjectData>();
		for(IObject o:results)
			datasets.add(new ProjectData((Project)o));
		return datasets;
		/*
	
			//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		Iterator<IObject> i = results.iterator();

		while (i.hasNext()) 
			{
			ProjectData project = new ProjectData((Project) i.next());
			Set<DatasetData> datasets = project.getDatasets();
			Iterator<DatasetData> j = datasets.iterator();
			while (j.hasNext()) 
				{
				DatasetData dataset = j.next();
				//Do something here
				//If images loaded.
				//dataset.getImages();
				}
			}

*/

		}
	
	/**
	 * Retrieve the Datasets for a user 
	 */
	public Set<DatasetData> getDatasetsForUser(long userId) throws ServerError
		{
		IContainerPrx proxy = getEntry().getContainerService();
		ParametersI param = new ParametersI();
		param.exp(omero.rtypes.rlong(userId));
		param.leaves(); //indicate to load the images
		List<IObject> results = proxy.loadContainerHierarchy(Dataset.class.getName(), new ArrayList<Long>(), param);
		
		Set<DatasetData> datasets=new HashSet<DatasetData>();
		for(IObject o:results)
			datasets.add(new DatasetData((Dataset)o));

		return datasets;
		/*
		
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		Iterator<IObject> i = results.iterator();
		while (i.hasNext()) 
			{
			DatasetData dataset = new DatasetData((Dataset) i.next());
			Set<ImageData> images = dataset.getImages();
			Iterator<ImageData> j = images.iterator();
			while (j.hasNext()) 
				{
				ImageData image = j.next();
				//Do something
				}
			}
*/

		
		
		}
	
	/**
	 * Retrieve the Images contained in a Dataset. 
	 */
	@SuppressWarnings("unchecked")
	public Set<ImageData> getImagesForDataset(Long datasetId) throws ServerError
		{
		
		IContainerPrx proxy = getEntry().getContainerService();
		ParametersI param = new ParametersI();
		param.leaves(); //indicate to load the images

		List<Long> ids = new ArrayList<Long>();
		ids.add(datasetId);
		List<IObject> results = proxy.loadContainerHierarchy(Dataset.class.getName(), ids, param);

		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.

		DatasetData dataset = new DatasetData((Dataset)results.get(0));
		
		return dataset.getImages();
		
		/*
		Set<ImageData> images = dataset.getImages();
		for (ImageData image:images)//j.hasNext()) 
			{
			image.getName();
			
			//ImageData image = j.next();
			//Do something
			}
*/


		}
	
	
	
	
	//Retrieve an Image if the identifier is known.
	public Image getImage(Long imageId) throws ServerError
		{
		IContainerPrx proxy = getEntry().getContainerService();
		List<Long> ids = new ArrayList<Long>();
		ids.add(imageId);
		List<Image> results = proxy.getImages(Image.class.getName(), ids, new ParametersI());
		
		return results.get(0);
		
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
//		ImageData image = new ImageData(results.get(0));

		
		}
	
	
	//Access information about the image for example to draw it. The model is a follow: Image-Pixels i.e. to access valuable data about the image you need to use the pixels object. We now only support one set of pixels per image (it used to be more!).
	/*
	public void getDimensions(ImageData image) 
		{
		
		PixelsData pixels = image.getDefaultPixels();   //There are more!!!
		int sizeZ = pixels.getSizeZ(); // The number of z-sections.
		int sizeT = pixels.getSizeT(); // The number of timepoints.
		int sizeC = pixels.getSizeC(); // The number of channels.
		int sizeX = pixels.getSizeX(); // The number of pixels along the X-axis.
		int sizeY = pixels.getSizeY(); // The number of pixels along the Y-axis.

		}
	*/
	
	/**
	 * 
	 * there are many more examples for wells etc. see http://trac.openmicroscopy.org.uk/ome/wiki/OmeroJava
	 * 
	 * 
	 * Image img = set.iterator().next();
  	  	ImageData test = new ImageData( img );
  	  	
  	  	
  	  and there are many writing examples
  	  
  	  ROIs
  	  
  	  deleting stuff
  	  
  	  server-side rendering & thumbnails
  	  

	 * 
	 */
	
	
	//How to create a file annotation and link to an image. 
	public void uploadFile(ImageData image, File file, String generatedSha1, String fileMimeType, String description, String namespace)
	throws ServerError, FileNotFoundException, IOException
		{
		
	// To retrieve the image see above.
		int INC = 262144;
		String name = file.getName();
		String absolutePath = file.getAbsolutePath();
		String path = absolutePath.substring(0, 
		  absolutePath.length()-name.length());

		IUpdatePrx iUpdate = getEntry().getUpdateService(); // service used to write object
		// create the original file object.
		OriginalFile originalFile = new OriginalFileI();
		originalFile.setName(omero.rtypes.rstring(name));
		originalFile.setPath(omero.rtypes.rstring(path));
		originalFile.setSize(omero.rtypes.rlong(file.length()));
		originalFile.setSha1(omero.rtypes.rstring(generatedSha1));
		originalFile.setMimetype(omero.rtypes.rstring(fileMimeType)); // or "application/octet-stream"
		// now we save the originalFile object
		originalFile = (OriginalFile) iUpdate.saveAndReturnObject(originalFile);

		// Initialize the service to load the raw data
		RawFileStorePrx rawFileStore = getEntry().createRawFileStore();
		rawFileStore.setFileId(originalFile.getId().getValue());

		FileInputStream stream = new FileInputStream(file);
		long pos = 0;
		int rlen;
		byte[] buf = new byte[INC];
		ByteBuffer bbuf;
		while ((rlen = stream.read(buf)) > 0) {
		  rawFileStore.write(buf, pos, rlen);
		  pos += rlen;
		  bbuf = ByteBuffer.wrap(buf);
		  bbuf.limit(rlen);
		}
		stream.close();

		originalFile = rawFileStore.save();
		// Important to close the service
		rawFileStore.close();

		//now we have an original File in DB and raw data uploaded.
		// We now need to link the Original file to the image using 
		// the File annotation object. That's the way to do it.
		FileAnnotation fa = new FileAnnotationI();
		fa.setFile(originalFile);
		fa.setDescription(omero.rtypes.rstring(description));
		fa.setNs(omero.rtypes.rstring(namespace)); // The name space you have set to identify the file annotation.

		// save the file annotation.
		fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);

		// now link the image and the annotation
		ImageAnnotationLink link = new ImageAnnotationLinkI();
		link.setChild(fa);
		link.setParent(image.asImage());
		// save the link back to the server.
		link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
		// To attach to a Dataset use DatasetAnnotationLink;

		
		}
	
	//Load all the annotations with a given namespace linked to images. 
	public List<Annotation> loadAllAnnotationsInNamespace(long userId, String namespace) throws ServerError
		{
		List<String> nsToInclude = new ArrayList<String>();
		nsToInclude.add(namespace);
		List<String> nsToExclude = new ArrayList<String>();
		ParametersI param = new ParametersI();
		param.exp(omero.rtypes.rlong(userId)); //load the annotation for a given user.
		IMetadataPrx proxy = getEntry().getMetadataService();
		// retrieve the annotations linked to images, for datasets use: omero.model.Dataset.class
		return proxy.loadSpecifiedAnnotations(FileAnnotation.class.getName(), nsToInclude, nsToExclude, param);
		}
	
	
	public void downloadFile(List<Annotation> annotations, File file) throws ServerError, IOException 
		{
		int INC = 32;

		int index = 0;  //TODO!!!!

		RawFileStorePrx store = getEntry().createRawFileStore();
		FileOutputStream stream = new FileOutputStream(file);
		for (Annotation annotation:annotations)
			{
			if (annotation instanceof FileAnnotation && index==0)
				{ // read the first one.
				FileAnnotationData fa = new FileAnnotationData((FileAnnotation) annotation);
				// The id of the original file

				long size = fa.getFileSize();
				store.setFileId(fa.getFileID());
				int offset = 0;
				try
					{
					for (offset = 0; (offset+INC)<size;)
						{
						stream.write(store.read(offset, INC));
						offset += INC;
						}
					}
				finally
					{
					stream.write(store.read(offset, (int) (size-offset)));
					stream.close();
					}
				break;
				}
			}

		store.close();

		}


	public client getClient()
		{
		return client;
		}
	public ServiceFactoryPrx getEntry()
		{
		return entry;
		}

	
	}
