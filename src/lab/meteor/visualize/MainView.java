package lab.meteor.visualize;

import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.shell.ShellView;
import co.gongzh.snail.View;

public class MainView extends View {
	ShellView commandView;
	DiagramView diagramView;
	
	public MainView() {
		commandView = new ShellView();
		diagramView = new DiagramView();
		addSubview(diagramView);
		addSubview(commandView);
	}
	
	@Override
	public void setSize(int width, int height) {
		diagramView.setSize(width, height);
		commandView.setSize(width, 30);
		commandView.setPosition(0, height - 30);
		super.setSize(width, height);
	}
	
	public ShellView getCommandView() {
		return commandView;
	}
	
	public DiagramView getDiagramView() {
		return diagramView;
	}
}
