package lab.meteor.visualize.diagram.widgets;

import java.awt.Color;

import lab.meteor.core.MPackage;
import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.diagram.IModelView;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.event.EventHandler;
import co.gongzh.snail.event.Key;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Alignment;
import co.gongzh.snail.util.Insets;
import co.gongzh.snail.util.Vector2D;

public class PackageWidget extends BlockWidget implements IModelView<MPackage> {
	
	MPackage model;
	
	final static int margin = 2;
	
	public PackageWidget(MPackage p, DiagramView v) {
		super(v);
		setBackgroundColor(Resources.COLOR_PACKAGE_BG);
		
		titleView.setInsets(Insets.make(5, 5, 5, 5));
		titleView.setTextAlignment(Alignment.LEFT_TOP);
		titleView.addEventHandler(TextView.TEXT_LAYOUT_CHANGED, new EventHandler() {
			
			@Override
			public void handle(View sender, Key key, Object arg) {
				titleView.setSize(getWidth() - 2, titleView.getPreferredHeight());
				contentView.setPosition(margin, titleView.getHeight() + margin);
				contentView.setHeight(getHeight() - titleView.getHeight() - margin * 2);
			}
		});
		
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

	@Override
	protected Color getTitleBackgroundColor() {
		return Resources.COLOR_PACKAGE_TITLE_BG;
	}

	@Override
	protected Color getBorderColor() {
		return Resources.COLOR_PACKAGE_BORDER;
	}

	@Override
	protected Color getTitleColor() {
		return Resources.COLOR_PACKAGE_NAME;
	}

	@Override
	protected void onClose() {
		// TODO Auto-generated method stub
		
	}

}
