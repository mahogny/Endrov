package endrov.typeImageset.gui;

import endrov.data.EvContainer;
import endrov.data.EvPath;
import endrov.gui.component.EvComboObjectOne;
import endrov.typeImageset.EvChannel;

public class EvComboChannel extends EvComboObjectOne<EvChannel>
	{
	private static final long serialVersionUID = 1L;
	
	public EvComboChannel(boolean allowNoSelection, boolean allowCreation)
		{
		super(new EvChannel(), allowNoSelection, allowCreation);
		}
	
	public EvContainer getImageset()
		{
		if(getSelectedObject()!=null)
			return (EvContainer)getSelectObjectParent(); //TODO not always the case
		else
			return null;
		}
	
	public String getChannelName()
		{
		EvPath p=getSelectedPath();
		if(p!=null)
			return p.getLeafName();
		else
			return null;
		}
	
	
	
	//TODO: on updateList, re-get current item according to path! it might have been replaced
	
	}
