package logobar;
import java.awt.*;
import java.util.*;

public interface BarDrawer {
    
    static final int BAR_WIDTH = 15;

    public int paint(GraphPanel gPanel, Graphics2D g, Vector position, 
		      int xPos, int yStart, float scale);
    
    public int  getBarSize();
    public void setBarSize(int val);
    
}
