package lab.meteor.visualize.table;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import lab.meteor.io.table.DataTable;
import lab.meteor.visualize.diagram.ListView;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;

public class DataTableView extends View {
	
	List<DataRowView> rows = new ArrayList<DataRowView>();
	
	DataTable model;
	int indexWidth = 25;
	int borderWidth = 1;
	int[] columnWidths;
	int headerHeight = 30;
	int rowHeight = 25;
	
	static final int MAX_ROW = 200;
	
	View contentView;
	ListView listView;
	Color borderColor = new Color(120, 120, 120);
	Color indexColor = new Color(250, 230, 200);
	Color headerColor = new Color(50, 100, 150);
	Color indexTextColor = new Color(100, 90, 0);
	
	DataHeaderView headerView;
	
	public DataTableView() {
		contentView = new View();
		contentView.setBackgroundColor(null);
		listView = new ListView();
		listView.getContentView().setBackgroundColor(null);
		contentView.addSubview(listView);
		contentView.setClipped(true);
		headerView = new DataHeaderView(this);
		headerView.setLeft(borderWidth);
		contentView.setTop(headerHeight);
		contentView.setLeft(borderWidth);
		addSubview(headerView);
		addSubview(contentView);
	}
	
	public void setModel(DataTable table) {
		model = table;
		listView.getContentView().removeAllSubviews();
		if (model == null)
			return;
		columnWidths = new int[table.getColumns().size()];
		for (int i = 0; i < columnWidths.length; i++) {
			columnWidths[i] = 100;
		}
		headerView.rebuild();
		int max = MAX_ROW > table.getTotalCount() ? table.getTotalCount() : MAX_ROW;
		for (int i = 0; i < max; i++) {
			DataRowView rv = new DataRowView(table.getRows().get(i), this);
			rv.setSize(getWidth(), rowHeight);
			addRow(rv);
		}
		this.layout();
	}
	
	public DataTable getModel() {
		return model;
	}
	
	protected void addRow(DataRowView row) {
		listView.getContentView().addSubview(row);
		rows.add(row);
	}
	
	public DataRowView getRowView(int index) {
		return rows.get(index);
	}
	
	@Override
	protected void layoutView() {
		int width = getWidth();
		int height = getHeight();
		headerView.setWidth(width - 2 * borderWidth);
		contentView.setSize(width - 2*borderWidth, height - headerHeight - borderWidth);
		listView.setSize(width-2*borderWidth, height - headerHeight - borderWidth);
		for (View v : listView.getContentView().getSubviews()) {
			v.setWidth(width);
		}
	}
	
	@Override
	protected void repaintView(ViewGraphics g) {
		if (model != null) {
			int x = 0;
			g.setStroke(new BasicStroke(borderWidth));
			g.setColor(borderColor);
			
			x += indexWidth + borderWidth;
			g.drawLine(x, 0, x, getHeight());
			for (int i = 0; i < columnWidths.length; i++) {
				g.drawLine(x, 0, x, getHeight());
				x += columnWidths[i] + borderWidth;
			}
		}
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
	}
	
	public int getRowSize() {
		return rows.size();
	}
}
