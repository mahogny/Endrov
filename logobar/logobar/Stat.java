package logobar;

/**
 *  Commenced: Friday, October 1, 2004
 *  Author: Asa Perez-Bercoff and Johan Koch
 *  Stat.java contains all the methods for statistical 
 *  calculations of the sequence table
 *
 */

import java.util.*; // Contains the class Vector.
import java.text.*; // Contains the class NumberFormat.

public class Stat
	{

	private int[][] iSeqTable;
	private int iSeqLength = 0;
	private int iNumberOfSeqs = 0;
	private static final double ln2 = Math.log(2.0);
	public static final double log2_21 = log2(21); // Since it's public it can be
																									// accessed from all classes.
	private double iCorrFact;
	private double[][] iFreqTable;
	private double[][] iAAHeightNoCorr;
	private double[][] iAAHeightUseCorr;
	private boolean isInit;
	private Vector[] iGraph;
	private Vector[] iGraphNoCorr;
	private int iMaxHeight;
	private GraphSort iGraphSorter;
	private int iGapsSortStyle;
	private int iGraphSortStyle;
	private boolean iUseCorrection;
	private Filehandler iFilehandler;

	/**
	 * Constructor Creates a Stat object
	 */
	public Stat()
		{
		isInit = false;
		iGapsSortStyle = GraphSort.GAPS_ON_TOP;
		iGraphSortStyle = GraphSort.SORT_DEFAULT;
		iGraphSorter = new GraphSortDefault(); // Default sorting style is gaps on
																						// top
		}

	public boolean isInit()
		{
		return isInit;
		}

	public int getMaxAAEntries()
		{
		return iGraphSorter.getMaxAAEntries();
		}

	public void analyzeFile(Filehandler filehandler) throws Exception
		{
		iFilehandler = filehandler;
		isInit = false;
		iFilehandler.parser();
		iSeqLength = iFilehandler.getSeqLength();
		iSeqTable = iFilehandler.getSeqArray();
		iNumberOfSeqs = iFilehandler.getNumberOfSeqs();
		calcCorrFactor();
		calcFreqData();

		// Get AA height table with correction factor
		double[] infoUseCorr = getInfoContData(true);
		iAAHeightUseCorr = calcHeightOfAAData(infoUseCorr);

		// Get AA height table without correction factor
		double[] infoNoCorr = getInfoContData(false);
		iAAHeightNoCorr = calcHeightOfAAData(infoNoCorr);

		printFreqOfaaTable();
		printHeightOfaaTable();

		iUseCorrection = false;
		GraphTable gtable = iGraphSorter.sort(iSeqLength, iAAHeightNoCorr,
				iFreqTable, iGapsSortStyle);
		iGraphNoCorr = gtable.iGraph;
		iUseCorrection = true;
		calcGraph();
		isInit = true;
		}

	/**
	 * Gets the logarithm of base 2
	 * 
	 * @return Log of base 2
	 */
	public static double log2(double x)
		{
		return (x==0) ? 0 : Math.log(x)/ln2;
		}

	/**
	 * Gets the length of the sequences
	 * 
	 * @return the sequence length
	 */
	public int getSeqLength()
		{
		return iSeqLength;
		}

	public boolean isUsingCorrectionFactor()
		{
		return iUseCorrection;
		}

	public void useCorrectionFactor(boolean enable)
		{
		iUseCorrection = enable;
		}

	public int getNumberOfSeqs()
		{
		return iNumberOfSeqs;
		}

	public double[][] getFreqTable()
		{
		return iFreqTable;
		}

	public double[][] getHeightOfAATable()
		{
		return (iUseCorrection) ? iAAHeightUseCorr : iAAHeightNoCorr;

		}

	public Vector[] getGraphWithNoCorrection()
		{
		return iGraphNoCorr;
		}

	public double[][] getHeightOfAATableNoCorrection()
		{
		return iAAHeightNoCorr;
		}

	private void calcGraph()
		{
		double[][] aaHeightTable = getHeightOfAATable();
		GraphTable gtable = iGraphSorter.sort(iSeqLength, aaHeightTable,
				iFreqTable, iGapsSortStyle);
		iMaxHeight = gtable.iMaxHeight;
		iGraph = gtable.iGraph;
		}

	public int getMaxHeight()
		{
		return iMaxHeight;
		}

	public Vector[] getGraph()
		{
		return iGraph;
		}

	public void setGapsSortStyle(int style)
		{
		if ((style>=1)&&(style<=2))
			{
			iGapsSortStyle = style;
			calcGraph();
			}
		}

	public void setGraphSortStyle(int style)
		{

		switch (style)
			{
			case GraphSort.SORT_DEFAULT:
				iGraphSorter = new GraphSortDefault();
				iGraphSortStyle = style;

				break;
			case GraphSort.SORT_BY_GROUP:
				iGraphSorter = new GraphSortByGroup();
				iGraphSortStyle = style;
				break;
			default:
				break;
			}
		}

	public int getGraphSortStyle()
		{
		return iGraphSortStyle;
		}

	public int getGapsSortStyle()
		{
		return iGapsSortStyle;
		}

	public void update()
		{
		calcGraph();
		// LogoBar.iDrawer.repaint();
		}

	/**
	 * Calculates the correction factor of the information amount calculations
	 */
	private void calcCorrFactor()
		{
		iCorrFact = 20/(2*ln2*iNumberOfSeqs); // 20 comes from (s-1) where s=21 for
																					// 21 equi-probable possibilities.

		}

	/**
	 * Calculates the frequency with which the amino acids occur
	 */
	private void calcFreqData()
		{
		iFreqTable = new double[iSeqLength][27];
		for (int i = 0; i<27; i++)
			{
			for (int j = 0; j<iSeqLength; j++)
				{
				double tmp = (double) iSeqTable[j][i]/(double) iNumberOfSeqs;
				if (tmp<0)
					tmp = 0;
				iFreqTable[j][i] = tmp;
				}
			}
		}

	/**
	 * Calculates the height of the amino acids
	 */
	private double[][] calcHeightOfAAData(double[] infoContArr)
		{

		double[][] aaHeight = new double[iSeqLength][27];
		for (int i = 0; i<27; i++)
			{
			for (int j = 0; j<iSeqLength; j++)
				{
				double tmp = iFreqTable[j][i]*infoContArr[j];
				if (tmp<0)
					tmp = 0;
				aaHeight[j][i] = tmp;
				}
			}
		return aaHeight;
		}

	private double[] getUncertData()
		{
		double[] uncertArr = new double[iSeqLength];
		for (int i = 0; i<iSeqLength; i++)
			{
			double uncertColRes = 0;
			for (int j = 0; j<27; j++)
				{
				uncertColRes += (iFreqTable[i][j]*log2(iFreqTable[i][j]));
				}
			uncertArr[i] = -1*uncertColRes;
			}
		return uncertArr;
		}

	private double[] getInfoContData(boolean useCorrection)
		{
		double[] uncertArr = getUncertData();
		double[] infoContArr = new double[iSeqLength];
		double corr = iCorrFact;
		if (!useCorrection)
			corr = 0;
		for (int i = 0; i<iSeqLength; i++)
			{
			infoContArr[i] = log2_21-(uncertArr[i]+corr);
			}
		return infoContArr;
		}

	// Printing tables to output file:
	// ===============================

	public void printFreqOfaaTable()
		{
		NumberFormat digits = NumberFormat.getInstance();
		NumberFormat integers = NumberFormat.getInstance();
		integers.setMinimumIntegerDigits(2);
		digits.setMaximumFractionDigits(2);
		digits.setMinimumFractionDigits(2);
		iFreqTable = getFreqTable();
		iFilehandler.iPrintOut.println("");
		iFilehandler.iPrintOut.println("Frequency table:");
		iFilehandler.iPrintOut.println("================");
		iFilehandler.iPrintOut.println("");
		iFilehandler.iPrintOut
				.println("                   A    C    D    E    F    G    H    I    K    L    M    N    P    Q    R    S    T    V    W    Y    -    ");
		iFilehandler.iPrintOut.println("");
		for (int i = 0; i<iSeqLength; i++)
			{
			iFilehandler.iPrintOut.print("Position "+integers.format((i+1))+"      "); // Loop
																																									// through
																																									// the
																																									// alaphabet.
			for (int j = 0; j<27; j++)
				{
				double freq = iFreqTable[i][j];
				if (j==1||j==9||j==14||j==20||j==23||j==25)
					{
					}
				else
					{
					iFilehandler.iPrintOut.print(digits.format(freq)+" ");
					}
				}
			iFilehandler.iPrintOut.println("");
			iFilehandler.iPrintOut.println("");
			}
		iFilehandler.iPrintOut.println("");
		iFilehandler.iPrintOut.println("");
		}

	public Filehandler getFilehandler()
		{
		return iFilehandler;
		}

	public void printHeightOfaaTable()
		{
		NumberFormat digits = NumberFormat.getInstance();
		NumberFormat integers = NumberFormat.getInstance();
		integers.setMinimumIntegerDigits(2);
		digits.setMaximumFractionDigits(2);
		digits.setMinimumFractionDigits(2);
		double[][] iaaHeight = getHeightOfAATable();
		iFilehandler.iPrintOut.println("");
		iFilehandler.iPrintOut.println("Height of the amino acids in the LogoBar:");
		iFilehandler.iPrintOut.println("=========================================");
		iFilehandler.iPrintOut.println("");
		iFilehandler.iPrintOut.println("The correction factor is: "
				+digits.format(iCorrFact));
		iFilehandler.iPrintOut.println("");
		iFilehandler.iPrintOut
				.println("                   A    C    D    E    F    G    H    I    K    L    M    N    P    Q    R    S    T    V    W    Y    -    ");
		iFilehandler.iPrintOut.println("");
		for (int i = 0; i<iSeqLength; i++)
			{
			iFilehandler.iPrintOut.print("Position "+integers.format((i+1))+"      "); // Loop
																																									// through
																																									// the
																																									// alphabet.
			for (int j = 0; j<27; j++)
				{
				double height = iAAHeightUseCorr[i][j];
				if (j==1||j==9||j==14||j==20||j==23||j==25)
					{
					}
				else
					{
					String s = digits.format(height);
					if (s.equals("-0.00"))
						s = "0.00";

					iFilehandler.iPrintOut.print(s+" ");
					}
				}
			iFilehandler.iPrintOut.println("");
			iFilehandler.iPrintOut.println("");
			}
		}

	/**
	 * Simple insertion sort.
	 * 
	 * @param a
	 *          an array of comparable items.
	 */
	public void vecSort(Vector vec)
		{
		int vec_len = vec.size();
		int i;
		for (int n = 0; n<vec_len; n++)
			{
			Comparable tmp = (Comparable) vec.elementAt(n);
			for (i = n; i>0&&tmp.compareTo(vec.elementAt(i-1))<0; i--)
				{
				vec.set(i, vec.elementAt(i-1));
				}
			vec.set(i, tmp);
			}
		}
	}
