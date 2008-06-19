package imserv;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

import javax.imageio.ImageIO;

/**
 * Generate thumbnails
 * 
 * @author Johan Henriksson
 */
public class ThumbMaker
	{
	private boolean isOST3(File file)
		{
		return file.isDirectory();
		}

	
	
	public byte[] getThumb(DataImpl data, File file) throws Exception
		{
		//OST3
		if(isOST3(file))
			{
			File thumbfile=new File(new File(file,"data"),"imserv.png");
			
			//Generate thumb and save down if it doesn't exist
			try
				{
				if(!thumbfile.exists() || file.lastModified()>thumbfile.lastModified())
					{
					//Locate slice to use
					File chandir=new File(file,"DIC");
					if(!chandir.exists())
						{
						String onechannel=data.channels.iterator().next();
						chandir=new File(file,onechannel);
						}
					
					LinkedList<String> frames=new LinkedList<String>();
					for(File child:chandir.listFiles())
						if(!child.getName().startsWith(".") && child.isDirectory())
							frames.add(child.getName());
					File framedir=new File(chandir,frames.get(frames.size()/2));
					
					LinkedList<String> slices=new LinkedList<String>();
					for(File child:framedir.listFiles())
						if(isImage(child.getName()))
							slices.add(child.getName());
					File slicefile=new File(framedir,slices.get(slices.size()/2));
					System.out.println(slicefile);

					//Draw image onto thumb
					BufferedImage im=new BufferedImage(80,80,BufferedImage.TYPE_3BYTE_BGR);
					Graphics g=im.getGraphics();
					BufferedImage sliceimage=ImageIO.read(slicefile);
					g.setColor(Color.RED);
					g.fillRect(0, 0, 80,80);
					
					g.drawImage(sliceimage, 0, 0, im.getWidth(), im.getHeight(), 
							Color.BLACK, null);
					ImageIO.write(im, "png", thumbfile);
					}
				}
			catch (RuntimeException e)
				{
				e.printStackTrace();
				}
			
			if(thumbfile.exists())
				return SendFile.getBytesFromFile(thumbfile);
			}
		return null;
		}


//return SendFile.getBytesFromImage(im);

	
	private static boolean isImage(String name)
		{
		return name.endsWith(".png") || name.endsWith(".jpg");
		}
	
	
	}
