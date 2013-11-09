package lab.meteor.visualize.diagram.widgets;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lab.meteor.core.MAttribute;
import lab.meteor.core.MClass;
import lab.meteor.core.MEnum;
import lab.meteor.core.MNativeDataType;
import lab.meteor.core.MProperty;
import lab.meteor.core.MReference;
import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.diagram.IModelView;
import lab.meteor.visualize.diagram.Line;
import lab.meteor.visualize.diagram.lines.BiLinkLine;
import lab.meteor.visualize.diagram.lines.InheritLine;
import lab.meteor.visualize.diagram.lines.LinkEnumLine;
import lab.meteor.visualize.diagram.lines.UniLinkLine;
import lab.meteor.visualize.resource.Resources;

public class ClassWidget extends BlockWidget implements IModelView<MClass> {
	
	MClass model;
	
	Map<Long, PropertyWidget> properties = new TreeMap<Long, PropertyWidget>();
	
	public ClassWidget(MClass c, DiagramView v) {
		super(v);
		setModel(c);
		diagramView = v;
		setSize(200,200);
	}
	
	PropertyWidget addProperty(MProperty p) {
		if (properties.containsKey(p.getID()))
			return null;
		PropertyWidget widget = new PropertyWidget(p, diagramView);
		listView.getContentView().addSubview(widget);
		properties.put(widget.getModel().getID(), widget);
		return widget;
	}
	
	PropertyWidget removeAttribute(MProperty p) {
		if (!properties.containsKey(p.getID()))
			return null;
		PropertyWidget widget = properties.get(p.getID());
		widget.removeFromSuperView();
		widget.setDiagramView(null);
		properties.remove(p.getID());
		return widget;
	}
	
	public PropertyWidget getPropertyWidget(long id) {
		return properties.get(id);
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

	@Override
	public void setModel(MClass model) {
		this.model = model;
		if (model == null) {
			this.setTitle("");
			for (Long id : properties.keySet()) {
				PropertyWidget pw = properties.get(id);
				pw.removeFromSuperView();
				pw.setDiagramView(null);
				pw.setModel(null);
				Line line = pw.getLine();
				if (line != null) {
					diagramView.removeLine(line);
					line.setStart(null);
					line.setEnd(null);
				}
			}
			properties.clear();
			List<Line> lines = getLines();
			for (Line l : lines) {
				l.setStart(null);
				l.setEnd(null);
				diagramView.removeLine(l);
			}
			return;
		}
		this.setTitle(model.getName());
		String[] atbs = model.getAttributeNames();
		for (String name : atbs) {
			MAttribute atb = model.getAttribute(name);
			PropertyWidget w = addProperty(atb);
			if (atb.getDataType().getNativeDataType() == MNativeDataType.Enum) {
				EnumWidget enm = diagramView.getEnumWidget(((MEnum)atb.getDataType()).getID());
				if (enm != null) {
					diagramView.addLine(new LinkEnumLine(w, enm));
				}
			}
		}
		String[] refs = model.getReferenceNames();
		for (String name : refs) {
			MReference ref = model.getReference(name);
			PropertyWidget w = addProperty(ref);
			ClassWidget cls = diagramView.getClassWidget(ref.getReference().getID());
			if (cls != null && cls != this) {
				if (ref.getOpposite() != null) {
					PropertyWidget opw = cls.getPropertyWidget(ref.getOpposite().getID());
					diagramView.addLine(new BiLinkLine(w, opw));
				} else
					diagramView.addLine(new UniLinkLine(w, cls));
			}
		}
		// utilizers
		MReference[] utilizers = model.getUtilizers();
		for (MReference utilizer : utilizers) {
			ClassWidget cw = diagramView.getClassWidget(utilizer.getOwner().getID());
			if (cw != null) {
				PropertyWidget pw = cw.getPropertyWidget(utilizer.getID());
//				if (!pw.isLinked()) {
					if (utilizer.getOpposite() != null) {
						PropertyWidget opw = this.getPropertyWidget(utilizer.getOpposite().getID());
						diagramView.addLine(new BiLinkLine(pw, opw));
					} else
						diagramView.addLine(new UniLinkLine(pw, this));
//				}
			}
		}
		// superclass
		if (model.getSuperClass() != null) {
			ClassWidget cw = diagramView.getClassWidget(model.getSuperClass().getID());
			if (cw != null) {
				diagramView.addLine(new InheritLine(this, cw));
			}
		}
		MClass[] subs = model.getSubClasses();
		for (MClass sub : subs) {
			ClassWidget cw = diagramView.getClassWidget(sub.getID());
			if (cw != null)
				diagramView.addLine(new InheritLine(cw, this));
		}
		listView.layout();
	}

	@Override
	public MClass getModel() {
		return model;
	}

}
