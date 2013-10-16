package lab.meteor.visualize.diagram;

import co.gongzh.snail.View;

public class DiagramView extends View {
	
	LinesView lineView;
	
	public DiagramView() {
		lineView = new LinesView();
		lineView.setBackgroundColor(null);
		addSubview(lineView);
		lineView.diagramView = this;
	}
	
	@Override
	public void setSize(int width, int height) {
		lineView.setSize(width, height);
		super.setSize(width, height);
	}
	
	public void addClassWidget(ClassWidget widget) {
		this.addSubview(widget);
		widget.diagramView = this;
	}
	
	public void removeClassWidget(ClassWidget widget) {
		widget.removeFromSuperView();
		widget.diagramView = null;
	}
	
	public void addEnumWidget(EnumWidget widget) {
		this.addSubview(widget);
		widget.diagramView = this;
	}
	
	public void removeEnumWidget(EnumWidget widget) {
		widget.removeFromSuperView();
		widget.diagramView = null;
	}
	
	public void addLine(Line line) {
		lineView.addLine(line);
		setSubviewIndex(lineView, count() - 1);
	}
	
	public void removeLine(Line line) {
		lineView.removeLine(line);
		setSubviewIndex(lineView, count() - 1);
	}
}
