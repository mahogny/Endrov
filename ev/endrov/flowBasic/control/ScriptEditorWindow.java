/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.control;

import java.awt.BorderLayout;
import java.awt.Rectangle;
//import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvData;


/**
 * Editor for scripts
 * @author Johan Henriksson
 *
 */
public class ScriptEditorWindow extends BasicWindow implements ActionListener
	{
	static final long serialVersionUID=0;
	//To avoid double-opening windows
	//Note that the order has to be strange or windows will not unload properly!
	public static WeakHashMap<ScriptEditorWindow,FlowUnitScript> editors=new WeakHashMap<ScriptEditorWindow, FlowUnitScript>();
	
	//Or should it be a tabbed editor? can be changed later. prepare for it
	private FlowUnitScript unit;
	
	private JTextArea textArea=new JTextArea();
	
	//normal Edit menu
	//buttons Save etc, keyboard shortcut
	//proper name in title. need an ID of sort
	
	JButton bSave=BasicIcon.getButtonSave();
	JMenuItem miSave=new JMenuItem("Save",BasicIcon.iconMenuSave);
	
	private final JMenu menuEdit=new JMenu("Edit");
	
	public ScriptEditorWindow(FlowUnitScript u)
		{
		unit=u;
		
		
		textArea.setText(unit.code);
		JScrollPane scroll=new JScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		//Re-code enter to insert the appropriate number of spaces on the next line.
		//Simple version
		textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "mynewline");
		textArea.getActionMap().put("mynewline", new TextAction("mynewline") {
			static final long serialVersionUID=0;
      public void actionPerformed(ActionEvent e) 
      	{
      	JTextComponent comp=getTextComponent(e);
      	if(comp.getSelectionStart() == comp.getSelectionEnd())
      		{
      		try
						{
						int pos = comp.getCaretPosition();
						Document doc = comp.getDocument();
						String part = doc.getText(0, pos);
						int lastEnter=part.lastIndexOf('\n');
						if(lastEnter==-1)
							lastEnter=0;
						int endws=lastEnter+1;
						String toadd="";
						for(;endws<pos;endws++)
							{
							char c=part.charAt(endws);
							if(c==' ' || c=='\t')
								toadd=toadd+c;
							else
								break;
							}
						doc.insertString(pos, "\n"+toadd, null);
						comp.moveCaretPosition(pos+1+toadd.length());
						}
					catch (BadLocationException e2)
						{
						e2.printStackTrace();
						}
      		}
        }
		});
		//Highlight save button when changed
		textArea.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent arg0){}
			public void keyReleased(KeyEvent arg0){}
			public void keyTyped(KeyEvent arg0){bSave.setEnabled(true);}
		});
		
		//Action listeners
		bSave.setEnabled(false);
		bSave.addActionListener(this);
		miSave.addActionListener(this);
		miSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));

		//Menu
		menuEdit.add(miSave);

		JPanel bPanel=new JPanel(new GridLayout(1,1));
		bPanel.add(bSave);
		JPanel top=new JPanel(new BorderLayout());
		top.add(bPanel,BorderLayout.WEST);
		setLayout(new BorderLayout());
		add(top,BorderLayout.NORTH);
		add(scroll,BorderLayout.CENTER);
		addMenubar(menuEdit);
		
		setTitleEvWindow("Edit Script "+u.getScriptID());
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(new Rectangle(300,400));
		}
	
	
	public void save()
		{
		unit.code=textArea.getText();
		SwingUtilities.invokeLater(new Runnable(){public void run(){bSave.setEnabled(false);}});
		}
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bSave || e.getSource()==miSave)
			save();
		}
	
	
	public static void openEditor(FlowUnitScript u)
		{
		for(Map.Entry<ScriptEditorWindow, FlowUnitScript> e:editors.entrySet())
			if(e.getValue()==u)
				{
				e.getKey().toFront();
				return;
				}
		editors.put(new ScriptEditorWindow(u),u);
		}
	
	
	public void loadedFile(EvData data){}
	public void windowSavePersonalSettings(Element e){}
	public void freeResources()
		{
		textArea.getActionMap().clear();
		}
	public void dataChangedEvent(){}
	
	
	
	public static void main(String arg[])
		{
		new ScriptEditorWindow(new FlowUnitScript());
		
		
		
		}
	}
