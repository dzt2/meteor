package lab.meteor.visualize.diagram;

import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;

@Deprecated
public class ShadowDecorator extends View {
	
	public ShadowDecorator() {
		setBackgroundColor(null);
		setPosition(-3, -2);
	}
	
	@Override
	public void setSize(int width, int height) {
		super.setSize(width + 9, height + 8);
	}
	
	@Override
	protected void repaintView(ViewGraphics g) {
		g.drawImage(Resources.IMG_BLOCK_SHADOW, 0, 0, getWidth(), getHeight());
	}
}
