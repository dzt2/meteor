package lab.meteor.visualize;

import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.shell.ScriptView;
import lab.meteor.visualize.shell.ShellView;
import co.gongzh.snail.View;

public class MainView extends View {
	ShellView commandView;
	DiagramView diagramView;
	ScriptView scriptView;
	
	public MainView() {
		commandView = new ShellView();
		diagramView = new DiagramView();
		scriptView = new ScriptView();
		addSubview(diagramView);
		addSubview(commandView);
		addSubview(scriptView);
		scriptView.setSize(500, 300);
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
