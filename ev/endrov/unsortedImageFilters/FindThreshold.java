package endrov.unsortedImageFilters;

import java.util.*;

import endrov.imageset.EvPixels;

/**
 * Find threshold given image statistics. Threshold is pixel>=thresholdValue.
 * 
 * @author Johan Henriksson
 *
 */
public class FindThreshold
	{

	
	//max entropy, a related algorithm
	//http://rsbweb.nih.gov/ij/plugins/entropy.html
	/**
	 * 
	 */
	
	
	//Look at this later
	//#  M. Sezgin and B. Sankur (2004). "Survey over image thresholding techniques and quantitative performance evaluation". Journal of Electronic Imaging 13 (1): 146–165. doi:10.1117/1.1631315. 
	//# ^ a b N. Otsu (1979). "A threshold selection method from gray-level histograms". IEEE Trans. Sys., Man., Cyber. 9: 62–66. doi:10.1109/TSMC.1979.4310076. 
	//# ^ Ping-Sung Liao and Tse-Sheng Chen and Pau-Choo Chung (2001). "A Fast Algorithm for Multilevel Thresholding". J. Inf. Sci. Eng. 17 (5): 713–727.
	
	//Want to implement k-means clustering at some point
	
	//Algorithm pseudo-code
	//http://homepages.inf.ed.ac.uk/rbf/CVonline/LOCAL_COPIES/MORSE/threshold.pdf
	
	//TODO can easily be done on 3D/4D data if several EvPixels given
	
	/**
	 * Otsu thresholding
	 * http://en.wikipedia.org/wiki/Otsu's_method
	 * 
	 * Complexity O(w*h+numColorUsed*log(numColorUsed))
	 */
	public static double findOtsuThreshold(EvPixels in)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		
		int numPixels=in.getWidth()*in.getHeight();
		
		SortedMap<Integer,Integer> hist=new TreeMap<Integer,Integer>(Histogram.intHistogram(in));

		//The goal is to maximize sigma_b

		//Starting value, from lowest index
		int n_b=0;
		int n_o=numPixels;
		double mu_b=0;
		double mu_o=0;
		for(Map.Entry<Integer, Integer> entry:hist.entrySet())
			mu_o+=entry.getKey()*entry.getValue();
		mu_o/=numPixels;

		Iterator<Map.Entry<Integer, Integer>> it=hist.entrySet().iterator();
		Map.Entry<Integer, Integer> curEntry=it.next();
		
		double maxSigma_b=0; //There are no pixels less, multiplication by 0.
		int maxSigmaThres=curEntry.getKey();
		
		int T=curEntry.getKey();
		int n_T=curEntry.getValue();

		//Go through all other values. O(n) by reusing last value
		while(it.hasNext())
			{
			//I think algorithm can be made numerically more stable by using only cumsums. here the paper is followed in detail.
			
			//Jump to next threshold
			int n_b1=n_b+n_T;
			int n_o1=n_o-n_T;
			mu_b=(mu_b*n_b+n_T*T)/n_b1;
			mu_o=(mu_o*n_o-n_T*T)/n_o1;
			n_b=n_b1;
			n_o=n_o1;
			
			//Update iterator
			curEntry=it.next();
			T=curEntry.getKey();
			n_T=curEntry.getValue();

			//Calculate new sigma_b and compare
			double diff=mu_b-mu_o;
			double sigma_b=n_b*n_o*diff*diff;
			if(sigma_b>=maxSigma_b)
				{
				maxSigma_b=sigma_b;
				maxSigmaThres=T;
				}
			
			//System.out.println("nb"+n_b+"   no"+n_o+"  nT"+n_T);
			//System.out.println("sigmab "+sigma_b+"     thr "+T);
			}
		
		return maxSigmaThres;
		}
	
	//The "bible" PDF on thresholding
	// www.busim.ee.boun.edu.tr/~sankur/SankurFolder/Threshold_survey.pdf
	
	/**
	 * Maximum entropy thresholding
	 * TODO Give reference!
	 * TODO test
	 * 
	 * Complexity O(w*h+numColorUsed*log(numColorUsed))
	 */
	public static double findThresholdMaxEntropy(EvPixels in)
		{
		int numPixels=in.getWidth()*in.getHeight();
		
		SortedMap<Integer,Integer> hist=new TreeMap<Integer,Integer>(Histogram.intHistogram(in));
		SortedMap<Integer,Integer> cumHist=Histogram.makeHistCumulative(hist);
		
		
		/**
		 * S = -sum p*log(p)
		 * Goal: Maximize S_bg + S_sig
		 * 
		 * p=numPix/totPix =>
		 * S = - (1/totPix) sum numPix*log(numPix/totPix)
		 * S = - (1/totPix) sum numPix*log(numPix) + (1/totPix) sum numPix*log(totPix)
		 * S = - (1/totPix) sum numPix*log(numPix) + log(totPix)
		 * 
		 * this can be done as a cumulative sum: sum -numPix*log(numPix)
		 * totPix refers to the size of the two S and will be done for each value in the histogram
		 */

		//Create the cumsum for part of S
		SortedMap<Integer,Double> cumS=new TreeMap<Integer,Double>();
		double accum=0;
		for(Map.Entry<Integer, Integer> e:hist.entrySet())
			{
			int value=e.getValue();
			accum+=-value*Math.log(value);
			cumS.put(e.getKey(),accum);
			}
		
		//The total sum of S
		double totalS=cumS.get(cumS.lastKey());

		//Go through, find
		Iterator<Map.Entry<Integer, Integer>> it=hist.entrySet().iterator();
		Iterator<Integer> itCount=cumHist.values().iterator();
		double maxVal=Double.MIN_VALUE;
		double maxThres=0;
		while(it.hasNext())
			{
			Map.Entry<Integer, Integer> curEntry=it.next();
			int curNum=itCount.next();
			
			double curThres=curEntry.getKey();
			double curCumS=curEntry.getValue();
			
			double a=curCumS/curNum + Math.log(curNum);
			int numPixB=numPixels-curNum;
			double b=(totalS-curCumS)/numPixB + Math.log(numPixB); 
			
			double curVal=a+b;
			if(curVal>maxVal)
				{
				maxVal=curVal;
				maxThres=curThres;
				}
			System.out.println(curVal);
			}
		
		return maxThres;
		}
	
	
	///////////////////////////////////////////////////////////////////////
	
	//IJ thresholding methods. 
	//http://www.dentistry.bham.ac.uk/landinig/software/auto_threshold.jar

	
	
	
  //  W. Tsai, "Moment-preserving thresholding: a new approach," Computer Vision,
  // Graphics, and Image Processing, vol. 29, pp. 377-393, 1985.
  // Ported to ImageJ plugin by G.Landini from the the open source project FOURIER 0.8
  // by  M. Emre Celebi , Department of Computer Science,  Louisiana State University in Shreveport
  // Shreveport, LA 71115, USA
  //  http://sourceforge.net/projects/fourier-ipal
  //  http://www.lsus.edu/faculty/~ecelebi/fourier.htm

	
	
	
  // W. Doyle, "Operation useful for similarity-invariant pattern recognition,"
  // Journal of the Association for Computing Machinery, vol. 9,pp. 259-267, 1962.
  // ported to ImageJ plugin by G.Landini from Antti Niemisto's Matlab code (GPL)
  // Original Matlab code Copyright (C) 2004 Antti Niemisto
  // See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation
  // and the original Matlab code.

	
	
	
	
  // Kapur J.N., Sahoo P.K., and Wong A.K.C. (1985) "A New Method for
  // Gray-Level Picture Thresholding Using the Entropy of the Histogram"
  // Graphical Models and Image Processing, 29(3): 273-285
  // M. Emre Celebi
  // 06.15.2007
  // Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines

	
	
	
	
  // Shanhbag A.G. (1994) "Utilization of Information Measure as a Means of
  //  Image Thresholding" Graphical Models and Image Processing, 56(5): 414-419
  // Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines

	
	
	
	
  //  Zack, G. W., Rogers, W. E. and Latt, S. A., 1977,
  //  Automatic Measurement of Sister Chromatid Exchange Frequency,
  // Journal of Histochemistry and Cytochemistry 25 (7), pp. 741-753
  //
  //  modified from Johannes Schindelin plugin
  // 
  // find min and max

	
	
	
	
	
	
	
	 // Implements Yen  thresholding method
  // 1) Yen J.C., Chang F.J., and Chang S. (1995) "A New Criterion 
  //    for Automatic Multilevel Thresholding" IEEE Trans. on Image 
  //    Processing, 4(3): 370-378
  // 2) Sezgin M. and Sankur B. (2004) "Survey over Image Thresholding 
  //    Techniques and Quantitative Performance Evaluation" Journal of 
  //    Electronic Imaging, 13(1): 146-165
  //    http://citeseer.ist.psu.edu/sezgin04survey.html
  //
  // M. Emre Celebi
  // 06.15.2007
  // Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines

	
	
	
	}
