package endrov.nucImage;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.EvComboObjectOne;
import endrov.basicWindow.EvComboSimpleFrame;
import endrov.data.EvData;
import endrov.imageset.EvChannel;
import endrov.nuc.NucLineage;
import endrov.nucImage.LineagingAlgorithm.LineageAlgorithmDef;
import endrov.util.EvSwingUtil;

/**
 * Window letting user control an algorithm for automatic lineaging
 * 
 * @author Johan Henriksson
 *
 */
public class WindowAutoLineaging extends BasicWindow
	{
	private static final long serialVersionUID = 1L;

	private EvComboObjectOne<NucLineage> comboLin=new EvComboObjectOne<NucLineage>(new NucLineage(), true, true);
	private EvComboObjectOne<EvChannel> comboChan=new EvComboObjectOne<EvChannel>(new EvChannel(), true, false);
	private JComboBox comboAlgo=new JComboBox(LineageAlgorithmDef.listAlgorithms);

	private JPanel panelOptions=new JPanel();
	private JLabel panelStatus=new JLabel();
	
	private JButton bStart=new JButton("Start");
	private JButton bStop=new JButton("Stop");
	
	private EvComboSimpleFrame frameStart=new EvComboSimpleFrame();
	
	/**
	 * Create and display window
	 */
	public WindowAutoLineaging()
		{
		setLayout(new GridLayout(1,1));
		
		JPanel top=new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.gridy=0; top.add(new JLabel("Lineage "),c);
		c.gridy=1; top.add(new JLabel("Channel "),c);
		c.gridy=2; top.add(new JLabel("Algorithm "),c);
		
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridx=1;
		c.weightx=1;
		
		c.gridy=0; top.add(comboLin,c);
		c.gridy=1; top.add(comboChan,c);
		c.gridy=2; top.add(comboAlgo,c);
		
		panelOptions.setBorder(BorderFactory.createTitledBorder("Options"));

		
		add(EvSwingUtil.compactVertical(
				top,
				panelOptions,
				panelStatus,
				EvSwingUtil.withLabel("Start frame", frameStart),
				EvSwingUtil.packEvenHorizontal(bStart,bStop)));
		
		setTitleEvWindow("Auto-lineage");
		packEvWindow();
		setVisibleEvWindow(true);
		}
	
	
	
	
	
	public void dataChangedEvent()
		{
		}

	public void freeResources()
		{
		}

	public void loadedFile(EvData data)
		{
		}

	public void windowSavePersonalSettings(Element e)
		{
		}

	
	
	}
