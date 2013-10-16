package lab.meteor.visualize.diagram;

import java.awt.Color;
import java.awt.RenderingHints;
import java.util.LinkedList;
import java.util.List;

import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Vector2D;

public abstract class BlockWidget extends Widget implements Linable {

	Vector2D pos0;
	
	ListView listView;
	View contentView;
	TextView titleView;
	TextView titleShadowView;
	ShadowDecorator shadowDecorator;
	
	final static int margin = 2;
	final static int titleHeight = 30;
	List<Line> lines = new LinkedList<Line>();
	
	public BlockWidget() {
		setBackgroundColor(null);
		setClipped(false);

		listView = new ListView();
		shadowDecorator = new ShadowDecorator();
		
		listView.setPosition(margin, titleHeight + margin);
		
		contentView = new View() {
			@Override
			protected void repaintView(ViewGraphics g) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(getTitleBackgroundColor());
				g.fillRect(1, 2, getWidth() - 2, titleHeight);
				g.setColor(getBorderColor());
				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
				
				g.setColor(Color.white);
				g.setFont(Resources.FONT_CLASS_ENUM);

				g.drawString(getTitle(), 20, 25);
				g.setColor(getTitleColor());
				g.drawString(getTitle(), 19, 24);
				
			}
		};
		contentView.setBackgroundColor(null);
		
		addSubview(shadowDecorator);
		addSubview(contentView);
		addSubview(listView);
	}
	
	public abstract String getTitle();
	public abstract Color getTitleBackgroundColor();
	public abstract Color getBorderColor();
	public abstract Color getTitleColor();
	
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		listView.setSize(width - margin * 2, height - titleHeight - margin * 2);
		shadowDecorator.setSize(width, height);
		contentView.setSize(width, height);
	}
	
	@Override
	protected void mousePressed(MouseEvent e) {
		pos0 = e.getPosition(this);
		this.getSuperView().setSubviewIndex(this, this.getSuperView().count() - 2);
		e.handle();
	}
	
	@Override
	protected void mouseDragged(MouseEvent e) {
		this.setPosition(Vector2D.subtract(e.getPosition(getSuperView()), pos0));
		e.handle();
	}
	
	@Override
	protected void mouseReleased(MouseEvent e) {
		e.handle();
	}
	
	Line line;
	
	@Override
	public Vector2D getCenter() {
		return this.transformPointToDiagram(Vector2D.make(getWidth()/2, getHeight()/2));
	}

	@Override
	public Vector2D getArchor(Vector2D target) {
		Vector2D c = Vector2D.make(getWidth() / 2, getHeight() / 2);
		Vector2D lt = Vector2D.make(0, 0);
		Vector2D rt = Vector2D.make(getWidth()-1, 0);
		Vector2D lb = Vector2D.make(0, getHeight()-1);
		Vector2D rb = Vector2D.make(getWidth()-1, getHeight()-1);
		
		Vector2D e = transformPointFromDiagram(target);
		if (lt.x < e.x && e.x < rt.x && lt.y < e.y && e.y < lb.y) {
			return target;
		}
		
		if (e.x == c.x) {
			if (e.y > c.y)
				return transformPointToDiagram(Vector2D.make(e.x, lb.y));
			else
				return transformPointToDiagram(Vector2D.make(e.x, lt.y));
		}
		if (e.y == c.y) {
			if (e.x > c.x)
				return transformPointToDiagram(Vector2D.make(rt.x, e.y));
			else
				return transformPointToDiagram(Vector2D.make(lt.x, e.y));
		}
		double v1 = (e.x - c.x) - (double)(lt.x - c.x) / (lt.y - c.y) * (e.y - c.y);
		double v2 = (e.x - c.x) - (double)(rt.x - c.x) / (rt.y - c.y) * (e.y - c.y);
		double v3 = (e.x - c.x) - (double)(lb.x - c.x) / (lb.y - c.y) * (e.y - c.y);
		double v4 = (e.x - c.x) - (double)(rb.x - c.x) / (rb.y - c.y) * (e.y - c.y);

		if (e.y < c.y && v1 >= 0 && v2 <= 0) {
			double x = c.x + (double)(e.x - c.x) / (e.y - c.y) * (lt.y - c.y);
			return transformPointToDiagram(Vector2D.make(x, lt.y));
		} else if (e.y > c.y && v3 >= 0 && v4 <= 0) {
			double x = c.x + (double)(e.x - c.x) / (e.y - c.y) * (lb.y - c.y);
			return transformPointToDiagram(Vector2D.make(x, lb.y));
		} else if (e.x < c.x && v1 <= 0 && v3 <= 0) {
			double y = c.y + (double)(e.y - c.y) / (e.x - c.x) * (lt.x - c.x);
			return transformPointToDiagram(Vector2D.make(lt.x, y));
		} else if (e.x > c.x && v2 >= 0 && v4 >= 0) {
			double y = c.y + (double)(e.y - c.y) / (e.x - c.x) * (rt.x - c.x);
			return transformPointToDiagram(Vector2D.make(rt.x, y));
		}
		return transformPointToDiagram(c);
	}

	@Override
	public void addLine(Line line) {
		lines.add(line);
	}

	@Override
	public void removeLine(Line line) {
		lines.remove(line);
	}
	
}
