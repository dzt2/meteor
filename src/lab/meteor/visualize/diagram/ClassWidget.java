package lab.meteor.visualize.diagram;

import java.awt.Color;
import java.awt.RenderingHints;
import java.util.LinkedList;
import java.util.List;

import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.util.Vector2D;

public class ClassWidget extends BlockWidget {
	
	List<AttributeWidget> atbList = new LinkedList<AttributeWidget>();
	
	public ClassWidget() {

		this.setSize(200, 250);
		
		addAttributeWidget(new AttributeWidget());
		addAttributeWidget(new AttributeWidget());
		addAttributeWidget(new AttributeWidget());
		addAttributeWidget(new AttributeWidget());
		addAttributeWidget(new AttributeWidget());
		addAttributeWidget(new AttributeWidget());
		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
//		addAttributeWidget(new AttributeWidget());
		listView.layout();
	}
	
	@Override
	public String getTitle() {
		// TODO
		return "AClass";
	}
	
	public void addAttributeWidget(AttributeWidget widget) {
		listView.getContentView().addSubview(widget);
		atbList.add(widget);
		widget.diagramView = this.diagramView;
	}
	
	public void removeAttributeWidget(AttributeWidget widget) {
		widget.removeFromSuperView();
		atbList.remove(widget);
		widget.diagramView = null;
	}
	
	public AttributeWidget getAttributeWidget(int index) {
		return atbList.get(index);
	}

	@Override
	public Color getTitleBackgroundColor() {
		return Resources.COLOR_CLASS_TITLE_BG;
	}

	@Override
	public Color getBorderColor() {
		return Resources.COLOR_CLASS_BORDER;
	}

	@Override
	public Color getTitleColor() {
		return Resources.COLOR_CLASS_NAME;
	}

}
