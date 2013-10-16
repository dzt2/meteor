package lab.meteor.visualize.diagram;

import java.awt.Color;

import lab.meteor.visualize.resource.Resources;

public class EnumWidget extends BlockWidget {

	public EnumWidget() {
		this.setSize(200, 250);
	}
	
	@Override
	public String getTitle() {
		return "A Enum";
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

}
