package lab.meteor.visualize.io;

import java.awt.Color;
import java.awt.EventQueue;

import lab.meteor.core.MElement;
import lab.meteor.core.MElement.MElementType;
import lab.meteor.core.MPackage;
import lab.meteor.core.script.MScriptHelper;
import lab.meteor.io.DataTableImporter;
import lab.meteor.io.ImportListener;
import lab.meteor.io.Importer;
import lab.meteor.io.table.DataRow;
import lab.meteor.io.table.DataTable;
import lab.meteor.visualize.diagram.widgets.ResizeButton;
import lab.meteor.visualize.resource.Resources;
import lab.meteor.visualize.table.DataTableView;
import co.gongzh.snail.Animation;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.event.EventHandler;
import co.gongzh.snail.event.Key;
import co.gongzh.snail.text.EditableTextView;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Alignment;
import co.gongzh.snail.util.Insets;
import co.gongzh.snail.util.Vector2D;

public class TableImportView extends View implements ImportListener {
	TextView titleView;
	DoButton importButton;
	DoButton cancelButton;
	DataTableView tableView;
	ResizeButton resizeButton;
	
	TextView packageLable;
	EditableTextView packageText;
	TextView resultView;
	
	Vector2D pos0;
	
	Animation showAnimation = new ShowAnimation(0.4f);
	Animation closeAnimation = new CloseAnimation(0.4f);
	
	boolean isImporting = false;
	
	public TableImportView() {
		setBackgroundColor(new Color(0, 0, 0, 200));
		titleView = new TextView();
		titleView.setHeight(30);
		titleView.setBackgroundColor(Color.black);
		titleView.setDefaultTextColor(Color.lightGray);
		titleView.setInsets(Insets.make(3,3,3,3));
		titleView.setTextAlignment(Alignment.LEFT_CENTER);
		
		importButton = new DoButton();
		importButton.setText("Import");
		importButton.setTextColor(new Color(100, 200, 0));
		importButton.setSize(70, 30);
		cancelButton = new DoButton();
		cancelButton.setText("Close");
		cancelButton.setTextColor(new Color(200, 100, 0));
		cancelButton.setSize(70, 30);
		importButton.setPosition(10, 40);
		cancelButton.setPosition(90, 40);
		
		packageLable = new TextView();
		packageLable.setBackgroundColor(null);
		packageLable.setDefaultTextColor(Color.white);
		packageLable.setText("Package");
		packageLable.setSize(50, 30);
		packageLable.setPosition(170, 40);
		packageLable.setTextAlignment(Alignment.CENTER_CENTER);
		addSubview(packageLable);
		
		packageText = new EditableTextView();
		packageText.setInsets(Insets.make(3, 3, 3, 3));
		packageText.setTextAlignment(Alignment.LEFT_CENTER);
		packageText.setSize(100, 30);
		packageText.setPosition(230, 40);
		addSubview(packageText);
		
		tableView = new DataTableView();
		tableView.setPosition(10, 80);
		
		resizeButton = new ResizeButton(this);
		
		resultView = new TextView();
		resultView.setPosition(340, 40);
		resultView.setHeight(30);
		resultView.setTextAlignment(Alignment.LEFT_CENTER);
		resultView.setBackgroundColor(null);
		resultView.setInsets(Insets.make(3, 3, 3, 3));
		addSubview(resultView);
		
		addSubview(titleView);
		addSubview(importButton);
		addSubview(cancelButton);
		addSubview(tableView);
		addSubview(resizeButton);
		
		titleView.addEventHandler(MOUSE_PRESSED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				MouseEvent e = (MouseEvent) arg;
				pos0 = e.getPosition(TableImportView.this);
				e.handle();
			}
			
		});
		
		titleView.addEventHandler(MOUSE_DRAGGED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				MouseEvent e = (MouseEvent) arg;
				Vector2D sub = Vector2D.subtract(e.getPosition(getSuperView()), pos0);
				TableImportView.this.setPosition(sub);
				e.handle();
			}
			
		});
		
		cancelButton.addEventHandler(MOUSE_CLICKED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				close();
			}
			
		});
		
		importButton.addEventHandler(MOUSE_CLICKED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				if (isImporting) {
					return;
				}
				final MElement e = MScriptHelper.getElement(packageText.getPlainText());
				if (e == null || e.getElementType() != MElementType.Package) {
					showError("package is not found.");
					return;
				}
				
				isImporting = true;
				showProgress("Importing...");
				DataTableImporter dti = new DataTableImporter();
				dti.addListener(TableImportView.this);
				dti.setPackage((MPackage) e);
				dti.doImport(tableView.getModel());
				
			}
			
		});
	}
	
	@Override
	public void setSize(int width, int height) {
		titleView.setWidth(width);
		tableView.setSize(width - 20, height - 90);
		resizeButton.setPosition(width - resizeButton.getWidth(), height - resizeButton.getHeight());
		resultView.setWidth(width - resultView.getLeft() - 10);
		super.setSize(width, height);
	}
	
	public void setTitle(String title) {
		this.titleView.setText(title);
	}
	
	public void setDataTable(DataTable table) {
		tableView.setModel(table);
	}
	
	public void show() {
		showAnimation.commit();
	}
	
	public void close() {
		closeAnimation.commit();
	}
	
	private class ShowAnimation extends Animation {

		public ShowAnimation(float duration) {
			super(duration);
		}

		@Override
		protected void animate(float progress) {
			setAlpha(progress);
		}
		
	}
	
	private class CloseAnimation extends Animation {

		public CloseAnimation(float duration) {
			super(duration);
		}

		@Override
		protected void animate(float progress) {
			setAlpha(1 - progress);
		}
		
		@Override
		protected void completed(boolean canceled) {
			removeFromSuperView();
		}
		
	}
	
	public void showError(String message) {
		resultView.setBackgroundColor(Resources.COLOR_ERROR_BG);
		resultView.setText(message);
		resultView.setTextColor(Resources.COLOR_ERROR_TEXT);
	}
	
	public void showWarning(String message) {
		resultView.setBackgroundColor(Resources.COLOR_WARNING_BG);
		resultView.setText(message);
		resultView.setTextColor(Resources.COLOR_WARNING_TEXT);
	}
	
	public void showFinish(String message) {
		resultView.setBackgroundColor(Resources.COLOR_SUCCESS_BG);
		resultView.setText(message);
		resultView.setTextColor(Resources.COLOR_SUCCESS_TEXT);
	}
	
	public void showProgress(String message) {
		resultView.setBackgroundColor(null);
		resultView.setText(message);
		resultView.setTextColor(Color.white);
	}

	@Override
	public void onProgress(Importer<?> i, int currentStep, int allStep) {
		final int cs = currentStep;
		final int as = allStep;
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if (cs % 10 == 0)
					showProgress(String.format("%d / %d", cs, as));
			}
		};
		EventQueue.invokeLater(r);
	}
	
	@Override
	public void onFinished(Importer<?> importer) {
		final Importer<?> im = importer;
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				switch (im.getResult().getResultState()) {
				case Error:
					showError(im.getResult().getMessage());
					break;
				case Warning:
					DataTable table = tableView.getModel();
					for (int i = 0; i < tableView.getRowSize(); i++) {
						DataRow row = table.getRows().get(i);
						if (row.hasTag("error")) {
							tableView.getRowView(i).setIndexColor(Color.red);
							System.out.println(i);
						}
					}
					showWarning(im.getResult().getMessage());
					break;
				case Success:
					showFinish(im.getResult().getMessage());
					break;
				}
				isImporting = false;
			}
			
		};
		EventQueue.invokeLater(r);
	}
	
}
