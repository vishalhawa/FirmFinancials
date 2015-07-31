package gui;
import javax.swing.JFrame;

import javax.swing.WindowConstants;

public class FrameDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame myFrame = new JFrame("This is my frame");
		
		myFrame.setSize(300,400);
		
		myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		myFrame.setVisible(true);


	}

}
