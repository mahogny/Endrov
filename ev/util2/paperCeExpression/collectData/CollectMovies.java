package util2.paperCeExpression.collectData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;


import util.BatchMovie;

import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.flowMisc.EvOpAutoContrastBrightness2D;
import endrov.flowProjection.EvOpProjectMaxZ;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;
import endrov.util.ProgressHandle;

public class CollectMovies
	{

	
	/**
	 * Make a thumbnail image
	 */
	public static File makeThumbnail(File file) throws IOException
		{
		File outfile2=new File(new File(file,"data"),"thumbnailPNG.png");
		if(outfile2.exists())
			{
			System.out.println("Skipping "+file);
			return outfile2;
			}
		
		System.out.println("Doing imageset "+file.getPath());
		EvData ost=EvData.loadFile(file);
	
		if(ost==null)
			{
			System.out.println("Cannot load "+file);
			return null;
			}
		else
			{
			
	
			ProgressHandle ph=new ProgressHandle(); 
	
	
			//Get the imageset
			if(ost.getIdObjectsRecursive(Imageset.class).isEmpty())
				return null;
			Imageset imset=ost.getIdObjectsRecursive(Imageset.class).values().iterator().next();
			
			//Generate max channels
			EvChannel chGFP=(EvChannel)imset.metaObject.get("GFP");
			if(chGFP!=null)
				{
				imset.metaObject.put("GFPmax", new EvOpAutoContrastBrightness2D(false).exec1(ph,new EvOpProjectMaxZ().exec1(ph, chGFP)));
				}
/*
			EvChannel chRFP=(EvChannel)imset.metaObject.get("RFP");
			if(chRFP!=null)
				imset.metaObject.put("RFPmax", new EvOpProjectMaxZ().exec1(ph, chRFP));
	*/		
			

			//Add channels that should be in the movie. Figure out best width (original width)
			//int width=336;
			EvDecimal z=new EvDecimal(17);
			String name="GFPmax";
			//for(String name:new String[]{"DIC","GFPmax","RFPmax"})
			if(imset.metaObject.containsKey(name) && !((EvChannel)imset.metaObject.get(name)).getFrames().isEmpty())
				{


				//Get original image size
				EvChannel ch=(EvChannel)imset.metaObject.get(name);

				EvDecimal frame=ch.closestFrame(new EvDecimal(3600*4));


				EvStack stack=ch.getStack(frame);
				int zu=stack.closestZint(z.doubleValue());
				
				EvPixels p=stack.getInt(zu).getPixels(ph);
				
				BufferedImage bim=p.quickReadOnlyAWT();
				
				ImageIO.write(bim, "png", outfile2);
				
				}


			return outfile2;
			}
		}
	
	
	
	
	
	
	
	
	
	
	/**
	 * Assumes movies are already created - then puts them all in the right directory for publication
	 */
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
		new PaperCeExpressionUtil(); //Get password right away so it doesn't stop later
		
		//Find recordings to compare
		Set<File> datas=PaperCeExpressionUtil.getAnnotated(); 

		/*
		if(JOptionPane.showConfirmDialog(null, "Empty cache?", "", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
			PaperCeExpressionUtil.removeAllTags();
		*/
		File outdir=new File("/home/tbudev3/articleMovies"); 

		
		try
			{
			outdir.mkdirs();
			EvFileUtil.deleteRecursive(outdir);
			outdir.mkdirs();
		
			
			TreeSet<String> htmlrows=new TreeSet<String>(); 
			
			for(File ostfile:PaperCeExpressionUtil.getAllOST())
				if(!ostfile.getName().startsWith("."))
					{
					System.out.println(ostfile);
					File fInfile=new File(new File(ostfile,"data"),"thumbnailMPEG4.avi");
					
					//Make movie!!
					if(!fInfile.exists())
						BatchMovie.makeMovie(ostfile);
					
					
					if(fInfile.exists())
						{
						
						String gfpgene=PaperCeExpressionUtil.getGeneName(ostfile);
						String orfname=PaperCeExpressionUtil.getORF(ostfile);
						
						System.out.println(gfpgene);
						
						boolean quantified=datas.contains(ostfile);
						String inset=quantified ? " quantified" : "";
						
						
						String annotatedname=gfpgene+" ("+ostfile.getName()+")"+inset;
						
						File fOutfileMovie=new File(outdir,annotatedname+".avi");
						System.out.println("----> "+fOutfileMovie);
						
						
						if(!fOutfileMovie.exists())
							EvFileUtil.copy(fInfile, fOutfileMovie);
						
						
						File fOutfilePNG=new File(outdir,annotatedname+".png");
						File fInfilePNG=makeThumbnail(ostfile);
						if(fInfilePNG!=null)
							EvFileUtil.copy(fInfilePNG, fOutfilePNG);
						else
							System.out.println("Warning: no thumbnail png");
						
						
						htmlrows.add(
							"<tr>"+
							"<td><img width=\"100\" src=\""+fOutfilePNG.getName()+"\"/></td>" +
							"<td><a href=\""+fOutfileMovie.getName()+"\">"+gfpgene+"</a></td>" +
							"<td>"+orfname+"</td><td>"+(quantified?"*":"")+"</td>" +
							"<td>"+ostfile.getName()+"</td>"+
							"</tr>\n"
							);
						
						}
					else
						{
						System.out.println("Failed to generate movie");
						}
					
					}

			//Write index.htm
			File fOutfileIndex=new File(outdir,"index.htm");
			StringBuilder htmlfile=new StringBuilder();
			htmlfile.append(
					"<table>\n" +
					"<tr>" +
					"<th>&nbsp;</th>" +
					"<th>Gene</th>" +
					"<th>ORF</th>" +
					"<th>Quantified</th>" +
					"<th>Recording</th>" +
					"</tr>\n");
			for(String line:htmlrows)
				htmlfile.append(line);
			htmlfile.append("</table>");
			EvFileUtil.writeFile(fOutfileIndex, htmlfile.toString());
			
			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		System.exit(0);
		
		
		}
	
	}
