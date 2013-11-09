package lab.meteor.visualize.diagram.lines;

import java.awt.Color;
import java.awt.Stroke;

import lab.meteor.visualize.diagram.Linable;
import lab.meteor.visualize.diagram.Line;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.ViewGraphics;

public class LinkEnumLine extends Line {

	public LinkEnumLine(Linable start, Linable end) {
		super(start, end);
	}
	
	@Override
	public Stroke getLineStroke() {
		return Resources.STROKE_LINES;
	}

	@Override
	public Color getLineColor() {
		return Resources.COLOR_ENUM_BORDER;
	}

	@Override
	protected void drawLine(ViewGraphics g, int length) {
		g.drawLine(0, 0, length, 0);
	}
	
}
