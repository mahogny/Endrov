package evplugin.lociBioformats;

import evplugin.imageset.*;

import java.awt.image.*;

public class TestLSM
	{
	public static void main(String[] arg)
		{
		try
			{
			Imageset rec=new BioformatsImageset("/home/mahogny/_imagedata/2chZT.lsm");
			
			System.out.println("_ "+System.currentTimeMillis());
			BufferedImage im=rec.getChannel("ch0").getImageLoader(5, 0).getJavaImage();
			im.getData();
			System.out.println("__"+System.currentTimeMillis());
			
			System.out.println("# "+System.currentTimeMillis());
			BufferedImage im2=rec.getChannel("ch0").getImageLoader(5, 0).getJavaImage();
			im2.getData();
			System.out.println("##"+System.currentTimeMillis());
			
			}
		catch (Exception e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		
		
		
		}
	}
