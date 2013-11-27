package lab.meteor.visualize.table;

import java.awt.Color;

import lab.meteor.io.table.DataRow;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Alignment;
import co.gongzh.snail.util.Insets;

public class DataRowView extends View {
	
	static int DEFAULT_HEIGHT = 25;
	
	DataTableView tableView;
	DataRow model;
	TextView indexView;
	
	Insets insets = Insets.make(2, 2, 2, 2);
	
	public DataRowView(DataRow row, DataTableView tableView) {
		setBackgroundColor(null);
		this.tableView = tableView;
		indexView = new TextView();
		indexView.setBackgroundColor(tableView.indexColor);
		indexView.setDefaultTextColor(tableView.indexTextColor);
		indexView.setSize(DEFAULT_HEIGHT, tableView.indexWidth);
		indexView.setTextAlignment(Alignment.LEFT_CENTER);
		indexView.setInsets(insets);
		indexView.setTop(tableView.borderWidth);
		setModel(row);
	}
	
	public void setIndexColor(Color c) {
		indexView.setBackgroundColor(c);
	}
	
	public void setModel(DataRow row) {
		model = row;
		this.removeAllSubviews();
		addSubview(indexView);
		int x = tableView.indexWidth + tableView.borderWidth;
		int[] spliters = tableView.columnWidths;
		indexView.setText(String.valueOf(row.getIndex()));
		for (int i = 0; i < row.getTable().getColumns().size(); i++) {
			TextView v = new TextView();
			v.setTextAlignment(Alignment.LEFT_CENTER);
			v.setTop(tableView.borderWidth);
			v.setHeight(DEFAULT_HEIGHT);
			v.setWidth(spliters[i]);
			v.setLeft(x);
			v.setText(String.valueOf(row.getValue(i)));
			v.setInsets(insets);
			x += spliters[i] + tableView.borderWidth;
			addSubview(v);
		}
	}
	
	@Override
	public void setSize(int width, int height) {
		int x = tableView.indexWidth + tableView.borderWidth;
		int[] spliters = tableView.columnWidths;
		for (int i = 0; i < model.getTable().getColumns().size() - 1; i++) {
			x += spliters[i] + tableView.borderWidth;
		}
		View last = this.getSubview(this.count() - 1);
		last.setWidth(width - x);
		for (View v : this.getSubviews()) {
			v.setHeight(getHeight() - 1);
		}
		super.setSize(width, height);
	}
	
	@Override
	protected void repaintView(ViewGraphics g) {
		g.setColor(tableView.borderColor);
		g.drawLine(0, 0, getWidth(), 0);
	}
}
