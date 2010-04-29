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
	private BufferedImage output_image = null;
	private FrivolousCell cell;

	public FrivolousModel()
		{
		//cell = new FrivolousCell(new File(FrivolousDeviceProvider.class.getResource("data").getFile()));
		try
			{
			cell = new FrivolousCell(EvFileUtil.getFileFromURL(FrivolousDeviceProvider.class.getResource("data").toURI().toURL()));
			convolve();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	public void convolve()
		{
		output_image = cell.getImage();
		if (model_action!=null)
			model_action.actionPerformed(new ActionEvent(this, 0, "image_updated"));
		}

	public BufferedImage getImage()
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
