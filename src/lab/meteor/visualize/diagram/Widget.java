package lab.meteor.visualize.diagram;

import co.gongzh.snail.View;
import co.gongzh.snail.util.Vector2D;

public abstract class Widget extends View {
	
	protected DiagramView diagramView;
	
	public Widget(DiagramView v) {
		this.diagramView = v;
	}
	
	public DiagramView getDiagramView() {
		return diagramView;
	}
	
	public void setDiagramView(DiagramView diagramView) {
		this.diagramView = diagramView;
	}
	
	public final Vector2D transformPointToDiagram(Vector2D point) {
		Vector2D rst = Vector2D.make(point);
		View[] hierarchy = getViewHierarchy();
		for (int i = hierarchy.length - 1; i >= 0; i--) {
			View v = hierarchy[i];
			rst = v.transformPointToSuperView(rst);
			if (v == diagramView)
				break;
		}
		return rst;
	}
	
	public final Vector2D transformPointFromDiagram(Vector2D point) {
		Vector2D rst = Vector2D.make(point);
		View[] hierarchy = getViewHierarchy();
		boolean start = false;
		for (int i = 0; i < hierarchy.length; i++) {
			View v = hierarchy[i];
			if (v == diagramView)
				start = true;
			if (start)
				rst = v.transformPointFromSuperView(rst);
		}
		return rst;
	}
}
