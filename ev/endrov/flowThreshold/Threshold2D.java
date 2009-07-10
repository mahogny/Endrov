package endrov.flowThreshold;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.math.EvOpImageGreaterThanScalar;
import endrov.imageset.EvPixels;

/**
 * Find threshold given image statistics. Threshold is pixel>=thresholdValue. This class is not for end-users.
 * 
 * TODO one other class that works on stack level?
 * 
 * @author Johan Henriksson
 *
 */
public abstract class Threshold2D extends EvOpSlice1
	{
	protected abstract double getThreshold(EvPixels in);
	
	
	public EvPixels exec1(EvPixels... p)
		{
		EvPixels in=p[0];
		double thres=getThreshold(in);
		EvPixels out=new EvOpImageGreaterThanScalar(thres).exec1(in);
		System.out.println("========= out");
		return out;
		}
	
	
	
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

	
	
	/**
	 * http://pacific.mpi-cbg.de/wiki/index.php/Auto_Threshold
	 * 
	 * 
	 *  Huang

Implements Huang's fuzzy thresholding method. This uses Shannon's entropy function (one can also use Yager's entropy function).

    * Huang, L-K & Wang, M-J J (1995), "Image thresholding by maximizing the index of nonfuzziness of the 2-D grayscale histogram", Pattern Recognition 28(1): 41-51, <http://portal.acm.org/citation.cfm?id=638891> 

Ported from ME Celebi's fourier_0.8 routines [1] and [2].


[edit] Intermodes

This assumes a bimodal histogram. The histogram is iteratively smoothed using a running average of size 3, until there are only two local maxima: j and k. The threshold t is then computed as (j+k)/2. Images with histograms having extremely unequal peaks or a broad and ﬂat valley are unsuitable for this method. method

    * Prewitt, JMS & Mendelsohn, ML (1966), "The analysis of cell images", Annals of the New York Academy of Sciences 128: 1035-1053, <http://www3.interscience.wiley.com/journal/119758871/abstract?CRETRY=1&SRETRY=0> 

Ported from Antti Niemistö's Matlab code. See here for an excellent slide presentation and the original Matlab code.


[edit] IsoData

Iterative procedure based on the isodata algorithm of:

    * Ridler, TW & Calvard, S (1978), "Picture thresholding using an iterative selection method", IEEE Transactions on Systems, Man and Cybernetics 8: 630-6032, <http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=4310039> 

This option should return the same values as the Image>Adjust>Threshold>Auto, when selecting ignore black and ignore white. ImageJ tries to guess which pixels belong to the object and which to the background, but now this can be overridden and force segmentation of the desired phase.

The procedure divides the image into object and background by taking an initial threshold, then the averages of the pixels at or below the threshold and pixels above are computed. The averages of those two values are computed, the threshold is incremented and the process is repeated until the threshold is larger than the composite average. That is,

threshold = (average background + average objects)/2. 

The code in ImageJ that implements this function is the getAutoThreshold() method in the ImageProcessor class. This plugin uses ImageJ implementation of the IsoData method (also known as iterative intermeans).


[edit] Li

Implements Li's Minimum Cross Entropy thresholding method based on the iterative version (2nd reference below) of the algorithm.

    * Li, CH & Lee, CK (1993), "Minimum Cross Entropy Thresholding", Pattern Recognition 26(4): 617-625 

    * Li, CH & Tam, PKS (1998), "An Iterative Algorithm for Minimum Cross Entropy Thresholding", Pattern Recognition Letters 18(8): 771-776 

    * Sezgin, M & Sankur, B (2004), "Survey over Image Thresholding Techniques and Quantitative Performance Evaluation", Journal of Electronic Imaging 13(1): 146-165, <http://citeseer.ist.psu.edu/sezgin04survey.html> 

Ported from ME Celebi's fourier_0.8 routines [3] and [4].


[edit] MaxEntropy

Implements Kapur-Sahoo-Wong (Maximum Entropy) thresholding method:

    * Kapur, JN; Sahoo, PK & Wong, ACK (1985), "A New Method for Gray-Level Picture Thresholding Using the Entropy of the Histogram", Graphical Models and Image Processing 29(3): 273-285 

Ported from ME Celebi's fourier_0.8 routines [5] and [6].


[edit] Mean

Uses the mean of grey levels as the trhreshold. It is used by some other methods as a first guess threshold.

    * Glasbey, CA (1993), "An analysis of histogram-based thresholding algorithms", CVGIP: Graphical Models and Image Processing 55: 532-537 


[edit] MinError(I)

An iterative implementation of Kittler and Illingworth's Minimum Error thresholding. This seems to converge more often than the original implementation.

Nevertheless, sometimes the algorithm does not converge to a solution. In that case a warning is reported to the log window. The Ignore black or Ignore white options might help to avoid this problem.

    * Kittler, J & Illingworth, J (1986), "Minimum error thresholding", Pattern Recognition 19: 41-47 

Ported from Antti Niemistö's Matlab code. See here for an excellent slide presentation and the original Matlab code.


[edit] Minimum

Similarly to the Intermodes method, this assumes a bimodal histogram. The histogram is iteratively smoothed using a running average of size 3, until there are only two local maxima. The threshold t is such that yt−1 > yt <= yt+1.

Images with histograms having extremely unequal peaks or a broad and ﬂat valley are unsuitable for this method.

    * Prewitt, JMS & Mendelsohn, ML (1966), "The analysis of cell images", Annals of the New York Academy of Sciences 128: 1035-1053, <http://www3.interscience.wiley.com/journal/119758871/abstract?CRETRY=1&SRETRY=0> 

Ported from Antti Niemistö's Matlab code. See here for an excellent slide presentation and the original Matlab code.


[edit] Moments

Tsai's method attempts to preserve the moments of the original image in the thresholded result.

    * Tsai, W (1985), "Moment-preserving thresholding: a new approach", Computer Vision, Graphics, and Image Processing 29: 377-393, <http://portal.acm.org/citation.cfm?id=201578> 

Ported from ME Celebi's fourier_0.8 routines [7] and [8].


[edit] Otsu

Otsu's threshold clustering algorithm searches for the threshold that minimizes the intra-class variance, defined as a weighted sum of variances of the two classes.

    * Otsu, N (1979), "A threshold selection method from gray-level histograms", IEEE Trans. Sys., Man., Cyber. 9: 62-66, doi:10.1109/TSMC.1979.4310076, <http://ieeexplore.ieee.org/xpl/freeabs_all.jsp?&arnumber=4310076> 

See also the Wikipedia article on Otsu's method.

Ported from C++ code by Jordan Bevik.


[edit] Percentile

Assumes the fraction of foreground pixels to be 0.5.

    * Doyle, W (1962), "Operation useful for similarity-invariant pattern recognition", Journal of the Association for Computing Machinery 9: 259-267, doi:10.1145/321119.321123, <http://portal.acm.org/citation.cfm?id=321119.321123> 

Ported from Antti Niemistö's Matlab code. See here for an excellent slide presentation and the original Matlab code.


[edit] RenyiEntropy

Similar to the MaxEntropy method, but using Renyi's entropy instead.

    * Kapur, JN; Sahoo, PK & Wong, ACK (1985), "A New Method for Gray-Level Picture Thresholding Using the Entropy of the Histogram", Graphical Models and Image Processing 29(3): 273-285 

Ported from ME Celebi's fourier_0.8 routines [9] and [10].


[edit] Shanbhag

Shanbhag, Abhijit G. (1994), "Utilization of information measure as a means of image thresholding", Graph. Models Image Process. (Academic Press, Inc.) 56 (5): 414--419, ISSN 1049-9652, DOI 10.1006/cgip.1994.1037

Ported from ME Celebi's fourier_0.8 routines [11] and [12].


[edit] Triangle

This is an implementation of the Triangle method:

    * Zack GW, Rogers WE, Latt SA (1977), "Automatic measurement of sister chromatid exchange frequency", J. Histochem. Cytochem. 25 (7): 741–53, PMID 70454, <http://www.jhc.org/cgi/pmidlookup?view=long&pmid=70454> 

Incorporated from from Johannes Schindelin plugin Triangle_Algorithm.

See also: http://www.ph.tn.tudelft.nl/Courses/FIP/noframes/fip-Segmenta.html#Heading118

The Triangle algorithm, a geometric method, cannot tell whether the data is skewed to one side or another, but assumes a maximum peak (mode) near one end of the histogram and searches towards the other end. This causes a problem in the absence of information of the type of image to be processed, or when the maximum is not near one of the histogram extremes (resulting in two possible threshold regions between that max and the extremes). The algorithm was extended to find out on which side of the max peak the data goes the furthest, and searches the threshold in largest range.


[edit] Yen

Implements Yen's thresholding method from:

    * Yen JC, Chang FJ, Chang S (1995), "A New Criterion for Automatic Multilevel Thresholding", IEEE Trans. on Image Processing 4 (3): 370-378, ISSN 1057-7149, doi:10.1109/83.366472, <http://ieeexplore.ieee.org/xpl/freeabs_all.jsp?arnumber=366472> 

    * Sezgin, M & Sankur, B (2004), "Survey over Image Thresholding Techniques and Quantitative Performance Evaluation", Journal of Electronic Imaging 13(1): 146-165, <http://citeseer.ist.psu.edu/sezgin04survey.html> 

Ported from ME Celebi's fourier_0.8 routines [13] and [14]. 
	 * 
	 * 
	 * 
	 */
	
	}