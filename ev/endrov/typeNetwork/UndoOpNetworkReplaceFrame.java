package endrov.typeNetwork;

import endrov.gui.window.EvBasicWindow;
import endrov.typeNetwork.Network.NetworkFrame;
import endrov.undo.UndoOpBasic;
import endrov.util.math.EvDecimal;

/**
 * Restore network frame by making an entire copy
 * 
 * @author Johan Henriksson
 * 
 */
public abstract class UndoOpNetworkReplaceFrame extends UndoOpBasic
	{
	private Network network;
	private NetworkFrame frameCopy;
	private boolean metaWasModified;
	private EvDecimal frame;
	private EvDecimal dateLastModify;	
	private EvDecimal newDateLastModify=new EvDecimal(System.currentTimeMillis());
	
	public void modifyObjects()
		{
		network.setMetadataModified();
		network.dateLastModify=newDateLastModify;
		}
	
	
	public UndoOpNetworkReplaceFrame(EvDecimal frame, Network network, String opname)
		{
		super(opname);
		this.network=network;
		this.frame=frame;
		
		NetworkFrame nf=network.frame.get(frame);
		if(nf!=null)
			frameCopy=nf.clone();
		
		metaWasModified=network.coreMetadataModified;
		dateLastModify=network.dateLastModify;
		}
	
	public void undo()
		{
		network.frame.put(frame, frameCopy);
		network.coreMetadataModified=metaWasModified;
		network.dateLastModify=dateLastModify;
		EvBasicWindow.updateWindows();
		}
	}