package lab.meteor.visualize.diagram.widgets;

import java.awt.Color;
import java.awt.RenderingHints;

import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.diagram.Linable;
import lab.meteor.visualize.diagram.Line;
import lab.meteor.visualize.diagram.Widget;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Alignment;
import co.gongzh.snail.util.Insets;
import co.gongzh.snail.util.Vector2D;

public abstract class ItemWidget extends Widget implements Linable {
	
	static final int dotSize = 10;
	static final int textOffX = 24;
	static final int defaultHeight = 24;
	
	TextView contentView;
	Line line;
	boolean isMouseOn = false;

	public ItemWidget(DiagramView v) {
		super(v);
		setBackgroundColor(null);
		
		contentView = new TextView();
		contentView.setDefaultFont(Resources.FONT_PROPERTY);
		contentView.setBackgroundColor(null);
		contentView.setDefaultTextColor(Resources.COLOR_PROPERTY_NAME);
		contentView.setTextAlignment(Alignment.LEFT_CENTER);
		contentView.setInsets(Insets.make(2, textOffX, 2, 2));
		
		this.setHeight(defaultHeight);
		this.addSubview(contentView);
	}
	
	protected TextView getContentView() {
		return contentView;
	}
	
	protected abstract Color getTypeColor();
		
	@Override
	protected void repaintView(ViewGraphics g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (isMouseOn) {
			g.setColor(Resources.COLOR_PROPERTY_HIGHLIGHT);
			g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 5, 5);
		}
		
		g.setColor(getTypeColor());
		int offx = (textOffX - dotSize) / 2;
		int offy = (getHeight() - dotSize) / 2;
		drawDot(g, offx, offy, dotSize);
	}
	
	abstract void drawDot(ViewGraphics g, int offx, int offy, int dotSize);
	
	@Override
	public void setSize(int width, int height) {
		contentView.setSize(width, height);
		super.setSize(width, height);
	}
	
	@Override
	protected void mouseEntered() {
		isMouseOn = true;
		this.setNeedsRepaint();
	}
	
	@Override
	protected void mouseExited() {
		isMouseOn = false;
		this.setNeedsRepaint();
	}
	
	@Override
	public void addLine(Line line) {
		this.line = line;
	}

	@Override
	public void removeLine(Line line) {
		this.line = null;
	}

	@Override
	public Vector2D getCenter() {
		return transformPointToDiagram(Vector2D.make(this.getWidth() / 2, getHeight() / 2));
	}

	@Override
	public Vector2D getArchor(Vector2D target) {
		Vector2D point = getCenter();
		int other = target.x;
		int left = point.x - getWidth() / 2;
		int right = point.x + getWidth() / 2;
		if (other < point.x) {
			return Vector2D.make(left, point.y);
		} else {
			return Vector2D.make(right, point.y);
		}
	}
}
