/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowThreshold;

import java.awt.Color;

import javax.swing.ImageIcon;

import endrov.flowBasic.RendererFlowUtil;


public class CategoryInfo
	{
	public static final String name="Thresholding";
	public static final Color bgColor=RendererFlowUtil.colOperation;
	
	public static final ImageIcon icon=new ImageIcon(CategoryInfo.class.getResource("jhFlowCategoryThreshold.png"));

	}
