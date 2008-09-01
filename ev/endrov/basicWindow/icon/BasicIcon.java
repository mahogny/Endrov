package endrov.basicWindow.icon;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Commonly used icons
 * @author Johan Henriksson
 *
 */
public class BasicIcon
	{

	public static final ImageIcon iconButtonDelete=new ImageIcon(BasicIcon.class.getResource("iconDelete.png"));
	public static final ImageIcon iconButtonDown=new ImageIcon(BasicIcon.class.getResource("iconDown.png"));
	public static final ImageIcon iconButtonUp=new ImageIcon(BasicIcon.class.getResource("iconUp.png"));
	public static final ImageIcon iconButtonTrash=new ImageIcon(BasicIcon.class.getResource("iconTrash.png"));
	public static final ImageIcon iconButtonHelp=new ImageIcon(BasicIcon.class.getResource("iconHelp.png"));
	public static final ImageIcon iconButtonCopy=new ImageIcon(BasicIcon.class.getResource("iconCopy.png"));
	public static final ImageIcon iconButtonPaste=new ImageIcon(BasicIcon.class.getResource("iconPaste.png"));
	public static ImageIcon iconMenuQuit=new ImageIcon(BasicIcon.class.getResource("iconMenuQuit.png"));
	public static ImageIcon iconFramePrev=new ImageIcon(BasicIcon.class.getResource("iconFramePrev.png"));
	public static ImageIcon iconFrameNext=new ImageIcon(BasicIcon.class.getResource("iconFrameNext.png"));
	public static ImageIcon iconFrameFirst=new ImageIcon(BasicIcon.class.getResource("iconFrameFirst.png"));
	public static ImageIcon iconFrameLast=new ImageIcon(BasicIcon.class.getResource("iconFrameLast.png"));
	public static ImageIcon iconPlayBackward=new ImageIcon(BasicIcon.class.getResource("iconPlayBackward.png"));
	public static ImageIcon iconPlayForward=new ImageIcon(BasicIcon.class.getResource("iconPlayForward.png"));
	public static ImageIcon iconPlayStop=new ImageIcon(BasicIcon.class.getResource("iconPlayStop.png"));

	
	
	
	
	public static JButton getButtonCopy()
		{
		JButton b=new JButton(iconButtonCopy);
		b.setToolTipText("Copy");
		return b;
		}
	
	public static JButton getButtonPaste()
		{
		JButton b=new JButton(iconButtonPaste);
		b.setToolTipText("Paste");
		return b;
		}
	
	public static JButton getButtonDelete()
		{
		JButton b=new JButton(iconButtonDelete);
		b.setToolTipText("Remove");
		return b;
		}
	
	//	private static ImageIcon iconMenuMaint=new ImageIcon(BasicWindow.class.getResource("iconMenuMaint.png"));
	//	private static ImageIcon iconMenuInfo=new ImageIcon(BasicWindow.class.getResource("iconMenuInfo.png"));

	}
