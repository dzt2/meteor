package lab.meteor.visualize.diagram.lines;

import java.awt.Color;
import java.awt.Stroke;

import lab.meteor.visualize.diagram.Linable;
import lab.meteor.visualize.diagram.Line;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.ViewGraphics;

public class BiLinkLine extends Line {

	public BiLinkLine(Linable start, Linable end) {
		super(start, end);
	}
	
	@Override
	public Stroke getLineStroke() {
		return Resources.STROKE_LINES;
	}

	@Override
	public Color getLineColor() {
		return Color.red;
	}

	@Override
	protected void drawLine(ViewGraphics g, int length) {
		g.drawLine(0, 0, length, 0);
	}

}
