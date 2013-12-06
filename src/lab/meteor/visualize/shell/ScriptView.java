package lab.meteor.visualize.shell;

import java.awt.Color;
import java.awt.EventQueue;
import java.text.BreakIterator;

import lab.meteor.core.script.MScriptEngine;
import lab.meteor.core.script.MScriptTask;
import lab.meteor.visualize.diagram.widgets.ResizeButton;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.event.EventHandler;
import co.gongzh.snail.event.Key;
import co.gongzh.snail.text.EditableTextView;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Alignment;
import co.gongzh.snail.util.Insets;
import co.gongzh.snail.util.Vector2D;

public class ScriptView extends View {
	
	TextView titleBarView;
	EditableTextView textView;
	
	MScriptEngine scriptEngine;
	MScriptTask task;
	
	View startButton;
	ResizeButton resizeButton;
	
	final static int padding = 30;
	Vector2D pos0;
	
	public ScriptView() {
		textView = new EditableTextView();
		scriptEngine = new MScriptEngine();
		task = new MScriptTask(scriptEngine, "") {
			@Override
			protected void started() {
				Runnable r = new Runnable() {

					@Override
					public void run() {
						titleBarView.setBackgroundColor(Resources.COLOR_WARNING_BG);
						titleBarView.setText("Running");
						titleBarView.setTextColor(Resources.COLOR_WARNING_TEXT);
					}
					
				};
				EventQueue.invokeLater(r);
			}
			
			@Override
			protected void interrupted(int line, int column, String message) {
				final int l = line;
				final String m = message;
				Runnable r = new Runnable() {

					@Override
					public void run() {
						titleBarView.setBackgroundColor(Resources.COLOR_ERROR_BG);
						titleBarView.setText("Error at line " + l + ": " + m);
						titleBarView.setTextColor(Resources.COLOR_ERROR_TEXT);
					}
				};
				EventQueue.invokeLater(r);
			}
			
			@Override
			protected void completed() {
				Runnable r = new Runnable() {

					@Override
					public void run() {
						titleBarView.setBackgroundColor(Resources.COLOR_SUCCESS_BG);
						titleBarView.setText("Finished");
						titleBarView.setTextColor(Resources.COLOR_SUCCESS_TEXT);
					}
				};
				EventQueue.invokeLater(r);
			};
		};
		
		setBackgroundColor(null);
		titleBarView = new TextView();
		titleBarView.addEventHandler(MOUSE_PRESSED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				MouseEvent e = (MouseEvent) arg;
				pos0 = e.getPosition(ScriptView.this);
			}
		});
		
		titleBarView.addEventHandler(MOUSE_DRAGGED, new EventHandler() {

			@Override
			public void handle(View sender, Key key, Object arg) {
				MouseEvent e = (MouseEvent) arg;
				setPosition(Vector2D.subtract(e.getPosition(getSuperView()), pos0));
			}
			
		});
		
		titleBarView.setHeight(padding);
		titleBarView.setInsets(Insets.make(5,5,5,5));
		titleBarView.setTextAlignment(Alignment.LEFT_CENTER);
		titleBarView.setBackgroundColor(Color.darkGray);
		titleBarView.setTextColor(Color.white);
		
		startButton = new View() {
			@Override
			protected void repaintView(ViewGraphics g) {
				g.drawImage(Resources.IMG_RUN, 0, 0, getWidth(), getHeight());
			}
			
			@Override
			protected void mouseClicked(MouseEvent e) {
				ScriptView.this.run();
			}
			
		};
		startButton.setBackgroundColor(null);
		startButton.setSize(32, 32);
		
		textView.setPosition(0, padding);
		textView.setDefaultTextColor(Color.white);
		textView.setDefaultFont(Resources.FONT_CMD_PRINT);
		textView.getCaretView().setBackgroundColor(Color.white);
		textView.setBreakIterator(BreakIterator.getLineInstance());
		textView.setTextAlignment(Alignment.LEFT_TOP);
		textView.setBackgroundColor(Resources.COLOR_SHELL_BG);
		textView.setInsets(Insets.make(5, 5, 5, 5));
		
		addSubview(titleBarView);
		addSubview(textView);
		addSubview(startButton);
		
		resizeButton = new ResizeButton(this);
		addSubview(resizeButton);
	}
	
	@Override
	public void setSize(int width, int height) {
		titleBarView.setWidth(width);
		textView.setSize(width, height - padding);
		startButton.setLeft(width - startButton.getWidth());
		resizeButton.setPosition(width - resizeButton.getWidth(), height - resizeButton.getHeight());
		super.setSize(width, height);
	}
	
	void run() {
		if (task.isRunning())
			return;
		task.setCode(textView.getPlainText());
		task.execute();
	}

}
