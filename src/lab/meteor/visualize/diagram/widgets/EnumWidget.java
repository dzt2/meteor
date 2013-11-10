package lab.meteor.visualize.diagram.widgets;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lab.meteor.core.MAttribute;
import lab.meteor.core.MEnum;
import lab.meteor.core.MSymbol;
import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.diagram.IModelView;
import lab.meteor.visualize.diagram.Line;
import lab.meteor.visualize.diagram.lines.LinkEnumLine;
import lab.meteor.visualize.resource.Resources;

public class EnumWidget extends BlockWidget implements IModelView<MEnum> {

	MEnum model;
	
	Map<Long, SymbolWidget> symbols = new TreeMap<Long, SymbolWidget>();
	
	public EnumWidget(MEnum e, DiagramView v) {
		super(v);
		setModel(e);
		setSize(200,200);
	}
	
	SymbolWidget addSymbol(MSymbol s) {
		if (symbols.containsKey(s.getID()))
			return null;
		SymbolWidget widget = new SymbolWidget(s, diagramView);
		listView.getContentView().addSubview(widget);
		symbols.put(widget.getModel().getID(), widget);
		return widget;
	}
	
	SymbolWidget removeSymbol(MSymbol s) {
		if (!symbols.containsKey(s.getID()))
			return null;
		SymbolWidget widget = symbols.get(s.getID());
		widget.removeFromSuperView();
		widget.setDiagramView(null);
		symbols.remove(s.getID());
		return widget;
	}

	@Override
	public Color getTitleBackgroundColor() {
		return Resources.COLOR_ENUM_TITLE_BG;
	}

	@Override
	public Color getBorderColor() {
		return Resources.COLOR_ENUM_BORDER;
	}

	@Override
	public Color getTitleColor() {
		return Resources.COLOR_ENUM_TYPE;
	}

	@Override
	public void setModel(MEnum model) {
		this.model = model;
		if (model == null) {
			this.setTitle("");
			List<Line> lines = getLines();
			for (Line l : lines) {
				l.setStart(null);
				l.setEnd(null);
				diagramView.removeLine(l);
			}
			return;
		}
		setTitle(model.getName());
		String[] syms = model.getSymbolNames();
		for (String name : syms) {
			addSymbol(model.getSymbol(name));
		}
		MAttribute[] referents = model.getUtilizers();
		for (MAttribute referent : referents) {
			ClassWidget cw = diagramView.getClassWidget(referent.getOwner().getID());
			if (cw != null) {
				PropertyWidget pw = cw.getPropertyWidget(referent.getID());
				if (!pw.isLinked()) {
					diagramView.addLine(new LinkEnumLine(pw, this));
				}
			}
		}
	}

	@Override
	public MEnum getModel() {
		return model;
	}

	@Override
	protected void onClose() {
		diagramView.removeEnum(model);
	}

}
