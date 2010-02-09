/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.imserv;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;

import bioserv.BioservGUI;

/**
 * imserv GUI panel for bioserv
 * @author Johan Henriksson
 */
public class ImservModulePanel extends JPanel implements ImservImpl.ImservListener,ActionListener
	{
	static final long serialVersionUID=0;

	private JList repList=new JList();
	
	private JButton bRepAdd=new JButton("Add");
	private JButton bRepRemove=new JButton("Remove");

	
	BioservGUI gui;
	ImservImpl imserv;
	
	public ImservModulePanel(BioservGUI gui, ImservImpl imserv)
		{
		this.gui=gui;
		this.imserv=imserv;

		bRepAdd.addActionListener(this);
		bRepRemove.addActionListener(this);

		JPanel repPanelButtons=new JPanel(new GridLayout(1,2));
		repPanelButtons.add(bRepAdd);
		repPanelButtons.add(bRepRemove);

		setLayout(new BorderLayout());
		add(new JScrollPane(repList),BorderLayout.CENTER);
		add(repPanelButtons,BorderLayout.SOUTH);


		}

	
	

	
	
	public void actionPerformed(ActionEvent e)
		{
	  if(e.getSource()==bRepAdd)
			{
			JFileChooser fc=new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int rval=fc.showOpenDialog(this);
			if(rval==JFileChooser.APPROVE_OPTION)
				imserv.addRepository(fc.getSelectedFile());
			}
		else if(e.getSource()==bRepRemove)
			{
			File file=(File)repList.getSelectedValue();
			if(file!=null)
				imserv.removeRepository(file);
			}
		
		
		}
	
	
	/**
	 * Callback: log message
	 */
	public void log(String s)
		{
		gui.log(s);
		}





	/**
	 * Callback: repository list updated
	 */
	public void repListUpdated()
		{
		SwingUtilities.invokeLater(new Runnable(){
		public void run()
			{
			repList.setModel(new ListModel(){
				public void addListDataListener(ListDataListener arg0){}
				public void removeListDataListener(ListDataListener arg0){}
				public Object getElementAt(int i)
					{
					return imserv.reps.get(i).dir;
//					return daemon.reps.get(i).dir.toString();
					}
				public int getSize()
					{
					return imserv.reps.size();
					}
			});
			}
		});
		}


	public String toString()
		{
		return "ImServ Repositories";
		}

	
		
	
	
	
	}
