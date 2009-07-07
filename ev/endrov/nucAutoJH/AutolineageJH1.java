package endrov.nucAutoJH;

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
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
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
		private EvComboObjectOne<EvChannel> comboChanDIC=new EvComboObjectOne<EvChannel>(new EvChannel(), true, false);
		private JTextArea inpRadius=new JTextArea("40");
		private JTextArea inpNucBgSize=new JTextArea("2");
		private JTextArea inpNucBgMul=new JTextArea("1");
		
		public JComponent getComponent()
			{
			JComponent p=EvSwingUtil.tableCompactWide(
					new JLabel("His-channel"),comboChanHis,
					new JLabel("DIC-channel"),comboChanDIC,
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
			EvChannel channelDIC=comboChanDIC.getSelectedObject();
			if(channelHis!=null && channelDIC!=null && lin !=null)
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
				EvStack stackDIC=channelDIC.getFrame(channelDIC.closestFrame(frame));
				
				
				double expectRadius=Double.parseDouble(inpRadius.getText());
				double bgMulSize=Double.parseDouble(inpNucBgSize.getText());
				//double bgMulValue=Double.parseDouble(inpNucBgMul.getText());
				
				/*
				double resXhis=stackHis.getResbinX();
				double resYhis=stackHis.getResbinY();
				double resZhis=stackHis.getResbinZinverted().doubleValue();
				
				double resXDIC=stackDIC.getResbinX();
				double resYDIC=stackDIC.getResbinY();
				double resZDIC=stackDIC.getResbinZinverted().doubleValue();
*/
				
				///// Find candidate coordinates ////
				double sigmaHis1=expectRadius;
				double sigmaHis2=sigmaHis1*bgMulSize;
				int whis=stackHis.getWidth();
				int hhis=stackHis.getHeight();
				EvPixels kernel1=GenerateSpecialImage.genGaussian2D(sigmaHis1, sigmaHis1, whis, hhis);
				EvPixels kernel2=GenerateSpecialImage.genGaussian2D(sigmaHis2, sigmaHis2, whis, hhis);
				//Could do the bgmul here
				EvPixels kernelDOG=EvOpImageSubImage.minus(kernel1, kernel2);
				EvStack stackHisDog=new EvOpCircConv2D(kernelDOG).exec1(stackHis);
				List<Vector3i> maximas=EvOpFindLocalMaximas3D.findMaximas(stackHisDog);
				
				
				//// Detect where the embryo is ////
				int dicVarSize=40;
				EvStack stackDICvar=new EvOpMovingVariance(dicVarSize,dicVarSize).exec1(stackDIC);
				EvStack stackDICt=new EvOpThresholdPercentile2D(0.8).exec1(stackDICvar);
				stackDICt=new EvOpBinMorphFillHoles2D().exec1(stackDICt);
				EvStack stackEmbryoMask=stackDICt;
				
				
				//// Choose candidates ////
				int[][] pixEmbryoMask=stackEmbryoMask.getArraysInt();
				int wdic=stackEmbryoMask.getWidth();
				int hdic=stackEmbryoMask.getHeight();
				int ddic=stackEmbryoMask.getDepth();
				int i=0;
				for(Vector3i v:maximas)
					{
					Vector3d wpos=stackHis.transformImageWorld(new Vector3d(v.x,v.y,v.z));
					Vector3d dicPos=stackDIC.transformWorldImage(wpos);
					
					
					System.out.println("r should be "+stackHis.scaleImageWorldX(20));
					
					
					System.out.println("in his: "+v);
					System.out.println("in dic: "+dicPos);
					System.out.println("in world: "+wpos);
					
					
					NucLineage.Nuc nuc=lin.getNucCreate(""+i);
					NucLineage.NucPos pos=nuc.getPosCreate(frame);
					pos.r=5;
					pos.setPosCopy(wpos);

					Vector3i dicPosi=new Vector3i((int)dicPos.x,(int)dicPos.y,(int)dicPos.z); 
					if(dicPosi.x>=0 && dicPosi.x<whis-1 && dicPosi.y>=0 && dicPosi.z<hdic && dicPosi.z>=0 && dicPosi.z<ddic)
						{
						int val=pixEmbryoMask[dicPosi.z][dicPosi.y*wdic+dicPosi.x];
						System.out.println("dic mask "+val);
						
						}
					else
						System.out.println("outside DIC");
					
					i++;
					}
				
				
				
				
				
				
				session.nowAtFrame(channelHis.closestFrameAfter(frame));
				}
			}

		public void stop()
			{
			//TODO
			}
		
		}
	}
