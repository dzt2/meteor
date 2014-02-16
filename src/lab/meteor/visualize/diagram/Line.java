package lab.meteor.visualize.diagram;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.layer.Layer;
import co.gongzh.snail.util.Vector2D;

public abstract class Line extends Layer {
	
	Linable start;
	Linable end;
	
	LinesView linesView;
	
	public Line(Linable start, Linable end) {
		setStart(start);
		setEnd(end);
	}
	
	public abstract Stroke getLineStroke();
	public abstract Color getLineColor();
	
	private Rectangle box; // used to detect mouse pick-up
	private AffineTransform boxTransform;
	
	// Two-Side Association
	public void setStart(Linable start) {
		if (this.start != null)
			this.start.removeLine(this);
		this.start = start;
		if (this.start != null)
			start.addLine(this);
	}
	
	public void setEnd(Linable end) {
		if (this.end != null)
			this.end.removeLine(this);
		this.end = end;
		if (this.end != null)
			end.addLine(this);
	}
	
	@Deprecated
	boolean hitTest(Vector2D p1, Vector2D p2) {
		boolean hit = false;
		View[] views = this.linesView.diagramView.getSubviewHierachyAtPoint(p1);
		if (views != null) {
			for (View v : views) {
				if (v == start) {
					hit = true;
					break;
				}
			}
			if (!hit)
				return false;
		}
		hit = false;
		views = this.linesView.diagramView.getSubviewHierachyAtPoint(p2);
		if (views != null) {
			for (View v : views) {
				if (v == end) {
					hit = true;
					break;
				}
			}
			if (!hit)
				return false;
		}
		return true;
	}
	
	@Override
	protected void repaintLayer(ViewGraphics g) {
		if (start.isHidden() || end.isHidden())
			return;
		Vector2D p1 = start.getArchor(end.getCenter()); // point 1
		Vector2D p2 = end.getArchor(start.getCenter()); // point 2
		if (p1.equals(p2))
			return;

		g.translate(p2.x, p2.y);
		p1 = Vector2D.subtract(p1, p2);
		double theta = Math.atan2(p1.y, p1.x);
		int length = (int) Math.sqrt(p1.x * p1.x + p1.y * p1.y);
		
		g.rotate(theta);
		g.setStroke(getLineStroke());
		g.setColor(getLineColor());
		drawLine(g, length);
		
		// update detection box
		final int radius = 3;
		this.box = new Rectangle(0, -radius, length, radius * 2);
		this.boxTransform = AffineTransform.getRotateInstance(-theta);
		this.boxTransform.translate(-p2.x, -p2.y);
		
		g.rotate(-theta);
		g.translate(-p2.x, -p2.y);
	}
	
	protected abstract void drawLine(ViewGraphics g, int length);
	
	@Override
	public boolean isInside(Vector2D point) {
		if (box != null)
			return box.contains(point.x, point.y);
		else
			return false;
	}
	
	
}
