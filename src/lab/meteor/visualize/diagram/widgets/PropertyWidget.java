package lab.meteor.visualize.diagram.widgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.util.Range;
import lab.meteor.core.MAttribute;
import lab.meteor.core.MElement.MElementType;
import lab.meteor.core.MNativeDataType;
import lab.meteor.core.MProperty;
import lab.meteor.core.MReference;
import lab.meteor.core.MReference.Multiplicity;
import lab.meteor.visualize.diagram.DiagramView;
import lab.meteor.visualize.diagram.IModelView;
import lab.meteor.visualize.diagram.Line;
import lab.meteor.visualize.resource.Resources;

public class PropertyWidget extends ItemWidget implements IModelView<MProperty> {

	final static Stroke stroke = new BasicStroke(2);
	
	MProperty model;
	
	public PropertyWidget(MProperty p, DiagramView v) {
		super(v);
		setModel(p);
	}
	
	public boolean isLinked() {
		return line != null;
	}
	
	public Line getLine() {
		return line;
	}
	
	@Override
	protected Color getTypeColor() {
		if (model == null)
			return Color.black;
		if (model.getElementType() == MElementType.Attribute) {
			MAttribute a = (MAttribute) model;
			if (a.getDataType().getNativeDataType() == MNativeDataType.Enum)
				return Resources.COLOR_ENUM_TITLE_BG;
			else
				return Resources.COLOR_PRIMITIVE_TYPE;
		} else {
			return Resources.COLOR_CLASS_TITLE_BG;
		}
	}

	@Override
	public void setModel(MProperty model) {
		this.model = model;
		setContent();
	}

	@Override
	public MProperty getModel() {
		return model;
	}
	
	private void setContent() {
		if (model == null)
			this.getContentView().setText("");
		else {
			String name = model.getName();
			String type = model.getType().getName();
			if (model.getElementType() == MElementType.Attribute) {
				this.getContentView().setText(name + " : " + type);
			} else {
				boolean star = ((MReference) model).getMultiplicity() == Multiplicity.Multiple;
				this.getContentView().setText(name + " : " + type + (star ? "*" : ""));
			}
			
			this.getContentView().setTextColor(Range.make(name.length()
					+ " : ".length(), type.length()), getTypeColor());
		}
	}

	@Override
	void drawDot(ViewGraphics g, int offx, int offy, int dotSize) {
		g.fillOval(offx, offy, dotSize, dotSize);
	}

}
