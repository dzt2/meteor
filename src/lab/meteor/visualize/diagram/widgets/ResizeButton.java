package lab.meteor.visualize.diagram.widgets;

import lab.meteor.visualize.resource.Resources;

import co.gongzh.snail.Animation;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.util.Vector2D;

public class ResizeButton extends View {
	View target;
	Animation fadeInAnimation;
	Animation fadeOutAnimation;
	
	public ResizeButton(View target) {
		this.target = target;
		setAlpha(0);
		setBackgroundColor(null);
		setSize(16, 16);
		fadeInAnimation = new FadeInAnimation();
		fadeOutAnimation = new FadeOutAnimation();
	}
	
	@Override
	protected void repaintView(ViewGraphics g) {
		g.drawImage(Resources.IMG_RESIZE, 0, 0, getWidth(), getHeight());
	}
	
	@Override
	protected void mouseEntered() {
		fadeOutAnimation.cancel();
		fadeInAnimation.commit();
	}
	
	@Override
	protected void mouseExited() {
		fadeInAnimation.cancel();
		fadeOutAnimation.commit();
	}
	
	Vector2D pos0;
	
	@Override
	protected void mousePressed(MouseEvent e) {
		pos0 = e.getPosition(this);
		e.handle();
	}
	
	@Override
	protected void mouseDragged(MouseEvent e) {
		Vector2D diff = Vector2D.subtract(e.getPosition(this), pos0);
		Vector2D newSize = Vector2D.add(target.getSize(), diff);
		target.setSize(newSize);
		e.handle();
	}
	
	@Override
	protected void mouseReleased(MouseEvent e) {
		e.handle();
	}
	
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
			super(0.2f);
		}
		
		@Override
		protected void animate(float progress) {
			setAlpha(1.0f - progress);
		}
	}
}
