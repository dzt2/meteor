package lab.meteor.visualize.diagram;

import co.gongzh.snail.util.Vector2D;

public interface Linable {
	
	void addLine(Line line);
	void removeLine(Line line);
	
	Vector2D getArchor(Vector2D target);
	Vector2D getCenter();
}
