package logobar;
import java.awt.*;
//import org.jibble.epsgraphics.*; // 
import java.util.*;


public class BarDrawerAll implements BarDrawer{

    protected boolean iLettersAtBottom;
    protected int     iBarSize;

    public BarDrawerAll(){
	iBarSize = 15;
	iLettersAtBottom = LogoBar.iPref.iIsLettersAtBottom;
    }
    
    public int getBarSize() {return iBarSize;}
    
    public void setBarSize(int val) {iBarSize = val;}
    
    public int paint(GraphPanel gPanel, Graphics2D g, Vector position, 
		      int xPos, int yStart, float scale){
	int prevHeight = 0;
	int fontSize   = LogoBar.iPref.iFontSize;
	int fontFact = fontSize/5;
	int fontFact2 = 6 - (fontSize/4);
	
	//	int Y_GRAPH_START = gPanel.getYGraphStart();
	ColorHandler cHandler = LogoBar.iColorHandler;
	for(ListIterator it = position.listIterator(position.size()); it.hasPrevious();){
	    LogoEntry l_entry = (LogoEntry)it.previous();
	    Color color = cHandler.getAAColor(l_entry.getAA());
 	    int height = (int)((float)l_entry.getHeight() * scale);
 	    int yy = gPanel.reverseY(height + yStart) - prevHeight;
	    
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
	    prevHeight += height;
	}
	return BarDrawer.BAR_WIDTH + xPos;
    }
    
    
}
