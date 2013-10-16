package lab.meteor.visualize;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import javax.swing.JFrame;

import co.gongzh.snail.ViewContext;
import lab.meteor.visualize.diagram.ClassWidget;
import lab.meteor.visualize.diagram.EnumWidget;
import lab.meteor.visualize.diagram.Line;
import lab.meteor.visualize.diagram.LinesView;
import lab.meteor.visualize.resource.Resources;
import lab.meteor.visualize.shell.ShellView;

public class Launcher {

	static MainView mainView;
	
	static JFrame frame;
	
	public static JFrame getFrame() {
		if (frame == null)
			frame = new JFrame();
		return frame;
	}
	
	public static void main(String[] args) {
		
		// standard Swing
		getFrame();
		frame.setTitle("Meteor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
		
		mainView = new MainView();
		mainView.setWidth(800);
		mainView.setHeight(600);
		mainView.setPosition(0, 0);
		
		ClassWidget cv0 = new ClassWidget();
		ClassWidget cv1 = new ClassWidget();
		mainView.getDiagramView().addClassWidget(cv0);
		mainView.getDiagramView().addClassWidget(cv1);
		mainView.getDiagramView().addEnumWidget(new EnumWidget());
//		Line line = new Line() {
//
//			@Override
//			public Stroke getLineStroke() {
//				return new BasicStroke();
//			}
//
//			@Override
//			public Color getLineColor() {
//				return Color.blue;
//			}
//			
//		};
//		line.setStart(cv0.getAttributeWidget(0));
//		line.setEnd(cv1.getAttributeWidget(0));
//		mainView.getDiagramView().addLine(line);
		Line line = new Line() {

			@Override
			public Stroke getLineStroke() {
				return new BasicStroke(3);
			}

			@Override
			public Color getLineColor() {
				return Color.cyan;
			}
			
		};
		line.setStart(cv0.getAttributeWidget(1));
		line.setEnd(cv1);
		mainView.getDiagramView().addLine(line);
		
		ViewContext context = new ViewContext(frame.getContentPane());
		context.setRootView(mainView);
		mainView.getViewContext().getSwingContainer().requestFocus();
	}

}
