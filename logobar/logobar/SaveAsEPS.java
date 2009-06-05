package logobar;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.jibble.epsgraphics.EpsGraphics2D;

public class SaveAsEPS
	{
	 public static void saveEpsFile(GraphPanel panel, String filePath, Drawer drawer_, Stat stat_, char[] consArray_) {
	 try{
	     int width  = panel.getWidth();  
	     int height = panel.getHeight(); 
	     String fileName;
	     if(filePath.indexOf('.') != -1){ 
		 int pointPos = filePath.lastIndexOf('.');   
		 fileName = filePath.substring(0, pointPos); 
	     }
	     else{
		 fileName = filePath; 
	     }
	     // Create correct extension is added to the file name.
	     FileOutputStream outputStream  = new FileOutputStream(fileName + ".eps"); 
	     EpsGraphics2D eps = new EpsGraphics2D("Output", outputStream, 0, 0, width, height); 
	     panel.iPaint.paint(panel, eps, stat_, consArray_);
	     eps.flush();
	     eps.close();
	     JOptionPane.showMessageDialog(null, "An eps file has been created.");
	 }
	 catch (IOException ioe) {
	     JOptionPane.showMessageDialog(null, ioe.toString());
	 }
     }
	}
