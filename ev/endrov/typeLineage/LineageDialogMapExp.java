package endrov.typeLineage;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.EvComboObjectOne;
import endrov.gui.window.EvBasicWindow;
import endrov.typeLineage.util.LineageMergeUtil;

/**
 * Dialog to remap expression patterns
 * 
 * @author Johan Henriksson
 *
 */
public class LineageDialogMapExp extends EvBasicWindow implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	
	public EvComboObjectOne<Lineage> comboLin=new EvComboObjectOne<Lineage>(new Lineage(), false, false);
	public JComboBox cOldExpName=new JComboBox/*<String>*/();
	public JTextField tfNewExpName=new JTextField("exp");
	
	private JButton bOk=new JButton("Ok");
	
	private Lineage toLin;
	
	public LineageDialogMapExp(Lineage toLin)
		{
		this.toLin=toLin;
		
		setLayout(new BorderLayout());
		
		add(EvSwingUtil.layoutTableCompactWide(
				new JLabel("From lineage: "), comboLin,
				new JLabel("Old exp name: "), cOldExpName,
				new JLabel("New exp name: "), tfNewExpName
				), BorderLayout.CENTER);
		
		add(bOk, BorderLayout.SOUTH);
		
		comboLin.addActionListener(this);
		
		bOk.addActionListener(this);
		
		dataChangedEvent();
		
		setTitleEvWindow("Integreate expression");
		packEvWindow();
		setVisibleEvWindow(true);
		}

	@Override
	public void dataChangedEvent()
		{
		comboLin.updateList();
		updateExpList();
		}

	private void updateExpList()
		{
		Lineage lin=comboLin.getSelectedObject();
		Vector<String> expList=new Vector<String>();
		if(lin!=null)
			expList.addAll(lin.getAllExpNames());
		cOldExpName.setModel(new DefaultComboBoxModel/*<String>*/(expList));
		}
	
	@Override
	public void windowFreeResources()
		{
		}

	@Override
	public void windowEventUserLoadedFile(EvData data)
		{
		}

	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}

	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bOk)
			{
			Lineage fromLin=comboLin.getSelectedObject();
			String fromExpName=(String)cOldExpName.getSelectedItem();
			String toExpName=tfNewExpName.getText();
			
			if(toLin!=null && fromExpName!=null)
				{
				LineageMergeUtil.mapExpression(fromLin, toLin, fromExpName, toExpName);
				EvBasicWindow.updateWindows();
				EvBasicWindow.showInformativeDialog("Done");
				}
			else
				EvBasicWindow.showInformativeDialog("Could not be done; select a lineage and expression name");
			}
		else if(e.getSource()==comboLin)
			{
			updateExpList();
			}
		
		}

	@Override
	public String windowHelpTopic()
		{
		return null;
		}
	}
