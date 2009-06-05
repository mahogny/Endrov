package logobar;
/** Paint style class tree...
 *  
 *  
 *  
 *  Asa Perez-Bercoff, Johan Koch
 *
 *  Using Java EPS Graphics2D package obtained 
 *  at http://www.jibble.org/epsgraphics/
 */

import java.awt.*;
import java.awt.Font;
//import org.jibble.epsgraphics.*; // 
import java.util.*;


public class PaintContStyle implements PaintStyle {  
    
    private BarDrawer iBarDrawer;
    
    public PaintContStyle(){
	iBarDrawer = BarDrawFactory.getBarDrawer();
    }

    public void paint(GraphPanel gPanel, Graphics2D g, Stat stat, char consensusArray[]){
	int fontSize = LogoBar.iPref.iFontSize;
	int smFont   = fontSize - 3;
	if(smFont > 20)
	    smFont = 20;
	
	int fontFact = fontSize - 12;
	int fontFact2 = 6 - (fontSize/4);
	int fontFact4 = smFont - 9;
	int fontFact3 = 6 - (smFont/4);
	
        float scale = gPanel.getScale();
	
        int xStart = 60;  // x-coor. where on the window the graph is started
        int seqLen = stat.getSeqLength();
        Vector graph[] = stat.getGraph(); // Array of vector repr. the rows in a graph. 	
        int y_axisHeight = (int)(1125F * scale);
	Font font = new Font("Sans serif", 
			     1, 
			     fontSize);
	Font smallerFont = new Font("Sans serif", 
			     0, 
			     fontSize - 2);
	
	Vector[] noCorrGraph = stat.getGraphWithNoCorrection();
        if(graph != null){
	    int Y_GRAPH_START = gPanel.getYGraphStart();
	    int barSize = iBarDrawer.getBarSize();
	    gPanel.setPreferredSize(new Dimension(seqLen * barSize + 100, y_axisHeight + Y_GRAPH_START + 60));
	    gPanel.revalidate();
	    
	    g.setFont(font);
	    drawAxis(gPanel, g, stat);
	    
	    int xpos = xStart;
	    for(int i = 0; i < seqLen; i++){
		g.setFont(font);

		Vector position = graph[i];
		Vector letterVec = noCorrGraph[i];
		//int xpos = xStart + barSize * i;
		
		//Draw the bars for the current sequence
		//position with a helper class
		int nXpos = iBarDrawer.paint(gPanel, g, position,
					     xpos, Y_GRAPH_START,scale);
		
		int yText = gPanel.reverseY(Y_GRAPH_START - 20);
		int yPos = gPanel.reverseY(Y_GRAPH_START - 30);
		
                g.setColor(Color.black);
		// First print consensus sequence
		//g.setFont(new Font("Monospaced", Font.BOLD, smFont));
		
		//Font bigFont = font.deriveFont(AffineTransform.getScaleInstance(1.0,42.7));
		//		GlyphVector gv = bigFont.createGlyphVector(g.getFontRenderContext(),String.valueOf(consensusArray[i]));
		//	g.translate(xpos + fontFact3, yText -20);// + fontFact4);
		//		Shape aShape = gv.getGlyphOutline(0);
		//	g.fill(aShape);
		//	g.scale(1,1.4);
                g.drawString(String.valueOf(consensusArray[i]), xpos + fontFact3, yPos + fontFact4 + 10);
		if(LogoBar.iPref.iShowLetterGraph){
		    ListIterator it = letterVec.listIterator(letterVec.size());
		    int mirrorOffset = 25;
		    while(it.hasPrevious()){
			g.setFont(smallerFont);
			LogoEntry l_entry = (LogoEntry)it.previous();
			char AA = l_entry.getAA();
			int freq = (int)(l_entry.getFreq() * 100);
			if(freq < LogoBar.iPref.iFreqCutoff)
			    break;
			if( AA != (char)(consensusArray[i])){
			    g.drawString(String.valueOf(AA), 
					 xpos + fontFact3, 
					 yPos + fontFact4 + mirrorOffset);
			    mirrorOffset += 15;
			    
			}
			
		    }
		}
		
		// Print position in the sequence
			g.setFont(smallerFont);
                if(i % 10 == 0)
                    g.drawString(String.valueOf(i + 1), xpos, yText + fontFact4 - 3);
		
		xpos = nXpos;
	    }

	}
    }
    
    private void drawAxis(GraphPanel gPanel, Graphics2D g, Stat stat)
    {
        float scale = gPanel.getScale();
        int xStartForY_axis = 30;
        int arrowX1 = 25;
        int arrowX2 = 31;
        int arrowX3 = 36;
        float yScale = 25F * scale;
        int y_axisHeight = (int)(45F * yScale);
        int yStartForY_axis = gPanel.reverseY(y_axisHeight + gPanel.getYGraphStart());
        int arrowY1 = gPanel.reverseY(y_axisHeight + gPanel.getYGraphStart() - 20);
        int arrowY2 = yStartForY_axis;
        int arrowY3 = arrowY1;
        int scaleY0 = (int)((float)yStartForY_axis + 45F * yScale);
	// System.out.print("Y0 ");
// 	System.out.print(scaleY0);
// 	System.out.print(" Y1 ");
	
	
	
        int scaleY1 = (int)((float)yStartForY_axis + 35F * yScale);
// 	System.out.println(scaleY1);

        int scaleY2 = (int)((float)yStartForY_axis + 25F * yScale);
        int scaleY3 = (int)((float)yStartForY_axis + 15F * yScale);
        int scaleY4 = (int)((float)yStartForY_axis + 5F * yScale);
        int scaleNoX = 20;
        int scaleX1 = 25;
        int scaleX2 = 36;
        g.setColor(Color.black);
        g.fillRect(xStartForY_axis, yStartForY_axis, 2, y_axisHeight);
        g.drawString("bits", scaleNoX, yStartForY_axis);
        g.drawLine(arrowX1, arrowY1, xStartForY_axis, yStartForY_axis);
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
    
    public void update(){
	iBarDrawer = BarDrawFactory.getBarDrawer();
    }
    
}


