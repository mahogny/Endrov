package logobar;
//package logoBar; // All files in the program must be declared to belong to the same package. Otherwise there's a problem when creating the jar file.
import java.awt.*;
import javax.swing.*;

public class GraphPanel extends JPanel{
    public PaintStyle iPaint;
    private Stat       iStat;
    private Drawer     iDrawer;


    
    
    public GraphPanel(Drawer d, Stat s) {
	super();
	iDrawer = d;
	iStat   = s;
	iPaint = new PaintContStyle();  // Continuous sequence logo is default.
    }
    
    public void setGraphType(){
	Preferences pref     = LogoBar.iPref;
	boolean isBlockStyle = pref.iIsBlockDivided; 
	if(isBlockStyle)
	    iPaint = new PaintBlockStyle();
	else
	    iPaint = new PaintContStyle();


	repaint();
    }
    // Method to draw the logobar graph.
    public void paintComponent(Graphics g){
	super.paintComponent(g);
	if(iStat.isInit()){
	    Graphics2D g2 = (Graphics2D) g;
	    char [] consArray = iDrawer.getConsArray();

	    //Pass the paint job over to the painting class
	    iPaint.paint(this, g2, iStat, consArray);
	}
    }
    
    public void update(){
	setGraphType();
	iPaint.update();
    }
    
     public  int reverseY(int y){
 	int new_y = getHeight() - y;
 	if(new_y < 0)
 	    new_y = 0;
 	return new_y;
	
    }
    
    public float getScale() {return iDrawer.getScale();}
    
    public int   getFontSize(){return iDrawer.getFontSize();}

    public int   getMaxAAEntries(){return iStat.getMaxAAEntries();}
    

    public int  getYGraphStart() { return 400;}
    
   
    
}
