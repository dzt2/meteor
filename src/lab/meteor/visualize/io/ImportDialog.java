package lab.meteor.visualize.io;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;

import lab.meteor.io.file.CsvFileLoader;
import lab.meteor.io.table.DataTable;
import lab.meteor.visualize.diagram.widgets.CloseButton;
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

public class ImportDialog extends View {
	
	EditableTextView urlView;
	DoButton doButton;
	CloseButton closeButton;
	
	TextView titleView;
	
	Animation showAnimation = new ShowAnimation(0.4f);
	Animation hiddenAnimation = new HiddenAnimation(0.4f);
	
	Animation showErrorAnimation = new ShowErrorAnimation(0.3f);
	Animation waitErrorAnimation = new WaitErrorAnimation(2.0f);
	Animation hideErrorAnimation = new HideErrorAnimation(0.3f);
	
	TextView errorView;
	
	static int padding = 20;
	
	boolean isOpened = false;
	
	public ImportDialog() {
		setBackgroundColor(new Color(0, 0, 0, 200));
		closeButton = new CloseButton();
		urlView = new EditableTextView();
		doButton = new DoButton();
		doButton.setSize(50, 30);
		doButton.setText("Open");
		urlView.setInsets(Insets.make(5, 5, 5, 5));
		urlView.setBackgroundColor(Color.white);
		urlView.setDefaultTextColor(Color.black);
		urlView.setHeight(30);
		urlView.setTextAlignment(Alignment.LEFT_CENTER);
		addSubview(doButton);
		addSubview(urlView);
		addSubview(closeButton);
		errorView = new TextView();
		errorView.setDefaultTextColor(Color.white);
		errorView.setInsets(Insets.make(3, 3, 3, 3));
		errorView.setBackgroundColor(Color.red);
		errorView.setAlpha(0);
		errorView.setHeight(0);
		addSubview(errorView);
		titleView = new TextView();
		titleView.setDefaultTextColor(Color.white);
		titleView.setText("Import CSV File");
		titleView.setHeight(30);
		
		setSize(500, 70);
		doButton.addEventHandler(MOUSE_CLICKED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				String url = urlView.getPlainText();
				try {
					CsvFileLoader cfi = new CsvFileLoader(url);
					DataTable table = cfi.loadTable();
					TableImportView tiv = new TableImportView();
					tiv.setAlpha(0);
					tiv.setTitle(url);
					tiv.setDataTable(table);
					View superView = getSuperView();
					superView.addSubview(tiv);
					tiv.setSize(500, 400);
					tiv.show();
					close();
				} catch (FileNotFoundException e) {
					errorView.setText("Cannot open file.");
					showError();
				} catch (IOException e) {
					errorView.setText("Cannot load table.");
					showError();
				}
				
			}
			
		});
		
		closeButton.addEventHandler(MOUSE_CLICKED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				close();
			}
			
		});
	}
	
	@Override
	public void setSize(int width, int height) {
		closeButton.setPosition(width - closeButton.getWidth()/2, 0 - closeButton.getHeight()/2);
		urlView.setPosition(padding, padding);
		urlView.setWidth(width - padding * 3 - doButton.getWidth());
		doButton.setLeft(padding * 2 + urlView.getWidth());
		urlView.setTop(padding);
		doButton.setTop(padding);
		errorView.setWidth(width);
		super.setSize(width, height);
	}
	
	Vector2D pos0;
	@Override
	protected void mousePressed(MouseEvent e) {
		pos0 = e.getPosition(this);
		this.getSuperView().setSubviewIndex(this, this.getSuperView().count() - 1);
		e.handle();
	}
	
	@Override
	protected void mouseDragged(MouseEvent e) {
		Vector2D sub = Vector2D.subtract(e.getPosition(getSuperView()), pos0);
		this.setPosition(sub);
		e.handle();
	}
	
	@Override
	protected void mouseReleased(MouseEvent e) {
		e.handle();
	}
	
	@Override
	public boolean isInside(Vector2D point) {
		return super.isInside(point) || 
				closeButton.isInside(closeButton.transformPointFromSuperView(point));
	}
	
	private class ShowAnimation extends Animation {

		public ShowAnimation(float duration) {
			super(duration);
		}

		@Override
		protected void animate(float progress) {
			setAlpha(progress * progress);
		}
				
	}
	
	private class HiddenAnimation extends Animation {

		public HiddenAnimation(float duration) {
			super(duration);
		}
		
		@Override
		protected void animate(float progress) {
			setAlpha(1 - progress * progress);
		}
		
		@Override
		protected void completed(boolean canceled) {
			removeFromSuperView();
		}
	}
	
	private class ShowErrorAnimation extends Animation {

		public ShowErrorAnimation(float duration) {
			super(duration);
		}

		@Override
		protected void animate(float progress) {
			errorView.setAlpha(progress);
			errorView.setHeight((int) (20 * progress));
		}
		
		@Override
		protected void completed(boolean canceled) {
			if (!canceled)
				waitErrorAnimation.commit();
		}
		
	}
	
	private class WaitErrorAnimation extends Animation {

		public WaitErrorAnimation(float duration) {
			super(duration);
		}

		@Override
		protected void animate(float progress) {
			// do noting
		}
		
		@Override
		protected void completed(boolean canceled) {
			if (!canceled)
				hideErrorAnimation.commit();
		}
		
	}
	
	private class HideErrorAnimation extends Animation {

		public HideErrorAnimation(float duration) {
			super(duration);
		}

		@Override
		protected void animate(float progress) {
			errorView.setAlpha(1 - progress);
			errorView.setHeight((int) (20 * (1 - progress)));
		}
		
	}
	
	public void showError() {
		showErrorAnimation.cancel();
		waitErrorAnimation.cancel();
		hideErrorAnimation.cancel();
		showErrorAnimation.commit();
	}
	
	public void show() {
		if (!isOpened) {
			doButton.textView.setNeedsRepaint();
			doButton.boardView.setNeedsRepaint();
			hiddenAnimation.cancel();
			showAnimation.commit();
			isOpened = true;
		}
	}
	
	public void close() {
		if (isOpened) {
			showAnimation.cancel();
			hiddenAnimation.commit();
			isOpened = false;
		}
	}
}
