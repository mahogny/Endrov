package logobar;
import java.awt.*;
//import org.jibble.epsgraphics.*; // 
import java.util.*;


public class BarDrawerOnlyConserved extends BarDrawerAll {
    
    BarDrawerOnlyConserved(){
	super();
    }

    public int paint(GraphPanel gPanel, Graphics2D g, Vector position, 
		 int xPos, int yStart, float scale){

	int yText      = gPanel.reverseY(20);
	int yPos       = gPanel.reverseY(10);
	int fontSize   = LogoBar.iPref.iFontSize;
	int fontFact = fontSize - 12;
	int fontFact2 = 6 - (fontSize/4);
	int Y_GRAPH_START = gPanel.getYGraphStart();
	if(!position.isEmpty()){
	    LogoEntry l_entry = (LogoEntry)position.lastElement();
	    ColorHandler _tmp = LogoBar.iColorHandler;
	    Color color = ColorHandler.getAAColor(l_entry.getAA());
	    int height = (int)((float)l_entry.getHeight() * scale);
	    int yy = gPanel.reverseY(height + yStart);
	    g.setColor(color);
	    g.fillRect(xPos, yy, BarDrawer.BAR_WIDTH, height);
	    g.setColor(Color.black);
	    if(color.getRed() < 126 && color.getGreen() < 126 && color.getBlue() < 126)
		g.setColor(Color.white);
	    g.drawRect(xPos, yy, BarDrawer.BAR_WIDTH, height);
	    if(height >= fontSize){
		char AA = l_entry.getAA();
		if(iLettersAtBottom)
		    g.drawString(String.valueOf(AA), xPos + fontFact2, (yy + height) - fontFact);
		else
		    g.drawString(String.valueOf(AA), xPos + fontFact2, yy + 12 + fontFact);
 		
		
	    }
	    
	}
	
	return BarDrawer.BAR_WIDTH + xPos;
    }
    
    
    
}
