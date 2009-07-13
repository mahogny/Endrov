package endrov.nucAutoJH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.vecmath.Vector3d;

import endrov.basicWindow.EvComboObjectOne;
import endrov.flowBasic.math.EvOpImageSubImage;
import endrov.flowFindFeature.EvOpFindLocalMaximas3D;
import endrov.flowFourier.EvOpCircConv2D;
import endrov.flowGenerateImage.GenerateSpecialImage;
import endrov.flowMultiscale.Multiscale;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.nuc.NucLineage;
import endrov.nucImage.LineagingAlgorithm;
import endrov.nucImage.LineagingAlgorithm.LineageAlgorithmDef;
import endrov.shell.Shell;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
import endrov.util.ImVector3d;
import endrov.util.Tuple;
import endrov.util.Vector3i;

/**
 * Autolineage algorithm
 * 
 * Meant to be used with his::rfp or equivalent marker
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class AutolineageJH1 extends LineageAlgorithmDef
	{
	public static void initPlugin() {}
	static
		{
		LineageAlgorithmDef.listAlgorithms.add(new AutolineageJH1());
		}
	
	@Override
	public LineagingAlgorithm getInstance()
		{
		return new Algo();
		}

	@Override
	public String getName()
		{
		return "JH1";
		}

	
	
	private static class Algo implements LineagingAlgorithm
		{
		private EvComboObjectOne<EvChannel> comboChanHis=new EvComboObjectOne<EvChannel>(new EvChannel(), true, false);
//		private EvComboObjectOne<EvChannel> comboChanDIC=new EvComboObjectOne<EvChannel>(new EvChannel(), true, false);
		private EvComboObjectOne<Shell> comboShell=new EvComboObjectOne<Shell>(new Shell(), true, false);
		private JTextField inpRadius=new JTextField("40");
		/*
		private JTextField inpNucBgSize=new JTextField("2");
		private JTextField inpNucBgMul=new JTextField("1");
		*/
		
		public JComponent getComponent()
			{
			JComponent p=EvSwingUtil.layoutTableCompactWide(
					new JLabel("His-channel"),comboChanHis,
//					new JLabel("DIC-channel"),comboChanDIC,
					new JLabel("Shell"),comboShell,
					new JLabel("E[r]"),inpRadius
					/*
					,
					
					new JLabel("Nuc bg size"),inpNucBgSize,
					new JLabel("Nuc bg mul"),inpNucBgMul
					*/
					);

			return p;
			}
		
		
		/**
		 * One candidate nuclei position
		 * @author Johan Henriksson
		 *
		 */
		private static class Candidate
			{
			int id;
			Vector3d pos;
			double bestSigma;
			double intensity;
			
			//double r;
			
			public String toString()
				{
				return "id="+id+"  bestSigma="+bestSigma+"  intensity="+intensity;
				}
			
			}

		public void run(LineageSession session)
			{
			EvDecimal frame=session.getStartFrame();
			NucLineage lin=session.getLineage();
			EvChannel channelHis=comboChanHis.getSelectedObject();
			//EvChannel channelDIC=comboChanDIC.getSelectedObject();
			Shell shell=comboShell.getSelectedObject();
			if(channelHis!=null && shell!=null && lin !=null)
				{
				//Start from this frame (if it exists) or the first frame after
				if(channelHis.getFrame(frame)==null)
					frame=channelHis.closestFrameAfter(frame);

				//Check if there is a keyframe before this one
				EvDecimal frameBefore=channelHis.closestFrameBefore(frame);
				if(frameBefore.equals(frame))
					frameBefore=null;
				
				System.out.println("cur frame "+frame);
				
				EvStack stackHis=channelHis.getFrame(frame);
				//EvStack stackDIC=channelDIC.getFrame(channelDIC.closestFrame(frame));
				
				
				double expectRadius=Double.parseDouble(inpRadius.getText());
//				double bgSize=Double.parseDouble(inpNucBgSize.getText());
				//double bgMulValue=Double.parseDouble(inpNucBgMul.getText());
				
				double resXhis=stackHis.getResbinX();
//				double resYhis=stackHis.getResbinY();
				double resZhis=1/stackHis.getResbinZinverted().doubleValue();
				/*
				
				double resXDIC=stackDIC.getResbinX();
				double resYDIC=stackDIC.getResbinY();
				double resZDIC=stackDIC.getResbinZinverted().doubleValue();
*/
				
				double resFrac=resXhis/resZhis;  //>1 means X has more pixels
				
				
				
				///// Find candidate coordinates ////
				double sigmaHis1=expectRadius;
				
				
				int whis=stackHis.getWidth();
				int hhis=stackHis.getHeight();
				int dhis=stackHis.getDepth();
				EvPixels kernel1=GenerateSpecialImage.genGaussian2D(sigmaHis1, sigmaHis1, whis, hhis);
				EvPixels kernel2=GenerateSpecialImage.genGaussian2D(sigmaHis1*2, sigmaHis1*2, whis, hhis);

				EvPixels kernelDOG=EvOpImageSubImage.minus(kernel1, kernel2);
				EvStack stackHisDog=new EvOpCircConv2D(kernelDOG).exec1(stackHis);
				List<Vector3i> maximas=EvOpFindLocalMaximas3D.findMaximas(stackHisDog);
				
				
				//TODO run with several kernel sizes. merge candidates.
				//Principle that highest intensity is the feature
				
//				System.out.println("# linjh maximas: "+maximas.size());

				//TODO 3d convolution
				//can make it faster
				
				
				/**
				 * Lindebergs theory of finding kernel size
				 * http://www.wisdom.weizmann.ac.il/~deniss/vision_spring04/files/mean_shift/mean_shift.ppt
				 * gaussian pyramids?
				 * can use for radius selection? O(n) for each level, need not test many. can calc approximate L-function from points
				 * 
				 * Lindeberg: ``Scale-space'', In: Encyclopedia of Computer Science and Engineering (Benjamin Wah, ed), John Wiley and Sons, Volume~IV, pages 2495--2504, Hoboken, New Jersey, Jan 2009. dx.doi.org/10.1002/9780470050118.ecse609 (Sep 2008) (PDF 1.2 Mb) 
				 * ftp://ftp.nada.kth.se/CVAP/reports/Lin08-EncCompSci.pdf
				 * 
				 * Lindeberg
				 * http://www.nada.kth.se/~tony/earlyvision.html
				 * 
				 */
				
				/**
				 * http://en.wikipedia.org/wiki/Scale-invariant_feature_transform
				 * also has equation for angle
				 * 
				 * www.ecs.syr.edu/faculty/lewalle/wavelets/mexhat.pdf 
				 * doubling sigma is correct to approximate mexican hat
				 * 
				 * 
				 */
				//TODO
				/**
				 * 
				 * 
				 * remove using mask.
				 * sort by decreasing intensity.
				 * take candidates
				 *   ignore candidates which are too close to accepted candidates (cell radius)
				 *   
				 */
				
				/**
				 * detecting the right sigma:
				 * only do it with the middle plane so find it first.
				 * then convolve a single point with ricker wavelet for multiple sigma
				 * 
				 * [2.^(1:5) , 0.75 * 2.^(1:5)]
				 *  1.5000    2.0000    3.0000    4.0000    6.0000    8.0000   12.0000   16.0000 24.0000   32.0000
				 *
				 * ie two sequences of convolving with gaussian, then do differences afterwards.
				 * find argmax and we know sigma, hence r
				 * 
				 * r=C*sigma*[some resolution]
				 * C is related to PSF and should be given by user
				 * 
				 * 
				 *
				 * 
				 * 
				 * 
				 * 
				 */
				
				
				
				
				//// Detect where the embryo is ////
				/*
				int dicVarSize=40;
				EvStack stackDICvar=new EvOpMovingVariance(dicVarSize,dicVarSize).exec1(stackDIC);
				EvStack stackDICt=new EvOpThresholdPercentile2D(0.8).exec1(stackDICvar);
				stackDICt=new EvOpBinMorphFillHoles2D().exec1(stackDICt);
				EvStack stackEmbryoMask=stackDICt;
				*/
				
				
				//Remove old coordinates at this keyframe
				deleteKeyFrame(lin, frame, true);
				
				//// Choose candidates ////
				/*
				int[][] pixEmbryoMask=stackEmbryoMask.getArraysInt();
				int wdic=stackEmbryoMask.getWidth();
				int hdic=stackEmbryoMask.getHeight();
				int ddic=stackEmbryoMask.getDepth();*/
				int id=0;
				List<Candidate> candlist=new LinkedList<Candidate>();
				for(Vector3i v:maximas)
					{
					Vector3d wpos=stackHis.transformImageWorld(new Vector3d(v.x,v.y,v.z));
//					Vector3d dicPos=stackDIC.transformWorldImage(wpos);
					
					if(shell.isInside(new ImVector3d(wpos.x,wpos.y,wpos.z)))
						{
						System.out.println("id=== "+id);
						double bestSigma=Multiscale.findFeatureScale(stackHis.getInt(v.z).getPixels(),sigmaHis1, v.x, v.y);
						System.out.println("Best fit sigma: "+bestSigma);
						System.out.println("res "+resXhis);

						//Use meanshift to get a better estimate of the XY-position
						System.out.println("Old wpos "+wpos);
						/*
						//Square kernel
						int mr=(int)(bestSigma*1.5);
						MeanShift2D.MeanShiftPreProcess meanshift=
						new MeanShift2D.MeanShiftPreProcess(stackHis.getInt(v.z).getPixels(), mr, mr);
						Vector2d mpos=meanshift.iterate(new Vector2d(v.x,v.y));
						*/
						/*
						//Gauss kernel
						double mul=1.2;
						MeanShiftGauss2D.MeanShiftPreProcess meanshiftXY=
						new MeanShiftGauss2D.MeanShiftPreProcess(stackHis.getInt(v.z).getPixels());
						Vector2d mpos=meanshiftXY.iterate(new Vector2d(v.x,v.y),bestSigma*mul,bestSigma*mul);
						wpos=stackHis.transformImageWorld(new Vector3d(mpos.x,mpos.y,v.z));
						System.out.println("new wpos "+wpos);
						*/
						
						/*
						
						//Do mean-shift in Z-direction
						double sigmaHis1z=bestSigma/resFrac;  //Arbitrary factor for psfZ
						double[] arr=new double[dhis];
						for(int i=0;i<dhis;i++)
							arr[i]=Multiscale.convolveGaussPoint2D(stackHis.getInt(i).getPixels(), 
									bestSigma, bestSigma, mpos.x, mpos.y);
						MeanShiftGauss1D.MeanShiftPreProcess meanshiftZ=new MeanShiftGauss1D.MeanShiftPreProcess(arr);
						double nz=meanshiftZ.iterate(v.z, sigmaHis1z);
						v.z=(int)Math.round(nz);
						wpos=stackHis.transformImageWorld(new Vector3d(mpos.x,mpos.y,nz));
						System.out.println("new wpos "+wpos);
						
						bestSigma=Multiscale.findFeatureScale(stackHis.getInt(v.z).getPixels(),bestSigma, v.x, v.y);
						System.out.println("better fit sigma: "+bestSigma);

						*/
						
						
						
						Candidate cand=new Candidate();
						cand.id=id++;
						cand.pos=wpos;
						cand.bestSigma=bestSigma;
						cand.intensity=Multiscale.convolveGaussPoint2D(stackHis.getInt(v.z).getPixels(), 
								bestSigma, bestSigma, cand.pos.x, cand.pos.y);
						candlist.add(cand);
/*
						=======
						NucLineage.Nuc nuc=lin.getCreateNuc(""+i);
						NucLineage.NucPos pos=nuc.getCreatePos(frame);
						pos.r=3;
						pos.setPosCopy(wpos);
>>>>>>> 1.10
*/
						}
					
					

					
					//System.out.println("r should be "+stackHis.scaleImageWorldX(20)); //5
					
					/*
					System.out.println("in his: "+v);
					System.out.println("in dic: "+dicPos);
					System.out.println("in world: "+wpos);
					*/
					
					
/*
					Vector3i dicPosi=new Vector3i((int)dicPos.x,(int)dicPos.y,(int)dicPos.z); 
					if(dicPosi.x>=0 && dicPosi.x<wdic-1 && dicPosi.y>=0 && dicPosi.z<hdic && dicPosi.z>=0 && dicPosi.z<ddic)
						{
						int val=pixEmbryoMask[dicPosi.z][dicPosi.y*wdic+dicPosi.x];
						//System.out.println("dic mask "+val);

						if(val>0)
							{
							}
						
						}
					//else
						//System.out.println("outside DIC");
	
					*/
					}
			
				
				double sigma2radiusFactor=0.55;
				List<Candidate> okCandidates=new LinkedList<Candidate>();

				
				/**
				 * Filtering: take the 2 largest nuclei. average size.
				 * if any is smaller than half of this, then it is likely noise
				 */
				Collections.sort(candlist, new Comparator<Candidate>(){
					public int compare(Candidate arg0, Candidate arg1)
						{
						return -Double.compare(arg0.bestSigma,arg1.bestSigma);
						}
				});
				//double averageNormalRadius=(candlist.get(0).bestSigma+candlist.get(1).bestSigma)/2;
				double normalSigma=candlist.get(1).bestSigma;
				System.out.println("representative normal sigma "+normalSigma);
				for(Candidate cand:candlist)
					{
					if(cand.bestSigma>normalSigma/2)
						okCandidates.add(cand);
					else
						System.out.println("Removed size: "+cand);
					}
				candlist.clear();
				candlist.addAll(okCandidates);
				okCandidates.clear();
				
				
				/**
				 * Filtering: Whenever there is overlap, take the one with the strongest intensity
				 */
				Collections.sort(candlist, new Comparator<Candidate>(){
				public int compare(Candidate arg0, Candidate arg1)
					{
					return -Double.compare(arg0.intensity,arg1.intensity);
					}
				});
				for(Candidate cand:candlist)
					{
					boolean overlap=false;
					for(Candidate other:okCandidates)
						{
						Vector3d v=new Vector3d(cand.pos);
						v.sub(other.pos);
						double r1=sigma2radiusFactor*cand.bestSigma;
						double r2=sigma2radiusFactor*other.bestSigma;
						if(v.length()<r1+r2)
							overlap=true;
						}
					if(!overlap)
						okCandidates.add(cand);
					else
						System.out.println("Deleted overlap: "+cand);
					}
				candlist.clear();
				candlist.addAll(okCandidates);
				okCandidates.clear();

				
				
	
				
				

				//Which are the nuclei to join with from before?
				List<NucBefore> joiningNucBefore=new ArrayList<NucBefore>();
				Collection<String> nucsBefore=collectNucleiToContinue(lin, frame);
				for(String name:nucsBefore)
					joiningNucBefore.add(new NucBefore(lin,name,frame));

				/**
				 * Join coordinates
				 */
				if(joiningNucBefore.isEmpty())
					{
					//There are no nucs before. Accept all candidates.
					System.out.println("No nuclei since before");
					
					for(Candidate cand:candlist)
						nucleusFromCandidate(lin, frame, sigma2radiusFactor, cand);

					}
				else
					{
					//There are nucs since before. Need to join

					//Collect all pairs of distances, sort the list to have the smallest distances first
					ArrayList<DistancePair> distpairs=new ArrayList<DistancePair>();
					for(NucBefore nameBefore:joiningNucBefore)
						for(Candidate candAfter:candlist)
							distpairs.add(new DistancePair(lin, nameBefore, candAfter, frame));
					Collections.sort(distpairs);
					
					//Join nuclei 1 before-1 after.
					//TODO use algo for minimal weighted bipartite matching
					//Since not the same elements before/after, not quite matching. By
					//adding virtual elements of infinite distance, the bad ones are sorted out
					//MunkresKuhn munkres=new MunkresKuhn();
					Set<NucBefore> usedBefore=new HashSet<NucBefore>();
					Set<Candidate> usedAfter=new HashSet<Candidate>();
					for(DistancePair p:distpairs)
						if(!usedBefore.contains(p.nucBefore))
							if(!usedAfter.contains(p.candAfter))
								{
								usedAfter.add(p.candAfter);
								usedBefore.add(p.nucBefore);
								p.nucBefore.children.add(p.candAfter);
								}
					
					//Greedily join nuclei 1 before-max 2 after
					usedBefore.clear();
					for(DistancePair p:distpairs)
						if(!usedBefore.contains(p.nucBefore))
							if(!usedAfter.contains(p.candAfter))
								{
								usedAfter.add(p.candAfter);
								usedBefore.add(p.nucBefore);
								p.nucBefore.children.add(p.candAfter);
								}
					
					//If there are candidates left over now, it means there are more than twice as many candidates
					//as nuclei in the frame before. The rest of the candidates are assumed bad and discarded.
					

					
					
					
					//Which nuclei are left over?
					/*
					Set<String> unusedAfter=new HashSet<String>(createdNuc);
					Set<String> unusedBefore=new HashSet<String>(nucsBefore);
					unusedAfter.removeAll(usedAfter);
					unusedBefore.removeAll(usedBefore);
					*/
					/**
					 * Unused nucs before might just not have been detected. In this case we
					 * should try and find them optimistically
					 */
					
					/**
					 * Unused nucs after are either false positive or divided nuclei.
					 * * can detect division using axis, PCA
					 * * can use division likely timing
					 * * Can use a stricter metric on radius
					 * 
					 */
					//
					
					
					
					/**
					 * Turn into nuclei
					 */
					for(NucBefore nb:joiningNucBefore)
						{
						String parentName=nb.name;
						NucLineage.Nuc parentNuc=lin.nuc.get(parentName);
						for(Candidate cand:nb.children)
							{
							Tuple<String,NucLineage.Nuc> child=nucleusFromCandidate(lin, frame, sigma2radiusFactor, cand);
							parentNuc.child.add(child.fst());
							child.snd().parent=parentName;
							}
						}
					
					
					
					}
				
				
				
				
				
				
				
				
				
				
				//TODO command to flatten lineage i.e. join single-child nuclei with parents
				
				session.nowAtFrame(channelHis.closestFrameAfter(frame));
				}
			}
		
		private static class NucBefore
			{
			String name;
			NucLineage.NucPos pos;
			Set<Candidate> children=new HashSet<Candidate>();
			
			
			public NucBefore(NucLineage lin, String name, EvDecimal frame)
				{
				this.name = name;
				pos=lin.nuc.get(name).pos.get(frame);
				NucLineage.Nuc nuc=lin.nuc.get(name);
				pos=nuc.pos.get(nuc.pos.headMap(frame).lastKey());
				if(pos==null)
					{
					System.out.println("no pos");
					System.out.println(nuc.pos);
					}
				}
			
			
			
			}
		
		private class DistancePair implements Comparable<DistancePair>
			{
			final NucBefore nucBefore;
			final Candidate candAfter;
			final double dist;
			public DistancePair(NucLineage lin, NucBefore nb, Candidate candAfter, EvDecimal frame)
				{
				this.candAfter=candAfter;
				this.nucBefore=nb;
				//NucLineage.Nuc nucBefore=lin.nuc.get(nb.name);
				Vector3d vAfter=new Vector3d(candAfter.pos);
				//Vector3d vBefore=nucBefore.pos.get(nucBefore.pos.headMap(frame).lastKey()).getPosCopy();
				vAfter.sub(nb.pos.getPosCopy());
				dist=vAfter.length();
				}
			
			/**
			 * Smallest distance first
			 */
			public int compareTo(DistancePair o)
				{
				return Double.compare(dist, o.dist);
				}
			}


		private Tuple<String,NucLineage.Nuc> nucleusFromCandidate(NucLineage lin, EvDecimal frame, double sigma2radiusFactor, Candidate cand)
			{
			String name=":"+frame.toString()+"_"+cand.id;
			NucLineage.Nuc nuc=lin.getCreateNuc(name);
			NucLineage.NucPos pos=nuc.getCreatePos(frame);
			pos.r=cand.bestSigma*sigma2radiusFactor; //TODO: resolution need to go in here
			pos.setPosCopy(cand.pos);
			return Tuple.make(name, nuc);
			}
		
		
		public void stop()
			{
			//TODO
			}
		
		
		/**
		 * Get names of nuclei that should get children or additional positions 
		 */
		public Collection<String> collectNucleiToContinue(NucLineage lin, EvDecimal frame)
			{
			List<String> names=new LinkedList<String>();
			for(String name:lin.nuc.keySet())
				{
				NucLineage.Nuc nuc=lin.nuc.get(name);
				if(nuc.child.isEmpty() && !nuc.pos.headMap(frame).isEmpty())
					names.add(name);
				}
			return names;
			}
		
		/**
		 * Delete given keyframe from all nuclei. Delete nuclei without keyframe
		 */
		private void deleteKeyFrame(NucLineage lin, EvDecimal frame, boolean alsoAfter)
			{
			List<String> toRemove=new LinkedList<String>();
			for(String name:lin.nuc.keySet())
				{
				NucLineage.Nuc nuc=lin.nuc.get(name);
				nuc.pos.remove(frame);
				
				List<EvDecimal> delFrames=alsoAfter ?
						new LinkedList<EvDecimal>(nuc.pos.tailMap(frame).keySet())
						: Arrays.asList(frame);
				
				for(EvDecimal f:delFrames)
					nuc.pos.remove(f);
				
				if(nuc.pos.isEmpty())
					{
					toRemove.add(name);
					if(nuc.parent!=null)
						lin.nuc.get(nuc.parent).child.remove(name);
					}
				}
			for(String name:toRemove)
				lin.nuc.remove(name);
			}
		
		
		public void dataChangedEvent()
			{
			comboChanHis.updateList();
			comboShell.updateList();
			}
		
		}
	}
