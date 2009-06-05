package logobar;
//
//	File:	AboutWindow.java
//

import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.*;

public class AboutWindow extends JFrame implements ActionListener {
    protected JLabel titleLabel, aboutLabel[];
    protected static int labelCount = 14;
    protected static int aboutWidth = 600;
    protected static int aboutHeight = 270;
    protected static int aboutTop = 200;
    protected static int aboutLeft = 350;
    protected Font titleFont, bodyFont;
    protected ResourceBundle resbundle;
    
    public AboutWindow() {
	super("");
	SymWindow aSymWindow = new SymWindow();
	this.addWindowListener(aSymWindow);	
	
	// Initialize useful fonts
	titleFont = new Font("Lucida Grande", Font.BOLD, 14);
	if (titleFont == null) {
	    titleFont = new Font("SansSerif", Font.BOLD, 14);
	}
		bodyFont  = new Font("Lucida Grande", Font.PLAIN, 10);
		if (bodyFont == null) {
			bodyFont = new Font("SansSerif", Font.PLAIN, 10);
		}
		
		this.getContentPane().setLayout(new BorderLayout(15, 15));
	
		aboutLabel = new JLabel[labelCount];
		aboutLabel[0] = new JLabel("");
		aboutLabel[1] = new JLabel("LogoBar");
		aboutLabel[1].setFont(titleFont);
		aboutLabel[2] = new JLabel("1.1");
		aboutLabel[2].setFont(bodyFont);
		aboutLabel[3] = new JLabel("");
		aboutLabel[4] = new JLabel("");
		
		aboutLabel[5] = new JLabel("Project supervisor: Thomas Bürglin");
		aboutLabel[5].setFont(bodyFont);
		
		aboutLabel[6] = new JLabel("Copyright (2005-2009) Johan Koch & Åsa Pérez-Bercoff");
		aboutLabel[6].setFont(bodyFont);
		
		aboutLabel[7] = new JLabel("Improvements 2009 by Johan Henriksson");
		aboutLabel[7].setFont(bodyFont);

		aboutLabel[8] = new JLabel("");

		aboutLabel[9] = new JLabel("If you use this program or modify this program for your use, please cite the following paper:");
		aboutLabel[9].setFont(bodyFont);
		
		aboutLabel[10] = new JLabel("Pérez-Bercoff, Å, Koch, J., and Bürglin, T.R. (2006)");
		aboutLabel[10].setFont(bodyFont);
		
		aboutLabel[11] = new JLabel("LogoBar: bar graph visualization of protein logos with gaps. Bioinformatics, 22, 112-114");
		aboutLabel[11].setFont(bodyFont);
		
		
		
		aboutLabel[12] = new JLabel();
		aboutLabel[12].setFont(bodyFont);
		aboutLabel[13] = new JLabel("Free under the GPL 2.0 license");			
		aboutLabel[13].setFont(bodyFont);
		Panel textPanel2 = new Panel(new GridLayout(labelCount, 1));
		for (int i = 0; i<labelCount; i++) {
			aboutLabel[i].setHorizontalAlignment(JLabel.CENTER);
			textPanel2.add(aboutLabel[i]);
		}
		this.getContentPane().add (textPanel2, BorderLayout.CENTER);
		this.pack();
		this.setLocation(aboutLeft, aboutTop);
		this.setSize(aboutWidth, aboutHeight);
		this.setResizable(false);
    }

    class SymWindow extends java.awt.event.WindowAdapter {
	    public void windowClosing(java.awt.event.WindowEvent event) {
		    setVisible(false);
	    }
    }
    
    public void actionPerformed(ActionEvent newEvent) {
        setVisible(false);
    }		
}
