package endrov.basicWindow.icon;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import endrov.util.JImageButton;

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
	public static final ImageIcon iconButtonTrash=new ImageIcon(BasicIcon.class.getResource("unkTrash.png"));
	public static final ImageIcon iconButtonHelp=new ImageIcon(BasicIcon.class.getResource("iconHelp.png"));
	public static final ImageIcon iconButtonCopy=new ImageIcon(BasicIcon.class.getResource("iconCopy.png"));
	public static final ImageIcon iconButtonPaste=new ImageIcon(BasicIcon.class.getResource("iconPaste.png"));
	public static final ImageIcon iconMenuNew=new ImageIcon(BasicIcon.class.getResource("gnomeNew.png"));
	public static final ImageIcon iconMenuLoad=new ImageIcon(BasicIcon.class.getResource("gnomeOpen.png"));
	public static final ImageIcon iconMenuQuit=new ImageIcon(BasicIcon.class.getResource("gnomeQuit.png"));
	public static final ImageIcon iconFramePrev=new ImageIcon(BasicIcon.class.getResource("iconFramePrev.png"));
	public static final ImageIcon iconFrameNext=new ImageIcon(BasicIcon.class.getResource("iconFrameNext.png"));
	public static final ImageIcon iconFrameFirst=new ImageIcon(BasicIcon.class.getResource("iconFrameFirst.png"));
	public static final ImageIcon iconFrameLast=new ImageIcon(BasicIcon.class.getResource("iconFrameLast.png"));
	public static final ImageIcon iconPlayBackward=new ImageIcon(BasicIcon.class.getResource("iconPlayBackward.png"));
	public static final ImageIcon iconPlayForward=new ImageIcon(BasicIcon.class.getResource("iconPlayForward.png"));
	public static final ImageIcon iconPlayStop=new ImageIcon(BasicIcon.class.getResource("iconPlayStop.png"));
	public static final ImageIcon iconController=new ImageIcon(BasicIcon.class.getResource("iconController.png"));
	
	public static final ImageIcon iconSave=new ImageIcon(BasicIcon.class.getResource("tangoSave.png"));
	
	
	public static JButton getButtonCopy()
		{
		return new JImageButton(iconButtonCopy,"Copy");
		}
	
	public static JButton getButtonPaste()
		{
		return new JImageButton(iconButtonPaste,"Paste");
		}
	
	public static JButton getButtonDelete()
		{
		return new JImageButton(iconButtonDelete,"Remove");
		}
	
	public static JButton getButtonSave()
		{
		return new JImageButton(iconSave,"Save");
		}
	
	//	private static ImageIcon iconMenuMaint=new ImageIcon(BasicWindow.class.getResource("iconMenuMaint.png"));
	//	private static ImageIcon iconMenuInfo=new ImageIcon(BasicWindow.class.getResource("iconMenuInfo.png"));

	}
