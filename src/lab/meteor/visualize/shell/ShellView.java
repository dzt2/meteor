package lab.meteor.visualize.shell;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.text.BreakIterator;

import lab.meteor.shell.MShell;
import lab.meteor.visualize.resource.Resources;
import co.gongzh.snail.MouseEvent;
import co.gongzh.snail.View;
import co.gongzh.snail.event.EventHandler;
import co.gongzh.snail.event.Key;
import co.gongzh.snail.text.CaretIndex;
import co.gongzh.snail.text.EditableTextView;
import co.gongzh.snail.text.TextView;
import co.gongzh.snail.util.Alignment;
import co.gongzh.snail.util.Insets;
import co.gongzh.snail.util.Range;

public class ShellView extends View {
	
	EditableTextView textView;
	TextView prefixView;
	
	MShell shell;
	
	final static int padding = 4;
	final static String defaultPrefix = "> ";
	
	public ShellView() {
		shell = new MShell();
		
		textView = new EditableTextView() {
			
			@Override
			protected void doEditableTextViewKeyCommand(int keyCode,
					boolean ctrl, boolean shift, CaretIndex caret, Range sel) {
				if (keyCode == KeyEvent.VK_ENTER) {
					if (getText().length() == 0)
						return;
					shell.parseCommand(getPlainText());
					prefixView.setText(shell.getCurrentPackage().toString() + defaultPrefix);
					setText("");
				} else {
					super.doEditableTextViewKeyCommand(keyCode, ctrl, shift, caret, sel);
				}
			}
		};
		
		setBackgroundColor(Resources.COLOR_COMMAND_BG);
		textView.setPosition(padding, padding);
		textView.setBackgroundColor(null);
		textView.setDefaultFont(Resources.FONT_COMMAND);
		textView.setDefaultTextColor(Color.white);
		textView.setBreakIterator(BreakIterator.getLineInstance());
		textView.setTextAlignment(Alignment.LEFT_CENTER);
		textView.getCaretView().setBackgroundColor(Color.white);
		
		prefixView = new TextView();
		prefixView.setPosition(padding, padding);
		prefixView.setBackgroundColor(null);
		prefixView.setDefaultFont(Resources.FONT_COMMAND);
		prefixView.setDefaultTextColor(Color.white);
		prefixView.setTextAlignment(Alignment.LEFT_CENTER);
		prefixView.setText(defaultPrefix);
		prefixView.addEventHandler(TextView.TEXT_LAYOUT_CHANGED, new EventHandler() {
			
			@Override
			public void handle(View sender, Key key, Object arg) {
				prefixView.setSize(prefixView.getPreferredWidth(), getHeight() - padding * 2);
				textView.setInsets(Insets.make(0, sender.getPreferredWidth(), 0, 0));
			}
		});
		
		
		this.addSubview(textView);
		this.addSubview(prefixView);
	}
	
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		textView.setSize(width - padding * 2, height - padding * 2);
		prefixView.setSize(prefixView.getPreferredWidth(), height - padding * 2);
	}
	
	@Override
	protected void mouseClicked(MouseEvent e) {
		if (textView.isKeyboardFocus() && e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
			textView.resignKeyboardFocus();
		}
	}

}
