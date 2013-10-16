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
	
	public abstract Stroke getLineStroke();
	public abstract Color getLineColor();
	
	private Rectangle box; // used to detect mouse pick-up
	private AffineTransform boxTransform;
	
	public void setStart(Linable start) {
		this.start = start;
	}
	
	public void setEnd(Linable end) {
		this.end = end;
	}
	
	private boolean hitTest(Vector2D p1, Vector2D p2) {
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
		
		Vector2D p1 = start.getArchor(end.getCenter()); // point 1
		Vector2D p2 = end.getArchor(start.getCenter()); // point 2
		if (p1.equals(p2))
			return;
		if (!hitTest(p1, p2))
			return;

		g.translate(p2.x, p2.y);
		p1 = Vector2D.subtract(p1, p2);
		double theta = Math.atan2(p1.y, p1.x);
		int length = (int) Math.sqrt(p1.x * p1.x + p1.y * p1.y);
		
		g.rotate(theta);
		g.setStroke(getLineStroke());
		g.setColor(getLineColor());
		g.drawLine(0, 0, length, 0); // draw the line
		
		// update detection box
		final int radius = 3;
		this.box = new Rectangle(0, -radius, length, radius * 2);
		this.boxTransform = AffineTransform.getRotateInstance(-theta);
		this.boxTransform.translate(-p2.x, -p2.y);
		
		g.rotate(-theta);
		g.translate(-p2.x, -p2.y);
	}
	@Override
	public boolean isInside(Vector2D point) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
