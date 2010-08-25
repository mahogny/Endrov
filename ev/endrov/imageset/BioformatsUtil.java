package endrov.imageset;

import java.io.File;

import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

import loci.common.DataTools;
import loci.common.services.ServiceFactory;
import loci.formats.FormatTools;
import loci.formats.IFormatWriter;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;

public class BioformatsUtil
	{

	/**
	 * Save single image as TIFF. 
	 */
	public static void saveImageAsTiff(EvPixels p, File file)
		{
		try
			{

			// http://loci.wisc.edu/trac/java/browser/trunk/components/bio-formats/utils/MinimumWriter.java

			ServiceFactory factory = new ServiceFactory();
			OMEXMLService service = factory.getInstance(OMEXMLService.class);
			IMetadata store = service.createOMEXMLMetadata();

			store.createRoot();
			store.setImageID("Image:0", 0);
			store.setPixelsID("Pixels:0", 0);

			store.setPixelsSizeX(new PositiveInteger(p.getWidth()), 0);
			store.setPixelsSizeY(new PositiveInteger(p.getHeight()), 0);
			store.setPixelsSizeZ(new PositiveInteger(1), 0);
			store.setPixelsSizeC(new PositiveInteger(1), 0);
			store.setPixelsSizeT(new PositiveInteger(1), 0);
			store.setChannelID("Channel:0:0", 0, 0);
			store.setChannelSamplesPerPixel(new PositiveInteger(1), 0, 0);



			//Convert to byte array
			int ptype;
			byte[] barr;
			if(p.getType()==EvPixelsType.SHORT)
				{
				short[] array=p.getArrayShort();
				ptype=FormatTools.INT16;
				barr=DataTools.shortsToBytes(array, true);
				}
			else if(p.getType()==EvPixelsType.FLOAT)
				{
				float[] array=p.getArrayFloat();
				ptype=FormatTools.FLOAT;
				barr=DataTools.floatsToBytes(array, true);
				}
			else
				//TODO Double
				{
				p=p.convertToInt(true);
				int[] array=p.getArrayInt();
				ptype=FormatTools.INT32;
				barr=DataTools.intsToBytes(array, true);
				}

			store.setPixelsBinDataBigEndian(Boolean.FALSE, 0, 0);
			store.setPixelsDimensionOrder(DimensionOrder.XYZCT, 0);
			store.setPixelsType(PixelType.fromString(FormatTools.getPixelTypeString(ptype)), 0);


			///// store binary data
			IFormatWriter writer = new ImageWriter();
			writer.setMetadataRetrieve(store);
			writer.setId(file.getAbsolutePath());
			writer.saveBytes(0, barr);
			writer.close();

			//for(int i=0;i<barr.length;i+=4)
			//	System.out.println("  "+barr[i]+" "+barr[i+1]+" "+barr[i+2]+" "+barr[i+3]);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	}
