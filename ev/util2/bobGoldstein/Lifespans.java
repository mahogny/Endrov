/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.bobGoldstein;

import java.io.File;
import java.io.IOException;

import util2.paperCeExpression.collectData.PaperCeExpressionUtil;

import endrov.ev.EV;
import endrov.lineage.Lineage;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;

public class Lifespans 
	{
	public static void main(String[] args)
		{
		EV.loadPlugins();
		
		try
			{
			System.out.println("here");
			
			Lineage lin=PaperCeExpressionUtil.loadModel();
			
			
			StringBuffer outputBuffer=new StringBuffer();
			outputBuffer.append("CELL NAME\tDIV TIME\n");
			
			for(String nucName:lin.particle.keySet())
				{
				Lineage.Particle nuc=lin.particle.get(nucName);

				if(!nuc.pos.isEmpty())
					{
					outputBuffer.append(
							nucName+"\t"+
							(nuc.getLastFrame().divide(new EvDecimal("60")))+"\n");
					}
				}

			
			EvFileUtil.writeFile(new File("/tmp/forbob.csv"), outputBuffer.toString());
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		System.exit(0);
		}
	}
