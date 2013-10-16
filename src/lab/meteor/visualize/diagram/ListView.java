package lab.meteor.visualize.diagram;

import java.awt.Color;
import java.awt.RenderingHints;
import java.util.List;

import co.gongzh.snail.Animation;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.MouseWheelEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;

public class ListView extends View {
	
	List<Object> modellist;
	
	private final View clipView;
	private final View contentView;
	private final View indicator;
	
	private int indicatorState;
	private Animation indicatorAnimation;
	private boolean mouseIsOnIndicator;
	private static final int INDICATOR_HIDDEN = 1;
	private static final int INDICATOR_FADING_IN = 2;
	private static final int INDICATOR_SHOWN = 3;
	private static final int INDICATOR_COUNTING = 4;
	private static final int INDICATOR_FADING_OUT = 5;
	
	private class IndicatorFadeInAnimation extends Animation {

		public IndicatorFadeInAnimation() {
			super(0.2f);
		}
		
		@Override
		protected void animate(float progress) {
			indicator.setAlpha(progress);
		}
		@Override
		protected void completed(boolean canceled) {
			if (!canceled) {
				synchronized (indicator) {
					indicatorAnimation = null;
					if (mouseIsOnIndicator) {
						indicatorState = INDICATOR_SHOWN;
					} else {
						indicatorState = INDICATOR_COUNTING;
						indicatorAnimation = new IndicatorCountingAnimation();
						indicatorAnimation.commit();
					}
				}
			}
		}
		
	}
	
	private class IndicatorFadeOutAnimation extends Animation {

		public IndicatorFadeOutAnimation() {
			super(0.55f);
		}
		
		@Override
		protected void animate(float progress) {
			indicator.setAlpha(1.0f - progress);
		}
		@Override
		protected void completed(boolean canceled) {
			if (!canceled) {
				synchronized (indicator) {
					indicatorAnimation = null;
					indicatorState = INDICATOR_HIDDEN;
				}
			}
		}
		
	}
	
	private class IndicatorCountingAnimation extends Animation {

		public IndicatorCountingAnimation() {
			super(0.8f);
		}
		
		@Override
		protected void animate(float progress) {
		}
		
		@Override
		protected void completed(boolean canceled) {
			if (!canceled) {
				synchronized (indicator) {
					indicatorState = INDICATOR_FADING_OUT;
					indicatorAnimation = new IndicatorFadeOutAnimation();
					indicatorAnimation.commit();
				}
			}
		}
		
	}

	public ListView() {
		super();
		setBackgroundColor(null);
		mouseIsOnIndicator = false;
		
		clipView = new View(0, 0, 0, 0) {
			@Override
			public void setSize(int width, int height) {
				super.setSize(width, height);
				if (contentView != null && contentView.getWidth() != width) {
					contentView.setWidth(width);
					ListView.this.layout();
				} else {
					updateContentsVisibilities();
				}
			}
		};
//		clipView.setPaintingEnabled(false);
		clipView.setClipped(true);
		addSubview(clipView);
		
		contentView = new View(0, 0, 0, 0) {
			@Override
			public void setSize(int width, int height) {
				boolean height_changed = false;
				if (getHeight() != height) height_changed = true;
				super.setSize(width, height);
				if (height_changed) updateIndicator();
			}
			@Override
			public void setPosition(int left, int top) {
				boolean top_changed = false;
				if (getTop() != top) top_changed = true;
				super.setPosition(left, top);
				if (top_changed) {
					updateIndicator();
					updateContentsVisibilities();
				}
			}
			@Override
			protected void subviewAdded(View subview) {
				subview.setHidden(true);
			}
		};
//		contentView.setPaintingEnabled(false);
		clipView.addSubview(contentView);
		
		indicator = new View(0, 0, 6, 20) {
			@Override
			protected void repaintView(ViewGraphics g) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.black);
				g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 10, 10);
//				g.drawStrechableImage(Resources.IMG_SCROLL_INDICATOR, getWidth(), getHeight());
			}
			@Override
			protected void mouseEntered() {
				mouseIsOnIndicator = true;
				showIndicator();
			}
			@Override
			protected void mouseExited() {
				mouseIsOnIndicator = false;
				switch (indicatorState) {
				case INDICATOR_SHOWN:
					synchronized (indicator) {
						indicatorState = INDICATOR_COUNTING;
						indicatorAnimation = new IndicatorCountingAnimation();
						indicatorAnimation.commit();
					}
					break;
				}
			}
			int mouse_y0;
			@Override
			protected void mousePressed(MouseEvent e) {
				e.handle();
				mouse_y0 = e.getPosition(ListView.this).y;
			}
			@Override
			protected void mouseReleased(MouseEvent e) { e.handle(); }
			@Override
			protected void mouseMoved(MouseEvent e) { e.handle(); }
			@Override
			protected void mouseDragged(MouseEvent e) {
				e.handle();
				int mouse_y = e.getPosition(ListView.this).y;
				float dy = (mouse_y - mouse_y0) / (float) (ListView.this.getHeight() - 8);
				contentView.setTop(contentView.getTop() - (int) (dy * contentView.getHeight()));
				limitContentViewTop();
				mouse_y0 = mouse_y;
			}
			@Override
			protected void mouseClicked(MouseEvent e) { e.handle(); }
		};
		indicator.setBackgroundColor(null);
		addSubview(indicator);
		
		// sync size
		setSize(0, 0); 
		
		// indicator state
		synchronized (indicator) {
			indicatorState = INDICATOR_FADING_OUT;
			indicatorAnimation = new IndicatorFadeOutAnimation();
			indicatorAnimation.commit();
		}
	}
	
	@Override
	protected void layoutView() {
		int cur_top = 0;
		for (View view : contentView) {
			view.setWidth(contentView.getWidth());
			view.setPosition(0, cur_top);
			cur_top += view.getHeight();
		}
		contentView.setHeight(cur_top);
		limitContentViewTop();
		updateContentsVisibilities();
	}
	
	public int getPreferredHeight() {
		return contentView.getHeight() + 4;
	}
	
	private void updateContentsVisibilities() {
		if (contentView == null)
			return;
		for (View view : contentView) {
			if (contentView.getTop() + view.getTop() + view.getHeight() > 0) {
				if (contentView.getTop() + view.getTop() > clipView.getHeight()) {
					break;
				}
				view.setHidden(false);
			}
		}
	}
	
	public View getContentView() {
		return contentView;
	}
	
	@Override
	protected void repaintView(ViewGraphics g) {
//		g.setColor(Color.gray);
//		g.fillRect(0, 0, this.getWidth(), this.getHeight());
//		g.drawStrechableImage(Resources.IMG_DEPTH_BACKGROUND, getWidth(), getHeight());
	}
	
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		if (clipView != null) {
			View.scaleViewWithMarginToSuperView(clipView, 2);
		}
		if (contentView != null) {
			limitContentViewTop();
		}
		if (indicator != null) {
			indicator.setLeft(width - 10);
			updateIndicator();
		}
	}
	
	@Override
	protected void mouseWheelMoved(MouseWheelEvent e) {
//		e.handle();
		contentView.setTop(contentView.getTop() - e.getRotation());
		limitContentViewTop();
	}

	private void limitContentViewTop() {
		if (contentView.getHeight() <= clipView.getHeight()) {
			contentView.setTop(0);
		} else if (contentView.getTop() > 0) {
			contentView.setTop(0);
		} else if (contentView.getTop() + contentView.getHeight() < clipView.getHeight()) {
			contentView.setTop(clipView.getHeight() - contentView.getHeight());
		}
	}
	
	private void updateIndicator() {
		float total = contentView.getHeight();
		if (total <= 0.0f) total = 1.0f;
		float window = clipView.getHeight();
		if (window > total) window = total;
		else if (window <= 0.0f) window = 1.0f;
		float top = -contentView.getTop();
		window /= total;
		top /= total;
		total = getHeight() - 8;
		indicator.setTop((int) (4 + top * total));
		indicator.setHeight((int) (window * total));
		if (contentView.getHeight() <= clipView.getHeight()) {
			indicator.setHidden(true);
		} else {
			indicator.setHidden(false);
			showIndicator();
		}
	}
	
	private void showIndicator() {
		if (contentView.getHeight() <= clipView.getHeight()) return;
		synchronized (indicator) {
			switch (indicatorState) {
			case INDICATOR_HIDDEN:
				indicatorState = INDICATOR_FADING_IN;
				indicatorAnimation = new IndicatorFadeInAnimation();
				indicatorAnimation.commit();
				break;
			case INDICATOR_COUNTING:
			case INDICATOR_FADING_OUT:
				if (indicatorAnimation != null) {
					indicatorAnimation.cancel();
				}
				indicator.setAlpha(1.0f);
				indicatorState = INDICATOR_COUNTING;
				indicatorAnimation = new IndicatorCountingAnimation();
				indicatorAnimation.commit();
				break;
			}
		}
	}
	
}
