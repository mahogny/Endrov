package evgui;

import javax.imageio.*;
//import com.sun.media.imageio.plugins.jpeg2000.*;
//import com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam;

public class ListJAIformats 
	{
	public static void main(String args[])
		{
		String[] wf=ImageIO.getWriterFormatNames();
		for(String s:wf)
			System.out.println(s+" ");
		System.out.println();
//		 String writerFormats[] = {"JPEG2000","JPEG 2000","JPEG-LOSSLESS","jpeg-ls"};
		
		}
	}