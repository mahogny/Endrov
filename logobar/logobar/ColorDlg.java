package logobar;

/**
 *  Commenced: Friday, April 15, 2005
 *  Author: Johan Koch
 *  
 *  GraphSortByGroup is part of LogoBar
 *  
 */

import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.io.*;
import java.util.StringTokenizer;
import javax.swing.*;
import java.awt.event.*;


class GrpColorMouseAdapter extends MouseAdapter{
    
    GrpColor iGrpColor;
    public GrpColorMouseAdapter(GrpColor grp){
	this.iGrpColor = grp;
    }
    
    public void mouseClicked(MouseEvent me){
	iGrpColor.changeColor();
    }
    

}

class GrpColor extends JPanel{
    
    public GrpColor(){
	super();
	addMouseListener( new GrpColorMouseAdapter(this));
    }

    public void changeColor(){
	Color newColor = JColorChooser.showDialog(
						  this,
						  "Choose Group Color",
						  this.getBackground());
	if(newColor != null)
	    this.setBackground(newColor);
	
    }
}


class AACombo extends JComboBox{
    public char AA;
    
    public AACombo(char aa){
	super();
	AA = aa;
    }
}
    


public class ColorDlg extends JDialog
    implements ActionListener
{
    
    public ColorDlg()
    {
        iMainPane = null;
        iColorPanel = null;
        iAAPanel = null;
        iDefCmb = null;
        colGrps = new GrpColor[21];
        aaCmb = new AACombo[21];
        initialize();
    }

    private void initialize()
    {
        setSize(300, 735);
        setResizable(false);
        setContentPane(getMainPane());
        repaint();
    }

    private JPanel getMainPane()
    {
        if(iMainPane == null)
        {
            iMainPane = new JPanel();
            iMainPane.setLayout(new FlowLayout());
            iMainPane.add(getColorPanel());
            iMainPane.add(getAAPanel());
            iMainPane.add(getColorSchemePanel());
            iMainPane.add(getOkPanel());
        }
        return iMainPane;
    }

    private JPanel getColorPanel()
    {
        if(iColorPanel == null)
        {
            iColorPanel = new JPanel();
            iColorPanel.setLayout(new FlowLayout());
            iColorPanel.setPreferredSize(new Dimension(125, 555));
            iColorPanel.setBorder(BorderFactory.createTitledBorder("Colour groups"));
            JLabel colGrpLab[] = new JLabel[21];
            for(int i = 0; i < 21; i++)
            {
                colGrpLab[i] = new JLabel();
                colGrpLab[i].setText("Group " + Integer.toString(i + 1) + ": ");
                colGrps[i] = new GrpColor();
                colGrps[i].setBorder(BorderFactory.createLoweredBevelBorder());
                colGrps[i].setBackground(ColorHandler.getColor(i));
                colGrps[i].setPreferredSize(new Dimension(30, 20));
                iColorPanel.add(colGrpLab[i]);
                iColorPanel.add(colGrps[i]);
            }

        }
        return iColorPanel;
    }

    private JPanel getAAPanel()
    {
        if(iAAPanel == null)
        {
            iAAPanel = new JPanel();
            iAAPanel.setLayout(new FlowLayout());
            iAAPanel.setPreferredSize(new Dimension(150, 555));
            iAAPanel.setBorder(BorderFactory.createTitledBorder("Amino acid - group"));
            JLabel aLab = new JLabel();
            aLab.setText("A:     ");
            aaCmb[0] = getAACombo('A');
            aaCmb[0].setSelectedIndex(ColorHandler.getAAColorGroup('A'));
            iAAPanel.add(aLab);
            iAAPanel.add(aaCmb[0]);
            JLabel cLab = new JLabel();
            cLab.setText("C:     ");
            aaCmb[1] = getAACombo('C');
            aaCmb[1].setSelectedIndex(ColorHandler.getAAColorGroup('C'));
            iAAPanel.add(cLab);
            iAAPanel.add(aaCmb[1]);
            JLabel dLab = new JLabel();
            dLab.setText("D:     ");
            aaCmb[2] = getAACombo('D');
            aaCmb[2].setSelectedIndex(ColorHandler.getAAColorGroup('D'));
            iAAPanel.add(dLab);
            iAAPanel.add(aaCmb[2]);
            JLabel eLab = new JLabel();
            eLab.setText("E:     ");
            aaCmb[3] = getAACombo('E');
            aaCmb[3].setSelectedIndex(ColorHandler.getAAColorGroup('E'));
            iAAPanel.add(eLab);
            iAAPanel.add(aaCmb[3]);
            JLabel fLab = new JLabel();
            fLab.setText("F:     ");
            aaCmb[4] = getAACombo('F');
            aaCmb[4].setSelectedIndex(ColorHandler.getAAColorGroup('F'));
            iAAPanel.add(fLab);
            iAAPanel.add(aaCmb[4]);
            JLabel gLab = new JLabel();
            gLab.setText("G:     ");
            aaCmb[5] = getAACombo('G');
            aaCmb[5].setSelectedIndex(ColorHandler.getAAColorGroup('G'));
            iAAPanel.add(gLab);
            iAAPanel.add(aaCmb[5]);
            JLabel hLab = new JLabel();
            hLab.setText("H:     ");
            aaCmb[6] = getAACombo('H');
            aaCmb[6].setSelectedIndex(ColorHandler.getAAColorGroup('H'));
            iAAPanel.add(hLab);
            iAAPanel.add(aaCmb[6]);
            JLabel iLab = new JLabel();
            iLab.setText("I:     ");
            aaCmb[7] = getAACombo('I');
            aaCmb[7].setSelectedIndex(ColorHandler.getAAColorGroup('I'));
            iAAPanel.add(iLab);
            iAAPanel.add(aaCmb[7]);
            JLabel kLab = new JLabel();
            kLab.setText("K:     ");
            aaCmb[8] = getAACombo('K');
            aaCmb[8].setSelectedIndex(ColorHandler.getAAColorGroup('K'));
            iAAPanel.add(kLab);
            iAAPanel.add(aaCmb[8]);
            JLabel lLab = new JLabel();
            lLab.setText("L:     ");
            aaCmb[9] = getAACombo('L');
            aaCmb[9].setSelectedIndex(ColorHandler.getAAColorGroup('L'));
            iAAPanel.add(lLab);
            iAAPanel.add(aaCmb[9]);
            JLabel mLab = new JLabel();
            mLab.setText("M:     ");
            aaCmb[10] = getAACombo('M');
            aaCmb[10].setSelectedIndex(ColorHandler.getAAColorGroup('M'));
            iAAPanel.add(mLab);
            iAAPanel.add(aaCmb[10]);
            JLabel nLab = new JLabel();
            nLab.setText("N:     ");
            aaCmb[11] = getAACombo('N');
            aaCmb[11].setSelectedIndex(ColorHandler.getAAColorGroup('N'));
            iAAPanel.add(nLab);
            iAAPanel.add(aaCmb[11]);
            JLabel pLab = new JLabel();
            pLab.setText("P:     ");
            aaCmb[12] = getAACombo('P');
            aaCmb[12].setSelectedIndex(ColorHandler.getAAColorGroup('P'));
            iAAPanel.add(pLab);
            iAAPanel.add(aaCmb[12]);
            JLabel qLab = new JLabel();
            qLab.setText("Q:     ");
            aaCmb[13] = getAACombo('Q');
            aaCmb[13].setSelectedIndex(ColorHandler.getAAColorGroup('Q'));
            iAAPanel.add(qLab);
            iAAPanel.add(aaCmb[13]);
            JLabel rLab = new JLabel();
            rLab.setText("R:     ");
            aaCmb[14] = getAACombo('R');
            aaCmb[14].setSelectedIndex(ColorHandler.getAAColorGroup('R'));
            iAAPanel.add(rLab);
            iAAPanel.add(aaCmb[14]);
            JLabel sLab = new JLabel();
            sLab.setText("S:     ");
            aaCmb[15] = getAACombo('S');
            aaCmb[15].setSelectedIndex(ColorHandler.getAAColorGroup('S'));
            iAAPanel.add(sLab);
            iAAPanel.add(aaCmb[15]);
            JLabel tLab = new JLabel();
            tLab.setText("T:     ");
            aaCmb[16] = getAACombo('T');
            aaCmb[16].setSelectedIndex(ColorHandler.getAAColorGroup('T'));
            iAAPanel.add(tLab);
            iAAPanel.add(aaCmb[16]);
            JLabel vLab = new JLabel();
            vLab.setText("V:     ");
            aaCmb[17] = getAACombo('V');
            aaCmb[17].setSelectedIndex(ColorHandler.getAAColorGroup('V'));
            iAAPanel.add(vLab);
            iAAPanel.add(aaCmb[17]);
            JLabel wLab = new JLabel();
            wLab.setText("W:     ");
            aaCmb[18] = getAACombo('W');
            aaCmb[18].setSelectedIndex(ColorHandler.getAAColorGroup('W'));
            iAAPanel.add(wLab);
            iAAPanel.add(aaCmb[18]);
            JLabel yLab = new JLabel();
            yLab.setText("Y:     ");
            aaCmb[19] = getAACombo('Y');
            aaCmb[19].setSelectedIndex(ColorHandler.getAAColorGroup('Y'));
            iAAPanel.add(yLab);
            iAAPanel.add(aaCmb[19]);
            JLabel gapsLab = new JLabel();
            gapsLab.setText("- (gaps):");
            aaCmb[20] = getAACombo('-');
            aaCmb[20].setSelectedIndex(ColorHandler.getAAColorGroup('-'));
            iAAPanel.add(gapsLab);
            iAAPanel.add(aaCmb[20]);
        }
        return iAAPanel;
    }

    private AACombo getAACombo(char aa)
    {
        AACombo box = new AACombo(aa);
        box.addItem("1");
        box.addItem("2");
        box.addItem("3");
        box.addItem("4");
        box.addItem("5");
        box.addItem("6");
        box.addItem("7");
        box.addItem("8");
        box.addItem("9");
        box.addItem("10");
        box.addItem("11");
        box.addItem("12");
        box.addItem("13");
        box.addItem("14");
        box.addItem("15");
        box.addItem("16");
        box.addItem("17");
        box.addItem("18");
        box.addItem("19");
        box.addItem("20");
        box.addItem("21");
        box.setPreferredSize(new Dimension(70, 20));
        return box;
    }

    private JPanel getOkPanel()
    {
        JPanel okPanel = new JPanel();
        okPanel.setPreferredSize(new Dimension(290, 40));
        okPanel.setLayout(new BoxLayout(okPanel, 2));
        JButton okButton = new JButton();
        okButton.setText("OK");
        okButton.setActionCommand("ok");
        JButton cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setActionCommand("cancel");
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        okPanel.add(Box.createHorizontalGlue());
        okPanel.add(cancelButton);
        okPanel.add(okButton);
        return okPanel;
    }

    private JPanel getColorSchemePanel()
    {
        JPanel csPanel = new JPanel();
        csPanel.setPreferredSize(new Dimension(290, 100));
        csPanel.setLayout(new FlowLayout());
        csPanel.setBorder(BorderFactory.createTitledBorder("Color schemes"));
        JLabel lab = new JLabel("Default schemes");
        csPanel.add(lab);
        iDefCmb = new JComboBox();
        
	iDefCmb.addItem("Cinema");
        iDefCmb.addItem("Web Logo");
	iDefCmb.addItem("Lesk");
        iDefCmb.addItem("Clustal X");
        iDefCmb.addItem("Mod. Cinema");
        iDefCmb.addItem("Cysteine");
        iDefCmb.addItem("Zinc Finger");
	iDefCmb.addItem("Helix Breaker");
	iDefCmb.addItem("The Cell");
	
        iDefCmb.setActionCommand("SchemeCmb");
        iDefCmb.addActionListener(this);
        csPanel.add(iDefCmb);
        JButton loadButton = new JButton();
        loadButton.setText("Load");
        loadButton.setActionCommand("Load");
        JButton saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.setActionCommand("Save");
        loadButton.addActionListener(this);
        saveButton.addActionListener(this);
        csPanel.add(saveButton);
        csPanel.add(loadButton);
        return csPanel;
    }

    public void actionPerformed(ActionEvent e)
    {
        if("ok".equals(e.getActionCommand()))
        {
            doColorChange();
            LogoBar.updateGraph();
            dispose();
        } else
        if("cancel".equals(e.getActionCommand()))
        {
            System.out.println("cancel clicked");
            dispose();
        } else
        if("SchemeCmb".equals(e.getActionCommand()))
            defColorSchemeChanged();
        else
        if("Save".equals(e.getActionCommand()))
            try
            {
                saveColorScheme();
            }
            catch(Exception ex)
            {
                System.out.println("Error while saving color file");
            }
        else
        if("Load".equals(e.getActionCommand()))
            try
            {
                loadColorScheme();
            }
            catch(Exception ex)
            {
                System.out.println("Error while loading color file");
            }
    }

    private void defColorSchemeChanged()
    {
        int selIdx = iDefCmb.getSelectedIndex();
        switch(selIdx)
        {
        case 0: // '\0'
            setCinemaScheme();
            break;

        case 1: // '\001'
            setWebLogoScheme();
            break;

       

        case 2: // '\003'
            setLeskScheme();
            break;

        case 3: // '\004'
            setClustalScheme();
            break;

        case 4: // '\005'
            setModClustalScheme();
            break;

        case 5: // '\006'
            setCystineScheme();
            break;

        case 6: // '\007'
            setZincScheme();
            break;
	case 7: // '\007'
            setHelixBreakScheme();
            break;
	case 8: // '\002'
            setCellScheme();
            break;
	    
        default:
            setCellScheme();
            break;
        }
    }

    private void doColorChange()
    {
        for(int i = 0; i < 21; i++)
        {
            Color col = colGrps[i].getBackground();
            ColorHandler.setColorGroup(i, col);
            int sel_grp_idx = aaCmb[i].getSelectedIndex();
            ColorHandler.setAAColorGroup(aaCmb[i].AA, sel_grp_idx);
        }

    }

    private void setWebLogoScheme()
    {
        colGrps[0].setBackground(ColorHandler.DarkGreen); //Green
    colGrps[1].setBackground(ColorHandler.LogoBlue);   //LogoBar blue
        colGrps[2].setBackground(new Color(255, 0, 0)); //red
        colGrps[3].setBackground(Color.lightGray);      //Grey
	colGrps[4].setBackground(Color.magenta);        //Magenta
        for(int i = 5; i < 21; i++)
            colGrps[i].setBackground(new Color(255, 255, 255));

        aaCmb[0].setSelectedIndex(3);  //A
        aaCmb[1].setSelectedIndex(0);  //C
        aaCmb[2].setSelectedIndex(2);  //D
        aaCmb[3].setSelectedIndex(2);  //E
        aaCmb[4].setSelectedIndex(3);  //F
        aaCmb[5].setSelectedIndex(0);  //G
        aaCmb[6].setSelectedIndex(1);  //H
        aaCmb[7].setSelectedIndex(3);  //I
        aaCmb[8].setSelectedIndex(1);  //K
        aaCmb[9].setSelectedIndex(3);  //L
        aaCmb[10].setSelectedIndex(3); //M
        aaCmb[11].setSelectedIndex(4); //N
        aaCmb[12].setSelectedIndex(3); //P
        aaCmb[13].setSelectedIndex(4); //Q
        aaCmb[14].setSelectedIndex(1); //R
        aaCmb[15].setSelectedIndex(0); //S
        aaCmb[16].setSelectedIndex(0); //T
        aaCmb[17].setSelectedIndex(3); //V
        aaCmb[18].setSelectedIndex(3); //W
        aaCmb[19].setSelectedIndex(0); //Y
        aaCmb[20].setSelectedIndex(20); //-
    }

    void setCellScheme()
    {
        colGrps[0].setBackground(Color.red);
        colGrps[1].setBackground(ColorHandler.LogoBlue);
        colGrps[2].setBackground(ColorHandler.DarkGreen);
        colGrps[3].setBackground(Color.orange);
        for(int i = 4; i < 21; i++)
            colGrps[i].setBackground(new Color(255, 255, 255));

        aaCmb[0].setSelectedIndex(3);  //A
        aaCmb[1].setSelectedIndex(3);  //C
        aaCmb[2].setSelectedIndex(0);  //D
        aaCmb[3].setSelectedIndex(0);  //E
        aaCmb[4].setSelectedIndex(3);  //F
        aaCmb[5].setSelectedIndex(3);  //G
        aaCmb[6].setSelectedIndex(1);  //H
        aaCmb[7].setSelectedIndex(3);  //I
        aaCmb[8].setSelectedIndex(1);  //K
        aaCmb[9].setSelectedIndex(3);  //L
        aaCmb[10].setSelectedIndex(3); //M
        aaCmb[11].setSelectedIndex(2); //N
        aaCmb[12].setSelectedIndex(3); //P
        aaCmb[13].setSelectedIndex(2); //Q
        aaCmb[14].setSelectedIndex(1); //R
        aaCmb[15].setSelectedIndex(2); //S
        aaCmb[16].setSelectedIndex(2); //T
        aaCmb[17].setSelectedIndex(3); //V
        aaCmb[18].setSelectedIndex(3); //W
        aaCmb[19].setSelectedIndex(2); //Y
        aaCmb[20].setSelectedIndex(20); //Gap
    }

    private void setCinemaScheme()
    {
        colGrps[0].setBackground(Color.red);
        colGrps[1].setBackground(ColorHandler.LogoBlue);
        colGrps[2].setBackground(Color.green);
        colGrps[3].setBackground(new Color(180, 115, 225));
        colGrps[4].setBackground(Color.yellow);
        colGrps[5].setBackground(Color.lightGray);
        colGrps[6].setBackground(new Color(185, 138, 120));
        for(int i = 7; i < 21; i++)
            colGrps[i].setBackground(new Color(255, 255, 255));

        aaCmb[0].setSelectedIndex(5);
        aaCmb[1].setSelectedIndex(4);
        aaCmb[2].setSelectedIndex(0);
        aaCmb[3].setSelectedIndex(0);
        aaCmb[4].setSelectedIndex(3);
        aaCmb[5].setSelectedIndex(6);
        aaCmb[6].setSelectedIndex(1);
        aaCmb[7].setSelectedIndex(5);
        aaCmb[8].setSelectedIndex(1);
        aaCmb[9].setSelectedIndex(5);
        aaCmb[10].setSelectedIndex(5);
        aaCmb[11].setSelectedIndex(2);
        aaCmb[12].setSelectedIndex(6);
        aaCmb[13].setSelectedIndex(2);
        aaCmb[14].setSelectedIndex(1);
        aaCmb[15].setSelectedIndex(2);
        aaCmb[16].setSelectedIndex(2);
        aaCmb[17].setSelectedIndex(5);
        aaCmb[18].setSelectedIndex(3);
        aaCmb[19].setSelectedIndex(3);
        aaCmb[20].setSelectedIndex(20);
    }

    private void setLeskScheme()
    {
        colGrps[0].setBackground(Color.yellow);
        colGrps[1].setBackground(Color.green);
        colGrps[2].setBackground(Color.magenta);
        colGrps[3].setBackground(Color.red);
        colGrps[4].setBackground(ColorHandler.LogoBlue);
        for(int i = 5; i < 21; i++)
            colGrps[i].setBackground(new Color(255, 255, 255));

        aaCmb[0].setSelectedIndex(0);
        aaCmb[1].setSelectedIndex(1);
        aaCmb[2].setSelectedIndex(3);
        aaCmb[3].setSelectedIndex(3);
        aaCmb[4].setSelectedIndex(1);
        aaCmb[5].setSelectedIndex(0);
        aaCmb[6].setSelectedIndex(2);
        aaCmb[7].setSelectedIndex(1);
        aaCmb[8].setSelectedIndex(4);
        aaCmb[9].setSelectedIndex(1);
        aaCmb[10].setSelectedIndex(1);
        aaCmb[11].setSelectedIndex(2);
        aaCmb[12].setSelectedIndex(1);
        aaCmb[13].setSelectedIndex(2);
        aaCmb[14].setSelectedIndex(4);
        aaCmb[15].setSelectedIndex(0);
        aaCmb[16].setSelectedIndex(0);
        aaCmb[17].setSelectedIndex(1);
        aaCmb[18].setSelectedIndex(1);
        aaCmb[19].setSelectedIndex(1);
        aaCmb[20].setSelectedIndex(20);
    }

    private void setClustalScheme()
    {
        colGrps[0].setBackground(Color.orange);
        colGrps[1].setBackground(Color.yellow);
        colGrps[2].setBackground(Color.red);
        colGrps[3].setBackground(Color.magenta);
        colGrps[4].setBackground(Color.green);
        colGrps[5].setBackground(Color.cyan);
        colGrps[6].setBackground(Color.pink);
        colGrps[7].setBackground(ColorHandler.LogoBlue);
        for(int i = 8; i < 21; i++)
            colGrps[i].setBackground(new Color(255, 255, 255));

        aaCmb[0].setSelectedIndex(7);
        aaCmb[1].setSelectedIndex(6);
        aaCmb[2].setSelectedIndex(3);
        aaCmb[3].setSelectedIndex(3);
        aaCmb[4].setSelectedIndex(7);
        aaCmb[5].setSelectedIndex(0);
        aaCmb[6].setSelectedIndex(5);
        aaCmb[7].setSelectedIndex(7);
        aaCmb[8].setSelectedIndex(2);
        aaCmb[9].setSelectedIndex(7);
        aaCmb[10].setSelectedIndex(7);
        aaCmb[11].setSelectedIndex(4);
        aaCmb[12].setSelectedIndex(1);
        aaCmb[13].setSelectedIndex(4);
        aaCmb[14].setSelectedIndex(2);
        aaCmb[15].setSelectedIndex(4);
        aaCmb[16].setSelectedIndex(4);
        aaCmb[17].setSelectedIndex(7);
        aaCmb[18].setSelectedIndex(7);
        aaCmb[19].setSelectedIndex(5);
        aaCmb[20].setSelectedIndex(20);
    }

    private void setModClustalScheme()
    {
        colGrps[0].setBackground(Color.green);
        colGrps[1].setBackground(ColorHandler.DarkGreen);
        colGrps[2].setBackground(Color.red);
        colGrps[3].setBackground(ColorHandler.LogoBlue);
        colGrps[4].setBackground(new Color(180, 115, 225));//Purple
        colGrps[5].setBackground(new Color(177, 240, 250)); //Light cyan
        for(int i = 6; i < 21; i++)
            colGrps[i].setBackground(new Color(255, 255, 255));

        aaCmb[0].setSelectedIndex(5);
        aaCmb[1].setSelectedIndex(5);
        aaCmb[2].setSelectedIndex(2);
        aaCmb[3].setSelectedIndex(2);
        aaCmb[4].setSelectedIndex(1);
        aaCmb[5].setSelectedIndex(5);
        aaCmb[6].setSelectedIndex(3);
        aaCmb[7].setSelectedIndex(0);
        aaCmb[8].setSelectedIndex(3);
        aaCmb[9].setSelectedIndex(0);
        aaCmb[10].setSelectedIndex(0);
        aaCmb[11].setSelectedIndex(4); //N
        aaCmb[12].setSelectedIndex(5);
        aaCmb[13].setSelectedIndex(4);
        aaCmb[14].setSelectedIndex(3);
        aaCmb[15].setSelectedIndex(4);
        aaCmb[16].setSelectedIndex(4);
        aaCmb[17].setSelectedIndex(0);
        aaCmb[18].setSelectedIndex(1);
        aaCmb[19].setSelectedIndex(1);
        aaCmb[20].setSelectedIndex(20);
    }

    private void setCystineScheme()
    {
        colGrps[0].setBackground(Color.green);
        colGrps[1].setBackground(ColorHandler.DarkGreen);
        colGrps[2].setBackground(Color.red);
        colGrps[3].setBackground(ColorHandler.LogoBlue);
        colGrps[4].setBackground(new Color(180, 115, 225));
        colGrps[5].setBackground(new Color(177, 240, 250)); //Purple
        colGrps[6].setBackground(Color.yellow);
        for(int i = 7; i < 21; i++)
            colGrps[i].setBackground(new Color(255, 255, 255));

        aaCmb[0].setSelectedIndex(5);
        aaCmb[1].setSelectedIndex(6);
        aaCmb[2].setSelectedIndex(2);
        aaCmb[3].setSelectedIndex(2);
        aaCmb[4].setSelectedIndex(1);
        aaCmb[5].setSelectedIndex(5);
        aaCmb[6].setSelectedIndex(3);
        aaCmb[7].setSelectedIndex(0);
        aaCmb[8].setSelectedIndex(3);
        aaCmb[9].setSelectedIndex(0);
        aaCmb[10].setSelectedIndex(0);
        aaCmb[11].setSelectedIndex(4); //N
        aaCmb[12].setSelectedIndex(5);
        aaCmb[13].setSelectedIndex(4);
        aaCmb[14].setSelectedIndex(3);
        aaCmb[15].setSelectedIndex(4);
        aaCmb[16].setSelectedIndex(4);
        aaCmb[17].setSelectedIndex(0);
        aaCmb[18].setSelectedIndex(1);
        aaCmb[19].setSelectedIndex(1);
        aaCmb[20].setSelectedIndex(20);
    }

    private void setZincScheme()
    {
        colGrps[0].setBackground(Color.green);
        colGrps[1].setBackground(ColorHandler.DarkGreen);
        colGrps[2].setBackground(Color.red);
        colGrps[3].setBackground(ColorHandler.LogoBlue);
        colGrps[4].setBackground(new Color(180, 115, 225));
        colGrps[5].setBackground(new Color(177, 240, 250));
        colGrps[6].setBackground(Color.yellow);
        for(int i = 7; i < 21; i++)
            colGrps[i].setBackground(new Color(255, 255, 255));

        aaCmb[0].setSelectedIndex(5);
        aaCmb[1].setSelectedIndex(6);
        aaCmb[2].setSelectedIndex(2);
        aaCmb[3].setSelectedIndex(2);
        aaCmb[4].setSelectedIndex(1);
        aaCmb[5].setSelectedIndex(5);
        aaCmb[6].setSelectedIndex(6);
        aaCmb[7].setSelectedIndex(0);
        aaCmb[8].setSelectedIndex(3);
        aaCmb[9].setSelectedIndex(0);
        aaCmb[10].setSelectedIndex(0);
        aaCmb[11].setSelectedIndex(4);
        aaCmb[12].setSelectedIndex(5);
        aaCmb[13].setSelectedIndex(4);
        aaCmb[14].setSelectedIndex(3);
        aaCmb[15].setSelectedIndex(4);
        aaCmb[16].setSelectedIndex(4);
        aaCmb[17].setSelectedIndex(0);
        aaCmb[18].setSelectedIndex(1);
        aaCmb[19].setSelectedIndex(1);
        aaCmb[20].setSelectedIndex(20);
    }
    
    private void setHelixBreakScheme()
    {
        colGrps[0].setBackground(Color.green);
        colGrps[1].setBackground(ColorHandler.DarkGreen);
        colGrps[2].setBackground(Color.red);
        colGrps[3].setBackground(ColorHandler.LogoBlue);
        colGrps[4].setBackground(new Color(180, 115, 225));//Purple
        colGrps[5].setBackground(new Color(177, 240, 250)); 
	colGrps[6].setBackground(Color.yellow);
        for(int i = 7; i < 21; i++)
            colGrps[i].setBackground(new Color(255, 255, 255));

        aaCmb[0].setSelectedIndex(5);
        aaCmb[1].setSelectedIndex(5);
        aaCmb[2].setSelectedIndex(2);
        aaCmb[3].setSelectedIndex(2);
        aaCmb[4].setSelectedIndex(1);
        aaCmb[5].setSelectedIndex(6);  //G
        aaCmb[6].setSelectedIndex(3);
        aaCmb[7].setSelectedIndex(0);
        aaCmb[8].setSelectedIndex(3);
        aaCmb[9].setSelectedIndex(0);
        aaCmb[10].setSelectedIndex(0);
        aaCmb[11].setSelectedIndex(4); //N
        aaCmb[12].setSelectedIndex(6); //P
        aaCmb[13].setSelectedIndex(4);
        aaCmb[14].setSelectedIndex(3);
        aaCmb[15].setSelectedIndex(4);
        aaCmb[16].setSelectedIndex(4);
        aaCmb[17].setSelectedIndex(0);
        aaCmb[18].setSelectedIndex(1);
        aaCmb[19].setSelectedIndex(1);
        aaCmb[20].setSelectedIndex(20);
    }


    private void saveColorScheme()
        throws Exception
    {
        JFileChooser fileDlg = new JFileChooser();
        fileDlg.setDialogType(1);
        int retVal = fileDlg.showSaveDialog(this);
        if(retVal == 0)
        {
            String fName = fileDlg.getSelectedFile().getPath();
            int i = fName.lastIndexOf(46);
            if(i > 0 && i < fName.length() - 1)
            {
                String tmp = fName.substring(0, i);
                System.out.println("Substring: " + tmp);
                fName = tmp;
            }
            String resName = fName + ".lbr";
            try
            {
                saveScheme2File(resName);
            }
            catch(Exception e)
            {
                System.out.println("Error while saving color file");
                throw e;
            }
        }
    }

    private void saveScheme2File(String filename)
        throws Exception
    {
        PrintWriter outFile = null;
        try
        {
            outFile = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
        }
        catch(Exception e)
        {
            System.out.println("Error in opening file");
            throw e;
        }
        outFile.println("LogoBar Color Scheme file");
        outFile.println("NOTE: Do NOT modify this file!");
        outFile.println("Begin Colors");
        for(int j = 0; j < 21; j++)
        {
            Color c_tmp = colGrps[j].getBackground();
            outFile.print(c_tmp.getRed());
            outFile.print(' ');
            outFile.print(c_tmp.getGreen());
            outFile.print(' ');
            outFile.print(c_tmp.getBlue());
            outFile.println();
        }

        outFile.println("End Colors");
        outFile.println("Begin Amino Acids");
        for(int j = 0; j < 21; j++)
        {
            int sel_grp = aaCmb[j].getSelectedIndex();
            outFile.println(sel_grp);
        }

        outFile.println("End Amino Acids");
        outFile.println("End File");
        outFile.close();
    }

    private void loadColorScheme()
        throws Exception
    {
        JFileChooser fileDlg = new JFileChooser();
        int retVal = fileDlg.showOpenDialog(this);
        if(retVal == 0)
        {
            String fName = fileDlg.getSelectedFile().getPath();
            BufferedReader inFile = new BufferedReader(new FileReader(fName));
            String buffer = inFile.readLine();
            if(!buffer.equals("LogoBar Color Scheme file"))
                throw new Exception("Not a color scheme file");
	    buffer = inFile.readLine();
	    while(!buffer.equals("Begin Colors"))
		buffer = inFile.readLine();
	    for(int i = 0; i < 21; i++){
		buffer = inFile.readLine();
		StringTokenizer st = new StringTokenizer(buffer);
		int red = Integer.parseInt(st.nextToken());
		int green = Integer.parseInt(st.nextToken());
		int blue = Integer.parseInt(st.nextToken());
                colGrps[i].setBackground(new Color(red, green, blue));
		
            }
	    while(!buffer.equals("Begin Amino Acids"))
		buffer = inFile.readLine();
	    
	    
            for(int i = 0; i < 21; i++){
		buffer = inFile.readLine();
		StringTokenizer st = new StringTokenizer(buffer);
		int grp_idx = Integer.parseInt(st.nextToken());
		aaCmb[i].setSelectedIndex(grp_idx);
	    }
	    
            inFile.close();
        }
    }

    private static final String COLOR_HEADER = "LogoBar Color Scheme file";
    private JPanel iMainPane;
    private JPanel iColorPanel;
    private JPanel iAAPanel;
    private JComboBox iDefCmb;
    private GrpColor colGrps[];
    private AACombo aaCmb[];
}
