package endrov.nuc;

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

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.EvComboObjectOne;
import endrov.data.EvData;
import endrov.util.EvSwingUtil;

/**
 * Dialog to remap expression patterns
 * 
 * @author Johan Henriksson
 *
 */
public class NucDialogMapExp extends BasicWindow implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	
	public EvComboObjectOne<NucLineage> comboLin=new EvComboObjectOne<NucLineage>(new NucLineage(), false, false);
	public JComboBox cOldExpName=new JComboBox(new Vector<String>());
	public JTextField tfNewExpName=new JTextField("exp");
	
	private JButton bOk=new JButton("Ok");
	
	private NucLineage toLin;
	
	public NucDialogMapExp(NucLineage toLin)
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
		NucLineage lin=comboLin.getSelectedObject();
		Vector<String> expList=new Vector<String>();
		if(lin!=null)
			expList.addAll(lin.getAllExpNames());
		cOldExpName.setModel(new DefaultComboBoxModel(expList));
		}
	
	@Override
	public void freeResources()
		{
		}

	@Override
	public void loadedFile(EvData data)
		{
		}

	@Override
	public void windowSavePersonalSettings(Element root)
		{
		}

	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bOk)
			{
			NucLineage fromLin=comboLin.getSelectedObject();
			String fromExpName=(String)cOldExpName.getSelectedItem();
			String toExpName=tfNewExpName.getText();
			
			if(toLin!=null && fromExpName!=null)
				{
				NucRemapUtil.mapExpression(fromLin, toLin, fromExpName, toExpName);
				BasicWindow.updateWindows();
				BasicWindow.showInformativeDialog("Done");
				}
			else
				BasicWindow.showInformativeDialog("Could not be done; select a lineage and expression name");
			}
		else if(e.getSource()==comboLin)
			{
			updateExpList();
			}
		
		}

	
	}
