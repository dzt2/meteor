package lab.meteor.visualize.diagram.widgets;

import java.awt.Color;

import co.gongzh.snail.ViewGraphics;
import lab.meteor.core.MSymbol;
import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.diagram.IModelView;
import lab.meteor.visualize.resource.Resources;

public class SymbolWidget extends ItemWidget implements IModelView<MSymbol> {

	MSymbol model;
	
	public SymbolWidget(MSymbol s, DiagramView v) {
		super(v);
		setModel(s);
	}
	
	@Override
	protected Color getTypeColor() {
		return Resources.COLOR_ENUM_TITLE_BG;
	}

	@Override
	public void setModel(MSymbol model) {
		this.model = model;
		if (model == null) {
			this.getContentView().setText("");
		} else {
			this.getContentView().setText(model.getName());
		}
	}

	@Override
	public MSymbol getModel() {
		return model;
	}

	@Override
	void drawDot(ViewGraphics g, int offx, int offy, int dotSize) {
		g.fillRect(offx, offy, dotSize, dotSize / 2);
	}

	@Override
	protected Color getIconColor() {
		return Resources.COLOR_ENUM_TITLE_BG;
	}

}
