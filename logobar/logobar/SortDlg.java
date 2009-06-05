package logobar;


import javax.swing.*;
import javax.swing.JSpinner;
import java.awt.*;
import java.awt.event.*;


public class SortDlg extends JDialog implements ActionListener {
    
    private JPanel iMainPanel = null;
    private JPanel iBarStylePanel = null;
    private JPanel iGapsPanel = null;
    private JPanel iSortPanel = null;
    private JPanel iFontPanel = null;
    private JPanel iMiscPanel = null;
    private JPanel iLetterGraphPanel = null;
    private JRadioButton iGapsTopBt    = null;
    private JRadioButton iGapsBottomBt = null;
    private JRadioButton iSortDefault  = null;
    private JRadioButton iSortByGroup  = null;
    private JCheckBox iOnlyMostConserved = null;
    private JCheckBox iLettersAtBottom = null;
    private JCheckBox iLetterGraph = null;
    private JCheckBox iUseCorrectionFactor = null;
    private int          iGapsStyle;
    private int          iGraphStyle;
    private JSpinner     iFontSize = null;
    private JSpinner     iBlockSize = null;
    private JSpinner     iFreqCutoff = null;
    
    public SortDlg() {
	super();
	initialize();
	
    }
    
    private void initialize(){
	Stat stat   = LogoBar.getStat();
	iGapsStyle  = stat.getGapsSortStyle();
	iGraphStyle = stat.getGraphSortStyle();

	this.setSize(310,590);
	this.setResizable(false);
	this.setContentPane(getMainPane());
	iFontSize.setValue(new Integer(10));
	iFontSize.setValue(new Integer(LogoBar.iPref.iFontSize));
//iFontSize.setMinimumSize(new Dimension(50,32));
	this.repaint();
    }

    private JPanel getMainPane(){
	
	if(iMainPanel == null){
	    
	    iMainPanel = new JPanel();
	    iMainPanel.setLayout(new FlowLayout());
	    iMainPanel.add(getBarStylePanel());
	    iMainPanel.add(getGapsPanel());
	    iMainPanel.add(getSortPanel());
	    iMainPanel.add(getFontPanel());
	    iMainPanel.add(getLetterGraphPanel());
	    iMainPanel.add(getMiscPanel());
	    iMainPanel.add(getOkPanel());
	    
	}
	return iMainPanel;
    }
    private JPanel getOkPanel(){
	JPanel okPanel = new JPanel();
	okPanel.setPreferredSize(new Dimension(290,40));
	okPanel.setLayout(new BoxLayout(okPanel, BoxLayout.LINE_AXIS));
	
	JButton okButton = new JButton();
	okButton.setText("OK");
	okButton.setActionCommand("ok");
	
	JButton cancelButton = new JButton();
	cancelButton.setText("Cancel");
	cancelButton.setActionCommand("cancel");
	
	okButton.addActionListener(this);
	cancelButton.addActionListener(this);

	okPanel.add(Box.createHorizontalGlue());
	okPanel.add(cancelButton);
	okPanel.add(okButton);
	return okPanel;
    }
   
    private JPanel getGapsPanel(){
	if(iGapsPanel == null){
	    iGapsPanel = new JPanel();
	    //iGapsPanel.setLayout(new BoxLayout(iGapsPanel, BoxLayout.X_AXIS));
	    iGapsPanel.setPreferredSize(new Dimension(270,70));
	    iGapsPanel.setBorder(BorderFactory.createTitledBorder("Placement of Gaps in graph"));
	    iGapsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	    iGapsTopBt = new JRadioButton("On top");
	    iGapsTopBt.setActionCommand("Gaps on top");
	    iGapsTopBt.addActionListener(this);

	    iGapsBottomBt = new JRadioButton("At bottom");
	    iGapsBottomBt.setActionCommand("Gaps at bottom");
	    iGapsBottomBt.addActionListener(this);

	    ButtonGroup gaps_gr = new ButtonGroup();
	    gaps_gr.add(iGapsTopBt);
	    gaps_gr.add(iGapsBottomBt);
	    
	    if(iGapsStyle == GraphSort.GAPS_ON_TOP)
		iGapsTopBt.setSelected(true);
	    else
		iGapsBottomBt.setSelected(true);
	    
	    
	    iGapsPanel.add(iGapsTopBt);
	    iGapsPanel.add(iGapsBottomBt);

	}
	return iGapsPanel;
    }
    
     private JPanel getBarStylePanel(){
	if(iBarStylePanel == null){
	    iBarStylePanel = new JPanel();
	    //iBarStylePanel.setLayout(new BoxLayout(iBarStylePanel, BoxLayout.X_AXIS));
	    iBarStylePanel.setPreferredSize(new Dimension(270,70));
	    iBarStylePanel.setBorder(BorderFactory.createTitledBorder("Graph bar style"));
	    iBarStylePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	    
	    JRadioButton logoStyle = new JRadioButton("LogoBar");
	    logoStyle.setActionCommand("LogoBar style");
	    logoStyle.addActionListener(this);

	    JRadioButton letterStyle = new JRadioButton("Traditional (letters)");
	    letterStyle.setActionCommand("Letter style");
	    letterStyle.addActionListener(this);

	    ButtonGroup gaps_gr = new ButtonGroup();
	    gaps_gr.add(logoStyle);
	    gaps_gr.add(letterStyle);
	    
	    if(!LogoBar.iPref.iShowWebLogo)
		logoStyle.setSelected(true);
	    else
		letterStyle.setSelected(true);
	    	    
	    iBarStylePanel.add(logoStyle);
	    iBarStylePanel.add(letterStyle);

	}
	return iBarStylePanel;
    }


     private JPanel getSortPanel(){
	if(iSortPanel == null){
	    iSortPanel = new JPanel();
	    //iSortPanel.setLayout(new BoxLayout(iSortPanel, BoxLayout.X_AXIS));
	    iSortPanel.setPreferredSize(new Dimension(270,70));
	    iSortPanel.setBorder(BorderFactory.createTitledBorder("Graph sort style"));
	    iSortPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	    iSortDefault = new JRadioButton("By amino acid");
	    iSortDefault.setActionCommand("Sort default");
	    iSortDefault.addActionListener(this);
	    
	    iSortByGroup = new JRadioButton("By group");
	    iSortByGroup.setActionCommand("Sort group");
	    iSortByGroup.addActionListener(this);
	    
	    ButtonGroup sort_gr = new ButtonGroup();
	    sort_gr.add(iSortDefault);
	    sort_gr.add(iSortByGroup);
	    
	    if(iGraphStyle == GraphSort.SORT_DEFAULT)
		iSortDefault.setSelected(true);
	    else
		iSortByGroup.setSelected(true);
	    iSortPanel.add(iSortDefault);
	    iSortPanel.add(iSortByGroup);
	   
	    
	    
	    
	}
	return iSortPanel;
    }
    
    private JPanel getFontPanel(){
	if(iFontPanel == null){
	    iFontPanel = new JPanel();
	    //iFontPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	    //iSortPanel.setLayout(new BoxLayout(iSortPanel, BoxLayout.X_AXIS));
	    iFontPanel.setPreferredSize(new Dimension(270,70));
	    iFontPanel.setBorder(BorderFactory.createTitledBorder("Font"));
	    
	    JLabel lab = new JLabel();
	    lab.setText("Size: ");
	    
	    JPanel temp = new JPanel();
	    
	    iFontSize = new JSpinner();
	    iFontSize.setMinimumSize(new Dimension(60,32));
	    iFontPanel.add(lab);
	    iFontPanel.add(iFontSize);
			       
	}
	return iFontPanel;
    }
     private JPanel getLetterGraphPanel(){
	 if(iLetterGraph == null){
	    iLetterGraphPanel = new JPanel();
	    
	    iLetterGraphPanel.setLayout(new FlowLayout(FlowLayout.LEFT));  // new BoxLayout(iLetterGraphPanel, BoxLayout.Y_AXIS));
	    iLetterGraphPanel.setPreferredSize(new Dimension(270,80));
	    iLetterGraphPanel.setBorder(BorderFactory.createTitledBorder("Freq. sort of residues"));
	    
	    
	    iLetterGraph =  new JCheckBox("Show           ");
	    boolean isLGraph = LogoBar.iPref.iShowLetterGraph;
	    iLetterGraph.setSelected(isLGraph);
	    iLetterGraph.setActionCommand("LGraph");
	    iLetterGraph.addActionListener(this);
	    JLabel lab = new JLabel();
	    lab.setText("Freq. cutoff: ");
	    
	    JPanel temp = new JPanel();
	    
	    iFreqCutoff = new JSpinner();
	    iFreqCutoff.setMinimumSize(new Dimension(60,32));
	    iFreqCutoff.setValue(new Integer(10));
	    iFreqCutoff.setValue(new Integer(LogoBar.iPref.iFreqCutoff));
	    
	
	    iLetterGraphPanel.add(iLetterGraph);
	    iLetterGraphPanel.add(lab);
	    iLetterGraphPanel.add(iFreqCutoff);
			       
	}
	return iLetterGraphPanel;
    }

    
    
    private JPanel getMiscPanel(){
	if(iMiscPanel == null){
	    iMiscPanel = new JPanel();
	    
	    iMiscPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	    //	    iMiscPanel.setLayout(new BoxLayout(iMiscPanel, BoxLayout.X_AXIS));
	    // iMiscPanel.setLayout(new FlowLayout());
	    iMiscPanel.setPreferredSize(new Dimension(270,140));
	    iMiscPanel.setBorder(BorderFactory.createTitledBorder("Misc"));
	    JLabel aLabel = new JLabel();
	    aLabel.setText("       Block size: ");
	    iBlockSize = new JSpinner();
	    iBlockSize.setMinimumSize(new Dimension(60,32));
	    iBlockSize.setValue(new Integer(10));
	    iBlockSize.setValue(new Integer(LogoBar.iPref.iBlockSize));
	    iMiscPanel.add(aLabel);
	    iMiscPanel.add(iBlockSize);
	    iOnlyMostConserved = new JCheckBox("Show only most conserved");
	    boolean isConserved = LogoBar.iPref.iShowOnlyMostConserved;
	    iOnlyMostConserved.setSelected(isConserved);
	    iOnlyMostConserved.setActionCommand("Only conserved");
	    iOnlyMostConserved.addActionListener(this);

	    iLettersAtBottom =  new JCheckBox("Letters at graph bottom");
	    boolean isBottom = LogoBar.iPref.iIsLettersAtBottom;
	    iLettersAtBottom.setSelected(isBottom);
	    iLettersAtBottom.setActionCommand("Letters at bottom");
	    iLettersAtBottom.addActionListener(this);
	    
	    iUseCorrectionFactor =  new JCheckBox("Use small sample correction");
	    boolean useCorr = LogoBar.iPref.iUseCorrectionFactor;
	    iUseCorrectionFactor.setSelected(useCorr);
	    iUseCorrectionFactor.setActionCommand("Use corr");
	    iUseCorrectionFactor.addActionListener(this);


	    iMiscPanel.add(iOnlyMostConserved);
	    iMiscPanel.add(iLettersAtBottom);
	    iMiscPanel.add(iUseCorrectionFactor);
	}
	return iMiscPanel;
    }

    
    public void actionPerformed(ActionEvent e) {
	if(e.getActionCommand() == "Gaps on top"){
	    iGapsStyle = GraphSort.GAPS_ON_TOP;
	}
	
	if(e.getActionCommand() == "Gaps at bottom"){
	    iGapsStyle = GraphSort.GAPS_IN_BOTTOM;
	}	
	if(e.getActionCommand() == "Sort default"){
	    iGraphStyle = GraphSort.SORT_DEFAULT;
	}
	if(e.getActionCommand() == "Sort group"){
	    iGraphStyle = GraphSort.SORT_BY_GROUP;
	}
	if(e.getActionCommand() == "ok"){
	    doSortChange();
	    this.dispose();
	}
	if(e.getActionCommand() == "cancel"){
	    this.dispose();
	}
	
	if(e.getActionCommand() == "Only conserved"){
	    LogoBar.iPref.iShowOnlyMostConserved = 
		!(LogoBar.iPref.iShowOnlyMostConserved);
	}

	if(e.getActionCommand() == "Letters at bottom"){
	    LogoBar.iPref.iIsLettersAtBottom = 
		!(LogoBar.iPref.iIsLettersAtBottom);
	}

	if(e.getActionCommand() == "LGraph"){
	    LogoBar.iPref.iShowLetterGraph = 
		!(LogoBar.iPref.iShowLetterGraph);
	}
	if(e.getActionCommand() == "Letter style"){
	    LogoBar.iPref.iShowWebLogo = true; 

	}

	if(e.getActionCommand() == "LogoBar style"){
	    LogoBar.iPref.iShowWebLogo = false; 

	}
	

	if(e.getActionCommand() == "Use corr"){
	    LogoBar.iPref.iUseCorrectionFactor = 
		!(LogoBar.iPref.iUseCorrectionFactor);
	}
	
    }
    
    private void doSortChange(){
	Stat stat = LogoBar.getStat();
	stat.setGraphSortStyle(iGraphStyle);
	stat.setGapsSortStyle(iGapsStyle);
	stat.useCorrectionFactor(LogoBar.iPref.iUseCorrectionFactor);

	LogoBar.iPref.iFontSize = ((int) Integer.parseInt(iFontSize.getValue().toString()));
	LogoBar.iPref.iBlockSize = ((int) Integer.parseInt(iBlockSize.getValue().toString()));
	LogoBar.iPref.iFreqCutoff = ((int) Integer.parseInt(iFreqCutoff.getValue().toString()));
	
	LogoBar.updateGraph();
    }
    
}
