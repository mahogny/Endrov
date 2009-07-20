package endrov.nucAutoJH;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import qhull.Voronoi;
import qhull.VoronoiNeigh;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.decomposition.DoubleEigenvalueDecomposition;

import endrov.basicWindow.EvComboObjectOne;
import endrov.data.EvContainer;
import endrov.flowBasic.math.EvOpImageSubImage;
import endrov.flowFindFeature.EvOpFindLocalMaximas3D;
import endrov.flowFourier.EvOpCircConv2D;
import endrov.flowGenerateImage.GenerateSpecialImage;
import endrov.flowMultiscale.Multiscale;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.line.EvLine;
import endrov.nuc.NucLineage;
import endrov.nucImage.LineagingAlgorithm;
import endrov.nucImage.LineagingAlgorithm.LineageAlgorithmDef;
import endrov.shell.Shell;
import endrov.util.*;

/**
 * Autolineage algorithm: JH1 <br/>
 * 
 * Meant to be used with his::rfp or equivalent marker. Optimized for wide-field fluorescence
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

	
	/**
	 * Instance of the algorithm
	 * @author Johan Henriksson
	 *
	 */
	private static class Algo implements LineagingAlgorithm
		{
		private EvComboObjectOne<EvChannel> comboChanHis=new EvComboObjectOne<EvChannel>(new EvChannel(), false, false);
		private EvComboObjectOne<Shell> comboShell=new EvComboObjectOne<Shell>(new Shell(), false, false);
//		private JTextField inpRadiusExpectedMax=new JTextField("6");
	//	private JTextField inpRadiusCutoff=new JTextField("1.5");
		private JTextField inpRadiusExpectedMax=new JTextField("3.3");
		private JTextField inpRadiusCutoff=new JTextField("0.83");
		private JTextField inpSigmaXY2radiusFactor=new JTextField("2.12575");
		private JButton bReassChildren=new JButton("Reassign parent-children");
		private JButton bReestParameters=new JButton("Re-estimate parameters");
		private JCheckBox cbForceNoDiv=new JCheckBox("");
		private JCheckBox cbCreateNew=new JCheckBox("");
		private JCheckBox cbCreateLines=new JCheckBox("");
		//private JTextField inpNucBgSize=new JTextField("2");
		//private JTextField inpNucBgMul=new JTextField("1");
		
		private int nextCandidateID=0;
		private boolean isStopping=true;
		private boolean isStopping(){return isStopping;}
		
		//private double sigmaXY2radiusFactor=0.55;
		private double newSigmaXY2radiusFactor;//=2.12575;
		private double resXY; //[px/um]
		
		private double scaleSigmaXY2radius(double sigma)
			{	
			return sigma*newSigmaXY2radiusFactor/resXY;
			}
		
		private double scaleRadius2sigmaXY(double radius)
			{
			return radius*resXY/newSigmaXY2radiusFactor;
			}
		
		
		/**
		 * Set if to stop the algorithm prematurely
		 */
		public void setStopping(boolean b)
			{
			isStopping=b;
			}
		
		/**
		 * Get custom GUI component
		 */
		public JComponent getComponent()
			{
			JComponent p=EvSwingUtil.layoutTableCompactWide(
					new JLabel("His-channel"),comboChanHis,
					new JLabel("Shell"),comboShell,
					new JLabel("Max radius [um]"),inpRadiusExpectedMax,
					new JLabel("Min radius [um]"),inpRadiusCutoff,
					new JLabel(""),new JLabel("-------"),
					new JLabel("σ to r"),inpSigmaXY2radiusFactor,
					new JLabel("Force no div"),cbForceNoDiv,
					new JLabel("No P-C link"),cbCreateNew,
					new JLabel("Create div lines"),cbCreateLines
					);

			bReassChildren.setToolTipText("Reassign parent-children relations in last frame");
			bReassChildren.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){reassignChildren();}});
			
			bReestParameters.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){reestParameters();}});

			inpRadiusExpectedMax.setToolTipText("Largest radius that will be sought for, but larger radii will be accepted");
			inpRadiusCutoff.setToolTipText("Smallest accepted radius");
			cbForceNoDiv.setToolTipText("Tell that no divisions are expected");
			inpSigmaXY2radiusFactor.setToolTipText("σ multiplied by this factor becomes radius. Depends on the marker and the PSF.");
			cbCreateNew.setToolTipText("Do not create parent-child links");
			cbCreateLines.setToolTipText("Create lines showing possible divisions. For debugging.");
			
			
			return EvSwingUtil.layoutCompactVertical(p,bReassChildren,bReestParameters);
			}
		
		

		/**
		 * Re-assign parent-children from the last frame and the frame before
		 */
		private void reassignChildren()
			{
			
			}

		/**
		 * Reestimate parameters using last frame
		 */
		private void reestParameters()
			{
			
			}
		
		
		/////////////////////////////////////////////////////////////////////////////////////
		///////////////////////// Candidate divisions ///////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////

		
		/**
		 * Candidate pair of dividing nuclei
		 * @author Johan Henriksson
		 *
		 */
		private static class CandDivPair 
			{
			public Candidate ca, cb;
			
			/** Error should be low for a good fit */
			public double error;

			/** Angle diff is within [0,1/4] lap */
			public double angleDiff;
			public double intensDiff;
			
			public double distance;
			
			//All ratios >= 1
			public double sigmaRatio;
			public double aPCratio;
			public double bPCratio;
			public double pcRatio;
			
			
			
			public CandDivPair(Candidate ca, Candidate cb)
				{
				this.ca = ca;
				this.cb = cb;
				
				//Compare angles using smallest principal component
				Vector2d va=new Vector2d(ca.eigvec[0].x,ca.eigvec[0].y);
				Vector2d vb=new Vector2d(cb.eigvec[0].x,cb.eigvec[0].y);
				//double angleDiff=Math.acos(ca.eigvec[0].dot(cb.eigvec[0])); //For 3D PCA
				angleDiff=Math.acos(va.dot(vb));
				va.scale(-1);
				angleDiff=Math.min(angleDiff, Math.acos(va.dot(vb))); //Can be made faster
				
				intensDiff=Math.abs(ca.intensity-cb.intensity); //relative scale?
				
				sigmaRatio=EvMathUtil.ratioAbove1(ca.bestSigma,cb.bestSigma);
				aPCratio=EvMathUtil.ratioAbove1(ca.eigval[0],ca.eigval[1]); //invert1 not really needed...
				bPCratio=EvMathUtil.ratioAbove1(cb.eigval[0],cb.eigval[1]);
				pcRatio=EvMathUtil.ratioAbove1(aPCratio,bPCratio);
				
				Vector3d diff=new Vector3d(ca.wpos);
				diff.sub(cb.wpos);
				distance=diff.length();
				}
			
			}
		
		/**
		 * Sort list of candidates best to worst
		 */
		public void sortCandDiv(List<CandDivPair> divlist)
			{
			//Get statistics about the candidates. To weight different errors,
			//they should be normalized.
			double maxIntensityDiff=Double.MIN_VALUE;
			double maxPcRatio=Double.MIN_VALUE;
			double maxSigmaRatio=Double.MIN_VALUE;
			for(CandDivPair p:divlist)
				{
				if(p.intensDiff>maxIntensityDiff)
					maxIntensityDiff=p.intensDiff;
				//p.angleDiff
				if(p.pcRatio>maxPcRatio)
					maxPcRatio=p.pcRatio;
				if(p.sigmaRatio>maxSigmaRatio)
					maxSigmaRatio=p.sigmaRatio;
				//p.angleDiff
				}
				
				
			//Calculate error of fit
			//* Small intensity -> intensity difference will be smaller and hence have smaller influence
			for(CandDivPair p:divlist)
				{
				double weightSigma=1;
				double weightPC=1;
				double weightIntensity=1;
				
				p.error
				=weightIntensity*p.intensDiff/maxIntensityDiff
				+weightSigma*p.sigmaRatio/maxSigmaRatio
				+weightPC*p.pcRatio/maxPcRatio;
				}
			
			//Do the sorting
			Collections.sort(divlist, new Comparator<CandDivPair>(){
			public int compare(CandDivPair o1, CandDivPair o2)
				{return Double.compare(o1.error, o2.error);}
			}); 
			}
		
		
		/**
		 * Generate candidate division pairs as the Delaunay edges. 
		 * Turns out to be hypersensitive to false positives.
		 */
		public LinkedList<CandDivPair> generateCandDivVoronoi(List<Candidate> candlist)
			{
			LinkedList<CandDivPair> pairs=new LinkedList<CandDivPair>();
			try
				{
				//Build basic network: two candidates must be delaunay neighbors to be considered at all.
				//Delaunay graphs contains perfect matchings
				//(toughness and delaunay triangulations Dillencourt)
				//If a bad candidate is predicted in between two dividing cells then this algorithm is in trouble
				Vector3d[] points=new Vector3d[candlist.size()]; 
				int curi=0;
				for(Candidate cand:candlist)
					points[curi++]=cand.wpos;
				Voronoi voro=new Voronoi(points);
				
				VoronoiNeigh vneigh=new VoronoiNeigh(voro, false, new HashSet<Integer>());
				Candidate[] candarray=candlist.toArray(new Candidate[]{});
				for(int i=0;i<vneigh.dneigh.size();i++)
					for(int j:vneigh.dneigh.get(i))
						if(j>i)
							pairs.add(new CandDivPair(candarray[i],candarray[j]));
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			return pairs;
			}
		
		
		/**
		 * Generate every possible candidate division pair (except not the symmetric pair)
		 */
		public LinkedList<CandDivPair> generateCandDivAll(List<Candidate> candlist)
			{
			LinkedList<CandDivPair> pairs=new LinkedList<CandDivPair>();
			for(Candidate ca:candlist)
				for(Candidate cb:candlist)
					if(ca.hashCode()<cb.hashCode())
						pairs.add(new CandDivPair(ca,cb));
			return pairs;
			}
		
		
		/**
		 * Filter candidate division pairs using heuristics
		 */
		public LinkedList<CandDivPair> filterCandDivHeuristic(List<CandDivPair> candlist)
			{
			LinkedList<CandDivPair> out=new  LinkedList<CandDivPair>();
			for(CandDivPair p:candlist)
				{
				double ra=scaleSigmaXY2radius(p.ca.bestSigma);//*sigma2radiusFactor;
				double rb=scaleSigmaXY2radius(p.cb.bestSigma);//*sigma2radiusFactor;
				
				//Cut-offs can be optimized with SVM or online by comparing with all
				//past values
				
				//Hard cut-offs
				if(p.distance<3*(ra+rb))
					{
//					if(p.angleDiff<45.0/180.0*Math.PI) //angle has been up to 80deg once due to bad prediction!
					if(p.angleDiff<70.0/180.0*Math.PI) 
						if(p.sigmaRatio<1.7)
							if(p.pcRatio<1.8)
								out.add(p);
					}
				
				}
			return out;
			}
		
		
		
		/**
		 * Filter candidate divisions such that a nucleus is assigned to exactly 0 or 1
		 * other candidate nucleus. It is done by finding (approximately) candidate pairs
		 * such that the sum of error is minimized.
		 * 
		 * Also sort by error in fit.
		 */
		public LinkedList<CandDivPair> filterCandDivWeightedOverlap(LinkedList<CandDivPair> pairs)
			{
			int total=pairs.size();
			//Greedy algorithm. Should rather use minimum weighted non-bipartite matching
			pairs=new LinkedList<CandDivPair>(pairs);
			sortCandDiv(pairs);
			System.out.println("Pairs first: "+pairs.size());
			HashSet<Candidate> usedCand=new HashSet<Candidate>();
			for(CandDivPair pair:new LinkedList<CandDivPair>(pairs))
				{
				if(usedCand.contains(pair.ca) || usedCand.contains(pair.cb))
					pairs.remove(pair);
				else
					{
					usedCand.add(pair.ca);
					usedCand.add(pair.cb);
					}
				}
			System.out.println("Deleted divpair weight: "+(total-pairs.size())+" / "+total);
			return pairs;
			}
		
		
		/**
		 * Debugging: draw lines for candidate divisions
		 */
		public static void createLinesFromCandDiv(Collection<CandDivPair> pairs, EvContainer parentContainer, EvDecimal frame)
			{
			for(CandDivPair pair:pairs)
				{
				EvLine line=new EvLine();
				line.pos.add(new EvLine.Pos3dt(new Vector3d(pair.ca.wpos.x,pair.ca.wpos.y,pair.ca.wpos.z),frame));
				line.pos.add(new EvLine.Pos3dt(new Vector3d(pair.cb.wpos.x,pair.cb.wpos.y,pair.cb.wpos.z),frame));
				parentContainer.addMetaObject(line);
				}
			}
		
		
		/**
		 * Find candidate dividing nuclei
		 */
		private LinkedList<CandDivPair> findDividing(EvContainer parentContainer, List<Candidate> candlist, EvDecimal frame)
			{
			//Get list of candidate pairs to filter
			LinkedList<CandDivPair> pairs=generateCandDivAll(candlist);
			
			//Remove the most obvious candidate non-pairs
			pairs=filterCandDivHeuristic(pairs);

			//Globally optimized selection of candidate pairs
			pairs=filterCandDivWeightedOverlap(pairs);

			//For debugging
			if(cbCreateLines.isSelected())
				createLinesFromCandDiv(pairs, parentContainer, frame);

			return pairs;
			}
		
		/////////////////////////////////////////////////////////////////////////////////////
		///////////////////////// Candidates ////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////
		
		/**
		 * One candidate nuclei position
		 * @author Johan Henriksson
		 *
		 */
		private static class Candidate
			{
			public int id;
			public Vector3d wpos;
			public double bestSigma;
			public double intensity;
			
			public double[] eigval;
			public Vector3d[] eigvec;

			public int numOverlap;
			
			public String toString()
				{
				return "id="+id+"  bestSigma="+bestSigma+"  intensity="+intensity;
				}
			}
		
		/**
		 * Find candidate nuclei. These are the only candidates considered, and the final
		 * selection is a filtered list.
		 * 
		 * Candidates are found by looking for features at several frequencies using
		 * difference of gaussian (Rickert wavelet approximation). Candidates has to be
		 * within the shell.
		 */
		public List<Candidate> findCandidatesDoG(EvStack stackHis, Shell shell, double sigmaHis1)
			{
			//stackHis=new EvOpImageConvertPixel(EvPixelsType.DOUBLE).exec1(stackHis);
			int whis=stackHis.getWidth();
			int hhis=stackHis.getHeight();
			EvPixels kernel1=GenerateSpecialImage.genGaussian2D(sigmaHis1, sigmaHis1, whis, hhis);
			EvPixels kernel2=GenerateSpecialImage.genGaussian2D(sigmaHis1*2, sigmaHis1*2, whis, hhis);
			if(isStopping()) return new LinkedList<Candidate>();
			EvPixels kernelDOG=EvOpImageSubImage.minus(kernel1, kernel2);
			if(isStopping()) return new LinkedList<Candidate>();
			EvStack stackHisDog=new EvOpCircConv2D(kernelDOG).exec1(stackHis);
			if(isStopping()) return new LinkedList<Candidate>();
			List<Vector3i> maximas=EvOpFindLocalMaximas3D.findMaximas(stackHisDog);
			
			
			List<Candidate> candlist=new LinkedList<Candidate>();
			for(Vector3i v:maximas)
				{
				if(isStopping()) return new LinkedList<Candidate>();
				
				
				Vector3d wpos=stackHis.transformImageWorld(new Vector3d(v.x,v.y,v.z));
				
				if(shell.isInside(new ImVector3d(wpos.x,wpos.y,wpos.z)))
					{
					//double bestSigma=Multiscale.findFeatureScale(stackHis.getInt(v.z).getPixels(),sigmaHis1, v.x, v.y);
					double bestSigma=Multiscale.findFeatureScale2(stackHis.getInt(v.z).getPixels(), 
							v.x, v.y, 0.3, sigmaHis1*1.25, 8, 3);
					//System.out.println("Best fit sigma: "+bestSigma);

					
					//DoG or original image?
//				DoubleEigenvalueDecomposition eig=LocalMomentum.apply(stackHisDog.getPixels()[(int)Math.round(v.z)], bestSigma, bestSigma, v.x, v.y);
				DoubleEigenvalueDecomposition eig=LocalMomentum.applyCircle(stackHis.getPixels()[(int)Math.round(v.z)], bestSigma*2, v.x, v.y);
				//originally *3 for circle
					
	
				
					DoubleMatrix2D eigvec=eig.getV();
					DoubleMatrix1D eigval=eig.getRealEigenvalues();
					double[] eigvalv=new double[]{eigval.getQuick(0),eigval.getQuick(1),0};
					Vector3d[] eigvecv=new Vector3d[]{
							new Vector3d(eigvec.getQuick(0, 0),eigvec.getQuick(0, 1),0),
							new Vector3d(eigvec.getQuick(1, 0),eigvec.getQuick(1, 1),0),
							new Vector3d(0,0,0)};

					Candidate cand=new Candidate();
					cand.id=nextCandidateID++;
					cand.wpos=wpos;
					cand.bestSigma=bestSigma;
					cand.eigval=eigvalv;
					cand.eigvec=eigvecv;
					cand.intensity=Multiscale.convolveGaussPoint2D(stackHis.getInt(v.z).getPixels(), 
							bestSigma, bestSigma, stackHis.transformWorldImageX(cand.wpos.x), stackHis.transformWorldImageY(cand.wpos.y)); 
					//Found bug! the random intensities explained
					candlist.add(cand);
					}
				}
			return candlist;
			}
		
		
		/**
		 * Filter candidate list by removing candidates overlapped by another candidate that has
		 * a stronger intensity. This is an extremely efficient filter and doesn't seem to
		 * make any mistakes
		 */
		public LinkedList<Candidate> filterCandidatesStrongestIntensityOverlap(LinkedList<Candidate> candlist)
			{
			candlist=new LinkedList<Candidate>(candlist);
			Collections.sort(candlist, new Comparator<Candidate>(){
			public int compare(Candidate arg0, Candidate arg1)
				{
				return -Double.compare(arg0.intensity,arg1.intensity);
				}
			});
			LinkedList<Candidate> outList=new LinkedList<Candidate>();
			for(Candidate cand:candlist)
				{
				boolean overlap=false;
				for(Candidate other:outList)
					{
					Vector3d v=new Vector3d(cand.wpos);
					v.sub(other.wpos);
					double r1=scaleSigmaXY2radius(cand.bestSigma);
					double r2=scaleSigmaXY2radius(other.bestSigma);
					if(v.length()<r1+r2)
						{
						overlap=true;
						other.numOverlap++;
						}
					}
				if(!overlap)
					outList.add(cand);
				}
			System.out.println("Deleted candidate overlaps: "+
					(candlist.size()-outList.size())+" / "+candlist.size());
			return outList;
			}
		
		/**
		 * Take #num first entries in list. Assumes list is sorted best to worst
		 */
		public LinkedList<Candidate> filterCandidatesTake(LinkedList<Candidate> candlist, int num)
			{
			LinkedList<Candidate> outList=new LinkedList<Candidate>();
			for(int i=0;i<num && i<candlist.size();i++)
				outList.add(candlist.get(i));
			System.out.println("Deleted candidate overflow: "+
					(candlist.size()-outList.size())+" / "+candlist.size());
			return outList;
			}

		
		/**
		 * Filter candidates by removing those having sigma below threshold
		 */
		private LinkedList<Candidate> filterCandidatesCutoffSigma(List<Candidate> candlist, double sigma)
			{
			int total=candlist.size();
			LinkedList<Candidate> outList=new LinkedList<Candidate>();
			for(Candidate cand:candlist)
				if(cand.bestSigma>sigma)
					outList.add(cand);
			System.out.println("Deleted candidate cutoff: "+(total-outList.size())+" / "+total);
			return outList;
			}

		
		/////////////////////////////////////////////////////////////////////////////////////
		/////////////////// Candidate before-after pairing //////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////

		
		

		
		/**
		 * A nucleus from the last frame
		 * @author Johan Henriksson
		 *
		 */
		private static class NucBefore
			{
			public final String name;
			public final NucLineage.NucPos pos;
			//public final Set<Candidate> children=new HashSet<Candidate>();
			
			public NucBefore(NucLineage lin, String name, EvDecimal frame)
				{
				this.name = name;
				NucLineage.Nuc nuc=lin.nuc.get(name);
//				pos=lin.nuc.get(name).pos.get(frame);
				pos=nuc.pos.get(nuc.pos.headMap(frame).lastKey());
				if(pos==null)
					{
					System.out.println("no pos");
					System.out.println(nuc.pos);
					}
				}
			}
		
		/**
		 * Pairing of cell from last frame to candidates
		 * @author Johan Henriksson
		 */
		public class BeforeAfterPair //implements Comparable<BeforeAfterPair>
			{
			public final NucBefore nucBefore;
			public final List<Candidate> candAfter;
			public final double dist;
			
			public BeforeAfterPair(NucLineage lin, NucBefore nb, Collection<Candidate> candAfter, EvDecimal frame)
				{
				this.candAfter=new LinkedList<Candidate>(candAfter);
				this.nucBefore=nb;
				//NucLineage.Nuc nucBefore=lin.nuc.get(nb.name);
				Vector3d vAfter=new Vector3d();
				for(Candidate c:candAfter)
					vAfter.add(new Vector3d(c.wpos));
				vAfter.scale(1.0/candAfter.size());
				//Vector3d vBefore=nucBefore.pos.get(nucBefore.pos.headMap(frame).lastKey()).getPosCopy();
				vAfter.sub(nb.pos.getPosCopy());
				dist=vAfter.length();
				}
			
			/**
			 * Smallest distance first
			 */
			/*
			public int compareTo(BeforeAfterPair o)
				{
				//TODO also compare size difference
				return Double.compare(dist, o.dist);
				}*/
			}

		
		public static void sortBeforeAfter(LinkedList<BeforeAfterPair> baList)
			{
			Collections.sort(baList, new Comparator<BeforeAfterPair>(){
				public int compare(BeforeAfterPair o1, BeforeAfterPair o2)
					{
					return Double.compare(o1.dist, o2.dist);
					}
			});
			}
		
		/**
		 * Generate complete list of candidate before-after matchings.
		 */
		public LinkedList<BeforeAfterPair> generateAllBeforeAfter(Collection<NucBefore> joiningNucBefore, Collection<Candidate> candlist, EvDecimal frame, NucLineage lin)
			{
			LinkedList<BeforeAfterPair> baPairs=new LinkedList<BeforeAfterPair>();
			for(NucBefore nameBefore:joiningNucBefore)
				for(Candidate candAfter:candlist)
					baPairs.add(new BeforeAfterPair(lin, nameBefore, Collections.singleton(candAfter), frame));
			return baPairs;
			}

		
		/**
		 * Given a list of (candidate before) - (candidates after), find the optimal match.
		 * Assumes list of pairs is sorted best to worst
		 * Return which candidates were used
		 */
		public LinkedList<BeforeAfterPair> findBestMatchingBeforeAfter(LinkedList<BeforeAfterPair> baPairs, Set<NucBefore> usedBefore, Set<Candidate> usedAfter)
			{
			LinkedList<BeforeAfterPair> outBa=new LinkedList<BeforeAfterPair>();
			
			//TODO use algo for minimal weighted bipartite matching
			//Since not the same elements before/after, not quite matching. By
			//adding virtual elements of infinite distance, the bad ones are sorted out
			//MunkresKuhn munkres=new MunkresKuhn();
			if(usedBefore==null)
				usedBefore=new HashSet<NucBefore>();
			if(usedAfter==null)
				usedAfter=new HashSet<Candidate>();
			for(BeforeAfterPair p:baPairs)
				if(!usedBefore.contains(p.nucBefore))
					{
					if(!EvSetUtil.containsAnyOf(usedAfter,p.candAfter))
						{
						outBa.add(p);
						usedAfter.addAll(p.candAfter);
						usedBefore.add(p.nucBefore);
						//p.nucBefore.children.addAll(p.candAfter);
						}
					}
			return outBa;
			}
			
		

		
		
		/*
		public void oldMatchingBeforeAfter(LinkedList<BeforeAfterPair> distpairs)
			{
			//Improvement: use algo for minimal weighted bipartite matching
			
			//Join nuclei 1 before-1 after.
			//Since not the same elements before/after, not quite matching. By
			//adding virtual elements of infinite distance, the bad ones are sorted out
			//MunkresKuhn munkres=new MunkresKuhn();
			Set<NucBefore> usedBefore=new HashSet<NucBefore>();
			Set<Candidate> usedAfter=new HashSet<Candidate>();
			for(BeforeAfterPair p:distpairs)
				if(!usedBefore.contains(p.nucBefore))
					if(!usedAfter.contains(p.candAfter)) //no longer works
						{
						usedAfter.addAll(p.candAfter);
						usedBefore.add(p.nucBefore);
						p.nucBefore.children.addAll(p.candAfter);
						}
			
			//Greedily join nuclei 1 before-max 2 after
			usedBefore.clear();
			for(BeforeAfterPair p:distpairs)
				if(!usedBefore.contains(p.nucBefore))
					if(!usedAfter.contains(p.candAfter)) //no longer works
						{
						usedAfter.addAll(p.candAfter);
						usedBefore.add(p.nucBefore);
						p.nucBefore.children.addAll(p.candAfter);
						}
			}*/

		
		/**
		 * Filter list of divisions: get the list with unused candidates having the best link
		 * 
		 * This uses a greedy algorithm
		 */
		/*
		 * This code is a mess and doesn't work as-is. has to be fixed if you want to use it
		 * 
		 * 
		public LinkedList<CandDivPair> filterCandDivKeepUnused(LinkedList<CandDivPair> divList, Collection<Candidate> candlist, Set<Candidate> usedCandidate)
			{
			LinkedList<CandDivPair> keepDiv=new LinkedList<CandDivPair>();
			Set<Candidate> toIterate=new HashSet<Candidate>(candlist);
			toIterate.removeAll(usedCandidate);
			
			Set<Candidate> usedCandidateHere=new HashSet<Candidate>(); //beep???
			for(Candidate cand:toIterate)
					{
					findOther: for(CandDivPair p:divList)
						{
						if(p.ca==cand || p.cb==cand)
							{
							Candidate otherCandidate=p.ca;
							if(otherCandidate==cand)
								otherCandidate=p.cb;
							if(!usedCandidate.contains(otherCandidate))
								{
								keepDiv.add(p);
								usedCandidate.add(otherCandidate);
								break findOther;
								}
							}
						}
					}
			return keepDiv;
			}
			*/
		
		/**
		 * Take the numTake best candidate dividing nuclei.
		 * Assumes divlist sorted best to worst
		 * 
		 * @return Unused candidates and the best candidate divisions
		 */
		public Tuple<LinkedList<CandDivPair>,LinkedList<Candidate>> takeBestDivCandidates(
				Collection<Candidate> candlist, LinkedList<CandDivPair> divList, int numTake)
			{
			LinkedList<CandDivPair> newDivPairs=new LinkedList<CandDivPair>();
			Set<Candidate> usedCand=new HashSet<Candidate>();
			int taken=0;
			for(CandDivPair cp:divList)
				{
				if(!usedCand.contains(cp.ca) && !usedCand.contains(cp.cb))
					{
					usedCand.add(cp.ca);
					usedCand.add(cp.cb);
					newDivPairs.add(cp);
					taken++;
					if(taken==numTake)
						break;
					}
				}
			
			//Collect remaining candidates
			LinkedList<Candidate> newcandlist=new LinkedList<Candidate>();
			for(Candidate c:candlist)
				if(!usedCand.contains(c))
					newcandlist.add(c);
			
			return Tuple.make(newDivPairs, newcandlist);
			}
		
		
		/**
		 * Fit together cell in last frame with cell in this frame.
		 * 
		 * Alternative not used strategy:
		 * virtual candidates, join 1-1. there will be tons of virtual cand, need to filter them aggressively. no guarantee that all nuclei are used;
		 * not very nice. if only 1 cell divides then hard to control aggression
		 */
		public LinkedList<BeforeAfterPair> chooseBeforeAfter(Collection<NucBefore> joiningNucBefore, Collection<Candidate> candlist, EvDecimal frameBefore, NucLineage lin, LinkedList<CandDivPair> divList, boolean forceNoDiv)
			{
			if(forceNoDiv)
				{
				//Take the best 1-1 matches, ignore the rest
				LinkedList<BeforeAfterPair> baPairs=generateAllBeforeAfter(joiningNucBefore, candlist, frameBefore, lin);
				sortBeforeAfter(baPairs);
				baPairs=findBestMatchingBeforeAfter(baPairs,null,null);
				return baPairs;
				}
			else
				{
				//We assume all candidates are real; we just don't know which are dividing or not. We know the number of dividing cells,
				//it is |candlist| - |joiningNucBefore|. Hence we pick the cells most likely to divide and partition them.
				//But: this approach is very sensitive to false positives
	
				int numDivCreate=candlist.size()-joiningNucBefore.size();
				numDivCreate=Math.min(numDivCreate, joiningNucBefore.size());
				//System.out.println("Want # div "+numDivCreate);
				Tuple<LinkedList<CandDivPair>,LinkedList<Candidate>> ret=takeBestDivCandidates(candlist, divList, numDivCreate);
				//System.out.println("got # div" +ret.fst().size());
				//System.out.println("unused # "+ret.snd().size());
				
				//Generate sorted candidate before<->after matchings
				LinkedList<BeforeAfterPair> baPairs=generateAllBeforeAfter(joiningNucBefore, ret.snd(), frameBefore, lin);
				for(CandDivPair div:ret.fst())
					for(NucBefore nb:joiningNucBefore)
						baPairs.add(new BeforeAfterPair(lin, nb, Arrays.asList(div.ca,div.cb),frameBefore));
				sortBeforeAfter(baPairs);
				baPairs=findBestMatchingBeforeAfter(baPairs,null,null);
				
				return baPairs;
				
				//Idea: it is possible at this stage to detect a bad fit. in that case, we would eliminate cells as long as the fit improves.
				//This could kill some too large false positives. Detection would be based on sigmaRatio and intensityRatio. Latter requires background sub.
	
	
				
				//If there are candidates left over now, it means there are more than twice as many candidates
				//as nuclei in the frame before. The rest of the candidates are assumed bad and discarded.
				}
			}
		/*
			{
			//Generate sorted candidate before after matchings
			LinkedList<BeforeAfterPair> baPairs=generateAllBeforeAfterSorted(joiningNucBefore, candlist, frameBefore, lin);

			//Join nuclei 1 before<->1 after. If there are divisions then there will be left-overs
			HashSet<Candidate> usedAfter=new HashSet<Candidate>(); //Will return content here
			findMatchingBeforeAfter(baPairs,null,usedAfter);
			
			//Which nuclei are left? These must have divided. Find the optimal match for each.
			//This assumes rather good locality in the divisions so that the 1-1 fit will leave the
			//ones that divided.
			LinkedList<CandDivPair> keepDiv=filterCandDivKeepUnused(divList, candlist, usedAfter);
			//Partition nuclei
			HashSet<Candidate> usedCand=new HashSet<Candidate>();
			usedC
			
			
			
			
			
			//If locality breaks down then one additional step is to try local permutations.
			//The problem is in fact to find to find N=A+B partitions, where A partions have 1 nuclei
			//and B partitions have 2 such that the division matching score is maximized, weighted
			//with the matching score from the last frame.
			
			// // Not done right now //
			
			//Join nuclei 1 before<->1 after. This time there should not be left-overs
			
			
			
			findMatchingBeforeAfter(baPairs,null,null);
			
			
			

			//////////////////////////////
			

			
			//If there are candidates left over now, it means there are more than twice as many candidates
			//as nuclei in the frame before. The rest of the candidates are assumed bad and discarded.
			
			
			}*/
		
		
		
		
		/**
		 * Create nuclei and add new coordinates for the next frame
		 */
		/*
		public void createNucFromPairing(Collection<NucBefore> joiningNucBefore, NucLineage lin, EvDecimal frame)
			{
			for(NucBefore nb:joiningNucBefore)
				{
				String parentName=nb.name;
				NucLineage.Nuc parentNuc=lin.nuc.get(parentName);
				for(Candidate cand:nb.children)
					{
					Tuple<String,NucLineage.Nuc> child=createNucleusFromCandidate(lin, frame, cand);
					parentNuc.child.add(child.fst());
					child.snd().parent=parentName;
					}
				}
			}*/

		
		/**
		 * Create nuclei and add new coordinates for the next frame
		 */
		public void createNucFromBeforeAfter(Collection<BeforeAfterPair> joiningNucBefore, NucLineage lin, EvDecimal currentFrame)
			{
			for(BeforeAfterPair nb:joiningNucBefore)
				{
				String parentName=nb.nucBefore.name;
				NucLineage.Nuc parentNuc=lin.nuc.get(parentName);
				for(Candidate cand:nb.candAfter)
					{
					Tuple<String,NucLineage.Nuc> child=createNucleusFromCandidate(lin, currentFrame, cand);
					parentNuc.child.add(child.fst());
					child.snd().parent=parentName;
					}
				}
			}
		
		/////////////////////////////////////////////////////////////////////////////////////
		///////////////////////// Main code /////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////

		
		/**
		 * Lineage one frame
		 */
		public void run(LineageSession session)
			{
			EvDecimal frame=session.getStartFrame();
			NucLineage lin=session.getLineage();
			EvChannel channelHis=comboChanHis.getSelectedObject();
			EvContainer parentContainer=session.getEvContainer();
			//EvChannel channelDIC=comboChanDIC.getSelectedObject();
			final Shell shell=comboShell.getSelectedObject();
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
				
				final EvStack stackHis=channelHis.getFrame(frame);
				resXY=stackHis.getResbinX();

				//Read parameters from the GUI
				double expectRadius=Double.parseDouble(inpRadiusExpectedMax.getText());
				double cutoffRadius=Double.parseDouble(inpRadiusCutoff.getText());
				newSigmaXY2radiusFactor=Double.parseDouble(inpSigmaXY2radiusFactor.getText());
				
				//double resXhis=stackHis.getResbinX();
				//double resYhis=stackHis.getResbinY();
				//double resZhis=1/stackHis.getResbinZinverted().doubleValue();
				//double resFrac=resXhis/resZhis;  //>1 means X has more pixels
//				double sigmaHis1=expectRadius;

				double expectedSigma=scaleRadius2sigmaXY(expectRadius);
				double cutoffSigma=scaleRadius2sigmaXY(cutoffRadius);

				System.out.println("max[sigma]: "+expectedSigma);
				System.out.println("min[sigma]: "+cutoffSigma);

				//Remove old coordinates at this keyframe
				deleteKeyFrame(lin, frame, true);
				
				//Find candidates. Search at several resolutions
				long startTime=System.currentTimeMillis();
				/*
				LinkedList<Candidate> candlist=new LinkedList<Candidate>();
				candlist.addAll(findCandidatesDoG(stackHis, shell, expectedSigma));
				if(isStopping) return;
				candlist.addAll(findCandidatesDoG(stackHis, shell, expectedSigma*0.8));
				if(isStopping) return;
				candlist.addAll(findCandidatesDoG(stackHis, shell, expectedSigma*0.65));
				if(isStopping) return;*/
				
				
				final EvStack finalStackHis=stackHis;//new EvOpImageConvertPixel(EvPixelsType.DOUBLE).exec1(stackHis);
				
				
				LinkedList<Candidate> candlist=new LinkedList<Candidate>();
				for(List<Candidate> list:EvParallel.map(
						Arrays.asList(expectedSigma, expectedSigma*0.65), 
//						Arrays.asList(expectedSigma, expectedSigma*0.8, expectedSigma*0.65), 
						new EvParallel.FuncAB<Double, List<Candidate>>()
							{
							public List<Candidate> func(Double in)
								{
								if(isStopping()) 
									return new LinkedList<Candidate>();
								return findCandidatesDoG(finalStackHis, shell, in);
								}
							}))
					candlist.addAll(list);
				if(isStopping) return;
				long endTime=System.currentTimeMillis();
				System.out.println("Total time to find features [s]: "+(endTime-startTime)/1000.0);
				//Better to parallelize on single-slice level
				
				//Sort by sigma and give new IDs based on it
				Collections.sort(candlist, new Comparator<Candidate>(){
				public int compare(Candidate arg0, Candidate arg1)
					{return -Double.compare(arg0.bestSigma,arg1.bestSigma);}
				});
				reassignID(candlist);

				//Remove overlapping candidates. Bulk removal.
				candlist=filterCandidatesStrongestIntensityOverlap(candlist);
				
				if(isStopping) return;
				
				//Remove candidates smaller than the cut-off radius.
				//Few but very annoying false positives
				candlist=filterCandidatesCutoffSigma(candlist, cutoffSigma);

				//Find nuclei to join with from frame before
				List<NucBefore> joiningNucBefore=new ArrayList<NucBefore>();
				Collection<String> nucsBefore=collectNucleiToContinue(lin, frame);
				for(String name:nucsBefore)
					joiningNucBefore.add(new NucBefore(lin,name,frame));

				
				/**
				 * Join coordinates
				 */
				if(joiningNucBefore.isEmpty() || cbCreateNew.isSelected())
					{
					//There are no nucs before. Accept all candidates.
					System.out.println("No nuclei since before");
					
					for(Candidate cand:candlist)
						createNucleusFromCandidate(lin, frame, cand);
					}
				else
					{
					//There are nucs since before. Need to join
					System.out.println("Continuing lineage from before");

					//Will not take more than twice as many nuclei than last frame.
					//Sort by intensity, keep 2N nuclei
					candlist=filterCandidatesTake(candlist, joiningNucBefore.size()*2);

					//Find candidates likely to either divide or have divided
					LinkedList<CandDivPair> divList=findDividing(parentContainer, candlist, frame);

					/*
					for(Candidate cand:candlist)
						createNucleusFromCandidate(lin, frame, cand);
					 */

					//Choose how to link last frame to this frame
					LinkedList<BeforeAfterPair> baPair=chooseBeforeAfter(
							joiningNucBefore, candlist, frameBefore, lin, divList, cbForceNoDiv.isSelected());

					//System.out.println("# pairs: "+baPair.size());
					
					// Turn before-after pairings into nuclei
					createNucFromBeforeAfter(baPair, lin, frame);
					
					//Redo list of all candidates
					candlist.clear();
					for(BeforeAfterPair nb:baPair)
						candlist.addAll(nb.candAfter);
					}
				
				//Use list of candidates to estimate parameters for the next frame
				estimateParameters(candlist);
				
				//Prepare to do next frame
				session.finishedAndNowAtFrame(channelHis.closestFrameAfter(frame));
				}
			}
		
		
		/**
		 * Estimate parameters to be used for the next frame
		 */
		public void estimateParameters(List<Candidate> candlist)
			{
			if(candlist.isEmpty())
				System.out.println("No nuclei in list, not estimating parameters");
			else
				{
				double sumSigma=0;
				double sumSigma2=0;
				double maxSigma=Double.MIN_VALUE;
				double minSigma=Double.MAX_VALUE;
				int countSigma=0;
				for(Candidate cand:candlist)
					{
					sumSigma+=cand.bestSigma;
					sumSigma2+=cand.bestSigma*cand.bestSigma;
					maxSigma=Math.max(maxSigma, cand.bestSigma);
					minSigma=Math.min(minSigma, cand.bestSigma);
					countSigma++;
					}
				//double varSigma=EvMathUtil.unbiasedVariance(sumSigma, sumSigma2, countSigma);
				double meanSigma=sumSigma/countSigma;

				System.out.println("mean sigma "+meanSigma);
				System.out.println("max sigma "+maxSigma);
				System.out.println("min sigma "+minSigma);
				System.out.println("# accepted "+candlist.size());

				//Set new parameters
				final double setExpectRadius=scaleSigmaXY2radius(maxSigma);
				final double setMinRadius=scaleSigmaXY2radius(minSigma*0.8);
				try
					{
					SwingUtilities.invokeAndWait(new Runnable(){
					public void run()
						{
						inpRadiusExpectedMax.setText(""+setExpectRadius);
						inpRadiusCutoff.setText(""+setMinRadius);
						}});
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				System.out.println("new sigma "+meanSigma);
				}
			}

		


		/**
		 * Turn a candidate into a new nucleus
		 */
		private Tuple<String,NucLineage.Nuc> createNucleusFromCandidate(NucLineage lin, EvDecimal frame, Candidate cand)
			{
			String name=":"+frame.toString()+"_"+cand.id+"_s"+cand.bestSigma;
			NucLineage.Nuc nuc=lin.getCreateNuc(name);
			NucLineage.NucPos pos=nuc.getCreatePos(frame);
			pos.r=scaleSigmaXY2radius(cand.bestSigma); 
			pos.setPosCopy(cand.wpos);
			pos.ovaloidAxisLength=new double[]{
					scaleSigmaXY2radius(cand.eigval[0]),
					scaleSigmaXY2radius(cand.eigval[1]),
							scaleSigmaXY2radius(cand.eigval[2])};
			pos.ovaloidAxisVec=cand.eigvec;
			return Tuple.make(name, nuc);
			}
		
		
		/**
		 * Redo all ID assignments
		 */
		public void reassignID(Collection<Candidate> candlist)
			{
			nextCandidateID=0;
			for(Candidate cand:candlist)
				cand.id=nextCandidateID++;
			}
		
		/**
		 * Get names of nuclei that should get children or additional positions 
		 */
		private static Collection<String> collectNucleiToContinue(NucLineage lin, EvDecimal frame)
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
		private static void deleteKeyFrame(NucLineage lin, EvDecimal frame, boolean alsoAfter)
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
