package lab.meteor.visualize.diagram;

import co.gongzh.snail.View;

public class ListItemView extends View {
	
	private boolean selected = false;
	
	public boolean isSelected() {
		return selected;
	}
	
//	public void select() {
//		ListView parent = (ListView) this.getSuperView();
//		if (parent.selectedItem != null)
//			parent.selectedItem.selected = false;
//		parent.selectedItem = this;
//		this.selected = true;
//	}
//	
//	public void deselect() {
//		ListView parent = (ListView) this.getSuperView();
//		parent.selectedItem = null;
//		this.selected = false;
//	}
}
