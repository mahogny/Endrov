package endrov.imglib;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.imglib2.algorithm.gauss.Gauss;
import net.imglib2.exception.ImgLibException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.real.FloatType;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowBasic.images.EvOpImageConvertPixel;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;

public class ImglibTest
	{
	public static void main(String[] args)
		{
		
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();

		
		EvData data=EvData.loadFile(new File("/home/mahogny/Desktop/4.png"));
		EvChannel ch=data.getIdObjectsRecursive(EvChannel.class).values().iterator().next();
		EvStack stack=ch.getStack(ch.getFirstFrame());
		
		stack=new EvOpImageConvertPixel(EvPixelsType.FLOAT).exec1(null, stack);
		
		ImgPlus<FloatType> imp=EvStackAdapter.wrapEvStack(stack);
		
		Img<FloatType> image=imp.getImg();
		
		Img<FloatType> out=Gauss.toFloat(8, image);
		
		FloatEvStack<FloatType> s2=(FloatEvStack<FloatType>)out;

		try
			{
			BufferedImage bim=s2.getEvStack().getInt(0).getPixels().quickReadOnlyAWT();
			
			JFrame f=new JFrame();
			f.add(new JLabel(new ImageIcon(bim)));
			f.pack();
			f.setVisible(true);
			}
		catch (ImgLibException e)
			{
			e.printStackTrace();
			}
		/*
		System.out.println();
		System.exit(0);
		*/
		}

	}
