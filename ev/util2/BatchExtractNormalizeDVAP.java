package util2;

import evplugin.ev.*;
import evplugin.imagesetOST.*;
import evplugin.nuc.*;
import evplugin.data.*;
import evplugin.imageset.*;

import java.util.*;
import java.io.*;
import java.awt.image.*;
//import javax.vecmath.*;

/**
 * Go through all imagesets in a directory and
 * extract a compressed voxel set for each embryo based on cell coordinates 
 * @author Johan Henriksson
 */
public class BatchExtractNormalizeDVAP
{

public static void calcAP(File file, BufferedWriter gnufile, BufferedWriter htmlfile, BufferedWriter rotfile, BufferedWriter omitted, int recordingNumber)
	{
	String PathForErrorFile = file.getPath();
	try
		{
		//System.out.println("current Imageset "+file.getPath());
		String currentpath = file.getPath();
		System.out.println("current imageset: "+currentpath);
		if (0==0) // all imagesets
		//if (currentpath.equals("/Volumes/TBU_xeon01_500GB01/ost4dgood/BC12759070823")) // only deal with this test image set
			{	
			System.out.println("imageset found, executing...");

			long currentTime=System.currentTimeMillis();
			OstImageset ost=new OstImageset(file.getPath());

			String currentostname = ost.toString();
			//System.out.println("current imageset: " + currentostname);
			//String currentostDescription = ost.description.toString();
			double timeStep = 1;
			timeStep = ost.meta.metaTimestep;
			//System.out.println("time step: " + timeStep + " seconds");

			for(EvObject evob:ost.metaObject.values()) // go through all objects in XML file
				{
				//System.out.println("timestep = "+ ost.metaObject.metaTimestep);

				if(evob instanceof NucLineage) // if the current object is a Lineage
					{
					NucLineage lin=(NucLineage)evob;
					//test whether the metadata contains enough information
					if(lin.nuc.get("post")!=null && lin.nuc.get("ant")!=null && 
							lin.nuc.get("shellmid1")!=null && lin.nuc.get("shellmid2")!=null &&
							lin.nuc.get("shellup")!=null && lin.nuc.get("shelldown")!=null &&
							lin.nuc.get("EMS")!=null &&
							lin.nuc.get("E")!=null && lin.nuc.get("Ep")!=null &&
							lin.nuc.get("venc")!=null)

						{
						//get reference frames for time adjustment
						NucLineage.Nuc EMSNuc = lin.nuc.get("EMS");
						NucLineage.Nuc ENuc = lin.nuc.get("E");
						NucLineage.Nuc EpNuc = lin.nuc.get("Ep");
						NucLineage.Nuc vencNuc = lin.nuc.get("venc");
						int EMSNucFirstKey = EMSNuc.pos.firstKey();
						int ENucFirstKey = ENuc.pos.firstKey(); 
						int EpNucFirstKey = EpNuc.pos.firstKey();
						int vencNucFirstKey = vencNuc.pos.firstKey();
						int EMSNucLastKey = EMSNuc.pos.lastKey();
						int ENucLastKey = ENuc.pos.lastKey();
						int EpNucLastKey = EpNuc.pos.lastKey();
						int vencNucLastKey = vencNuc.pos.lastKey();
//						System.out.println("EMS first key " + EMSNucFirstKey + ", last key " + EMSNucLastKey);
//						System.out.println("E first key " + ENucFirstKey + ", last key " + ENucLastKey);
//						System.out.println("Ep first key " + EpNucFirstKey + ", last key " + EpNucLastKey);
//						System.out.println("venc first key " + vencNucFirstKey + ", last key " + vencNucLastKey);

						NucLineage.NucPos ventralRefPos = getIntPos(lin,"E",ENucLastKey);

						Imageset.ChannelImages ch=ost.channelImages.get("GFP");
						TreeMap<Integer, TreeMap<Integer, EvImage>> images=ch.imageLoader;

						//create an output file in the data directory

						BufferedWriter outFile;
						File outFilePath=new File(ost.datadir(),"DVtimeRotatedGFP.txt");
						outFile = new BufferedWriter( new FileWriter(outFilePath) );
						
						BufferedWriter angleFile;
						File angleFilePath=new File(ost.datadir(),"rotationAngleTableNotMod.txt");
						angleFile = new BufferedWriter(new FileWriter(angleFilePath));
						
						BufferedWriter distanceFile;
						File distanceFilePath=new File(ost.datadir(),"distanceToAP.txt");
						distanceFile = new BufferedWriter(new FileWriter(distanceFilePath));
						
						
						double[] rotationAngle=new double[3]; // statistics of rotation angle
						double[] rotationNumber=new double[3];
						for (int v=0;v<3;v++)
							{
							rotationAngle[v]=0;
							rotationNumber[v]=0;
							}

						//scaling to write doubles into file:
						int scaleX = 25;
						int scaleY = 25;
						int scaleZ = 25;
						outFile.write("imageset: " + currentostname + "\n");
						outFile.write("scaleX\n");
						outFile.write(scaleX+"\n");
						outFile.write("scaleY\n");
						outFile.write(scaleY+"\n");
						outFile.write("scaleZ\n");
						outFile.write(scaleZ+"\n");

						//write timeStep
						outFile.write("timeStep\n");
						outFile.write(timeStep + "\n");

						//write the 'time scale' reference frames from annotations
						outFile.write("ENucFirstKey\n");
						outFile.write(ENucFirstKey+"\n");
						outFile.write("ENucLastKey\n");
						outFile.write(ENucLastKey+"\n");
						outFile.write("EpNucFirstKey\n");
						outFile.write(EpNucFirstKey +"\n");
						outFile.write("EpNucLastKey\n");
						outFile.write(EpNucLastKey +"\n");
						outFile.write("vencNucFirstKey\n");
						outFile.write(vencNucFirstKey+"\n");
						outFile.write("vencNucLastKey\n");
						outFile.write(vencNucLastKey+"\n");

						//mask which pixels to take
						boolean goodpix [][][] = null;
						int goodpixcoordX [][][] = null;
						int goodpixcoordY [][][] = null;
						int goodpixcoordZ [][][] = null;
						//old positions for rotation matrix update
						double oldAntPosX = -1;
						double oldAntPosY = -1;
						double oldAntPosZ = -1;

						double oldPostPosX = -1;
						double oldPostPosY = -1;
						double oldPostPosZ = -1; 

						//Vector<int[][][]> v=new Vector<int[][][]>();						
						//int[][][] vox=new int[5][5][5];
						//v.add(vox);
						//v.get(0)[1][1][1]=5;
						//example v.add(5);
						//v.get(0)[32][4];


						int allframes = images.size(); //determine the number of frames

						System.out.println(" scaleX: "+scaleX+" scaleY: "+scaleX+" scaleZ: "+scaleX + " allframes: "+allframes);

						double rawIntens[][][][] = new double [scaleX][scaleY][scaleZ][allframes];
						double rawExptime[] = new double [allframes];
						double rawFrame[] = new double[allframes];

						//double normIntens[][][][] = new double [scaleX][scaleY][scaleZ][allframes];
						//double normExptime[] = new double [allframes];
						//double normFrame[] = new double[allframes];

						int frameCounter = 0;
						double lastDistanceToAP = -1;

						for(int frame:images.keySet())
							{
							TreeMap<Integer, EvImage> zs=images.get(frame);
							System.out.printf("\n********* frame "+frame+" ***********\n");
							try
								{
								//get exposure
								double exptime=0;
								String exptimes=ch.getFrameMeta(frame, "exposuretime");
								if(exptimes!=null)
									exptime=Double.parseDouble(exptimes); // convert to double
								else
									System.out.println("No exposure time for frame "+frame);


								//read a pixel
								//int x=0;
								//int y=0;
								//double[] pix=new double[3];
								//r.getPixel(x, y, pix);
								//double p=pix[0];
								//System.out.println("pixel 0,0,"+frame+" = "+p+"; with expt: "+(p*exptime));


								// sample: get image coordinates from real world coordinates
								//double imx=evim.transformWorldImageX(antpos.x);
								//double imy=evim.transformWorldImageY(antpos.y);


								// get real world coordinates from image
								//double realx=evim.transformImageWorldX(x);
								//double realy=evim.transformImageWorldY(y);
								//double worldz=z/ost.meta.resZ;



								//translate AP axis to 0,0,0 (ant will translated)
								// pick the closest frame to the time point of this image



								NucLineage.NucPos antpos = getIntPos(lin, "ant",  frame);								
								NucLineage.NucPos postpos = getIntPos(lin, "post", frame);
								NucLineage.NucPos shellmid1 = getIntPos(lin, "shellmid1",  frame);
								NucLineage.NucPos shellmid2 = getIntPos(lin, "shellmid2", frame);
								//NucLineage.NucPos shellup = getIntPos(lin, "shellup", frame);
								//NucLineage.NucPos shelldown = getIntPos(lin, "shelldown", frame);
								NucLineage.NucPos EMSpos = getIntPos(lin,"EMS",frame);
								NucLineage.NucPos Epos = getIntPos(lin,"E",frame);									
								NucLineage.NucPos Eppos = getIntPos(lin,"Ep",frame);
								NucLineage.NucPos vencpos = getIntPos(lin,"venc",frame);

//								System.out.println(" ant:  "+antpos.x+", "+antpos.y+", "+antpos.z);
//								System.out.println(" post: "+postpos.x+", "+postpos.y+", "+postpos.z);
//								System.out.println(" shellmid1: "+shellmid1.x+", "+shellmid1.y+", "+shellmid1.z);
//								System.out.println(" shellmid2: "+shellmid1.x+", "+shellmid1.y+", "+shellmid1.z);
//								System.out.println(" shellup: "+shellup.x+", "+shellup.y+", "+shellup.z);
//								System.out.println(" shelldown: "+shelldown.x+", "+shelldown.y+", "+shelldown.z);
//								System.out.println(" EMS:  "+EMSpos.x+", "+EMSpos.y+", "+EMSpos.z);
//								System.out.println(" E:   "+Epos.x+", "+Epos.y+", "+Epos.z);
//								System.out.println(" Ep:   "+Eppos.x+", "+Eppos.y+", "+Eppos.z);
//								System.out.println(" venc: "+vencpos.x+", "+vencpos.y+", "+vencpos.z);


								//NucLineage.NucPos antpos =getpos(lin, "ant",  frame);
								//NucLineage.NucPos postpos=getpos(lin, "post", frame);

								//double APTransX=antpos.x;
								//double APTransY=antpos.y;
								//double APTransZ=antpos.z;
								//translate the ant coordinate
								//double transAntX=0;
								//double transAntY=0;
								//double transAntZ=0;
								//translate the post coordinate
								double transPostX=postpos.x-antpos.x;
								double transPostY=postpos.y-antpos.y;
								double transPostZ=postpos.z-antpos.z;

								//determine angle AP to image panes (AP to pane XY of real world coordinate system)	
								//double angleAPXY = Math.acos(Math.abs(transPostX * transPostX + transPostY * transPostY) / (Math.abs(Math.sqrt(transPostX * transPostX + transPostY * transPostY + transPostZ * transPostZ)) * Math.abs(Math.sqrt(transPostX * transPostX + transPostY * transPostY))));
								double angleAPXY = Math.atan2(transPostZ,transPostX);
								// rotate down onto XY pane, keep angle APXY for future use!
								//rotate around Y axis
								//matrix Ry(a)
								//    |  cos(a) 0 sin(a) |
								//  = |     0    1    0  |
								//    | -sin(a) 0 cos(a) |
								double rotPostX= Math.cos(angleAPXY)*transPostX+           Math.sin(angleAPXY)*transPostZ;
								double rotPostY=                                transPostY;
								double rotPostZ=-Math.sin(angleAPXY)*transPostX+           Math.cos(angleAPXY)*transPostZ;
								//determine angle of new rotPost to projection of this vector on XZ
								//double angleAPonXYtoXZ = Math.acos(Math.abs(rotPostX * rotPostX + rotPostZ * rotPostZ) / (Math.abs(Math.sqrt(rotPostX * rotPostX + rotPostY * rotPostY + rotPostZ * rotPostZ)) * Math.abs(Math.sqrt(rotPostX * rotPostX +  rotPostZ * rotPostZ))));
								double angleAPonXYtoXZ = -Math.atan2(rotPostY,rotPostX); // checked, should be negative here!
								// rotate rotPostaround z axis onto x axis
								//Rz(a)
								//    | cos(a) -sin(a) 0 |
								//  = | sin(a)  cos(a) 0 |
								//    |    0       0    1|
								double oldRotPostX = rotPostX;
								double oldRotPostY = rotPostY;
								double oldRotPostZ = rotPostZ;

								rotPostX=Math.cos(angleAPonXYtoXZ)*oldRotPostX-Math.sin(angleAPonXYtoXZ)*oldRotPostY;
								rotPostY=Math.sin(angleAPonXYtoXZ)*oldRotPostX+Math.cos(angleAPonXYtoXZ)*oldRotPostY;
								rotPostZ=                                                                              oldRotPostZ;

//								System.out.println("angleAPXY   = "+angleAPXY+"r = "+angleAPXY/(Math.PI/2)*90+"¡");
//								System.out.println("angleAPXYXZ = "+angleAPonXYtoXZ+"r = "+angleAPonXYtoXZ/(Math.PI/2)*90+"¡");
//								System.out.println("oldrotPost= "+oldRotPostX+","+oldRotPostY+","+oldRotPostZ+" at frame "+frame);
//								System.out.println("rotPost  = "+rotPostX+","+rotPostY+","+rotPostZ+" at frame "+frame);

								//save calculations
								//these angles must be and used from nowon.
								//keep the values for the two matrices instead
								double cosAPXY = Math.cos(angleAPXY);
								double sinAPXY = Math.sin(angleAPXY);
								double cosAPonXYtoXZ = Math.cos(angleAPonXYtoXZ);
								double sinAPonXYtoXZ = Math.sin(angleAPonXYtoXZ);

								//calculate AntPost distance;
								double apdiffX = antpos.x-postpos.x;
								double apdiffY = antpos.y-postpos.y;
								double apdiffZ = antpos.z-postpos.z;
								double antpost = Math.sqrt(apdiffX*apdiffX+apdiffY*apdiffY+apdiffZ*apdiffZ);
//								System.out.println("AntPost: "+antpost);

								// mis-termed variable: should not be "radius", rather diameter
								double middiffX = shellmid1.x-shellmid2.y;
								double middiffY = shellmid1.y-shellmid2.y;
								double middiffZ = shellmid1.z-shellmid2.z;
								double eggsmallradius = Math.sqrt(middiffX*middiffX+middiffY*middiffY+middiffZ*middiffZ);
								//System.out.println("eggsmallradius: "+eggsmallradius);

								//get the up-down distance as well, compare the the mid diameter (for internal control)

								/*
								double middiffUDX = shellup.x-shellup.y;
								double middiffUDY = shellup.y-shellup.y;
								double middiffUDZ = shellup.z-shellup.z;
								double eggsmallradiusUD = Math.sqrt(middiffUDX*middiffUDX+middiffUDY*middiffUDY+middiffUDZ*middiffUDZ);
*/
								//								System.out.println("eggsmallradius mid="+eggsmallradius+" ud="+eggsmallradiusUD);
								
								
								// rotate remaining coordinates accordingly

								int markers=0; // 0 additional coordinates
								int numberOfMarkers = 6;
								String[] coordName = new String[numberOfMarkers];

								double[] coordX = new double[numberOfMarkers];
								double[] coordY = new double[numberOfMarkers];
								double[] coordZ = new double[numberOfMarkers];

								double[] coordFirstKey = new double[numberOfMarkers];
								double[] coordLastKey = new double[numberOfMarkers];

								double[] rotCoordX = new double[numberOfMarkers];
								double[] rotCoordY = new double[numberOfMarkers];
								double[] rotCoordZ = new double[numberOfMarkers];

								//add additional marker cells here, if it works, move our the ventral reference position

								coordName[markers]= "ventralRefPos";
								coordX[markers]=ventralRefPos.x;
								coordY[markers]=ventralRefPos.y;
								coordZ[markers]=ventralRefPos.z;
								coordFirstKey[markers]=0;
								coordLastKey[markers]=0;
								markers++;

								coordName[markers]= "EMS";
								coordX[markers]=EMSpos.x;
								coordY[markers]=EMSpos.y;
								coordZ[markers]=EMSpos.z;
								coordFirstKey[markers]=EMSNucFirstKey;
								coordLastKey[markers]=EMSNucLastKey;
								markers++;

								coordName[markers]= "E";
								coordX[markers]=Epos.x;
								coordY[markers]=Epos.y;
								coordZ[markers]=Epos.z;
								coordFirstKey[markers]=ENucFirstKey;
								coordLastKey[markers]=ENucLastKey;
								markers++;

								coordName[markers]= "Ep";
								coordX[markers]=Eppos.x;
								coordY[markers]=Eppos.y;
								coordZ[markers]=Eppos.z;
								coordFirstKey[markers]=EpNucFirstKey;
								coordLastKey[markers]=EpNucLastKey;
								markers++;

								coordName[markers]= "venc";
								coordX[markers]=vencpos.x;
								coordY[markers]=vencpos.y;
								coordZ[markers]=vencpos.z;
								coordFirstKey[markers]=vencNucFirstKey;
								coordLastKey[markers]=vencNucLastKey;
								markers++;

								double currentEmbRotAngle=0;
								double currentDistanceToAP=-1;
								String currentVentralMarker="";
								for (int mc=0;mc<markers;mc++)
									{

									double transCoordX=coordX[mc]-antpos.x;
									double transCoordY=coordY[mc]-antpos.y;
									double transCoordZ=coordZ[mc]-antpos.z;

									rotCoordX[mc]= cosAPXY*transCoordX+           sinAPXY*transCoordZ;
									rotCoordY[mc]=                    transCoordY;
									rotCoordZ[mc]=-sinAPXY*transCoordX+           cosAPXY*transCoordZ;
									// rotate around z axis; memorize current position
									double oldRotCoordX = rotCoordX[mc];
									double oldRotCoordY = rotCoordY[mc];
									double oldRotCoordZ = rotCoordZ[mc];
									rotCoordX[mc]=cosAPonXYtoXZ*oldRotCoordX-sinAPonXYtoXZ*oldRotCoordY;
									rotCoordY[mc]=sinAPonXYtoXZ*oldRotCoordX+cosAPonXYtoXZ*oldRotCoordY;
									rotCoordZ[mc]=                                                 oldRotCoordZ;

									System.out.println(coordName[mc]+": "+rotCoordX[mc]+", "+ ", "+rotCoordY[mc]+", "+rotCoordZ[mc]);
									if (Math.sqrt(rotCoordY[mc]*rotCoordY[mc]+rotCoordZ[mc]*rotCoordZ[mc])>eggsmallradius/2/4)
										if (mc>0)
											//if (rotCoordX[0] !=  rotCoordX[mc] && rotCoordY[0] !=  rotCoordY[mc] && rotCoordZ[0] !=  rotCoordZ[mc])
												{
												System.out.println("current "+coordName[mc] + "-first: "+coordFirstKey[mc]+", -last: "+coordLastKey[mc]);
												if (frame >= coordFirstKey[mc])
													{
													//currentEmbRotAngle=Math.acos(Math.abs(rotCoordY[0]*rotCoordY[mc]+rotCoordZ[0]*rotCoordZ[mc])/(Math.sqrt(rotCoordY[0]*rotCoordY[0]+rotCoordZ[0]*rotCoordZ[0])*Math.sqrt(rotCoordY[mc]*rotCoordY[mc]+rotCoordZ[mc]*rotCoordZ[mc])))/Math.PI*180;
													// using atan2 instead to get direction of rotation atan2(y1,z1)-atan2(y2,z2)
													//currentEmbRotAngle=Math.atan2(rotCoordY[0],rotCoordZ[0])-Math.atan2(rotCoordY[mc],rotCoordZ[mc])/Math.PI*180;
													currentEmbRotAngle=Math.atan2(rotCoordY[mc],rotCoordZ[mc])/Math.PI*180;
													currentVentralMarker=coordName[mc];
													currentDistanceToAP = Math.sqrt(rotCoordY[mc]*rotCoordY[mc]+rotCoordZ[mc]*rotCoordZ[mc]);
													}
												}
									}
								
								//collect rotation statistics
								int rotationTime=0;
								if (currentVentralMarker.contains("E"))
									rotationTime=0;
								else if (currentVentralMarker.contains("venc"))
									rotationTime=1;
								rotationAngle[rotationTime]+=currentEmbRotAngle;
								rotationNumber[rotationTime]++;
									
								
								
								System.out.println("frame "+frame+" current_ rotation angle "+coordName[0]+", "+currentVentralMarker+" : "+currentEmbRotAngle);
								angleFile.write(currentVentralMarker+"\t"+frame+"\t"+currentEmbRotAngle+"\n");
								if (currentVentralMarker != "venc")
									if (lastDistanceToAP != currentDistanceToAP && currentDistanceToAP >=0)
										{
										distanceFile.write(frame+"\t"+currentDistanceToAP+"\n");
										lastDistanceToAP = currentDistanceToAP;
										}
								

								// project the rotated embryonic rotation reference coordinates on to ZY pane by ignoring the X coordinate
								// determine the angle relative to the ventralRefPos => index 0 for rotated coordinates






								//if (0==1){ // TODO: this is to temporally remove image analysis

								//find every pixel that is close enough to AP
								// -scan the image line by line
								// -get real world coordinate for this pixel
								// -calculate distance to AP
								// -if within range
								//  	add exposure time information
								// 
								int totalpixels=0; // total pixels to export
								double totalintens=0;

								// array to hold intensity values
								double[][][] intens=new double[scaleZ][scaleY][scaleX]; // attention: Z Y X
								int[][][]    pixelsfound=new int[scaleZ][scaleY][scaleX];
								double scalefactorX = antpost / scaleX;
								//double scalefactorY = eggsmallradius / scaleY;
								//double scalefactorZ = eggsmallradius / scaleZ;

								//System.out.println("scalefactors XYZ: " + scalefactorX + ", " + scalefactorY + ", " +scalefactorZ);
								//System.out.println("antpost:  " +antpost + " eggsmallradius: "+ eggsmallradius);

								if (0==1) {
									{
									//initialize array
									for (int ax=0;ax<scaleX;ax++)
										for (int ay=0;ay<scaleY;ay++)
											for (int az=0;az<scaleZ;az++)
												{
												intens[az][ay][ax]=0;
												pixelsfound[az][ay][ax]=0;
												}
									boolean updateall = false;
									if (goodpix == null || 
											antpos.x != oldAntPosX  ||
											antpos.y != oldAntPosY  ||
											antpos.z != oldAntPosZ  ||
											postpos.x!= oldPostPosX ||
											postpos.y!= oldPostPosY ||
											postpos.z!= oldPostPosZ){
											
											oldAntPosX = antpos.x;
											oldAntPosY = antpos.y;
											oldAntPosZ = antpos.z;
											
											oldPostPosX = postpos.x;
											oldPostPosY = postpos.y;
											oldPostPosZ = postpos.z; 
											
											updateall = true;
											
									}
									boolean allocated=false;
									double rotPixX=0;
									double rotPixY=0;
									double rotPixZ=0;
									for(int z:zs.keySet())
										{
										EvImage evim=zs.get(z);	
										//get image
										BufferedImage bufi=evim.getJavaImage();
										Raster r=bufi.getData();
										bufi=evim.getJavaImage();
										r=bufi.getData();
										int w=bufi.getWidth();
										int h=bufi.getHeight();
										int s=zs.lastKey()+1; //stack Z
										//check whether the mask needs an update
										
										if(!allocated && updateall)
											{
											goodpix = new boolean [s][h][w];
											goodpixcoordX = new int [s][h][w];
											goodpixcoordY = new int [s][h][w];
											goodpixcoordZ = new int [s][h][w];
											allocated=true;
											//System.out.println("allocating mask");
											}
										
										//loop through every pixel of this image
										//transform the coordinate of this pixel to real
										if (updateall)
											{
											//System.out.println("updating mask z:"+z);
											//take all pixels according to mask
											for (int co=0;co<w;co++)
												for (int ro=0;ro<h;ro++)
													{
													
													//get real world coordinate for this pixel
													//and translate pixel
													double transPixX=evim.transformImageWorldX(co)-antpos.x;
													double transPixY=evim.transformImageWorldY(ro)-antpos.y;
													double transPixZ=z/ost.meta.resZ-antpos.z;
													//rotate around Y axis
													
													//just for testing, rotate postpos again
													//transPixX=postpos.x-antpos.x;
													//transPixY=postpos.y-antpos.y;
													//transPixZ=postpos.z-antpos.z;
													
													rotPixX= cosAPXY*transPixX+           sinAPXY*transPixZ;
													rotPixY=                    transPixY;
													rotPixZ=-sinAPXY*transPixX+           cosAPXY*transPixZ;
													// rotate around z axis; memorize current position
													double oldRotPixX = rotPixX;
													double oldRotPixY = rotPixY;
													double oldRotPixZ = rotPixZ;
													rotPixX=cosAPonXYtoXZ*oldRotPixX-sinAPonXYtoXZ*oldRotPixY;
													rotPixY=sinAPonXYtoXZ*oldRotPixX+cosAPonXYtoXZ*oldRotPixY;
													rotPixZ=                                                 oldRotPixZ;
													//check if x coordinate 0<=x<=Post
													if ((rotPixX>=0) && (rotPixX<=rotPostX))
														{
														//check if distance to AP is within a range of a fraction of the distance Ant-Post (distance in yz) 
														if (Math.sqrt(rotPixY*rotPixY+rotPixZ*rotPixZ) <= eggsmallradius)
															{
															//adjust mask to export these pixels
															goodpix[z][ro][co] = true;
//														write to proper position in intensity map
															int saveX = (int) Math.round(rotPixX / scalefactorX);
															int saveY = (int) Math.round(rotPixY / scalefactorX + scaleX/2);
															int saveZ = (int) Math.round(rotPixZ / scalefactorX + scaleX/2);
															
															//System.out.println("rotPixXYZ: "+rotPixX+ ", " +rotPixY+ ", " + rotPixZ );
															//System.out.println("saveXYZ: "+saveX+ ", " +saveY+ ", " +saveZ );
															
															if (saveX < 0)
																saveX = 0;
															if (saveY < 0)
																saveY = 0;
															if (saveZ < 0)
																saveZ = 0;
															if (saveX >= scaleX)
																saveX = scaleX-1;
															if (saveY >= scaleY)
																saveY = scaleY-1;
															if (saveZ >= scaleZ)
																saveZ = scaleZ-1;
															
															goodpixcoordX[z][ro][co] = saveX;
															goodpixcoordY[z][ro][co] = saveY;
															goodpixcoordZ[z][ro][co] = saveZ;
															}
														}
													}
											}	
										//System.out.println("checking pixels in layer "+z);
										for (int co=0;co<w;co++)
											for (int ro=0;ro<h;ro++)
												{
												if (goodpix[z][ro][co])
													{
													totalpixels++;
													double[] pix=new double[3];
													r.getPixel(co, ro, pix);
													double p = pix[0];
													
													intens[goodpixcoordZ[z][ro][co]][goodpixcoordY[z][ro][co]][goodpixcoordX[z][ro][co]] += p;
													pixelsfound[goodpixcoordZ[z][ro][co]][goodpixcoordY[z][ro][co]][goodpixcoordX[z][ro][co]]++;
													totalintens+=p;
													}
												}
										}
									System.out.println(totalpixels+ " pixels found in frame "+ frame);
									System.out.println("frame: " +frame + " average: " + (totalintens / totalpixels) * exptime);
									outFile.write("frame\n");
									outFile.write(""+frame+"\n");
									outFile.write("exptime\n");
									outFile.write(""+exptime+"\n");
									for (int zi=0;zi<scaleZ;zi++)
										{
										for (int yi=0;yi<scaleY;yi++)
											{
											for (int xi=0;xi<scaleX;xi++)
												{
												
												String localintensity;
												if (pixelsfound[zi][yi][xi] > 0)
													{
													localintensity = Double.toString(intens[zi][yi][xi]/ (double) pixelsfound[zi][yi][xi]);
													rawIntens[xi][yi][zi][frameCounter] = intens[zi][yi][xi]/ (double) pixelsfound[zi][yi][xi];  
													}
												else
													{
													localintensity = "0";
													rawIntens[xi][yi][zi][frameCounter] = 0;
													}
												
												outFile.write(""+localintensity+"\t");
												rawExptime[frameCounter] = exptime;
												rawFrame[frameCounter] = frame;
												
												}
											outFile.write("\n");
											}
										}
									outFile.write("*\n");
									outFile.flush(); //force write

									}								
								}}
							catch (RuntimeException e)
								{
								// catches defective frame errors
								e.printStackTrace();
								System.out.println("Java_error: "+e);
								omitted.write(currentpath + "\terror "+e+"\n");
								}
							//} //TODO: remove to activate image analysis 
							}
						outFile.flush();
						outFile.close();
						angleFile.close();
						distanceFile.close();
						System.out.println(" timeX "+(System.currentTimeMillis()-currentTime));
						System.out.println("AP done");
						frameCounter++;
						
						gnufile.write("set output \'"+ost.datadir()+"/"+currentostname+"-angles.png\'\n");
						gnufile.write("set title \'"+currentostname+" rotation angle (degrees)\'\n");
						gnufile.write("set xrange [*:*]\n");
						gnufile.write("set yrange [-180:180]\n");
						gnufile.write("plot \'"+ost.datadir()+"/rotationAngleTableNotMod.txt\' using 2:3 with lines\n");
						gnufile.write("set output \'"+ost.datadir()+"/"+currentostname+"-APdist.png\'\n");
						gnufile.write("set title \'"+currentostname+" distance to center (micrometer)\'\n");
						gnufile.write("set xrange [*:*]\n");
						gnufile.write("set yrange [*:*]\n");
						gnufile.write("plot \'"+ost.datadir()+"/distanceToAP.txt\' using 1:2 with lines\n");
						htmlfile.write("<img src=\""+ost.datadir()+"/"+currentostname+"-angles.png\" width=\"320\" heigth=\"240\">\n");
						htmlfile.write("<img src=\""+ost.datadir()+"/"+currentostname+"-APdist.png\" width=\"320\" heigth=\"240\">\n");
						// collect rotation angle average at the end of gastrulation and correlate with ventral enclosure
						double averageRotationAngle = rotationAngle[0]/rotationNumber[0]-rotationAngle[1]/rotationNumber[1];
						rotfile.write(currentpath+"\t"+recordingNumber+"\t"+averageRotationAngle+"\n");
						//recordingNumber++;
						}
					}
				}
			}
		else
			System.out.println("skipping");
			omitted.write(currentpath + "\tskipped\n");
		}
	catch (Exception e)
		{
		// TODO Auto-generated catch block
		e.printStackTrace();
		try
			{
			omitted.write(PathForErrorFile + "\terror2 "+e+"\n");
			}
		catch (IOException e1)
			{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			}
		}
	}

public static NucLineage.NucPos getpos(NucLineage lin, String name, int frame)
	{
	//Get position
	NucLineage.Nuc nuc=lin.nuc.get(name);
	NucLineage.NucPos pos;
	Map<Integer,NucLineage.NucPos> hm=nuc.pos.headMap(frame);
	if(hm.isEmpty())
		pos=nuc.pos.get(nuc.pos.firstKey());
	else
		pos=hm.get(nuc.pos.lastKey());
	return pos;
	}


public static NucLineage.NucPos getIntPos(NucLineage lin, String name, double frame)
	{
	NucLineage.NucInterp i=lin.nuc.get(name).interpolate(frame);
	if(i!=null)
		return i.pos;
	else
		{
		NucLineage.Nuc nuc=lin.nuc.get(name);
		double first=nuc.pos.firstKey();
//		double last=nuc.pos.lastKey();
		if(frame<=first)
			return nuc.pos.get(nuc.pos.firstKey());
		else
			return nuc.pos.get(nuc.pos.lastKey());
		}
	}


/**
 * Entry point
 * @param arg Command line arguments
 */
public static void main(String[] arg)
	{
	Log.listeners.add(new StdoutLog());
	EV.loadPlugins();

	if(arg.length==0)
		arg=new String[]{
				/*					"/Volumes/TBU_xeon01_500GB01/ost3dfailed/",
						"/Volumes/TBU_xeon01_500GB01/ost3dgood/",*/
				"/Volumes/TBU_xeon01_500GB01/ost4dgood/",
				"/Volumes/TBU_xeon01_500GB02/ost4dgood/"
				//"/Volumes/TBU_xeon01_500GB02/daemon/output/"

	};
	
	
	try
		{
		BufferedWriter gnuplotFile;
		File gnuplotFilePath=new File("gnuplotrotationangle.txt");
		gnuplotFile = new BufferedWriter(new FileWriter(gnuplotFilePath));
		gnuplotFile.write("set terminal png\n");
		
		BufferedWriter htmlGnuplotFile;
		File htmlGnuplotFilePath=new File("htmlGnuplotrotationangle.htm");
		htmlGnuplotFile = new BufferedWriter(new FileWriter(htmlGnuplotFilePath));
		htmlGnuplotFile.write("<html><body>\n");
		
		BufferedWriter rotationStatisticsFile;
		File rotationStatisticsFilePath=new File("rotationStatistics.txt");
		rotationStatisticsFile = new BufferedWriter(new FileWriter(rotationStatisticsFilePath));
		
		BufferedWriter rotationStatisticsTranslationFile;
		File rotationStatisticsTranslationFilePath=new File("rotationStatisticsTranslation.txt");
		rotationStatisticsTranslationFile = new BufferedWriter(new FileWriter(rotationStatisticsTranslationFilePath));	
		
		BufferedWriter omittedFile;
		File omittedFilePath=new File("omittedRecordings.txt");
		omittedFile = new BufferedWriter(new FileWriter(omittedFilePath));	
		
		int evaluatedRecording = 0;
		
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					{
					long currentTime=System.currentTimeMillis();
					calcAP(file, gnuplotFile, htmlGnuplotFile, rotationStatisticsFile, omittedFile, evaluatedRecording);
					rotationStatisticsTranslationFile.write(evaluatedRecording+"\t"+file+"\n");
					evaluatedRecording++;
					System.out.println(" timeY "+(System.currentTimeMillis()-currentTime));
					}
		gnuplotFile.close();
		htmlGnuplotFile.write("\n</body></html>");
		htmlGnuplotFile.close();
		rotationStatisticsFile.close();
		omittedFile.close();
		}
	catch (IOException e)
		{
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}

}
