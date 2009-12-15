package endrov.frivolous.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class Model {

	private ActionListener model_action;

	//private BufferedImage input_image = null;
	private BufferedImage output_image = null;
	
	private Cell cell;
	
	public Model() {
		cell = new Cell("../../darvid/res/cell1/new/");
		convolve();
	}

	public void readFile(File file){
		/*Timer timer = new Timer("Model");
		try {
			input_image = ImageIO.read(file);
		} catch (IOException e) {
			//FIXME: hantera fel vid inläsning
		}

		timer.show("Image is read");
		
		convolve();
		
		timer.show("Image convolved");*/
	}

	public void convolve(){
		output_image = cell.getImage();
		if(model_action != null)
			model_action.actionPerformed(new ActionEvent(this, 0, "image_updated")); //vad gör nollan?
	}
	
	public BufferedImage getImage(){
		return output_image;
	}
	
	public void setActionListener(ActionListener listener){
		model_action = listener;
	}
	
	public Settings_new getSettings(){
		return cell.getSettings();
	}
	public void updatePSF(){
		cell.updatePSF();
	}
	
}