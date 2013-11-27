package lab.meteor.visualize.diagram.widgets;

import java.awt.Color;
import java.awt.RenderingHints;
import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;

import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.diagram.Linable;
import lab.meteor.visualize.diagram.Line;
import lab.meteor.visualize.diagram.Widget;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.Animation;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.event.EventHandler;
import co.gongzh.snail.event.Key;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Alignment;
import co.gongzh.snail.util.Insets;
import co.gongzh.snail.util.Vector2D;

public abstract class BlockWidget extends Widget implements Linable {

	Vector2D pos0;
	
	View contentView;
	TextView titleView;
	ResizeButton resizeButton;
	CloseButton closeButton;
//	ShadowDecorator shadowDecorator;
	
	Animation fadeInAnimation = new FadeInAnimation();
	Animation fadeOutAnimation = new FadeOutAnimation();
	
	List<Line> lines = new LinkedList<Line>();
	
	public BlockWidget(DiagramView v) {
		super(v);
		setClipped(false);
		titleView = new TextView();
		
//		shadowDecorator = new ShadowDecorator();
		
		titleView.setPosition(1, 1);
		titleView.setDefaultFont(Resources.FONT_CLASS_ENUM);
		titleView.setDefaultTextColor(getTitleColor());
		titleView.setBackgroundColor(getTitleBackgroundColor());
		titleView.setInsets(Insets.make(5, 5, 5, 5));
		titleView.setTextAlignment(Alignment.CENTER_CENTER);
		titleView.setBreakIterator(BreakIterator.getCharacterInstance());
		
//		contentView = new View() {
//			@Override
//			protected void repaintView(ViewGraphics g) {
//				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//				g.setColor(getBorderColor());
//				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);	
//			}
//		};
		contentView = new View();
		contentView.setBackgroundColor(null);
		contentView.setClipped(true);
		
		resizeButton = new ResizeButton(this);
		closeButton = new CloseButton();
		closeButton.addEventHandler(MOUSE_CLICKED, new EventHandler() {
			
			@Override
			public void handle(View sender, Key key, Object arg) {
				fadeOutAnimation.commit();
			}
		});
		
//		addSubview(shadowDecorator);
		addSubview(contentView);
		addSubview(titleView);
		addSubview(resizeButton);
		addSubview(closeButton);
		
		setAlpha(0);
		fadeInAnimation.commit();
	}
	
	public String getTitle() {
		return this.titleView.getPlainText();
	}
	
	public void setTitle(String title) {
		this.titleView.setText(title);
	}

	protected abstract Color getTitleBackgroundColor();
	protected abstract Color getBorderColor();
	protected abstract Color getTitleColor();
	
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		titleView.setSize(width - 2, titleView.getPreferredHeight());
//		shadowDecorator.setSize(width, height);
		contentView.setSize(width, height);
		resizeButton.setPosition(width - resizeButton.getWidth(), height - resizeButton.getHeight());
		closeButton.setPosition(width - closeButton.getWidth()/2, -closeButton.getHeight()/2);
	}
	
	@Override
	protected void repaintView(ViewGraphics g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getBorderColor());
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	}
	
	@Override
	protected void mousePressed(MouseEvent e) {
		pos0 = e.getPosition(this);
		this.getSuperView().setSubviewIndex(this, this.getSuperView().count() - 1);
		e.handle();
	}
	
	@Override
	protected void mouseDragged(MouseEvent e) {
		Vector2D sub = Vector2D.subtract(e.getPosition(getSuperView()), pos0);
		this.setPosition(sub);
		e.handle();
	}
	
	@Override
	protected void mouseReleased(MouseEvent e) {
		e.handle();
	}
	
	@Override
	public Vector2D getCenter() {
		return this.transformPointToDiagram(Vector2D.make(getWidth()/2, getHeight()/2));
	}

	@Override
	public Vector2D getArchor(Vector2D center) {
		Vector2D c = Vector2D.make(getWidth() / 2, getHeight() / 2);
		Vector2D lt = Vector2D.make(0, 0);
		Vector2D rt = Vector2D.make(getWidth()-1, 0);
		Vector2D lb = Vector2D.make(0, getHeight()-1);
		Vector2D rb = Vector2D.make(getWidth()-1, getHeight()-1);
		
		Vector2D e = transformPointFromDiagram(center);
		if (lt.x < e.x && e.x < rt.x && lt.y < e.y && e.y < lb.y) {
			return center;
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

	public List<Line> getLines() {
		return lines;
	}
	
	@Override
	public void addLine(Line line) {
		lines.add(line);
	}

	@Override
	public void removeLine(Line line) {
		lines.remove(line);
	}
	
	@Override
	public boolean isInside(Vector2D point) {
		return super.isInside(point) || 
				closeButton.isInside(closeButton.transformPointFromSuperView(point));
	}
	
	protected abstract void onClose();
	
	private class FadeInAnimation extends Animation {

		public FadeInAnimation() {
			super(0.2f);
		}
		
		@Override
		protected void animate(float progress) {
			setAlpha(progress);
		}
		
	}
	
	private class FadeOutAnimation extends Animation {

		public FadeOutAnimation() {
			super(0.3f);
		}
		
		@Override
		protected void animate(float progress) {
			setAlpha(1.0f - progress);
		}
		
		@Override
		protected void completed(boolean canceled) {
			onClose();
		}
	}
	
}
