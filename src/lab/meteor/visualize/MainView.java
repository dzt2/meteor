package lab.meteor.visualize;

import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.io.ImportButton;
import lab.meteor.visualize.shell.ScriptButton;
import lab.meteor.visualize.shell.ScriptView;
import lab.meteor.visualize.shell.ShellView;
import co.gongzh.snail.View;

public class MainView extends View {
	ShellView shellView;
	DiagramView diagramView;
	ScriptView scriptView;
	ScriptButton scriptButton;
	ImportButton importButton;
	
	public MainView() {
		shellView = new ShellView();
		diagramView = new DiagramView();
		scriptView = new ScriptView();
		
		addSubview(diagramView);
		addSubview(shellView);
		addSubview(scriptView);
		
		
		shellView.getShell().setShowListener(diagramView);
		scriptView.setSize(500, 300);
		scriptButton = new ScriptButton(scriptView);
		addSubview(scriptButton);
		scriptView.setHidden(true);
		
		importButton = new ImportButton(this);
		addSubview(importButton);
	}
	
	@Override
	public void setSize(int width, int height) {
		diagramView.setSize(width, height);
		shellView.setSize(width, 30);
		shellView.setPosition(0, height - 30);
		scriptButton.setLeft(width - scriptButton.getWidth());
		importButton.setLeft(scriptButton.getLeft() - importButton.getWidth());
		super.setSize(width, height);
	}
	
	public ShellView getCommandView() {
		return shellView;
	}
	
	public DiagramView getDiagramView() {
		return diagramView;
	}
}
