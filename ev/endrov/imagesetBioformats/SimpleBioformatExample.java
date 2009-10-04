package endrov.imagesetBioformats;
import java.io.File;

import loci.formats.ChannelSeparator;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import endrov.imageset.BioformatsSliceIO;


public class SimpleBioformatExample
	{
	/** Path to imageset */
	public File basedir;
	
	public IFormatReader imageReader=null;
	public IMetadata retrieve=null;
	
	double[][][] stack;
	
	/**
	 * Open a new recording
	 */
	public SimpleBioformatExample(File basedir) throws Exception
		{
		this.basedir=basedir;
		if(!basedir.exists())
			throw new Exception("File does not exist");

		imageReader=new ImageReader();
		retrieve=MetadataTools.createOMEXMLMetadata();
		imageReader.setMetadataStore(retrieve);
		imageReader.setId(basedir.getAbsolutePath());
		imageReader=new ChannelSeparator(imageReader);
		
		buildDatabase();
		}
	
	

	/**
	 * Scan recording for channels and build a file database
	 */
	public void buildDatabase()
		{
		int seriesIndex=0;

		imageReader.setSeries(seriesIndex);
		int numz=imageReader.getSizeZ();
		int numt=imageReader.getSizeT();
		int numc=imageReader.getSizeC();

		//It *must* be 0,0
		Float fdx=retrieve.getDimensionsPhysicalSizeX(0, 0); //um/px
		Float fdy=retrieve.getDimensionsPhysicalSizeY(0, 0); //um/px
		Float fdz=retrieve.getDimensionsPhysicalSizeZ(0, 0); //um/px

		//Enlist images
		int channelnum=0;
		int framenum=0;

		if(fdx==null || fdx==0) fdx=1.0f;
		if(fdy==null || fdy==0) fdy=1.0f;
		if(fdz==null || fdz==0) fdz=1.0f;
		
		stack=new double[numz][][];

		//For every slice
		for(int slicenum=0;slicenum<numz;slicenum++)
			{
			int curPixel;
			Integer bandID=null;
			if(imageReader.isRGB())
				{
				curPixel=imageReader.getIndex(slicenum, 0, framenum);
				bandID=channelnum;
				}
			else
				curPixel=imageReader.getIndex(slicenum, channelnum, framenum);

			BioformatsSliceIO io=new BioformatsSliceIO(imageReader, curPixel, bandID, "");
			double[][] oneplane=io.loadJavaImage().getArrayDouble2D();
			stack[slicenum]=oneplane;
			}
		}


	
	}
