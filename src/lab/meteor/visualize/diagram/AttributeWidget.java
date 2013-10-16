package lab.meteor.visualize.diagram;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.RenderingHints;

import lab.meteor.core.MAttribute;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.util.Vector2D;

public class AttributeWidget extends Widget implements Linable {
	
//	private MAttribute model;
	
	Line line;
	
	@Override
	public Vector2D getCenter() {
		return transformPointToDiagram(Vector2D.make(this.getWidth() - 25, 12));
	}
	
	@Override
	public Vector2D getArchor(Vector2D target) {
		return getCenter();
	}
	
	final static int margin_horizon = 5;
		
	public AttributeWidget() {
		setBackgroundColor(null);
		this.setHeight(25);
	}
	
	public void setModel(MAttribute model) {
		// TOOD
//		this.model = model;
	}
	
	String getName() {
		// TODO
		return "name" + ":";
	}
	
	String getType() {
		// TODO
		return "integer";
	}
	
	Color getTypeColor() {
		// TODO
		return Resources.COLOR_PRIMITIVE_TYPE;
	}
	
	int typeOffset = 0;
	
	@Override
	protected void repaintView(ViewGraphics g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (isMouseOn) {
			g.setColor(new Color(220, 220, 220));
			g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
		}
		
		g.setColor(Resources.COLOR_PROPERTY_NAME);
		g.setFont(Resources.FONT_PROPERTY);
		
		FontMetrics fm = g.getFontMetrics(Resources.FONT_PROPERTY);
		g.drawString(getName(), 5, 17);
		g.setColor(getTypeColor());
		g.drawString(getType(), 5 + (int)fm.stringWidth(getName()), 17);
		g.fillOval(getWidth() - 30, 7, 10, 10);
		
	}
	
	boolean isMouseOn = false;
	
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
}
