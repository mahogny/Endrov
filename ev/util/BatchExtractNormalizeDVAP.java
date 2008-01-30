package util;

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

public static void calcAP(File file)
	{
	try
		{
		System.out.println("current Imageset "+file.getPath());
		String currentpath = file.getPath();
		System.out.println("current imageset: "+currentpath);
		//if (0==0) // all imagesets
		if (currentpath.equals("/Volumes/TBU_xeon01_500GB01/ost4dgood/BC12759070823")) // only deal with this test image set
			{	
			System.out.println("imageset found, executing...");

			long currentTime=System.currentTimeMillis();
			OstImageset ost=new OstImageset(file.getPath());

			String currentostname = ost.toString();
			System.out.println("current imageset: " + currentostname);
			//String currentostDescription = ost.description.toString();
			double timeStep = 1;
			timeStep = ost.meta.metaTimestep;
			System.out.println("time step: " + timeStep + " seconds");

			for(EvObject evob:ost.metaObject.values()) // go through all objects in XML file
				{
				//System.out.println("timestep = "+ ost.metaObject.metaTimestep);
				
				if(evob instanceof NucLineage) // if the current object is a Lineage
					{
					NucLineage lin=(NucLineage)evob;
					//test whether the metadata contains enough information
					if(lin.nuc.get("post")!=null && lin.nuc.get("ant")!=null && 
					   lin.nuc.get("shellmid1")!=null && lin.nuc.get("shellmid2")!=null &&
					   lin.nuc.get("E")!=null && lin.nuc.get("Ep")!=null &&
					   lin.nuc.get("venc")!=null)

						{
						//get reference frames for time adjustment
						NucLineage.Nuc ENuc = lin.nuc.get("E");
						NucLineage.Nuc EpNuc = lin.nuc.get("Ep");
						NucLineage.Nuc vencNuc = lin.nuc.get("venc");
						int ENucFirstKey = ENuc.pos.firstKey(); 
						int EpNucFirstKey = EpNuc.pos.firstKey();
						int vencNucFirstKey = vencNuc.pos.firstKey();
						int ENucLastKey = ENuc.pos.lastKey();
						int EpNucLastKey = EpNuc.pos.lastKey();
						int vencNucLastKey = vencNuc.pos.lastKey();
						System.out.println("E first key " + ENucFirstKey + ", last key " + ENucLastKey);
						System.out.println("Ep first key " + EpNucFirstKey + ", last key " + EpNucLastKey);
						System.out.println("venc first key " + vencNucFirstKey + ", last key " + vencNucLastKey);
						
						Imageset.ChannelImages ch=ost.channelImages.get("GFP");
						TreeMap<Integer, TreeMap<Integer, EvImage>> images=ch.imageLoader;
						
						//create an output file in the data directory
					
						BufferedWriter outFile;

						File outFilePath=new File(ost.datadir(),"DVtimeRotatedGFP.txt");
						outFile = new BufferedWriter( new FileWriter(outFilePath) );

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
						double rawIntens[][][][] = new double [scaleX][scaleY][scaleZ][allframes];
						double rawExptime[] = new double [allframes];
						double rawFrame[] = new double[allframes];
						double normIntens[][][][] = new double [scaleX][scaleY][scaleZ][allframes];
						double normExptime[] = new double [allframes];
						double normFrame[] = new double[allframes];
						
						int frameCounter = 0;
						
						for(int frame:images.keySet())
							{
							TreeMap<Integer, EvImage> zs=images.get(frame);

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
								NucLineage.NucPos antpos =getpos(lin, "ant",  frame);
								NucLineage.NucPos postpos=getpos(lin, "post", frame);

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
								// rotate rotPostaround z axis
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

//						System.out.println("angleAPXY   = "+angleAPXY+"r = "+angleAPXY/(Math.PI/2)*90+"¡");
//						System.out.println("angleAPXYXZ = "+angleAPonXYtoXZ+"r = "+angleAPonXYtoXZ/(Math.PI/2)*90+"¡");
//						System.out.println("oldrotPost= "+oldRotPostX+","+oldRotPostY+","+oldRotPostZ+" at frame "+frame);
//						System.out.println("rotPost  = "+rotPostX+","+rotPostY+","+rotPostZ+" at frame "+frame);

								//calculate AntPost distance;
								double apdiffX = antpos.x-postpos.x;
								double apdiffY = antpos.y-postpos.y;
								double apdiffZ = antpos.z-postpos.z;
								double antpost = Math.sqrt(apdiffX*apdiffX+apdiffY*apdiffY+apdiffZ*apdiffZ);
								//System.out.println("AntPost: "+antpost);

								NucLineage.NucPos shellmid1=getpos(lin, "shellmid1",  frame);
								NucLineage.NucPos shellmid2=getpos(lin, "shellmid2", frame);
								double middiffX = shellmid1.x-shellmid2.y;
								double middiffY = shellmid1.y-shellmid2.y;
								double middiffZ = shellmid1.z-shellmid2.z;
								double eggsmallradius = Math.sqrt(middiffX*middiffX+middiffY*middiffY+middiffZ*middiffZ);
								//System.out.println("eggsmallradius: "+eggsmallradius);

								//these angles must be and used from nowon.
								//keep the values for the two matrices instead
								double cosAPXY = Math.cos(angleAPXY);
								double sinAPXY = Math.sin(angleAPXY);
								double cosAPonXYtoXZ = Math.cos(angleAPonXYtoXZ);
								double sinAPonXYtoXZ = Math.sin(angleAPonXYtoXZ);

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
//												write to proper position in intensity map
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


												//if within range
												//retrieve and keep value

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


									//System.out.println(p+ " pixels found in layer "+ z);



									//read a pixel
									//int x=0;
									//int y=0;
									//double[] pix=new double[3];
									//r.getPixel(x, y, pix);
									//double p=pix[0];
									//System.out.println("pixel 0,0,"+frame+" = "+p+"; with expt: "+(p*exptime));




									//Rx(a)
									//    | 1    0       0   |
									//  = | 0 cos(a) -sin(a) |
									//    | 0 sin(a)  cos(a) |

									//Ry(a)
									//    |  cos(a) 0 sin(a) |
									//  = |     0    1    0  |
									//    | -sin(a) 0 cos(a) |
									// matrix3d has this already built in, but doing it anyway for practice
//							double rotMatrixY[][]=new double[3][3];
//							rotMatrixY[0][0]=Math.cos(APXY) ; rotMatrixY[0][1]=0; rotMatrixY[0][2]=Math.sin(APXY);
//							rotMatrixY[1][0]=0              ; rotMatrixY[1][1]=1; rotMatrixY[1][2]=0             ;
//							rotMatrixY[2][0]=-Math.sin(APXY); rotMatrixY[2][1]=0; rotMatrixY[2][2]=Math.cos(APXY);


									//Rz(a)
									//    | cos(a) -sin(a) 0 |
									//  = | sin(a)  cos(a) 0 |
									//    |    0       0    1|
									//double rotMatrixZ[][]=new double[3][3];
//							rotMatrixZ[0][0]=Math.cos(APXZ) ; rotMatrixZ[0][1]=-Math.sin(APXZ); rotMatrixZ[0][2]=0;
//							rotMatrixZ[1][0]=Math.sin(APXZ) ; rotMatrixZ[1][1]=Math.cos(APXZ) ; rotMatrixZ[1][2]=0;
//							rotMatrixZ[2][0]=0              ; rotMatrixZ[2][1]=0              ; rotMatrixZ[2][2]=1;

//							double rotPostX = 0;
//							double rotPostY = 0;
//							double rotPostZ = 0;
//							for(int my=0;my<3;my++){
//							for(int mx=0;mx<3;mx++){

//							}
//							}		



									// multiply by rotate around Z axis
									//Rz(a)
									//    | cos(a) -sin(a) 0 |
									//  = | sin(a)  cos(a) 0 |
									//    |    0       0    1|
//							rotPostX*=Math.cos(APXZ)*transPostX-Math.sin(APXZ)*transPostX;
//							rotPostY*=Math.sin(APXZ)*transPostY+Math.cos(APXZ)*transPostY;
//							rotPostZ*=                                                     transPostZ;
//							System.out.println("oldrotPost="+rotPostX+","+rotPostY+","+rotPostZ+" at frame "+frame);

									//double [] translateFunction = new double[3];
									//translateFunction = translateToX(transPostX,transPostY,transPostZ,APXY); //(double realX, double realY, double realZ, double angleToXY, double angleToXZ)
									//System.out.println("newrotPost="+translateFunction[0]+","+translateFunction[1]+","+translateFunction[2]+" at frame "+frame);
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
							catch (RuntimeException e)
								{
								// catches defective frame errors
								e.printStackTrace();
								}
							}
							outFile.flush();
							outFile.close();
							
						System.out.println(" timeX "+(System.currentTimeMillis()-currentTime));
						System.out.println("AP done");
						frameCounter++;
						}
					}
				}
			}
		else
			System.out.println("skipping");
		}
	catch (Exception e)
		{
		// TODO Auto-generated catch block
		e.printStackTrace();
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


//public static double[] translateToX(double realX, double realY, double realZ, double angle){
////translate and rotate a real world coordinate onto the real world X axis
//double[] trToX = new double[3];
//trToX[0] = 0;
//trToX[1] = 0;
//trToX[2] = 0;

////rotate around Y axis
////Ry(a)
////|  cos(a) 0 sin(a) |
////= |     0    1    0  |
////| -sin(a) 0 cos(a) |

//trToX[0]= Math.cos(angleToXY)*realX+           Math.sin(angleToXY)*realX;
//trToX[1]=                           realY;
//trToX[2]=-Math.sin(angleToXY)*realZ+           Math.cos(angleToXY)*realZ;

////multiply by rotate around Z axis
////Rz(a)
////| cos(a) -sin(a) 0 |
////= | sin(a)  cos(a) 0 |
////|    0       0    1|
//trToX[0]*=Math.cos(angleToXZ)*realX-Math.sin(angleToXZ)*realX;
//trToX[1]*=Math.sin(angleToXZ)*realY+Math.cos(angleToXZ)*realY;
//trToX[2]*=                                                     realZ;
////System.out.println("trTo="+trToX[0]+","+trToX[1]+","+trToX[2]);
//return trToX;
//}


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

	};
	for(String s:arg)
		for(File file:(new File(s)).listFiles())
			if(file.isDirectory())
				{
				long currentTime=System.currentTimeMillis();
				calcAP(file);
				System.out.println(" timeY "+(System.currentTimeMillis()-currentTime));
				}
	}
}
