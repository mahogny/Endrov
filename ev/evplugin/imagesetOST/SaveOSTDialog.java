package evplugin.imagesetOST;

import javax.swing.JFrame;
import evplugin.imageset.Imageset;

/**
 * Dialog for selecting channel compression settings
 * @author Johan Henriksson
 */
public class SaveOSTDialog extends JFrame
	{
	static final long serialVersionUID=0;
	
	public SaveOSTDialog(Imageset rec)
		{
		
		
		
		}
	
	
	/*
	
	String qualitys=JOptionPane.showInputDialog("Quality between 0 and 1? 1 for png (lossless), 0.99 for very high quality jpg (lossy)");
	if(qualitys!=null)
		{
		double quality=Double.parseDouble(qualitys);
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(EvData.getLastDataPath()));
		int returnVal = chooser.showSaveDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION)
			{
			BatchThread thread=new SaveOSTThread((Imageset)meta, chooser.getSelectedFile().getAbsolutePath(),quality);
			new BatchWindow(thread);
			}
		}
	
	*/
	
	}