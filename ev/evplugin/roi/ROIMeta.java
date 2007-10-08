package evplugin.roi;

//import org.jdom.*;
//import java.util.*;
import evplugin.imageWindow.*;
//import evplugin.metadata.*;

public class ROIMeta
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	//private static final String metaType="filter";
	
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ROIImageExtension());
//		Metadata.extensions.put(metaType,new ImagesetMetaObjectExtension());
		}

	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/
	
	/*
	public static class ImagesetMetaObjectExtension implements MetaObjectExtension
		{
		public MetaObject extractObjects(Element e)
			{
			FilterMeta meta=new FilterMeta();
			
			for(Object oi:e.getChildren())
				{
				Element i=(Element)oi;
				
				if(i.getName().equals("timestep"))
					meta.metaTimestep=Double.parseDouble(i.getValue());
				else if(i.getName().equals("resX"))
					meta.resX=Double.parseDouble(i.getValue());
				else if(i.getName().equals("resY"))
					meta.resY=Double.parseDouble(i.getValue());
				else if(i.getName().equals("resZ"))
					meta.resZ=Double.parseDouble(i.getValue());
				else if(i.getName().equals("NA"))
					meta.metaNA=Double.parseDouble(i.getValue());
				else if(i.getName().equals("objective"))
					meta.metaObjective=Double.parseDouble(i.getValue());
				else if(i.getName().equals("optivar"))
					meta.metaOptivar=Double.parseDouble(i.getValue());
				else if(i.getName().equals("campix"))
					meta.metaCampix=Double.parseDouble(i.getValue());
				else if(i.getName().equals("slicespacing"))
					meta.metaSlicespacing=Double.parseDouble(i.getValue());
				else if(i.getName().equals("sample"))
					meta.metaSample=i.getValue();
				else if(i.getName().equals("description"))
					meta.metaDescript=i.getValue();
				else if(i.getName().equals("channel"))
					{
					FilterMeta.Channel ch=extractChannel(meta, i);
					meta.channel.put(ch.name, ch);
					}
				else if(i.getName().equals("frame"))
					extractFrame(meta.metaFrame, i);
				else
					meta.metaOther.put(i.getName(), i.getValue());
				}
			
			return meta;
			}
	
	
		public FilterMeta.Channel extractChannel(FilterMeta data, Element e)
			{
			FilterMeta.Channel ch=new FilterMeta.Channel();
			ch.name=e.getAttributeValue("name");
			
			for(Object oi:e.getChildren())
				{
				Element i=(Element)oi;
				
				if(i.getName().equals("dispX"))
					ch.dispX=Double.parseDouble(i.getValue());
				else if(i.getName().equals("dispY"))
					ch.dispY=Double.parseDouble(i.getValue());
				else if(i.getName().equals("binning"))
					ch.chBinning=Integer.parseInt(i.getValue());
				else if(i.getName().equals("frame"))
					extractFrame(ch.metaFrame, i);
				else
					ch.metaOther.put(i.getName(), i.getValue());
				}
			
			return ch;
			}
		
		public void extractFrame(HashMap<Integer,HashMap<String,String>> metaFrame, Element e)
			{
			int fid=Integer.parseInt(e.getAttributeValue("frame"));
			for(Object oi:e.getChildren())
				{
				Element i=(Element)oi;
				HashMap<String,String> frame=metaFrame.get(fid);
				if(frame==null)
					{
					frame=new HashMap<String,String>();
					metaFrame.put(fid, frame);
					}
				frame.put(i.getName(), i.getValue());
				}
			
			}
		
		}
		*/

	
	/******************************************************************************************************
	 *                               Channel                                                              *
	 *****************************************************************************************************/

	}
