package lab.meteor.visualize;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import lab.meteor.core.MDatabase;
import lab.meteor.dba.MongoDBAdapter;
import co.gongzh.snail.ViewContext;

public class Launcher {

	static MainView mainView;
	
	static JFrame frame;
	
	static MongoConnector connector;
	
	public static JFrame getFrame() {
		if (frame == null) {
			frame = new JFrame();
			mainView = new MainView();
			frame.setTitle("Meteor");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					connector.close();
				}
			});
		}
		return frame;
	}
	
	public static void main(String[] args) {
		
		connector = new MongoConnector("meteor");
		connector.open();
		MDatabase.getDB().setDBAdapter(new MongoDBAdapter(connector.getDB()));
		MDatabase.getDB().initialize();
		MDatabase.getDB().setAutoSave(true);
		
		// standard Swing
		getFrame();
		frame.setSize(800, 600);
		frame.setVisible(true);
		
		mainView.setWidth(800);
		mainView.setHeight(600);
		mainView.setPosition(0, 0);
		
		ViewContext context = new ViewContext(frame.getContentPane());
		context.setRootView(mainView);
		mainView.getViewContext().getSwingContainer().requestFocus();
	}

}
