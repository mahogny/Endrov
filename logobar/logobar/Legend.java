package logobar;
/** Display the color legend of the graph
 *  Gets the table with the aa heights, and creates a consensus sequence
 *  and a graph made out of bars.
 *  Johan Koch
 */

//package logoBar; // All files in the program must be declared to belong to the same package. Otherwise there's a problem when creating the jar file.
import java.awt.*;
//import org.jibble.epsgraphics.*; // Java EPS Graphics2D package obtained at http://www.jibble.org/epsgraphics/
import javax.swing.*;
import java.util.*;
import java.lang.String;
import java.awt.event.*;

class GrpCMouseAdapter extends MouseAdapter{
    
    GrpColorPanel iGrpColor;
    public GrpCMouseAdapter(GrpColorPanel grp){
	this.iGrpColor = grp;
    }
    
    public void mouseClicked(MouseEvent me){
	iGrpColor.changeColor();
    }
    

}

class GrpColorPanel extends JPanel{
    private int iIdx;
    public GrpColorPanel(int idx){
	super();
	setBorder(BorderFactory.createLineBorder(Color.black));
	addMouseListener( new GrpCMouseAdapter(this));
	iIdx = idx;
    }

    public void changeColor(){
	Color newColor = JColorChooser.showDialog(
						  this,
						  "Choose Group Color",
						  this.getBackground());
	if(newColor != null){
	    this.setBackground(newColor);
	    //Set the new color in the ColorHandler
	    ColorHandler cHandler = LogoBar.iColorHandler;
	    cHandler.setColorGroup(iIdx, newColor);
	    LogoBar.updateGraph();
	}
    }
}





public class Legend extends JPanel{
    
    private static Vector iLegendVec[];

    public Legend() {
	
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	update();
	repaint();
    }

    public void update(){
	//Re-build the color legend of
	//amino acid color groups
	System.out.println("Im making the legend");
	iLegendVec = new Vector[21];
	ColorHandler cHandler = LogoBar.iColorHandler;
	for(int i = 0; i< 21; i++){
	    iLegendVec[i] = new Vector(21,0);
	}
	
	//Get chararcter ASCII code for
	//the Amino acid
	int Agrp = cHandler.getAAColorGroup('A');
	int Cgrp = cHandler.getAAColorGroup('C');
	int Dgrp = cHandler.getAAColorGroup('D');
	int Egrp = cHandler.getAAColorGroup('E');
	int Fgrp = cHandler.getAAColorGroup('F');
	int Ggrp = cHandler.getAAColorGroup('G');
	int Hgrp = cHandler.getAAColorGroup('H');
	int Igrp = cHandler.getAAColorGroup('I');
	
	int Kgrp = cHandler.getAAColorGroup('K');
	int Lgrp = cHandler.getAAColorGroup('L');
	int Mgrp = cHandler.getAAColorGroup('M');
	int Ngrp = cHandler.getAAColorGroup('N');
	int Pgrp = cHandler.getAAColorGroup('P');
	int Qgrp = cHandler.getAAColorGroup('Q');
	int Rgrp = cHandler.getAAColorGroup('R');
	int Sgrp = cHandler.getAAColorGroup('S');
	int Tgrp = cHandler.getAAColorGroup('T');
	int Vgrp = cHandler.getAAColorGroup('V');
	int Wgrp = cHandler.getAAColorGroup('W');
	int Ygrp = cHandler.getAAColorGroup('Y');
	int GapGrp = cHandler.getAAColorGroup('-');
	
	iLegendVec[Agrp].add(new Integer((int) 'A'));
	iLegendVec[Cgrp].add(new Integer((int) 'C'));
	iLegendVec[Dgrp].add(new Integer((int) 'D'));
	iLegendVec[Egrp].add(new Integer((int) 'E'));
	iLegendVec[Fgrp].add(new Integer((int) 'F'));
	iLegendVec[Ggrp].add(new Integer((int) 'G'));
	iLegendVec[Hgrp].add(new Integer((int) 'H'));
	iLegendVec[Igrp].add(new Integer((int) 'I'));
	iLegendVec[Kgrp].add(new Integer((int) 'K'));
	iLegendVec[Lgrp].add(new Integer((int) 'L'));
	iLegendVec[Mgrp].add(new Integer((int) 'M'));
	iLegendVec[Ngrp].add(new Integer((int) 'N'));
	iLegendVec[Pgrp].add(new Integer((int) 'P'));
	iLegendVec[Qgrp].add(new Integer((int) 'Q'));
	iLegendVec[Rgrp].add(new Integer((int) 'R'));
	iLegendVec[Sgrp].add(new Integer((int) 'S'));
	iLegendVec[Tgrp].add(new Integer((int) 'T'));
	iLegendVec[Vgrp].add(new Integer((int) 'V'));
	iLegendVec[Wgrp].add(new Integer((int) 'W'));
	iLegendVec[Ygrp].add(new Integer((int) 'Y'));
	iLegendVec[GapGrp].add(new Integer((int) '-'));

	paint();
    }
    
    public void paint() {
	removeAll();
	Font f = new Font( getFont().getFontName(),
			   getFont().getStyle(),
			   10);
	
	ColorHandler cHandler = LogoBar.iColorHandler;
	//	super.paintComponent(g);
	for(int i = 0; i < 21; i++){
	    Vector  aaVec = iLegendVec[i];
	    Iterator aaIt = aaVec.iterator();
	    String aaStr ="";
	    int lastAA = 0;
	    while(aaIt.hasNext()){
		int chCode = (int) ((Integer) aaIt.next()).intValue();
		lastAA = chCode;
		String tmpChar = String.valueOf((char)chCode);
		aaStr = aaStr + tmpChar +", ";
	    }
	    if(aaStr.length() != 0){
		Color color = cHandler.getAAColor((char) lastAA);
		JPanel aPanel = new JPanel();
		aPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		GrpColorPanel aColorFrame = new GrpColorPanel(i);
		aColorFrame.setBackground(color);
		aColorFrame.setPreferredSize(new Dimension(20,20));;
		
		JLabel aLabel = new JLabel();
		aLabel.setFont(f);
		String aString = " = ";
		aString = aString + aaStr;
		String aString2 = aString.substring(0, aString.length() - 2);
		aLabel.setText(aString2);

		aPanel.add(aColorFrame);
		aPanel.add(aLabel);

		int pwidth = getWidth(); 
		aPanel.setPreferredSize(new Dimension(pwidth,35));
		add(aPanel);
	    }
	}
	repaint();
    }
    
}


    
