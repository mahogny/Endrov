package logobar;
import java.awt.*;
//import org.jibble.epsgraphics.*; // 
import java.util.*;
import java.awt.geom.*;
import java.awt.Font;
import java.awt.font.GlyphVector;

/** Class that draws bars as letters.
 * This class draws all bars at a logo position
 * as letters. All residues are drawn.
 */
public class BarDrawerLettersAll implements BarDrawer{

    protected boolean iLettersAtBottom;
    protected int     iBarSize;
    public BarDrawerLettersAll(){
	iBarSize = 22;
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
	Font font = new Font("Sans serif", 
			     1, 
			     fontSize);

	//The base font we use for drawing the
	//letter bars
 	Font bfont = new Font("Sans serif", 
			      0, 
			      16);
	g.setFont(bfont);
	
	//The color handler has the color for each amino acid
	ColorHandler cHandler = LogoBar.iColorHandler;
	
	// Iterate thorugh the logo position
	for(ListIterator it = position.listIterator(position.size()); it.hasPrevious();){
	    
	    //Get the residue data
	    LogoEntry l_entry = (LogoEntry)it.previous();
	    
	    //Get the color for the residue
	    Color color = cHandler.getAAColor(l_entry.getAA());
	    
	    //..and the height
 	    int  height = (int) ((float) l_entry.getHeight() * scale);
	    
	    //...and the amino acid as a char
	    char aa = (char) l_entry.getAA();
	    
	    //Special case is the gap
	    if(aa == '-'){
		//Dont create a BIG letter from
		//the gap symbol
		//Use a LogoBar bar instead
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
		//Set font to big font base again
		g.setFont(bfont);
	    }
	    else{
		//get the height of the amino acid without
		//any scaling
		
		float hNoScale = (float)(height / (250F * scale ));
		
		//Get the base Y position of the letter
		int yy = gPanel.reverseY( yStart) - prevHeight;
		
		//Y letter scale
		float lYScale = 22F * scale * hNoScale - 0.5F;

		//Prevent negativ scaling which
		//will cause the letters to flip
		//upside down
		if(lYScale < 0.01F) lYScale = 0.01F;
		

		//Standard letter X scale
		float lXScale = 1.5F;

		//Special care to letter W and M
		if( (aa == 'W') || (aa == 'M')) lXScale = 1.2F;
		
		float xOffset = 0F;
		if( aa == 'I') xOffset = 4.5F;
		
		//Create the font for the graph letter
		Font bigFont = font.deriveFont(AffineTransform.getScaleInstance(lXScale,lYScale));
		
		//Create a vector graphic representaion
		//of the letter
		GlyphVector gv = bigFont.createGlyphVector(g.getFontRenderContext(),String.valueOf(aa));
		
		//Translate letter into position
		g.translate(xPos + xOffset, yy);// + fontFact4);
		Shape aShape = gv.getGlyphOutline(0);
		g.setColor(color);
		
		//Draw the letter
		g.fill(aShape);
		
		//Translate back into original position
		g.translate(-(xPos + xOffset), -(yy));
		
	    }   
	    prevHeight += height;
	}
	g.setFont(font);
	return 22 + xPos;
    }
    
    
}
    
