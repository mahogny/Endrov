package logobar;

/** 
 *  Monday, July 26, 2004
 *  Asa Perez-Bercoff
 *  This is the main file (Mainwin.java) it calls Parser.java where
 *  all string manipulation occur, and where a vector is created.
 *  It also calls all the necessary files to create the graphical
 *  output.
 */

//package logoBar; // All files in the program must be declared to belong to the same package. Otherwise there's a problem when creating the jar file.
import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // For the accelerator

public class LogoBar extends JFrame
	{
	
  public static boolean canSaveEps()
  	{
  	try
			{
			Class applicationClass = Class.forName("org.jibble.epsgraphics.EpsGraphics2D");
			if(applicationClass==null)
				return false;
			}
		catch (ClassNotFoundException e1)
			{
			return false;
			}
		return true;
  	}

	public static void main(String[] args)
		{
		new LogoBar();
		}

	// instance variables:
	private static Drawer iDrawer;
	private static Stat iStat;

	public static ColorHandler iColorHandler;
	public static Preferences iPref;
	private static LegendDlg iLegendDlg;
	JMenuBar aMenuBar;
	JMenu fileMenu, optionsMenu, graphStyleMenu, aboutMenu;
	JMenuItem openMenuItem, saveMenuItem, printMenuItem, exitMenuItem,
			noBlocksItem, blocksItem;
	JScrollPane iScrollPane;
	JFileChooser iFileChooser;
	NameFilter iFileFilter1, iFileFilter2,iFileFilterEps;

	
	
	
	public LogoBar()
		{ 
		int META_Key = 2;
		int META_n_SHIFT_Key = 3;

		if (MacHack.isMac())
			{
			META_Key = 4;
			META_n_SHIFT_Key = 5;
			}

		// create and connect all components
		setTitle("LogoBar");
		aMenuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		optionsMenu = new JMenu("Options");

		graphStyleMenu = new JMenu("Graph style");
		fileMenu.setMnemonic('F'); // short cuts for the menu
		openMenuItem = new JMenuItem("Open file...", 'O');
		saveMenuItem = new JMenuItem("Save image", 'S');
		// printMenuItem = new JMenuItem("Print...", 'P');
		JMenu viewMenu = new JMenu("View");
		JMenuItem colorLegendItem = new JMenuItem("Color Legend");
		viewMenu.add(colorLegendItem);

		// Alternative way to reach a menu post in the constructor. Below are the
		// accelerators of the menu posts.
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, META_Key,
				false));
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, META_Key,
				false));
		// printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
		// META_Key, false));
		exitMenuItem = new JMenuItem("Exit", 'Q');
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, META_Key,
				false));
		aMenuBar.add(fileMenu);
		aMenuBar.add(optionsMenu);
		aMenuBar.add(viewMenu);
		fileMenu.add(openMenuItem);
		if(canSaveEps())
			fileMenu.add(saveMenuItem);
		// fileMenu.add(printMenuItem);
		fileMenu.add(exitMenuItem);

		optionsMenu.add(graphStyleMenu);

		JMenuItem colorSettings = new JMenuItem("Color settings...");
		optionsMenu.add(colorSettings);

		JMenuItem sortSettings = new JMenuItem("Graph settings...");
		optionsMenu.add(sortSettings);

		JMenuItem zoomInItem = new JMenuItem("Zoom In");
		zoomInItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				META_n_SHIFT_Key, false));

		JMenuItem zoomOutItem = new JMenuItem("Zoom Out");
		zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, META_Key,
				false));
		optionsMenu.add(zoomInItem);
		optionsMenu.add(zoomOutItem);

		iPref = new Preferences();

		noBlocksItem = new JRadioButtonMenuItem("Continuous", true);
		blocksItem = new JRadioButtonMenuItem("Block divided", false);
		graphStyleMenu.add(noBlocksItem);
		graphStyleMenu.add(blocksItem);
		ButtonGroup bg = new ButtonGroup(); // Creates a group for the options
																				// continious or block divided.
		bg.add(noBlocksItem);
		bg.add(blocksItem); // Puts the two options in the same group.

		// ActionListeners that calls the methods chosen from the file menus.

		// ----- File Menu -----//
		openMenuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					openFile();
					}
			});

		saveMenuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					saveFile();
					}
			});
		exitMenuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					exit();
					}
			});

		// ----- Options Menu ----- //
		noBlocksItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					setGraphType(false);
					}
			});
		blocksItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					setGraphType(true);
					}
			});
		colorSettings.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					openColorSettingsDlg();
					}
			});

		sortSettings.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					openSortSettingsDlg();
					}
			});

		zoomInItem.addActionListener(new ActionListener()
			{

				public void actionPerformed(ActionEvent e)
					{
					zoomIn();
					}

			});
		zoomOutItem.addActionListener(new ActionListener()
			{

				public void actionPerformed(ActionEvent e)
					{
					zoomOut();
					}

			});

		colorLegendItem.addActionListener(new ActionListener()
			{

				public void actionPerformed(ActionEvent e)
					{
					showLegendDlg();
					}

			});

		// ----- FileChooser settings ----- //
		iFileChooser = new JFileChooser();
		iFileFilter1 = new NameFilter(".aln");
		iFileFilter2 = new NameFilter(".txt");
		iFileFilterEps = new NameFilter(".eps");
		// iFileChooser.addChoosableFileFilter(iFileFilter1);
		// iFileChooser.addChoosableFileFilter(iFileFilter2);
		iFileChooser.setFileFilter(iFileFilter1);

		iStat = new Stat();
		iDrawer = new Drawer(iStat);
		this.setContentPane(iDrawer);
		this.setJMenuBar(aMenuBar);
		pack();

		if (MacHack.isMac())
			MacHack.addMacAbout();
		else
			addAbout();

		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		iLegendDlg = new LegendDlg(this);
		// iLegendDlg.setJMenuBar(aMenuBar);
		// this.getContentPane().add(iLegendDlg);
		iLegendDlg.setJMenuBar(null);
		// iLegendDlg.show();
		showLegendDlg();
		iLegendDlg.setLocation(300, 50);

		}

	public void addAbout()
		{
		aboutMenu = new JMenu("About");
		JMenuItem aboutItem = new JMenuItem("About LogoBar");
		aboutMenu.add(aboutItem);
		aMenuBar.add(aboutMenu);
		aboutItem.addActionListener(new ActionListener()
			{

				public void actionPerformed(ActionEvent e)
					{
					showAboutWindow();
					}

			});

		}

	public void openFile()
		{
		int result = iFileChooser.showOpenDialog(null);
		if (result==JFileChooser.APPROVE_OPTION)
			{
			String fileName = iFileChooser.getSelectedFile().getName(); // name of
																																	// file
			String filePath = iFileChooser.getSelectedFile().getAbsolutePath(); // path
																																					// to
																																					// file
			// Filehandler.out.println("Analyzed multiple alignment file: " +
			// fileName);
			Filehandler filehandler;
			try
				{
				filehandler = new Filehandler(filePath, fileName);
				iStat.analyzeFile(filehandler);
				}
			catch (Exception exception)
				{
				JOptionPane.showMessageDialog(null, "Cannot open "+fileName+" Error: "
						+exception.toString());
				return;
				}
			iDrawer.repaint();
			iDrawer.getConsensus();
			// Filehandler.out.println();
			// Filehandler.out.println("<!---End of output file!.-->");
			System.out.println("Output file created!");
			filehandler.closeFile(); // Closes the output file.
			}
		}

	public void showLegendDlg()
		{
		iLegendDlg.show();
		}

	public void printFile()
		{
		PrintUtilities.printComponent(iDrawer);
		}

	public void saveFile()
		{
		JFileChooser fileChooser = new JFileChooser();
		NameFilter filter = new NameFilter(".eps");
		fileChooser.setFileFilter(iFileFilterEps);
		if (fileChooser.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
			{
			iDrawer.saveEpsFile(fileChooser.getSelectedFile().getAbsolutePath());
			}
		}

	public void exit()
		{
		System.exit(0); // Closes the program when the user clicks on 'Exit' in the
										// file menu.
		}

	public void setGraphType(boolean enable)
		{
		iPref.iIsBlockDivided = enable;
		updateGraph();
		}

	public void openColorSettingsDlg()
		{
		ColorDlg dlg = new ColorDlg();
		dlg.show();
		}

	public void openSortSettingsDlg()
		{
		SortDlg dlg = new SortDlg();
		dlg.show();
		}

	public static void updateGraph()
		{
		iDrawer.update();
		iLegendDlg.update();
		iLegendDlg.repaint(5);
		iLegendDlg.paint();
		iLegendDlg.repaint();
		// repaint();
		}

	public static Stat getStat()
		{
		return iStat;
		}

	public void zoomIn()
		{
		iDrawer.zoomIn();
		}

	public void zoomOut()
		{
		iDrawer.zoomOut();
		}

	public static void setFontSize(int val)
		{
		iDrawer.setFontSize(val);
		}

	public static int getFontSize()
		{
		return iDrawer.getFontSize();
		}

	public static void showAboutWindow()
		{
		AboutWindow aWindow = new AboutWindow();
		aWindow.show();
		}
	}

/**
 * $Log: not supported by cvs2svn $ Revision 1.9 2006/04/13 14:44:06 johan Modified Files:
 * LogoBar.java Revision 1.8 2006/01/09 12:23:49 johan Fixed: Output file
 * creation bug. Now a new output.txt is created every time a new file is
 * loaded. Revision 1.7 2005/08/03 08:38:02 johan Changed: LogoBar.java
 * PaintStyle.java SortDlg.java Stat.java Revision 1.6 2005/06/10 12:02:33 johan
 * Added: Font size modification
 * ----------------------------------------------------------------------
 * Revision 1.5 2005/05/12 16:00:58 johan Changed: All files rescude after JAR
 * incident..
 * ----------------------------------------------------------------------
 * Revision 1.4 2005/04/19 11:37:05 johan Changed: The sort dlg is complete
 * ----------------------------------------------------------------------
 * Revision 1.3 2005/04/18 23:33:38 johan Modified Files: ColorDlg.java
 * Drawer.java GraphSort.java GraphSortByGroup.java LogoBar.java Stat.java Added
 * Files: GraphSortDefault.java SortDlg.java Removed Files:
 * GraphSortGapsOnTop.java
 * ----------------------------------------------------------------------
 * Revision 1.2 2005/04/18 16:05:18 johan Added: ColorDlg
 * ----------------------------------------------------------------------
 * Revision 1.1.1.1 2005/04/12 12:58:15 johan Started LogoBar project Revision
 * 1.5 2005/01/26 15:46:45 pbasa Changed: Default color for GAPS is now set to
 * white ---------------------------------------------------------------------
 * Revision 1.4 2004/12/21 09:42:47 pbasa *** empty log message ***
 */
