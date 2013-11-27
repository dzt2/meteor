package lab.meteor.visualize.shell;

import java.awt.Color;
import java.text.BreakIterator;

import lab.meteor.core.script.MScriptEngine;
import lab.meteor.visualize.diagram.widgets.ResizeButton;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.text.EditableTextView;
import co.gongzh.snail.util.Alignment;
import co.gongzh.snail.util.Insets;
import co.gongzh.snail.util.Vector2D;

public class ScriptView extends View {
	
	EditableTextView textView;
	
	MScriptEngine scriptEngine;
	
	View startButton;
	ResizeButton resizeButton;
	
	final static int padding = 20;
	
	public ScriptView() {
		textView = new EditableTextView();
		scriptEngine = new MScriptEngine();
		
		setBackgroundColor(new Color(0, 0, 0, 100));
		
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
		textView.setBackgroundColor(new Color(0, 0, 0, 100));
		textView.setInsets(Insets.make(5, 5, 5, 5));
		
		addSubview(textView);
		addSubview(startButton);
		
		resizeButton = new ResizeButton(this);
		addSubview(resizeButton);
	}
	
	@Override
	public void setSize(int width, int height) {
		textView.setSize(width, height - padding);
		startButton.setLeft(width - startButton.getWidth());
		resizeButton.setPosition(width - resizeButton.getWidth(), height - resizeButton.getHeight());
		super.setSize(width, height);
	}
	
	Vector2D pos0;
	
	@Override
	protected void mousePressed(MouseEvent e) {
		pos0 = e.getPosition(this);
//		this.getSuperView().setSubviewIndex(this, this.getSuperView().count() - 2);
	}
	
	@Override
	protected void mouseDragged(MouseEvent e) {
		this.setPosition(Vector2D.subtract(e.getPosition(getSuperView()), pos0));
	}
	
	@Override
	protected void mouseReleased(MouseEvent e) {
	}
	
	@Override
	protected void mouseClicked(MouseEvent e) {
//		if (textView.isKeyboardFocus() && e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
//			textView.resignKeyboardFocus();
//		}
	}
	
	void run() {
		scriptEngine.run(textView.getPlainText());
	}

}
