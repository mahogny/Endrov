package endrov.nucAutoJH;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.vecmath.Vector3d;

import endrov.basicWindow.EvComboObjectOne;
import endrov.flowAveraging.EvOpMovingVariance;
import endrov.flowBasic.math.EvOpImageSubImage;
import endrov.flowBinaryMorph.EvOpBinMorphFillHoles2D;
import endrov.flowFindFeature.EvOpFindLocalMaximas3D;
import endrov.flowFourier.EvOpCircConv2D;
import endrov.flowGenerateImage.GenerateSpecialImage;
import endrov.flowThreshold.EvOpThresholdPercentile2D;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.nuc.NucLineage;
import endrov.nucImage.LineagingAlgorithm;
import endrov.nucImage.LineagingAlgorithm.LineageAlgorithmDef;
import endrov.shell.Shell;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
import endrov.util.ImVector2;
import endrov.util.ImVector3d;
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
		private JTextArea inpRadius=new JTextArea("40");
		private JTextArea inpNucBgSize=new JTextArea("2");
		private JTextArea inpNucBgMul=new JTextArea("1");
		
		public JComponent getComponent()
			{
			JComponent p=EvSwingUtil.tableCompactWide(
					new JLabel("His-channel"),comboChanHis,
//					new JLabel("DIC-channel"),comboChanDIC,
					new JLabel("Shell"),comboShell,
					new JLabel("E[r]"),inpRadius,
					new JLabel("Nuc bg size"),inpNucBgSize,
					new JLabel("Nuc bg mul"),inpNucBgMul
					);

			return p;
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
				double bgSize=Double.parseDouble(inpNucBgSize.getText());
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
				double sigmaHis1z=sigmaHis1/resFrac;
				
				
				double sigmaHis2=sigmaHis1*bgSize;
				int whis=stackHis.getWidth();
				int hhis=stackHis.getHeight();
				EvPixels kernel1=GenerateSpecialImage.genGaussian2D(sigmaHis1, sigmaHis1, whis, hhis);
				EvPixels kernel2=GenerateSpecialImage.genGaussian2D(sigmaHis2, sigmaHis2, whis, hhis);
				//Could do the bgmul here
				EvPixels kernelDOG=EvOpImageSubImage.minus(kernel1, kernel2);
				EvStack stackHisDog=new EvOpCircConv2D(kernelDOG).exec1(stackHis);
				List<Vector3i> maximas=EvOpFindLocalMaximas3D.findMaximas(stackHisDog);
				
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
				int i=0;
				for(Vector3i v:maximas)
					{
					Vector3d wpos=stackHis.transformImageWorld(new Vector3d(v.x,v.y,v.z));
//					Vector3d dicPos=stackDIC.transformWorldImage(wpos);
					
					if(shell.isInside(new ImVector3d(wpos.x,wpos.y,wpos.z)))
						{
						NucLineage.Nuc nuc=lin.getNucCreate(""+i);
						NucLineage.NucPos pos=nuc.getPosCreate(frame);
						pos.r=3;
						pos.setPosCopy(wpos);
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
					i++;
					}
				
				
				
				
				
				
				session.nowAtFrame(channelHis.closestFrameAfter(frame));
				}
			}

		public void stop()
			{
			//TODO
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
		
		
		}
	}
