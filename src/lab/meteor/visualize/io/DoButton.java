package lab.meteor.visualize.io;

import java.awt.Color;
import java.awt.RenderingHints;

import co.gongzh.snail.Animation;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.event.EventHandler;
import co.gongzh.snail.event.Key;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Alignment;

public class DoButton extends View {
	
	TextView textView;
	View boardView;
	
	static int offset = 2;
	
	Animation pressAnimation = new PressAnimation(0.1f);
	Animation releaseAnimation = new ReleaseAnimation(0.1f);
	
	public DoButton() {
		setBackgroundColor(null);
		boardView = new View() {
			@Override
			protected void repaintView(ViewGraphics g) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.white);
				g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
			}
		};
		boardView.setBackgroundColor(null);
		textView = new TextView();
		textView.setBackgroundColor(null);
		textView.setDefaultTextColor(Color.darkGray);
		textView.setTextAlignment(Alignment.CENTER_CENTER);
		textView.setBackgroundColor(null);
//		textView.setPaintMode(PaintMode.DIRECTLY);
		addSubview(boardView);
		addSubview(textView);
		this.addEventHandler(MOUSE_PRESSED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				releaseAnimation.cancel();
				pressAnimation.commit();
			}
			
		});
		this.addEventHandler(MOUSE_RELEASED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				pressAnimation.cancel();
				releaseAnimation.commit();
			}
			
		});
	}
	
	public void setText(String text) {
		this.textView.setText(text);
	}
	
	public void setTextColor(Color color) {
		this.textView.setTextColor(color);
	}
	
	@Override
	protected void repaintView(ViewGraphics g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.gray);
		g.fillRoundRect(0, offset, getWidth(), getHeight()-offset, 10, 10);
	}
	
	@Override
	public void setSize(int width, int height) {
		textView.setSize(width, height - offset);
		boardView.setSize(width, height - offset);
		super.setSize(width, height);
	}
	
	private class PressAnimation extends Animation {

		public PressAnimation(float duration) {
			super(duration);
		}

		@Override
		protected void animate(float progress) {
			textView.setTop((int) (progress * offset));
			boardView.setTop((int) (progress * offset));
		}
		
	}
	
	private class ReleaseAnimation extends Animation {

		public ReleaseAnimation(float duration) {
			super(duration);
		}

		@Override
		protected void animate(float progress) {
			textView.setTop((int) ((1 - progress) * offset));
			boardView.setTop((int) ((1 - progress) * offset));
		}
		
	}
}
