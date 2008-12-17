package endrov.data;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

//this vs batch. scrap one? two different dialogs?

public class LoadProgressDialog extends JFrame implements EvData.FileIOStatusCallback
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0; 
	
	
	private JProgressBar pbar;
	private JLabel plabel=new JLabel("");
	private int curFile=0;
	private String status="Preparing to load";
	private double proc=0;
	
	public LoadProgressDialog(int numfiles)
		{
		pbar=new JProgressBar(0,100*numfiles);
		setTitle("Loading");
		setSize(500, 70);
		setLocationRelativeTo(null);
		setLayout(new GridLayout(2,1));
		add(pbar);
		add(plabel);
		}

	public void setCurFile(int curfile)
		{
		this.curFile=curfile;
		proc=0;
		safeRepaint();
		}
	
	
	
	public void fileIOStatus(double proc, String text)
		{
		status=text;
		this.proc=proc;
		safeRepaint();
		}
	
	
	private void safeRepaint()
		{
		final LoadProgressDialog f=this;
		SwingUtilities.invokeLater(new Runnable(){
		public void run(){
			f.updateValues();}
		});
		}
	
	private void updateValues()
		{
		setVisible(true);
		plabel.setText(status);
		pbar.setValue(100*curFile+(int)(100*proc));
		repaint();
		}
	
	
	}
