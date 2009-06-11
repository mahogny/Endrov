package endrov.unsortedImageFilters.threshold;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.imageMath.EvOpImageGreaterThanScalar;

/**
 * Find threshold given image statistics. Threshold is pixel>=thresholdValue. This class is not for end-users.
 * 
 * TODO one other class that works on stack level?
 * 
 * @author Johan Henriksson
 *
 */public abstract class EvOpThreshold2D extends EvOpSlice1
	{
	public abstract double getThreshold(EvPixels in);
	public EvPixels exec1(EvPixels... p)
		{
		EvPixels in=p[0];
		double thres=getThreshold(in);
		return new EvOpImageGreaterThanScalar(thres).exec1(in);
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