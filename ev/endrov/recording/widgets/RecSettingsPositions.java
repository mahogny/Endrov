package endrov.recording.widgets;

import java.util.LinkedList;
import java.util.List;

import endrov.recording.StoredStagePosition;

public class RecSettingsPositions
	{
	public List<StoredStagePosition> positions=new LinkedList<StoredStagePosition>();
	
	public RecSettingsPositions(LinkedList<StoredStagePosition> positions)
		{
		this.positions = new LinkedList<StoredStagePosition>(positions);
		}
	
	}