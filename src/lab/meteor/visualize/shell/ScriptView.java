package lab.meteor.visualize.shell;

import java.awt.Color;
import java.text.BreakIterator;

import lab.meteor.core.script.MScriptEngine;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.ViewGraphics;
import co.gongzh.snail.text.EditableTextView;
import co.gongzh.snail.util.Alignment;
import co.gongzh.snail.util.Vector2D;

public class ScriptView extends View {
	
	EditableTextView textView;
	
	MScriptEngine scriptEngine;
	
	View startButton;
	
	final static int padding = 10;
	
	public ScriptView() {
		textView = new EditableTextView();
		scriptEngine = new MScriptEngine();
		
		setBackgroundColor(new Color(0, 0, 0, 200));
		
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
		startButton.setSize(16, 16);
		
		textView.setPosition(padding, padding);
		textView.setDefaultTextColor(Color.white);
		textView.setDefaultFont(Resources.FONT_CMD_PRINT);
		textView.getCaretView().setBackgroundColor(Color.white);
		textView.setBreakIterator(BreakIterator.getLineInstance());
		textView.setTextAlignment(Alignment.LEFT_TOP);
		textView.setBackgroundColor(null);
//		textView.setInsets(Insets.make(padding, padding, padding, padding));
		
		addSubview(textView);
		addSubview(startButton);
	}
	
	@Override
	public void setSize(int width, int height) {
		textView.setSize(width - padding * 2, height - padding * 2);
		startButton.setLeft(width - startButton.getWidth());
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
