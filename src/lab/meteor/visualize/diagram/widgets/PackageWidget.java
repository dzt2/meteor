package lab.meteor.visualize.diagram.widgets;

import java.text.BreakIterator;

import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.event.EventHandler;
import co.gongzh.snail.event.Key;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Alignment;
import co.gongzh.snail.util.Insets;
import co.gongzh.snail.util.Vector2D;
import lab.meteor.core.MPackage;
import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.diagram.IModelView;
import lab.meteor.visualize.diagram.Widget;
import lab.meteor.visualize.resource.Resources;

public class PackageWidget extends Widget implements IModelView<MPackage> {

	Vector2D pos0;
	
	MPackage model;
	TextView titleView;
	View contentView;
	ResizeButton resizeButton;
	
	final static int margin = 2;
	
	public PackageWidget(MPackage p, DiagramView v) {
		super(v);
		setBackgroundColor(Resources.COLOR_PACKAGE_BG);
		setClipped(true);
		titleView = new TextView();
		titleView.setPosition(1, 1);
		titleView.setDefaultFont(Resources.FONT_CLASS_ENUM);
		titleView.setDefaultTextColor(Resources.COLOR_PACKAGE_NAME);
		titleView.setBackgroundColor(Resources.COLOR_PACKAGE_TITLE_BG);
		titleView.setInsets(Insets.make(5, 5, 5, 5));
		titleView.setTextAlignment(Alignment.LEFT_TOP);
		titleView.setBreakIterator(BreakIterator.getCharacterInstance());
		titleView.addEventHandler(TextView.TEXT_LAYOUT_CHANGED, new EventHandler() {
			
			@Override
			public void handle(View sender, Key key, Object arg) {
				titleView.setSize(getWidth() - 2, titleView.getPreferredHeight());
			}
		});
		
		contentView = new View();
		contentView.setBackgroundColor(null);
		
		resizeButton = new ResizeButton(this);
		
		addSubview(contentView);
		addSubview(titleView);
		addSubview(resizeButton);
		
		setSize(200,200);
		
		setModel(p);
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
	public void setSize(int width, int height) {
		super.setSize(width, height);
		titleView.setSize(width - 2, titleView.getPreferredHeight());
		contentView.setPosition(margin, titleView.getHeight() + margin);
		contentView.setSize(width - margin * 2, height - titleView.getHeight() - margin * 2);
		resizeButton.setPosition(width - resizeButton.getWidth(), height - resizeButton.getHeight());
	}
	
	@Override
	protected void repaintView(ViewGraphics g) {
		g.setColor(Resources.COLOR_PACKAGE_BORDER);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
	}

	@Override
	public void setModel(MPackage model) {
		this.model = model;
		setTitle(model.getName());
	}

	@Override
	public MPackage getModel() {
		return model;
	}
	
	public String getTitle() {
		return this.titleView.getPlainText();
	}
	
	public void setTitle(String title) {
		this.titleView.setText(title);
	}

}
