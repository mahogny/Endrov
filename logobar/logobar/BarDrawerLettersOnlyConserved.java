package logobar;
import java.awt.*;
//import org.jibble.epsgraphics.*; // 
import java.util.*;
import java.awt.geom.*;
import java.awt.Font;
import java.awt.font.GlyphVector;

public class BarDrawerLettersOnlyConserved extends  BarDrawerLettersAll{
    
    public BarDrawerLettersOnlyConserved() {
	super();
    }

    public int paint(GraphPanel gPanel, Graphics2D g, Vector position, 
		     int xPos, int yStart, float scale){
	
	int prevHeight = 0;
	int fontSize   = LogoBar.iPref.iFontSize;
	int fontFact = fontSize/5;
	int fontFact2 = 6 - (fontSize/4);
	Font font = new Font("Sans serif", 
			     1, 
			     fontSize);
 	Font bfont = new Font("Sans serif", 
			      0, 
			      16);
	g.setFont(bfont);
	//	int Y_GRAPH_START = gPanel.getYGraphStart();
	ColorHandler cHandler = LogoBar.iColorHandler;
	if(!position.isEmpty()){
	    //for(ListIterator it = position.listIterator(position.size()); it.hasPrevious();){
	    LogoEntry l_entry = (LogoEntry)position.lastElement();
	    Color color = cHandler.getAAColor(l_entry.getAA());
 	    int  height = (int) ((float) l_entry.getHeight() * scale);
	    char aa = (char) l_entry.getAA();
	    if(aa == '-'){
		g.setColor(color);
		g.setFont(font);
		int yy = gPanel.reverseY( yStart + height) - prevHeight;
		//draw box that prepresents the gap
		g.fillRect(xPos, yy, 18, height);
		g.setColor(Color.black);
		if(color.getRed() < 126 && color.getGreen() < 126 && color.getBlue() < 126)
		    g.setColor(Color.white);
		g.drawRect(xPos, yy,18, height);
		if(height >= fontSize){
		    char AA = l_entry.getAA();
		    if(iLettersAtBottom)
			g.drawString(String.valueOf(AA), xPos + fontFact2, (yy + height) - fontFact);
		    else
			g.drawString(String.valueOf(AA), xPos + fontFact2, yy + 12 + fontFact);
		}
		g.setFont(bfont);
	    }
	    else{
		float hNoScale = (float)(height / (250F * scale ));
		int yy = gPanel.reverseY( yStart) - prevHeight;
		float lScale = 22F * scale * hNoScale - 0.5F;
		Font bigFont = font.deriveFont(AffineTransform.getScaleInstance(1.5,lScale));
		GlyphVector gv = bigFont.createGlyphVector(g.getFontRenderContext(),String.valueOf(aa));
		g.translate(xPos, yy);// + fontFact4);
		Shape aShape = gv.getGlyphOutline(0);
		g.setColor(color);
		g.fill(aShape);
		//g.scale(1,1.4);
		g.translate(-xPos, -(yy));//+ fontFact4));
	    }
	}
	g.setFont(font);
	return 22 + xPos;
     }
}
