package endrov.recording.widgets;

import java.util.LinkedList;
import java.util.List;

import endrov.recording.positionsWindow.Position;

public class RecSettingsPositions
	{
	public List<Position> positions=new LinkedList<Position>();
	
	public RecSettingsPositions(LinkedList<Position> positions)
		{
		this.positions = new LinkedList<Position>(positions);
		}
	
	}