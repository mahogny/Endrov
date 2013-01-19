/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareFrivolous;

import java.io.File;

import endrov.util.io.EvFileUtil;


/**
 * @author David Johansson, Arvid Johansson, Johan Henriksson
 */
public class FrivolousModel
	{
	public FrivolousCell cell;

	
	public static File getStandardExperiment()
		{
		try
			{
			return EvFileUtil.getFileFromURL(FrivolousModel.class.getResource("data").toURI().toURL());
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
		}
	
	public FrivolousModel(File f)
		{
		try
			{
			cell = new FrivolousCell(f);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	//area scanned by detectors. before changing, make sure code can handle it
	public final int imageWidth=512, imageHeight=512; 

	public int[] convolve(int offsetX, int offsetY, boolean simulatePSF, boolean simulateNoise)
		{
		return cell.getImage(offsetX, offsetY, imageWidth, imageHeight, simulatePSF, simulateNoise);
		}

	public FrivolousSettingsNew getSettings()
		{
		return cell.getSettings();
		}

	public void updatePSF()
		{
		cell.updatePSF();
		}
	
	public void stop()
		{
		cell.stop();
		}

	}
