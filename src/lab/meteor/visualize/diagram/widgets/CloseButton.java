package lab.meteor.visualize.diagram.widgets;

import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.Animation;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;

public class CloseButton extends View {
	
	Animation fadeInAnimation;
	Animation fadeOutAnimation;
	
	public CloseButton() {
		fadeInAnimation = new FadeInAnimation();
		fadeOutAnimation = new FadeOutAnimation();
		setAlpha(0);
		setBackgroundColor(null);
		setSize(26,26);
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
	
	@Override
	protected void repaintView(ViewGraphics g) {
		g.drawImage(Resources.IMG_CLOSE, 0, 0, getWidth(), getHeight());
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
