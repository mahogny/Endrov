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

	public static final ImageIcon iconButtonDelete=new ImageIcon(BasicIcon.class.getResource("gnome-edit-delete.png"));
	public static final ImageIcon iconButtonDown=new ImageIcon(BasicIcon.class.getResource("tango-go-down.png"));
	public static final ImageIcon iconButtonUp=new ImageIcon(BasicIcon.class.getResource("tango-go-up.png"));
	public static final ImageIcon iconButtonTrash=new ImageIcon(BasicIcon.class.getResource("tangoTrash.png"));
	public static final ImageIcon iconButtonHelp=new ImageIcon(BasicIcon.class.getResource("iconHelp.png"));
	public static final ImageIcon iconButtonCopy=new ImageIcon(BasicIcon.class.getResource("tango-edit-copy.png"));
	public static final ImageIcon iconButtonPaste=new ImageIcon(BasicIcon.class.getResource("tango-edit-paste.png"));
	public static final ImageIcon iconMenuNew=new ImageIcon(BasicIcon.class.getResource("tango-document-new.png"));
	public static final ImageIcon iconMenuLoad=new ImageIcon(BasicIcon.class.getResource("tango-document-open.png"));
	public static final ImageIcon iconMenuSave=new ImageIcon(BasicIcon.class.getResource("tango-document-save.png"));
	public static final ImageIcon iconMenuSaveAs=new ImageIcon(BasicIcon.class.getResource("tango-document-save-as.png"));
	public static final ImageIcon iconMenuQuit=new ImageIcon(BasicIcon.class.getResource("gnome-application-exit.png"));
	public static final ImageIcon iconFramePrev=new ImageIcon(BasicIcon.class.getResource("tango-go-previous.png"));
	public static final ImageIcon iconFrameNext=new ImageIcon(BasicIcon.class.getResource("tango-go-next.png"));
	public static final ImageIcon iconFrameFirst=new ImageIcon(BasicIcon.class.getResource("tango-go-first.png"));
	public static final ImageIcon iconFrameLast=new ImageIcon(BasicIcon.class.getResource("tango-go-last.png"));
	public static final ImageIcon iconPlayBackward=new ImageIcon(BasicIcon.class.getResource("iconPlayBackward.png"));
	public static final ImageIcon iconPlayForward=new ImageIcon(BasicIcon.class.getResource("iconPlayForward.png"));
	public static final ImageIcon iconPlayStop=new ImageIcon(BasicIcon.class.getResource("iconPlayStop.png"));
	public static final ImageIcon iconController=new ImageIcon(BasicIcon.class.getResource("gnome-input-gaming.png"));

	public static final ImageIcon iconZoomFitBest=new ImageIcon(BasicIcon.class.getResource("gnome-zoom-fit-best.png"));

	public static final ImageIcon iconKeyboard=new ImageIcon(BasicIcon.class.getResource("tango-input-keyboard.png"));
	public static final ImageIcon iconImage=new ImageIcon(BasicIcon.class.getResource("gnome-image-x-generic.png"));

	public static final ImageIcon iconEndrov=new ImageIcon(BasicIcon.class.getResource("programIcon.png"));
	public static final ImageIcon iconData=new ImageIcon(BasicIcon.class.getResource("iconData.png"));

	public static final ImageIcon iconAdd=new ImageIcon(BasicIcon.class.getResource("oxygen-list-add.png"));
	public static final ImageIcon iconRemove=new ImageIcon(BasicIcon.class.getResource("oxygen-list-remove.png"));

	
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
		return new JImageButton(iconMenuSave,"Save");
		}

	public static ImageIcon iconLabelZoom=new ImageIcon(BasicIcon.class.getResource("silkMagnifier.png"));
	public static ImageIcon iconLabelRotate=new ImageIcon(BasicIcon.class.getResource("labelRotate.png"));
	
	//	private static ImageIcon iconMenuMaint=new ImageIcon(BasicWindow.class.getResource("iconMenuMaint.png"));
	//	private static ImageIcon iconMenuInfo=new ImageIcon(BasicWindow.class.getResource("iconMenuInfo.png"));

	}
