package logobar;

/** Modified version of Marty Hall's DrawingPanel.java
 *  Gets the table with the aa heights, and creates a consensus sequence
 *  and a graph made out of bars.
 *  Asa Perez-Bercoff
 */

//package logoBar; // All files in the program must be declared to belong to the same package. Otherwise there's a problem when creating the jar file.
import java.awt.*; //import org.jibble.epsgraphics.*; // Java EPS Graphics2D package obtained at http://www.jibble.org/epsgraphics/
import javax.swing.*;
import java.text.*; // Contains the class NumberFormat.
import java.lang.String;

public class Drawer extends JPanel
	{

	private int iSeqLength;
	private Stat iStat;
	private char[] iConsensusArray;
	private JScrollPane iScrollPaneLeft;
	private JScrollPane iScrollPaneRight;
	private float iScale = 0.5f;
	private int iFontSize = 14;
	// private Legend iLegend;
	// private LegendDlg iLegendDlg;
	public double halfOflog2_21 = iStat.log2_21/2;
	private GraphPanel iGraphPanel;
	public static final int Y_GRAPH_START = 250;

	public Drawer(Stat stat)
		{
		// super(JSplitPane.HORIZONTAL_SPLIT);
		this.setBackground(Color.white);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		iStat = stat;
		addGraphPanel();
		}

	private void addGraphPanel()
		{
		Dimension dim = this.getSize();
		int height = dim.height;
		int width = dim.width;

		iGraphPanel = new GraphPanel(this, iStat);
		iScrollPaneLeft = new JScrollPane(iGraphPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		iScrollPaneLeft.setPreferredSize(new Dimension(width, height));

		add(iScrollPaneLeft);

		}

	public int getFontSize()
		{
		return iFontSize;
		}

	public void setFontSize(int val)
		{
		iFontSize = val;
		}

	public int reverseY(int y)
		{
		int new_y = getHeight()-y;
		if (new_y<0)
			new_y = 0;
		return new_y;

		}

	public void zoomIn()
		{
		iScale += 0.15f;
		repaint();
		}

	public void zoomOut()
		{
		if ((double) iScale>0.3f)
			{
			iScale -= 0.15f;
			repaint();
			}
		}

	public float getScale()
		{
		return iScale;
		}

	public void update()
		{
		iStat.update();
		iGraphPanel.update();
		updateUI();
		}

	// Method that draws the logobar graph to an eps file using the
	// org.jibble.epsgraphics package.
	public void saveEpsFile(String filePath)
		{

		SaveAsEPS.saveEpsFile(iGraphPanel, filePath, this, iStat, iConsensusArray);
		}

	public char[] getConsArray()
		{
		return iConsensusArray;
		}

	// Gives the consensus sequence of the sequence logo.
	public char[] getConsensus()
		{

		NumberFormat nf = NumberFormat.getInstance();
		NumberFormat digits = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		iSeqLength = iStat.getSeqLength();
		double[][] heightAATable = iStat.getHeightOfAATableNoCorrection();
		iConsensusArray = new char[iSeqLength];
		for (int i = 0; i<iSeqLength; i++)
			{
			digits.setMinimumIntegerDigits(2);
			double max = 0;
			int aaIdx = 0;
			for (int j = 0; j<27; j++)
				{
				double tmp = heightAATable[i][j];
				if (tmp>=halfOflog2_21)
					{ // log2_21 / 2 is approx. 2.196
					max = tmp;
					aaIdx = j;
					break;
					}
				else if (tmp>=max)
					{
					max = tmp;
					aaIdx = j;
					}
				}
			int char_sym = 0;
			if (max<=0)
				char_sym = 32;
			else if (aaIdx==26)
				char_sym = 45;
			else
				char_sym = aaIdx+65;
			iConsensusArray[i] = (char) char_sym;
			}

		Filehandler filehandler = iStat.getFilehandler();

		// Printing the consensus sequence to output file.
		filehandler.iPrintOut.println("");
		filehandler.iPrintOut.println("");
		filehandler.iPrintOut.println("Consensus sequence:");
		filehandler.iPrintOut.println("===================");
		filehandler.iPrintOut.println("");
		for (int i = 0; i<iSeqLength; i++)
			{
			filehandler.iPrintOut.print(iConsensusArray[i]+"  ");
			}
		filehandler.iPrintOut.println("");
		filehandler.iPrintOut.println("");
		return iConsensusArray;
		}

	}

/**
 * $Log: not supported by cvs2svn $ Revision 1.8 2006/01/09 12:23:49 johan Fixed: Output
 * file creation bug. Now a new output.txt is created every time a new file is
 * loaded. Revision 1.7 2005/08/12 14:20:05 johan Major revision: Added WegLogo
 * style graph support Revision 1.6 2005/08/03 15:39:16 johan Added: Mirrored
 * letter graph. Revision 1.5 2005/08/03 08:37:17 johan Changed: This class is
 * now divided into two windows, graph and color legend. Hence this class has
 * more become a controller class Revision 1.4 2005/06/10 12:02:33 johan Added:
 * Font size modification
 * ----------------------------------------------------------------------
 * Revision 1.3 2005/05/12 16:00:58 johan Changed: All files rescude after JAR
 * incident..
 * ----------------------------------------------------------------------
 * Revision 1.2 2005/04/18 23:33:38 johan Modified Files: ColorDlg.java
 * Drawer.java GraphSort.java GraphSortByGroup.java LogoBar.java Stat.java Added
 * Files: GraphSortDefault.java SortDlg.java Removed Files:
 * GraphSortGapsOnTop.java
 * ----------------------------------------------------------------------
 * Revision 1.1.1.1 2005/04/12 12:58:04 johan Started LogoBar project Revision
 * 1.9 2005/01/11 15:37:15 pbasa Modified Files: Added halfOflog2_21 Drawer.java
 * ----------------------------------------------------------------------
 * Revision 1.8 2004/12/09 16:34:12 pbasa Modified Files: in saveEpsFile added
 * option of removing the dot from file name Drawer.java
 * ----------------------------------------------------------------------
 * Revision 1.7 2004/12/03 14:02:05 pbasa Added: Save EPS file function
 * ----------------------------------------------------------------------
 * Revision 1.6 2004/11/22 17:06:24 pbasa Modified Files: Drawer.java
 * LogoEntry.java NameFilter.java Added Files: LogoBar.java Added: Printing
 * function and exit from file menu.
 * ----------------------------------------------------------------------
 * Revision 1.4 2004/11/18 14:13:17 pbasa Changed: paint() now operates on the
 * vector of position objects
 * ----------------------------------------------------------------------
 * Revision 1.3 2004/11/17 10:28:08 pbasa Changed: Added insertionSort
 * ----------------------------------------------------------------------
 * Revision 1.2 2004/11/16 16:10:33 pbasa Changed: Added Log message
 * ----------------------------------------------------------------------
 */
