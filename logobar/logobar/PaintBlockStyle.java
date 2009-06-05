package logobar;
/** Paint style class tree...
 *  
 *  Author: Asa Perez-Bercoff and Johan Koch
 *
 *  Using package Java EPS Graphics2D package obtained at http://www.jibble.org/epsgraphics/
 */


import java.awt.*;
//import org.jibble.epsgraphics.*; 
import java.util.*;

public class PaintBlockStyle implements PaintStyle {    
    
    private BarDrawer iBarDrawer;

    public PaintBlockStyle(){
	iBarDrawer = BarDrawFactory.getBarDrawer();
    }
    
    public void paint(GraphPanel gPanel, Graphics2D g, Stat stat, char[] consensusArray){	
	int fontSize = LogoBar.iPref.iFontSize;
	int smFont   = fontSize - 3;
	if(smFont > 20)
	    smFont = 20;
	int fontFact = fontSize/5;
	int fontFact2 = 6 - (fontSize/4);
	int fontFact4 = smFont - 9;
	int fontFact3 = 6 - (smFont/4);
	float scaleFact = gPanel.getScale();
        float yAxScale = scaleFact * 25.f;
        
        int xStart = 60;
        int xStartForY_axis = 30;
       int y_axisHeight = (int)(1125f * scaleFact);
        int seqLen = stat.getSeqLength();
	int blockSize = LogoBar.iPref.iBlockSize;
        int numberOfBlocks = seqLen / blockSize + 1;
        int yGraphStart =  (int)(y_axisHeight)+ 50;//y_axisHeight + 60;
        Vector graph[] = stat.getGraph();
	int letterGraphOffset = 0;
	int maxNrLetters = 0;
	//if(LogoBar.iPref.iShowLetterGraph)
	    //letterGraphOffset = gPanel.getMaxAAEntries() * fontSize + 30;
	if(graph != null){
	    int barSize = iBarDrawer.getBarSize();
	    gPanel.setPreferredSize(new Dimension(100 + (blockSize * barSize), 
						  (y_axisHeight + yGraphStart + letterGraphOffset)* numberOfBlocks));
	    gPanel.revalidate();
	    g.setColor(Color.black);
	    Font font = new Font("Sans serif", 
				 1, 
				 fontSize);
	    Font smallerFont = new Font("Sans serif", 
					0, 
					fontSize - 2);
	    
	    int yStartForY_axis = yGraphStart;
	    Vector[] noCorrGraph = stat.getGraphWithNoCorrection();
	    
	    
	    for(int i = 0; i < numberOfBlocks; i++){
		maxNrLetters = 0;
		g.setFont(font);
		//int prevHeightY = yGraphStart * (i + 1) - 10;
		
		drawAxis(gPanel, g, yStartForY_axis);
		int xpos = xStart;
		for(int j = 0; j < blockSize && i * blockSize + j < seqLen; j++){
		    g.setFont(font);
		    Vector position = graph[i * blockSize + j];
		    
		    Vector letterVec = noCorrGraph[i * blockSize + j];
		    //int npos = xStart + barSize * j;
		    // int prevHeight =  + (yGraphStart * (numberOfBlocks - i + 1));
		    int yText = yStartForY_axis + 20;
		    int yPos = yText + 10;
		    
		    //Draw the actual bar
		    int nXpos = iBarDrawer.paint(gPanel, g, position,xpos,gPanel.reverseY(yStartForY_axis),
						 scaleFact);
		    
		    //Return to black color
		    g.setColor(Color.black);
		    

		   
		    char consChar = (char)consensusArray[i * blockSize + j];
		    g.drawString(String.valueOf(consChar), xpos + fontFact3, yPos + fontFact4 + 10);
		    if(LogoBar.iPref.iShowLetterGraph){

			//Drawing the mirrored letter graph
			ListIterator it = letterVec.listIterator(letterVec.size());
			int mirrorOffset = 25;
			int nrLetters = 0;
			while(it.hasPrevious()){
			    g.setFont(smallerFont);
			    LogoEntry l_entry = (LogoEntry)it.previous();
			    int freq = (int)(l_entry.getFreq() * 100);
			    if(freq < LogoBar.iPref.iFreqCutoff)
				break;
			    nrLetters++;
			    char AA = l_entry.getAA();
			    if( AA != consChar){    //Don't draw the letter of the consensus array
				g.drawString(String.valueOf(AA), 
					     xpos + fontFact3, 
					     yPos + fontFact4 + mirrorOffset);
				mirrorOffset += 15;
			    }
			}
			if(maxNrLetters < nrLetters) maxNrLetters = nrLetters;
			
		    } 
		    
		    g.setFont(smallerFont);
		    if(j % 10 == 0)
			g.drawString(String.valueOf(i * blockSize + j + 1), 
				     xpos + 3, 
				     yText + fontFact4  - 3);
		    
		    
		    xpos = nXpos;
		}
		letterGraphOffset = maxNrLetters * fontSize + 30;
		//Offset to next graph block
		yStartForY_axis += (yGraphStart + letterGraphOffset);
	// 	System.out.print("y start");
// 		System.out.println(yStartForY_axis);
		
	    }
	    
	}   
    }
    
    public void update(){
	iBarDrawer = BarDrawFactory.getBarDrawer();
    }
    
    private void drawAxis(GraphPanel gPanel, Graphics2D g, int startY){
	int scaleNoX = 20;
        int scaleX1 = 25;
        int scaleX2 = 36;
	int xStart = 60;
        int xStartForY_axis = 30;
	float scaleFact = gPanel.getScale();
	int y_axisHeight = (int)(1125f * scaleFact);
	int yStartAx = gPanel.reverseY(gPanel.reverseY(startY) + y_axisHeight);
	
        float yAxScale = scaleFact * 25.f;
        int barSize = 15;
	
	
      
	int arrowX1 = 25;
        int arrowX2 = 31;
        int arrowX3 = 36;
       	int arrowY1 = yStartAx + 15;
	int arrowY2 = yStartAx;
	int arrowY3 = arrowY1;
	int scaleY0 = (int)((float)yStartAx + 45F * yAxScale);
	int scaleY1 = (int)((float)yStartAx + 35F * yAxScale);
	int scaleY2 = (int)((float)yStartAx + 25F * yAxScale);
	int scaleY3 = (int)((float)yStartAx + 15F * yAxScale);
	int scaleY4 = (int)((float)yStartAx + 5F * yAxScale);
	g.fillRect(xStartForY_axis, yStartAx, 2, y_axisHeight);
	g.drawString("bits", scaleNoX, yStartAx);
	g.drawLine(arrowX1, arrowY1, xStartForY_axis, yStartAx);
	g.drawLine(arrowX2, arrowY2, arrowX3, arrowY3);
	g.drawLine(scaleX1, scaleY0, scaleX2, scaleY0);
	g.drawString("0", scaleNoX, scaleY0);
	g.drawLine(scaleX1, scaleY1, scaleX2, scaleY1);
	g.drawString("1", scaleNoX, scaleY1);
	g.drawLine(scaleX1, scaleY2, scaleX2, scaleY2);
	g.drawString("2", scaleNoX, scaleY2);
	g.drawLine(scaleX1, scaleY3, scaleX2, scaleY3);
	g.drawString("3", scaleNoX, scaleY3);
	g.drawLine(scaleX1, scaleY4, scaleX2, scaleY4);
	g.drawString("4", scaleNoX, scaleY4);
	




    }
}




