package endrov.recordingOld;

import java.awt.image.BufferedImage;
import java.util.*;

import mmc.MacroRemoteClient;

/**
 * Thomas Burglin Microscope definition 
 * 
 * @author Johan Henriksson
 */
public class MicroscopeTBU extends Microscope
	{
	private MicroscopeChannelData curchannel;

	private Map<Integer, String> filtersEmission=new HashMap<Integer, String>();
	private Map<Integer, String> filtersExcitation=new HashMap<Integer, String>();
	
	
	private MacroRemoteClient macro=new MacroRemoteClient("localhost");
	
	public MicroscopeTBU()
		{
		filtersEmission.put(0, "RFP"); //TODO microscope settings
		
	

		
		}
	
	

	@Override
	public double[] getAxis()
		{
		return null;
		}

	@Override
	public int[] getAxisId()
		{
		return new int[]{AXISPZ,AXISMZ};
		}

	@Override
	public double[] getAxisRange(int id)
		{
		return null; //PZ!
		}

	@Override
	public MicroscopeChannel getChannel(String ch)
		{
		MicroscopeChannelData dc=new MicroscopeChannelData();
		return dc;
		}

	@Override
	public void setAxis(double[] a)
		{
		//double pz=a[AXISPZ];
		}

	@Override
	public void setChannel(MicroscopeChannel ch)
		{
		curchannel=(MicroscopeChannelData)ch;
		}

	@Override
	public Map<Integer, String> getFilters(int id)
		{
		Map<Integer, String> map=new HashMap<Integer, String>();
		if(id==EXCITATION)
			return filtersExcitation;
		else if(id==EMISSION)
			return filtersEmission;
		return map;
		}
	
	
	//////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	
	
	@Override
	public BufferedImage captureImage()
		{
		// TODO Auto-generated method stub
		return null;
		}

	
	//////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////

	
	@Override
	public BufferedImage[] captureStack()
		{
		curchannel.binning=1;
		macro.getClipboard();
		
//macro.mouseClick(300, 200, Macro.MOUSE_RIGHT);
		
		macro.keyType("foobarTest");
		macro.keyUp();macro.keyUp();macro.keyUp();
		
		
		/*
		BufferedImage image=client.getScreenshot();
		try
			{
			ImageIO.write(image,"png", new File("imagetest.jpg"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		client.quit();*/
		
		
		return null;
		}

	
	}
