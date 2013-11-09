package lab.meteor.visualize.diagram;

import java.util.Map;
import java.util.TreeMap;

import lab.meteor.core.MClass;
import lab.meteor.core.MElement;
import lab.meteor.core.MEnum;
import lab.meteor.core.MElement.MElementType;
import lab.meteor.shell.IShowListener;
import lab.meteor.visualize.diagram.widgets.ClassWidget;
import lab.meteor.visualize.diagram.widgets.EnumWidget;
import co.gongzh.snail.View;

public class DiagramView extends View implements IShowListener {
	
	LinesView lineView;
	
	private Map<Long, ClassWidget> classes = new TreeMap<Long, ClassWidget>();
	private Map<Long, EnumWidget> enumes = new TreeMap<Long, EnumWidget>();
	
	public DiagramView() {
		lineView = new LinesView();
		lineView.setBackgroundColor(null);
		addSubview(lineView);
		lineView.diagramView = this;
	}
	
	@Override
	public void setSize(int width, int height) {
		lineView.setSize(width, height);
		super.setSize(width, height);
	}
	
	public void addClass(MClass c) {
		if (classes.containsKey(c.getID()))
			return;
		ClassWidget widget = new ClassWidget(c, this);
		classes.put(c.getID(), widget);
		addSubview(widget);
		widget.diagramView = this;
	}
	
	public void removeClass(MClass c) {
		if (!classes.containsKey(c.getID()))
			return;
		ClassWidget widget = classes.get(c.getID());
		widget.removeFromSuperView();
		widget.diagramView = null;
		classes.remove(c.getID());
	}
	
	public void addEnum(MEnum e) {
		if (enumes.containsKey(e.getID()))
			return;
		EnumWidget widget = new EnumWidget(e, this);
		enumes.put(e.getID(), widget);
		addSubview(widget);
		widget.diagramView = this;
	}
	
	public void removeEnum(MEnum e) {
		if (!enumes.containsKey(e.getID()))
			return;
		EnumWidget widget = enumes.get(e.getID());
		widget.removeFromSuperView();
		widget.diagramView = null;
		enumes.remove(e.getID());
	}
	
	public ClassWidget getClassWidget(long id) {
		return classes.get(id);
	}
	
	public EnumWidget getEnumWidget(long id) {
		return enumes.get(id);
	}
	
	public void addLine(Line line) {
		lineView.addLine(line);
	}
	
	public void removeLine(Line line) {
		lineView.removeLine(line);
	}

	@Override
	public void show(MElement e) {
		if (e.getElementType() == MElementType.Class) {
			addClass((MClass) e);
		} else if (e.getElementType() == MElementType.Enum) {
			addEnum((MEnum) e);
		}
	}
}
