package logobar;

/**
 *  Commenced: Monday, July 26, 2004
 *  Asa Perez-Bercoff
 *  Filehandler.java contains all the methods for parsing the input
 *  file (the multiple alignment file). It then creates a vector with two columns:
 *  One containing the sequence name and the other one with the actual sequence.
 *  The filehandler also contains a method to save all the statistical data calculated 
 *  and the graph created.

 */

//package logoBar; // All files in the program must be declared to belong to the same package. Otherwise there's a problem when creating the jar file.
import java.io.*; // for the BufferedReader
import java.util.*; // contains the class Vector
import javax.swing.*;

public class Filehandler
	{

	// instance variables:
	private String iName = null;
	private String iPath = null;
	private String iFile = null;
	private String[][] seqStrs;
	private int[][] iSeqLogoArray;
	private int iSeqLength;
	private int iNumberOfSeqs;
	public PrintWriter iPrintOut;

	/**
	 * Format a number to a certain number of digits.
	 * 
	 * @param n
	 *          The number
	 * @param len
	 *          The length of the final string
	 */
	public static String pad(int n, int len)
		{
		String s = Integer.toString(n);
		String topad = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		int slen = s.length();
		len -= slen;
		if (slen<=0)
			return s;
		else if (len<100)
			return topad.substring(0, len)+s;
		else
			{
			StringBuffer sb = new StringBuffer(slen+len+10);
			while (len>0)
				{
				sb.append('0');
				len--;
				}
			sb.append(s);
			return sb.toString();
			}
		}

	/**
	 * Format a number to a certain number of digits.
	 * 
	 * @param n
	 *          The number
	 * @param len
	 *          The length of the final string
	 */
	public static String padSpace(String s, int len)
		{
		// String s=Integer.toString(n);
		String topad = "                                                                                                    ";
		int slen = s.length();
		len -= slen;
		if (slen<=0)
			return s;
		else if (len<100)
			return topad.substring(0, len)+s;
		else
			{
			StringBuffer sb = new StringBuffer(slen+len+10);
			while (len>0)
				{
				sb.append(' ');
				len--;
				}
			sb.append(s);
			return sb.toString();
			}
		}

	// A static instance variable i.e. it's available from all classes of this
	// program.
	// Allows one to write to the output file by just writing to the statical out
	// object defined here in the Filehandler class.
	// public static PrintWriter out = null;
	// static {
	// try{
	// out = new PrintWriter(new BufferedWriter(new FileWriter("output.txt")) );
	// }
	// catch (Exception e){
	// System.out.println(e.toString());
	// }
	// out.println("<!---Beginning of output file!.-->");
	// out.println("");
	// }

	// Constructor:
	// ============

	// Constructor for the openFile method in the Mainwin class:
	public Filehandler(String p, String n)
		{ // Variables from new Filehandler(path, name) in the main class
			// (Mainwin.java).
		this.iPath = p;
		this.iName = n;
		try
			{
			iPrintOut = new PrintWriter(new BufferedWriter(new FileWriter(
					"output.txt")));
			}
		catch (Exception e)
			{
			System.out.println(e.toString());
			}
		iPrintOut.println("Analyzed multiple alignment file: "+n);
		}

	public void closeFile()
		{
		iPrintOut.close();
		}

	// Methods in the Filehandler class:
	// =================================

	public int getSeqLength()
		{
		return iSeqLength;
		}

	public int[][] getSeqArray()
		{
		return iSeqLogoArray;
		}

	public int getNumberOfSeqs()
		{
		return iNumberOfSeqs;
		}

	public void parser() throws Exception
		{

		BufferedReader inFile = new BufferedReader(new FileReader(iPath)); // open
																																				// inFile
		String s;
		Vector vec = new Vector(10, 4); // Initialization of a vector with capacity
																		// 10, which increases with 4 when
																		// necessary.
		s = inFile.readLine(); // Reads in the first line of the alignment (input)
														// file.
		if (!s.startsWith("CLUSTAL"))
			{ // Checks if the input is of the correct format.
			throw new Exception("This is not a CLUSTAL file.");
			}

		while (true)
			{
			s = inFile.readLine(); // Creating a string line by line from the inFile.
															// (Line by line since it's a loop.)
			if (s==null)
				continue;
			if (s.length()==0)
				continue;
			else
				break;
			}

		// We found the first string with information
		s = checkValidString(s);
		int aa; // amino acid i.e. row position in seqLogoArray
		int afterSpace = s.lastIndexOf(" ")+1; // afterSpace finds the position
																						// _after_ the last space
		// after the sequence name, but before the actual sequence.

		while (s!=null)
			{ // As long as the string s isn't empty...
			s = checkValidString(s);
			if (s.length()>0)
				{ // and if the length of the sequence is larger than 0 do:
				if (s.charAt(0)>32)
					{ // If the character at the first position of the string isn't any of
						// the 32 non-printing ones...
					vec.add(s); // ... add the string to the vector.
					}
				}
			else
				break;
			s = inFile.readLine(); // Read the next line of the input file.

			}
		// afterSpace = s.lastIndexOf(" ") + 1;
		iNumberOfSeqs = vec.size();
		String[][] seqStrs = createSeqStringArray(vec, afterSpace);

		// Reads in the remaining groups with aligned sequences.
		s = inFile.readLine();

		while (s!=null)
			{
			s = checkValidString(s);
			int a_space = s.lastIndexOf(" ")+1;

			if (s.indexOf(seqStrs[0][0])>-1)
				{

				seqStrs[1][0] += s.substring(a_space);
				for (int i = 1; i<iNumberOfSeqs; i++)
					{
					s = inFile.readLine();
					s = checkValidString(s);
					seqStrs[1][i] += s.substring(a_space);
					}
				}
			s = inFile.readLine();

			}
		int j = 0;

		// Counting the number of times an amino acid occurs in every column of the
		// aligned input sequences.
		iSeqLength = seqStrs[1][0].length();
		iPrintOut.println("Number of amino acids in the sequences: "+iSeqLength); // Write
																																							// data
																																							// to
																																							// output
																																							// file.
		System.out.println("iSeqLength in the parser method: "+iSeqLength);
		System.out.println("iSeqLength in the parser method: "+iSeqLength);
		iPrintOut.println("Number of aligned sequences: "+iNumberOfSeqs); // Write
																																			// data to
																																			// output
																																			// file.
		iPrintOut.println("");
		iPrintOut.println("");
		iSeqLogoArray = new int[iSeqLength][27]; // Array of arrays (containing
																							// bytes).
		boolean hasIllegal = false;
		for (j = 0; j<iNumberOfSeqs; j++)
			{
			byte[] stringToBytes = seqStrs[1][j].getBytes();
			int k = j+1; // loop nr k
			for (int column = 0; column<iSeqLength; column++)
				{

				if ((stringToBytes[column]=='-'))
					{
					iSeqLogoArray[column][26]++;
					}
				else if (isIllegalChar(stringToBytes[column]))
					{

					hasIllegal = true;
					iSeqLogoArray[column][26]++;
					// continue;
					}
				// In case of lower case letters in the multiple alignment.
				else if (stringToBytes[column]>=97&&stringToBytes[column]<=122)
					{
					stringToBytes[column] -= 97;
					aa = stringToBytes[column];
					iSeqLogoArray[column][aa]++;
					}

				// Upper case letters in the multiple alignment.
				else if (stringToBytes[column]>=65&&stringToBytes[column]<=90)
					{
					stringToBytes[column] -= 65;
					aa = stringToBytes[column];
					iSeqLogoArray[column][aa]++;
					}

				// Throw exception in case of other characters in the multiple alignment
				// file.
				else
					{
					System.out.print("Character: ");
					System.out.print(stringToBytes[column]);
					System.out.print(" in column ");
					System.out.print(column);
					System.out.print(" in row ");
					System.out.print(j);

					System.out.println(" is illegal!!!");
					throw new Exception("File contains unauthorized characters!");
					}
				}
			}
		if (hasIllegal)
			JOptionPane.showMessageDialog(null,
					"File contained b,j,z or x  which\nhas been grouped with Gaps");
		printNumberOfaaTable(); // Call method to print to outfile.

		}

	private boolean isIllegalChar(byte c)
		{

		switch (c)
			{
			case 'B':
				return true;

			case 'b':
				return true;

			case 'J':
				return true;

			case 'j':
				return true;

			case 'X':
				return true;

			case 'x':
				return true;

			case 'Z':
				return true;

			case 'z':
				return true;

			default:
				return false;
			}

		}

	private String[][] createSeqStringArray(Vector vec, int afterSpace)
		{
		String[][] result = new String[2][iNumberOfSeqs]; // Create an array of
																											// arrays (containing
																											// strings) consisting of
																											// 2 columns
		// and as many rows as required (as there are aligned sequences).

		// Reads in the first group of the aligned sequences.
		int j = 0; // This counter will end up being equal to iNumberOfSeqs, as is
								// obvious from the for-loop below.
		for (j = 0; j<iNumberOfSeqs; j++)
			{
			String s = (String) (vec.get(j)); // Getting the strings out of the
																				// vector.

			// Get the sequence name:
			result[0][j] = s.substring(0, s.indexOf(" ")); // Gives the substring that
																											// starts at position 0
			// and ends at the position before space. This
			// substring is saved in the 1st column, row j.
			// Get the sequence:
			result[1][j] = s.substring(afterSpace); // Gives the substring that starts
																							// at the position afterSpace.
			}
		return result;
		}

	private String checkValidString(String str)
		{
		// if(str == null) return str;

		int aSpace = str.lastIndexOf(" ");
		if (aSpace<=0)
			return str;
		if ((aSpace+1)>=str.length()-1)
			return str;
		char tmpCh = str.charAt(aSpace+1);
		if (Character.isDigit(tmpCh))
			{

			// Clustal X file
			// We have a space in the string
			str = str.substring(0, aSpace);
			str.trim();

			}
		return str;
		}

	// Print table to output file.
	// ===========================

	public void printNumberOfaaTable()
		{
		// NumberFormat integers = NumberFormat.getInstance();
		// integers.setMinimumIntegerDigits(4);
		iPrintOut.println("");
		iPrintOut.println("");
		iPrintOut
				.println("Number of amino acids at each position of the alignment:");
		iPrintOut
				.println("========================================================");
		iPrintOut.println('\r');
		// iPrintOut.println("             A    B    C    D    E    F    G    H    I    J    K    L    M    N    O    P    Q    R    S    T    U    V    W    X    Y    Z    -    ");

		String[] chars = new String[]
			{ "A", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N", "P", "Q",
					"R", "S", "T", "V", "W", "Y", "-" };

		int maxCount = 0;
		for (int i = 0; i<iSeqLength; i++)
			for (int j = 0; j<27; j++)
				if (maxCount<iSeqLogoArray[i][j])
					maxCount = iSeqLogoArray[i][j];
		maxCount = 1+(int) Math.ceil(Math.log10(maxCount));

		// iPrintOut.println("Pos.       A    C    D    E    F    G    H    I    K    L    M    N    P    Q    R    S    T    V    W    Y    -    ");

		iPrintOut.print("Pos. ");
		for (String s : chars)
			iPrintOut.print(padSpace(s, maxCount));
		iPrintOut.println();

		iPrintOut.println('\r');
		System.out.println("iSeqLength in the method printNumberOfaaTable: "
				+iSeqLength);

		for (int i = 0; i<iSeqLength; i++)
			{
			iPrintOut.print(pad(i+1, 4)+" "); // Loop through the alphabet.
			for (int j = 0; j<27; j++)
				{
				if (j==1||j==9||j==14||j==20||j==23||j==25)
					{
					}
				else
					{
					String s = ""+iSeqLogoArray[i][j];
					// if(s.equals("-0.00"))
					// s="0.00";
					iPrintOut.print(padSpace(s, maxCount));
					// iPrintOut.print(s+"    ");
					}
				}
			iPrintOut.println("");
			iPrintOut.println("");
			}
		iPrintOut.println("");
		iPrintOut.println("");
		}

	}
