package lab.meteor.visualize.table;

import java.awt.Color;

import co.gongzh.snail.View;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Alignment;

public class DataHeaderView extends View {
	DataTableView tableView;
	
	public DataHeaderView(DataTableView table) {
		setBackgroundColor(null);
		this.tableView = table;
	}
	
	public void rebuild() {
		this.removeAllSubviews();
		int x = tableView.indexWidth + tableView.borderWidth;
		int[] spliters = tableView.columnWidths;
		for (int i = 0; i < tableView.model.getColumns().size(); i++) {
			TextView v = new TextView();
			v.setTextAlignment(Alignment.CENTER_CENTER);
			v.setBackgroundColor(tableView.headerColor);
			v.setDefaultTextColor(Color.white);
			v.setHeight(tableView.headerHeight);
			v.setWidth(spliters[i]);
			v.setLeft(x);
			v.setText(tableView.model.getColumns().get(i).getName());
			x += spliters[i] + tableView.borderWidth;
			addSubview(v);
		}
	}
	
	@Override
	public void setSize(int width, int height) {
		if (tableView.model != null) {
			int x = tableView.indexWidth + tableView.borderWidth;
			int[] spliters = tableView.columnWidths;
			for (int i = 0; i < tableView.model.getColumns().size() - 1; i++) {
				x += spliters[i] + tableView.borderWidth;
			}
			View last = this.getSubview(this.count() - 1);
			last.setWidth(width - x);
		}
		super.setSize(width, height);
	}
}
