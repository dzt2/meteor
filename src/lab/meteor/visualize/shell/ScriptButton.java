package lab.meteor.visualize.shell;

import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.Animation;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;

public class ScriptButton extends View {
	
	ScriptView scriptView;
	
	boolean isOpened = false;
	int lastWidth = 100;
	int lastHeight = 100;
	
	Animation openAnimation = new OpenAnimation(0.5f);
	Animation closeAnimation = new CloseAnimation(0.5f);
	
	public ScriptButton(ScriptView scriptView) {
		setSize(48, 48);
		setBackgroundColor(null);
		this.scriptView = scriptView;
		lastWidth = scriptView.getWidth();
		lastHeight = scriptView.getHeight();
	}
	
	@Override
	protected void repaintView(ViewGraphics g) {
		g.drawImage(Resources.IMG_SCRIPT, 0, 0, getWidth(), getHeight());
	}
	
	@Override
	protected void mouseClicked(MouseEvent e) {
		if (isOpened && openAnimation.isPlaying())
			return;
		if (!isOpened && closeAnimation.isPlaying())
			return;
		isOpened = !isOpened;
		if (isOpened) {
			scriptView.setHidden(false);
			closeAnimation.cancel();
			openAnimation.commit();
		} else {
			lastWidth = scriptView.getWidth();
			lastHeight = scriptView.getHeight();
			openAnimation.cancel();
			closeAnimation.commit();
		}
	}
	
	private class OpenAnimation extends Animation {

		public OpenAnimation(float duration) {
			super(duration);
		}

		@Override
		protected void animate(float progress) {
			if (scriptView != null) {
				float v = progress * progress;
				scriptView.setAlpha(v);
				scriptView.setWidth((int)(v * lastWidth));
				scriptView.setHeight((int)(v * lastHeight));
			}
		}
		
	}
	
	private class CloseAnimation extends Animation {

		public CloseAnimation(float duration) {
			super(duration);
		}

		@Override
		protected void animate(float progress) {
			if (scriptView != null) {
				float v = progress * progress;
				scriptView.setAlpha(1 - v);
				scriptView.setWidth((int)((1 - v) * lastWidth));
				scriptView.setHeight((int)((1 - v) * lastHeight));
			}
		}
		
		@Override
		protected void completed(boolean canceled) {
			if (scriptView != null)
				scriptView.setHidden(true);
		}
		
	}
	
}
