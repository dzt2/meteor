package lab.meteor.visualize.diagram;

import co.gongzh.snail.layer.LayeredView;

public class LinesView extends LayeredView {
	
	DiagramView diagramView;
	
	public void addLine(Line line) {
		this.addLayer(line);
		line.linesView = this;
	}
	
	public void removeLine(Line line) {
		this.removeLayer(line);
		line.linesView = null;
	}
	
}
