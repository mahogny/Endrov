/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverFrivolous;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import endrov.util.EvFileUtil;


/**
 * @author David Johansson, Arvid Johansson, Johan Henriksson
 */
public class FrivolousModel
	{

	private ActionListener model_action;
	//private BufferedImage output_image = null;
	private int[] output_image = null;
	private FrivolousCell cell;

	public FrivolousModel()
		{
		//cell = new FrivolousCell(new File(FrivolousDeviceProvider.class.getResource("data").getFile()));
		try
			{
			cell = new FrivolousCell(EvFileUtil.getFileFromURL(FrivolousDeviceProvider.class.getResource("data").toURI().toURL()));
			convolve(0,0);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	//area scanned by detectors. before changing, make sure code can handle it
	public final int imageWidth=512, imageHeight=512; 

	public void convolve(int offsetX, int offsetY)
		{
		output_image = cell.getImage(offsetX, offsetY, imageWidth, imageHeight);
		if (model_action!=null)
			model_action.actionPerformed(new ActionEvent(this, 0, "image_updated"));
		}

	public int[] /*BufferedImage*/ getImage()
		{
		return output_image;
		}

	public void setActionListener(ActionListener listener)
		{
		model_action = listener;
		}

	public FrivolousSettingsNew getSettings()
		{
		return cell.getSettings();
		}

	public void updatePSF()
		{
		cell.updatePSF();
		}

	}
